var express = require('express');
var serveIndex = require('serve-index')

var multer  = require('multer')

var storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, __dirname + '/data');
  },
  filename: function (req, file, cb) {
    //console.log(file);
    //cb(null, file.fieldname + '-' + Date.now());
    cb(null, file.originalname);
  }
})

var upload = multer({ storage: storage })

var app = express();

app.set('view engine', 'jade');
app.set('views', __dirname + '/views')

app.use('/data', express.static(__dirname + '/data'));
app.use('/data', serveIndex(__dirname + '/data/', {'icons': true}))

app.get('/', function (req, res) {
  res.render('index', { title: 'File Server', message: 'This is the File server'});
});

app.post('/', upload.any(), function (req, res, next) {

    //console.log(req);



  res.render('index', { title: 'File Server', message: 'Done) Filename: ' + JSON.stringify(req.files[0].originalname)});
  // req.file is the `avatar` file
  // req.body will hold the text fields, if there were any
})

var server = app.listen(3000, function () {
  var host = server.address().address;
  var port = server.address().port;

  console.log('Example app listening at http://%s:%s', host, port);
});
