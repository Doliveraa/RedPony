var ast = require('../astralframework/astralframework');

conn = ast.connect();
conn.addFileType();
conn.addFile();
conn.getFiles('32.101, 101.568');
