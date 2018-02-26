const MONGOOSE = require('mongoose');

const FileSchema = MONGOOSE.Schema({
  homeid: {
    required: true,
    type: String,
    unique: true,
  },
  userid: {
    required: true,
    type: String,
    unique: true,
  },
  name: {
    type: String,
    required: true,
  },
  location: {
    type: String,
    required: true,
  },
  experation: {
    type: Date,
  },
  data: {
    type: String,
    required: true,
  },
});

module.exports = MONGOOSE.model('File', FileSchema);
