const MONGOOSE = require('mongoose');

const HomebaseSchema = MONGOOSE.Schema({
  appName: {
    required: true,
    type: String,
    unique: true,
    trim: true,
  },
  homeid: {
    required: true,
    type: String,
    unique: true,
    trim: true,
  },

});

module.exports = MONGOOSE.model('Homebase', HomebaseSchema);
