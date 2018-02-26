const MONGOOSE = require('mongoose');

MONGOOSE.Promise = require('bluebird');

const UserSchema = MONGOOSE.Schema({
  homeid: {
    required: true,
    type: String,
    unique: true,
    trim: true,
  },
  userid: {
    required: true,
    type: String,
    unique: true,
    trim: true,
  },

});

module.exports = MONGOOSE.model('User', UserSchema);
