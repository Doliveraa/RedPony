const USER = require('../schemas/user.js');


const create = function create(_info){
  console.log('control: create');
  return new Promise((fullfill, reject) => {
    const newUser = new USER();
    newUser.userid = _info.userid;
    newUser.homeid = _info.homeid;
    newUser.save()
      .then(() => fullfill(newUser))
      .catch(error => reject('error when saving new user'));
  });// end promise
};// end create()


module.exports = {
  create,
};
