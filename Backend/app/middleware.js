const USERS = require('./control/user.js');
const HELPER = require('./helper');

const createUser = function createUser(_request, _response){

  return new Promise((fullfill, reject) => {
    console.log('Middleware: createUser');
    const errorMsg = [];

    if ( !HELPER.valid(_request.params.homeid)) {errorMsg.push('missing homeid')}
    if ( !HELPER.valid(_request.body.userid)) {errorMsg.push('missing userid')}


    if (errorMsg.length > 0){
      console.log('Middleware: error'+ errorMsg.join());
      reject(errorMsg.join());
    }
    else {
      const info = {
        userid: _request.body.userid,
        homeid: _request.params.homeid,
      }
      USERS.create(info)
        .then((newUser) => {
          const success ={ message: 'Successfully created new user',};
          fullfill(success);
        })// end then
        .catch((error) => reject(error)); // end users.create()
    }
  });
};

module.exports = {
  createUser,
};
