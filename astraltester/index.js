var ast = require('../astralframework');

conn = ast.connect();
conn.addFile('hello_image',  'image', '-8.945406', '38.575078', '2018-03-25T12:00:00Z', {path: './hello.jpg', caption: 'hello toast'});
conn.getFiles('-8.945406, 38.575078');
