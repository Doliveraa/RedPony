const MONGOOSE = require('mongoose');

//MONGOOSE.Promise = require('bluebird');

const UserSchema = MONGOOSE.Schema({
  homeid: {
    required: true,
    type: String,
    trim: true,
  },
  userid: {
    required: true,
    type: String,
    trim: true,
  },
  data: Object,

});

module.exports = MONGOOSE.model('User', UserSchema);
