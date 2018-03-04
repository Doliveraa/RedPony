var http = require('http');
var moment = require('moment');
moment().format();

// Helper methods

/**
 * Checks if value is a string
 * @param {undefined} value value to check
 * @param {bool} true if value is a string, false if not
 */
function isString (value) {
	return typeof value === 'string' || value instanceof String;
};

/**
 * Checks if value is a boolean
 * @param {undefined} value value to check
 * @param {bool} true if value is a boolean, false if not
 */
function isBoolean (value) {
	return typeof value === 'boolean';
};

/**
 * Checks if value is a number
 * @param {undefined} value value to check
 * @param {bool} true if value is a number, false if not
 */
function isNumber (value) {
	return typeof value === 'number' && isFinite(value);
};

/**
 * Checks if value is a valid location
 * @param {undefined} lat latitude 
 * @param {undefined} lon longitude
 * @param {bool} true if value is a valid location, false if not
 */
function isLocation (lat, lon) {
	return (isNumber(lat) && isNumber(lon) && lat >= -90 
			&& lat <= 90 && lon >= -180 && lon <= 180);
}

//dtypes mapped to checking functions
const dtypes = {
	'string': isString,
	'bool': isBoolean,
	'number': isNumber,
	'location': isLocation
}

/**
 * Astral class
 * @param {string} key application key
 */
class Astral {
    constructor(key) {
		this.key = key;
		//TODO: authenticate application with key
        console.log('Astral constructor');
    }
    
    /**
     * Adds a file type 
     * @param {FileType} ft file type
     */
    addFileType(f) {
        console.log('Adding file type');
    }
    
    /**
     * Confirms File Type
     * @param {string} type type of file
     * @param {Object} data file data
     * @returns {boolean} true if file matches type, false if not
     */ 
    confirmType(type, data) {
		//get type schema from database, if it doesn't exist return false
		
		//test schema: image
		var schema = {
			'name': 'image',
			'data': {
				'path': {
					'isRequired':true, 
					'dtype':'string'
				},
				'caption': {
					'isRequired':false, 
					'default':'N/A', 
					'dtype':'string'
				}
			}
		}
		
		//console.log(schema.data);
		
		//check that data has all required values
		for (var key in schema.data) {
			if (schema.data[key].isRequired === true) {
				
				//console.log(data);
				if (!(key in data)) return false; 
			}
		}
		
		//check that data has no extra values
		for (var key in data) {
			if (!(key in schema.data)) return false;
		}
		
		//check that all dtypes are correct
		for (var key in data) {
			if (schema.data[key].dtype in dtypes) {
				var check = dtypes[schema.data[key].dtype]
				if (!check(data[key])) {
					return false;
				}
			} else {
				if (!this.confirmType(data[key].dtype, data[key])) {
					return false;
				}
			}
		}
		
		return true;
	}

	/**
	 * Adds a file
	 * @param {string} name file name
	 * @param {string} type file type
	 * @param {number} lat latitude (file location)
	 * @param {number} lon longitude (file location)
	 * @param {string} expiration file expiration date/time
	 * @param {Object} file data
	 */
    addFile(name, type, lat, lon, expiration, data) {
		expiration = moment(expiration);
		if (isString(name) && isString(type) && isLocation(lon, lat) 
			&& expiration.isValid() && this.confirmType(type, data)) {
			var file = {
				'name': name,
				'type': type,
				'location': {'lon': lon, 'lat': lat},
				'expiration': expiration.format(),
				'data': data
			};
			
			//TODO: send file 
			//console.log(file);
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
		//TODO: get files
        console.log('getting files at %s', location);
    }
    
    _send(method, path, body) {
		var options = {
			hostname: 'localhost',
			port: 8080,
			path: path,
			method: method,
			headers: {
				'homeid': 1978,
				'Content-Type': 'application/json'
			}
		};
		
		var req = http.request(options, function (res) {
			console.log("Sending request");
		});
		
		req.on('error', function(e) {
			console.log("Request error: " + e.message);
		});
		
		req.write(JSON.stringify(body));
		req.end();
	}
    
    addUser(userid, password) {
		var body = {
			userid: userid,
			password: password
		};
		
		this._send('POST', '/users/:homeid', body);
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
