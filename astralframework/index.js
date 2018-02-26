var moment = require('moment');
moment().format();

// Helper methods
function isString (value) {
	return typeof value === 'string' || value instanceof String;
};

// Returns if a value is a boolean
function isBoolean (value) {
	return typeof value === 'boolean';
};

function isLocation (lat, lon) {
	return (lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180);
}

/**
 * Astral class
 * @param key
 */
class Astral {
    constructor(key) {
		this.key = key;
        console.log('Astral constructor');
    }
    
    /**
     * Adds a file type 
     * @param {FileType} ft file type
     */
    addFileType(f) {
        console.log('Adding file type');
    }
    
    confirmType(type, data) {
		//get type from database, if it doesn't exist return false
		//check that data adheres to type
		return true
	}

	/**
	 * Adds a file
	 * @param {File} f file
	 */
    addFile(name, type, lat, lon, expiration, data) {
		expiration = moment(expiration);
		if (isString(name) && isString(type) && isLocation(lon, lat) 
			&& expiration.isValid() && this.confirmType(type, data)) {
				var file = {
				name: name,
				type: type,
				location: {lon: lon, lat: lat},
				expiration: expiration,
				data: data
			};
			
			//TODO: send file 
			console.log(file);
		} else {
			console.log('Invalid file. Check type.');
		}
		
    }

	/**
	 * Get files, given restrictions
	 * @param {string} location location restriction
	 * @param {string} [null] owner userid of file owner
	 * @param {number} [50] maximum number of files to get
	 */
    getFiles(location, owner=null, maximum=50) {
        console.log('getting files at %s', location);
    }
}

/** 
 * Connects to astral API 
 * @param {string} key connection key
 * @returns {Astral} Astral connection to backend
 */
exports.connect = function(key) {
    return new Astral(key);
}
