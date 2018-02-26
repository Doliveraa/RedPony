const USER = require('./schemas/user');
const FILE = require('./schemas/file');
let router;

const routing = function routing(express_router) {
    router = express_router;

    router.route('/').get(function (req, res) {
        res.json({ message: 'welcome to the rest api' });
    });

    //get a user using userid and homeid
    router.route('/:homeid/users/:userid').get(function (req, res) {
        USER.find({ homeid: req.params.homeid, userid: req.params.userid }, function (err, user) {
            res.json(user);
        });
    });

    //create a user
    router.route('/:homeid/users').post(function (req, res) {
        var user = new USER();
        user.userid = req.body.userid;
        user.homeid = req.params.homeid;

        user.save(function (error) {
            if (err)
                res.send(error);

            res.json({ message: 'User created' });
        });
    });

    //create a file
    router.route('/:homeid/users/:userid/files').post(function (req, res) {
        var file = new FILE();
        file.userid = req.params.userid;
        file.homeid = req.params.homeid;
        file.name = req.body.name;
        file.location = req.body.location;
        file.experiation = req.body.experiation;
        file.data = req.body.data;

        file.save(function (error) {
            if (err)
                res.send(error);

            res.json({ message: 'file created' });
        });
    });

    //get a users created files using homeid and userid
    router.route('/:homeid/users/:userid/files').get(function (req, res) {
        FILE.find({ homeid: req.params.homeid, userid: req.params.userid }, function (err, files) {
            res.json(files);
        });
    });

    //get files near a users location
    router.route('/:homeid/users/:userid/files').get(function (req, res) {

        //****** this VVV is placeholder code we need to decide how to handle radius look into Haversine formula
        var min = req.body.locationX + 10;
        var max = req.body.locationY - 10;
        FILE.find({ location: { $gt: min, $lt: max } }, function (err, files) {
            res.json(files);
        });
    });


    return router;
};

module.exports = routing;
