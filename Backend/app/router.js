const USER = require('./schemas/user');
const FILE = require('./schemas/file');
const UUID = require('uuid/v1');
const MIDDLEWARE = require('./middleware');

// noinspection JSAnnotator
let router;
const routing = function routing(express_router) {
    router = express_router;

    router.route('/').get(function (req, res) {
        res.json({ message: 'Welcome to the REST API' });
    });

    //get a user using homeid in the http params and userid in the request body
    router.route('/users').get(function (req, res) {
        MIDDLEWARE.getUser(req, res, {password:0});
    });

    //create a user (not using promises)
    router.route('/users').post(function (req, res) {
        MIDDLEWARE.createUser(req, res);
    });

    //create a user (not using promises)
    router.route('/users').put(function (req, res) {
        MIDDLEWARE.updateUser(req, res);
    });

    //get a users info using homeid and userid
    router.route('/users').get(function (req, res) {
        MIDDLEWARE.getUser(req, res);
    });

    //create a file
    router.route('/files').post(function (req, res) {
        MIDDLEWARE.createFile(req, res);
    });

    //get a users created files using homeid and userid
    router.route('/files').get(function (req, res) {
        MIDDLEWARE.getFiles(req, res);
    });

    //update a users file
    router.route('/files').put(function (req, res) {
        MIDDLEWARE.updateFiles(req, res);
    });

    return router;
};

module.exports = routing;
