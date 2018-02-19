
class Astral {
    constructor(key) {
		this.key = key;
        console.log('Astral constructor');
    }

    addFileType() {
        console.log('Adding file type');
    }

    addFile() {
        console.log('Adding file');
    }

    getFiles(location, owner=null, maximum=null) {
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
