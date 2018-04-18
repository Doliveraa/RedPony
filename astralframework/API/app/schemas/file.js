const mongoose = require('mongoose');

const FileSchema = mongoose.Schema({
    owner: {
        type: String,
        required: true
    },
    name: {
        type: String,
        required: true
    },
    location: {
        type: [Number],
        index: '2dsphere',
        required: true
    },
    expirationDate: {
        type: Date,
        required: true
    },
    data: {
        type: Object,
        required: true
    }
});

FileSchema.statics.getNearby = function(coordinates, meters, callback) {
    let today = new Date();
    File.find({
        location: {
            $nearSphere: {
                $geometry: {
                    type: "Point",
                    coordinates: coordinates
                },
                $maxDistance: meters
            }
        },
        expirationDate: {
            $gt: today
        }
    }).exec(function(err, files) {
        if (err) {
            return callback(err);
        } else if (!files) {
            err = new Error("File not found");
            err.status = 500;
            return callback(err);
        }
        return callback(null, files);
    })
};

const File = mongoose.model('File', FileSchema);
module.exports = File;