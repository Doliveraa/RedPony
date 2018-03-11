const express = require('express');
const app = express();
const bodyParser = require('body-parser');
const mongoose = require('mongoose');
const fs = require('fs');
const config = JSON.parse(fs.readFileSync('config.json'));

mongoose.connect('mongodb://localhost/astral');
const db = mongoose.connection;

db.on('error', console.error.bind(console, 'connection error:'));
db.once('open', function() {
    console.log('Connected to Database');
});

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));

const routes = require('./routes/router');
app.use('/', routes);

app.use(function(req, res, next) {
    let err = new Error('File Not Found');
    err.status = 404;
    next(err);
});

app.use(function(err, req, res, next) {
    res.status(err.status ||  500);
    res.status(200).send(err.message);
});

app.listen(config.port, function() {
    console.log(`app started on port ${config.port}`);
});
