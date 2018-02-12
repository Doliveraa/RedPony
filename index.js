const MONGOOSE = require('mongoose');
const EXPRESS = require('express');
const DB_CONFIG = require('./config/mongo');
const ROUTER = reauire('./app/router');

const app = EXPRESS();
const port = 8080;

const ROUTER = ROUTES(EXPRESS.Router());
app.use('/',ROUTER);

app.listen(port);

console.log('Hello from rest api running on port: '+port);

MONGOOSE.connect(DB_CONFIG.path);
