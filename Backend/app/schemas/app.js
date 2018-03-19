const mongoose = require('mongoose');

const AppSchema = mongoose.Schema({
    name: {
        type: String,
        required: true
    }
});

const App = mongoose.model('App', AppSchema);
module.exports = App;