const express = require('express');
const jwt = require('jsonwebtoken');
const router = express.Router();
const User = require('../models/user');
const File = require('../models/file');
const App = require('../models/app');
const fs = require('fs');
const config = JSON.parse(fs.readFileSync('config.json'));

//TODO:
//1. Edit files
//2. Remove users and files
//filetypes???
//framework???

findApp = function(token, callback) {
    jwt.verify(token, config.secret, function(err, decoded) {
        if (err) {
            err = new Error('Unable to app key');
            err.status = 500;
            return callback(err, null);
        }

        App.findById(decoded.id, function(err, app) {
            if (err || !app) {
                err = new Error('App not found');
                err.status = 500;
                return callback(err, null);
            }

            callback(null, app);
        });
    })
};

findUser = function(appKey, token, restrictions, callback) {
    findApp(appKey, function(err, app) {
        if (err) return callback(err, null);
        jwt.verify(token, config.secret, function(err, decoded) {
            if (err) {
                err = new Error('Unable to verify user token');
                err.status = 500;
                return callback(err, null);
            }

            User.findOne({_id: decoded.id, app: app.name}, restrictions, function(err, user) {
                if (err || !user) {
                    console.log(err);
                    err = new Error('User not found');
                    err.status = 500;
                    return callback(err, null);
                }
                return callback(null, user);
            });
        });
    });
};

router.get('/', function(req, res) {
    return res.status(200).send("Welcome to Astral");
});

router.post('/users', function(req, res, next) {
    if (!req.get('appKey')) {
        let err = new Error('No app key  provided');
        err.status = 500;
        return next(err);
    }

    findApp(req.get('appKey'), function(err, app) {
        if (err) return next(err);

        if (req.body.email &&
            req.body.username &&
            req.body.password &&
            req.body.data) {

            let userData = {
                app: app.name,
                email: req.body.email,
                username: req.body.username,
                password: req.body.password,
                data: req.body.data
            };

            User.create(userData, function(err, user) {
                if (err) {
                    err = new Error('Error creating user');
                    err.status = 500;
                    return next(err);
                } else {
                    let token = jwt.sign({ id: user._id }, config.secret);
                    return res.status(200).send(token);
                }
            });
        } else {
            let err = new Error('Invalid parameters');
            err.status = 401;
            return next(err);
        }
    });

});

router.get('/users', function(req, res, next) {
    if (!req.get('appKey')) {
        let err = new Error('No app key  provided');
        err.status = 500;
        return next(err);
    }

    if (req.get('email') && req.get('password')) {
        findApp(req.get('appKey'), function(err, app) {
            if (err) return next(err);

            User.authentication(app.name, req.get('email'), req.get('password'), function (error, user) {
                if (error || !user) {
                    let err = new Error('Wrong email or password');
                    err.status = 401;
                    return next(err);
                } else {
                    let token = jwt.sign({ id: user._id }, config.secret);
                    return res.status(200).send(token);
                }
            })
        });
    } else if (req.get('appKey') && req.get('token')) {
        findUser(req.get('appKey'), req.get('token'), {password: 0}, function(err, user) {
            if (err) return next(err);
            return res.status(200).send(user);
        })
    } else {
        let err = new Error('Invalid header parameters');
        err.status = 500;
        return next(err);
    }
});

router.put('/users', function(req, res, next) {
    if (!req.get('token')) {
        let err = new Error("No token provided");
        err.status = 401;
        return next(err);
    }

    if (!req.get('appKey')) {
        let err = new Error("No app key provided");
        err.status = 401;
        return next(err);
    }

    findUser(req.get('appKey'), req.get('token'), {}, function(err, user) {
        if (err) return next(err);

        if (req.body.username) user.username = req.body.username;
        if (req.body.password) user.password = req.body.password;
        if (req.body.email) user.email = req.body.email;
        if (req.body.data) user.data = req.body.data;

        user.save(function(err) {
           if (err) {
               let err = new Error('Error updating user');
               err.status = 401;
               return next(err);
           }
           return res.status(200).send('User updated successfully');
        });
    });
});

router.post('/files', function(req, res, next) {
    if (!req.get('token')) {
        let err = new Error("No token provided");
        err.status = 401;
        return next(err);
    }

    if (!req.get('appKey')) {
        let err = new Error("No app key provided");
        err.status = 401;
        return next(err);
    }

    findUser(req.get('appKey'), req.get('token'), {}, function(err, user) {
        if (err) return next(err);

        if (req.body.name &&
            req.body.latitude &&
            req.body.longitude &&
            req.body.expirationDate &&
            req.body.data) {

            let fileData = {
                owner: user.username,
                name: req.body.name,
                location: [
                    req.body.longitude,
                    req.body.latitude
                ],
                expirationDate: req.body.expirationDate,
                data: req.body.data
            };

            File.create(fileData, function(err) {
                if (err) return next(err);
                else return res.status(200).send("File created successfully");
            });
        } else {
            let err = new Error("Invalid parameters");
            err.status = 401;
            return next(err);
        }
    });

});

router.get('/files', function(req, res, next) {
    if (!req.get('token')) {
        let err = new Error("No token provided");
        err.status = 401;
        return next(err);
    }

    if (!req.get('appKey')) {
        let err = new Error("No app key provided");
        err.status = 401;
        return next(err);
    }

    findUser(req.get('appKey'), req.get('token'), {}, function(err, user) {
        if (err) return next(err);

        if (!req.get('latitude') || !req.get('longitude') || !req.get('radius')) {
            let err = new Error('No location information provided');
            err.status = 401;
            return next(err);
        }

        let coordinates = [Number(req.get('longitude')), Number(req.get('latitude'))];
        File.getNearby(coordinates, req.get('radius'), function(err, files) {
            if (err) return next(err);
            res.status(200).send(files);
        })
    });
});

module.exports = router;
