const MONGOOSE = require('mongoose');
const EXPRESS = require('express');
const DB_CONFIG = require('./config/mongo');
const ROUTES = require('./app/router');
const BODY_PARSER = require('body-parser');

const app = EXPRESS();
const port = 8080;

app.use(BODY_PARSER.urlencoded({
  parameterLimit: 100000000,
  limit: '10000kb',
  extended: true,
}));
app.use(BODY_PARSER.json());

const ROUTER = ROUTES(EXPRESS.Router());
app.use('/',ROUTER);

app.listen(port);
console.log('Running on port: '+port);

console.log('Hello from rest api running on port: '+port);

MONGOOSE.connect(DB_CONFIG.path);
