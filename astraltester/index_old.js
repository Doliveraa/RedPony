module.exports = class Astral {
    constructor(token) {
        // Connect and authenticate through REST API
    }

    addFileType(schema) {
        /* Add a file type
         * 
         * example:
         *
         * {"name":"picture",
         *  "path": {"required"=true, dtype="string"i},
         *  "description": {"required"=false, "default"="N/A", dtype="string"}
         * }
    }

    addFile(file) {
        /* Add a file
         * Checks data requirements defined by addFileType
         * 
         * example:
         * 
         * {"name":"dog_pic1.png",                  
         *  "type":"picture",
         *  "owner": "jaredraycoleman",
         *  "location":"33.7542, -118.2019",        
         *  "expiration":"2018-04-19T13:26:12.123Z", 
         *  "data": {"path":"pics/dog_pic1.png"} 
         * }
    }


}
