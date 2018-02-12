const USER = require('./schemas/user');
const FILE = require('./schemas/file');
let router;

const routing = function (express_router) {
    router = express_router;

    router.route('/').get(function (request, response) {
        res.json({ message: 'welcome to the rest api' });
    });


    //get a user using userid and homeid
    router.route('/:homeid/users/:userid').get(function (request, response) {
        USER.find({ homeid: request.params.homeid, userid: request.params.userid }, function (err, user) {
            response.json(user);
        });
    });

    //create a user
    router.route('/:homeid/users').post(function (request, response) {
        var user = new USER();
        user.userid = request.body.userid;
        user.homeid = request.params.homeid;

        user.save(function (error) {
            if (err)
                response.send(error);

            res.json({ message: 'User created' });
        });
    });

    //create a file
    router.route('/:homeid/users/:userid/files').post(function (request, response) {
        var file = new FILE();
        file.userid = request.params.userid;
        file.homeid = request.params.homeid;
        file.name = request.body.name;
        file.location = request.body.location;
        file.experiation = request.body.experiation;
        file.data = request.body.data;

        file.save(function (error) {
            if (err)
                response.send(error);

            res.json({ message: 'file created' });
        });
    });

    //get a users created files using homeid and userid
    router.route('/:homeid/users/:userid/files').get(function (request, response) {
        FILE.find({ homeid: request.params.homeid, userid: request.params.userid }, function (err, files) {
            response.json(files);
        });
    });

    //get files near a users location
    router.route('/:homeid/users/:userid/files').get(function (request, response) {

        //****** this VVV is placeholder code we need to decide how to handle radius look into Haversine formula
        var min = request.body.locationX + 10;
        var max = request.body.locationY - 10;
        FILE.find({ location: { $gt: min, $lt: max } }, function (err, files) {
            response.json(files);
        });
    });


    return router;
};

module.exports = routing;