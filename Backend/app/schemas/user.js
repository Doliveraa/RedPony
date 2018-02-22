const MONGOOSE = require('mongoose');

const UserSchema = MONGOOSE.Schema
    ({
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

    });

module.exports = MONGOOSE.model('User', UserSchema);