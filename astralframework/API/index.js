const MONGOOSE = require('mongoose');
const EXPRESS = require('express');
const DB_CONFIG = require('./config/mongo');
const ROUTES = require('./app/router');
const BODY_PARSER = require('body-parser');
const FS = require('fs');
const CONFIG = JSON.parse(FS.readFileSync('config/config.json'));

const app = EXPRESS();
app.use(BODY_PARSER.urlencoded({ extended: true }));
app.use(BODY_PARSER.json());

const ROUTER = ROUTES(EXPRESS.Router());
app.use('/',ROUTER);

app.listen(CONFIG.port);
console.log('Running on port: '+ CONFIG.port);

MONGOOSE.connect(DB_CONFIG.path);
