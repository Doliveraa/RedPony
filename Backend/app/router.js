const USER = require('./schemas/user');
const HOMEBASE = require('./schemas/homebase');
const FILE = require('./schemas/file');
const UUID = require('uuid/v1');
const MIDDLEWARE = require('./middleware');

let router;
const routing = function routing(express_router) {
    router = express_router;

    router.route('/').get(function (req, res) {
        res.json({ message: 'welcome to the rest api' });
    });

    //get a user using homeid in the http params and userid in the request body
    router.route('/users/:homeid').get(function (req, res) {
        USER.find({ homeid: req.params.homeid, userid: req.body.userid }, function (err, user) {
            res.json(user);
        });
    });

    //create a user (using promises)
    router.route('/promises/users/:homeid').post(function (req, res) {
      console.log('Router: create user ');
      console.log(req.params.homeid);
      console.log(req.body.userid);
      MIDDLEWARE.createUser(req, res)
        .then(result => res.json(result))
        .catch(err => {console.log(err); res.json(err)});
    });

    //create a user (not using promises)
    router.route('/users').post(function (req, res) {
      console.log('Router: create user ');

      HOMEBASE.findOne({ homeid: req.body.homeid },function (err, homebase) {
        if(err) {
          res.send(err);
        }
        //if homebase not found send error
        if(!homebase) {
          console.log('error: homeid invalid');
          res.json({ error: 'homeid invalid' });
        }
        //if homebase exists create user and return success message
        else {
          console.log('creating new user');
          var newUser = new USER();
          newUser.userid = req.body.userid;
          newUser.homeid = req.body.homeid;
          newUser.save(function (err) {
            if (err)
              res.send(err);
            res.json({ message: 'user created' });
          });
        }
      });
    });

    // create homebase
    router.route('/homebase').post(function (req, res) {
      console.log('Router: create homebase');
      HOMEBASE.findOne({ appName: req.body.appName },function (err, homebase) {
        if(err) {
          res.send(err);
        }
        //if homebase not found create one
        if(!homebase) {
          console.log('creating new homebase');
          var newHome = new HOMEBASE();
          newHome.appName = req.body.appName;
          newHome.homeid = UUID();
          newHome.save(function(err, homebase) {
            if (err){
              res.send(err);
            }
            res.json({ homeid: homebase.homeid });
          });
        }
        //if homebase exist return message
        else {
          console.log('homebase found');
          res.json({ message: "homebase exists" });
        }
      });

    });

    //create a file
    router.route('/files').post(function (req, res) {
      console.log('create file');
      USER.findOne({ userid: req.body.userid, homeid: req.body.homeid },function (err, user) {
        if(err) {
          res.send(err);
        }
        //if user not found send error
        if(!user) {
          console.log('error: userid invalid');
          res.json({ error: 'userid invalid' });
        }

        if(user){
          var file = new FILE();
          file.homeid = req.body.homeid;
          file.userid = req.body.userid;
          file.name = req.body.name;
          file.location = req.body.location;
          file.expirationDate = new Date(req.body.expirationDate);

          file.save(function (err) {
            if (err)
              res.send(err);

            res.json({ message: 'file created' });
          })
        }
      });

    });

    //get a users created files using homeid and userid
    router.route('/files').get(function (req, res) {
      console.log('get user owned files');
      FILE.find({ userid: req.query.userid, homeid: req.query.homeid  }, function (err, files) {
        if (err){
          res.send(err);
        }

        //if files not found send message
        if(!files.length) {
          console.log('user has no files');
          res.json({ message: 'user has no files' });
        }
        else{
          res.json(files);
        }
      });
    });

    //get files near a users location
    router.route('/files/nearby/:userid').get(function (req, res) {

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
