
class Astral {
    constructor() {
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

/* Connects to astral API 
 * 
 * @param key connection key
 * @returns conn Astral connection to backend
 */
exports.connect = function() {
    return new Astral();
}
