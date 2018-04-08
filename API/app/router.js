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

    router.route('/users').post(function (req, res) {
        MIDDLEWARE.createUser(req, res);
    });

    router.route('/users').put(function (req, res) {
        MIDDLEWARE.updateUser(req, res);
    });

    router.route('/users').get(function (req, res) {
        MIDDLEWARE.getUser(req, res);
    });

    router.route('/files').post(function (req, res) {
        MIDDLEWARE.createFile(req, res);
    });

    router.route('/files').get(function (req, res) {
        MIDDLEWARE.getFiles(req, res);
    });

    router.route('/files').put(function (req, res) {
        MIDDLEWARE.updateFile(req, res);
    });

    return router;
};

module.exports = routing;
