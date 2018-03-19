const User = require('./schemas/user');
const File = require('./schemas/file');
const App = require('./schemas/app');
const HELPER = require('./helper');
const jwt = require('jsonwebtoken');

const fs = require('fs');
const config = JSON.parse(fs.readFileSync('config/config.json'));

const findApp = function(token, callback) {
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
        })
    })
};

const findUser = function(appKey, token, restrictions, callback) {
    findApp(appKey, function(err, app) {
        if (err) return callback(err, null);
        jwt.verify(token, config.secret, function(err, decoded) {
            if (err) {
                err = new Error('Unable to verify user token');
                err.status = 500;
                return callback(err, null);
            }

            User.findOne({_id: decoded.id, app: app.name}, restrictions,
                         function(err, user) {
                if (err || !user) {
                    console.log(err);
                    err = new Error('User not found');
                    err.status = 500;
                    return callback(err, null);
                }
                return callback(null, user);
            })
        })
    })
};

const getToken = function(appKey, email, password, callback) {
    findApp(appKey, function(err, app) {
        if (err) return callback(err, null);
        User.authentication(app.name, req.get('email'), req.get('password'),
                            function (error, user) {
            if (error || !user) {
                let err = new Error('Wrong email or password');
                err.status = 401;
                return callback(err, null);
            } else {
                let token = jwt.sign({ id: user._id }, config.secret);
                return callback(null, token);
            }
        })
    })
};

const createUser = function createUser(req, res){
    findApp(req.get('appKey'), function(err, app) {
        if (err) return callback(err, null);

        if (req.body.email && req.body.username &&
            req.body.password && req.body.data) {

            let userData = {
                app: app.name,
                email: req.body.email,
                username: req.body.username,
                password: req.body.password,
                data: req.body.data
            };

            User.create(userData, function(err, user) {
                if (err) {
                    res.status = 401;
                    return res.json({message: "Unable to create user"});
                } else {
                    let token = jwt.sign({ id: user._id }, config.secret);
                    return res.json({"token": token});
                }
            });
        } else {
            res.status = 401;
            return res.json({message: "Invalid Parameters"});
        }
    })
};

const getUser = function(req, res) {
    if (req.get("appKey")) {
        if (req.get("email") && req.get("password")) {
            getToken(req.get("appKey"), req.get("email"), req.get("password"), function(err, token) {
                if (err) {
                    res.status = 401;
                    return res.json({message: "Invalid email or password"});
                }
            })
        } else if (req.get("token")) {
            findUser(req.get("appKey"), req.get("token"), {password:0}, function(err, user) {
                if (err) {
                    res.status = 401;
                    res.json({message: "Could not verify user"});
                }
                return res.json(user);
            })
        } else {
            res.status = 401;
            return res.json({message: "No user authentication information provided"})
        }
    } else {
        res.status = 401;
        res.json({message: "No appKey provided"});
    }
};

const updateUser = function(req, res) {
    if (req.get("appKey") && req.get("token")) {
        findUser(req.get('appKey'), req.get('token'), {}, function(err, user) {
            if (err) {
                res.status = 401;
                return res.json({message: err.message});
            }

            if (req.body.username) user.username = req.body.username;
            if (req.body.password) user.password = req.body.password;
            if (req.body.email) user.email = req.body.email;
            if (req.body.data) user.data = req.body.data;

            user.save(function(err) {
               if (err) {
                   res.status = 401;
                   return res.json({message: "Unable to update user"});
               }
               return res.status(200).send('User updated successfully');
            });
        });
    } else {
        res.status = 401;
        return res.json({message: "no appKey or token provided"});
    }
};

const createFile = function(req, res) {
    if (req.get("appKey") && req.get("token")) {
        findUser(req.get('appKey'), req.get('token'), {}, function(err, user) {
            if (err) {
                res.status = 401;
                return res.json({message: "Unable to verify user"});
            }

            if (req.body.name && req.body.latitude && req.body.longitude &&
                req.body.expirationDate && req.body.data) {

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

                // TODO: if username is changed, user's files must also be updated

                File.create(fileData, function(err) {
                    if (err) {
                        console.log(err);
                        res.status = 401;
                        res.json({message: "Error creating file"});
                    }
                    else return res.json({message:"File created successfully"});
                });
            } else {
                let err = new Error("Invalid parameters");
                err.status = 401;
                return next(err);
            }
        });
    } else {
        res.status = 401;
        return res.json({message: "No appKey or token provided"});
    }
};

const getFiles = function(req, res) {
    if (req.get("appKey") && req.get("token")) {
        // TODO: Get files by user or location
        return res.json({message: "Not yet implemented"});
    } else {
        res.status = 401;
        return res.json({message: "No appKey or token provided"});
    }
};

module.exports = {
  createUser,
  getUser,
  updateUser,
  createFile,
  getFiles
};
