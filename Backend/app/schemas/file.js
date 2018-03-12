const MONGOOSE = require('mongoose');

const FileSchema = MONGOOSE.Schema({
  homeid: {
    required: true,
    type: String,
  },
  userid: {
    required: true,
    type: String,
  },
  name: {
    type: String,
    required: true,
  },
  location: {
    type: Object,
    Longitude: {
      type: String,
      required: true,
    },
    Latitude: {
      type: String,
      required: true,
    }
  },
  expirationDate: {
    type: Date,
    required: true,
  },
  data: {
    type: Object,
  },
});

module.exports = MONGOOSE.model('File', FileSchema);
