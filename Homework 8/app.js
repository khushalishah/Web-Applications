var http = require('http');
var https = require("https");
var url = require('url');
var cors = require('cors')
var express = require('express')

var app = express()
app.use(cors())

app.get('/', function(req, res, next) {
	var q = url.parse(req.url, true).query;
	var longitude = 0;
	var latitude = 0;
	var result = "";
	
	//check if pagination should be performed or not
	if(q.pagetoken == null){
		//check if location is empty
		if(q.location != ''){
			var geoCodeURL = "https://maps.googleapis.com/maps/api/geocode/json?address="+encodeURIComponent(q.location)+"&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
			https.get(geoCodeURL, res => {
				res.setEncoding("utf8");
				let body = "";
				res.on("data", data => {
					body += data;
				});
				res.on("end", () => {
					body = JSON.parse(body);
					latitude = body.results[0].geometry.location.lat;
					longitude = body.results[0].geometry.location.lng;
					var placesURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+','+longitude+'&radius='+(q.distance*1609.34)+'&types='+encodeURIComponent(q.category)+'&keyword='+encodeURIComponent(q.keyword)+"&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
					getPlaces(placesURL);
				});
			});
		}else{
		  latitude = q.lattitude;
		  longitude = q.longitude;
		  var placesURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+','+longitude+'&radius='+(q.distance*1609.34)+'&types='+encodeURIComponent(q.category)+'&keyword='+encodeURIComponent(q.keyword)+"&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
		  getPlaces(placesURL);
		}
	}else{
		var placesURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken="+q.pagetoken+"&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
		getPlaces(placesURL);
	}
	
	function getPlaces(placesURL){
		
		https.get(placesURL, res => {
			res.setEncoding("utf8");
			let body = "";
			res.on("data", data => {
				result += data;
			});
			res.on('end', sendResponse);
		});
	}
	
	function sendResponse() {
        res.json(JSON.parse(result));
    }
	
	
});

/*http.createServer(function (req, res) {
  res.writeHead(200, {'Content-Type': 'application/json'});
  var q = url.parse(req.url, true).query;
  var longitude = 0;
  var latitude = 0;
  var result = "";
  
  //check if location is empty
  if(q.location != ''){
	var geoCodeURL = "https://maps.googleapis.com/maps/api/geocode/json?address="+encodeURIComponent(q.location)+"&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
	https.get(geoCodeURL, res => {
		res.setEncoding("utf8");
		let body = "";
		res.on("data", data => {
			body += data;
		});
		res.on("end", () => {
			body = JSON.parse(body);
			latitude = body.results[0].geometry.location.lat;
			longitude = body.results[0].geometry.location.lng;
			getPlaces();
		});
	});
  }else{
	  latitude = q.lattitude;
	  longitude = q.longitude;
	  getPlaces();
  }
  function getPlaces(){
  var placesURL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+','+longitude+'&radius='+(q.distance*1609.34)+'&types='+encodeURIComponent(q.category)+'&keyword='+encodeURIComponent(q.keyword)+"&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
  
  https.get(placesURL, res => {
	res.setEncoding("utf8");
	let body = "";
	res.on("data", data => {
		result += data;
	});
	res.on('end', sendResponse);
});
  }

function sendResponse() {
        res.writeHead(200, {'Content-Type': 'application/json'});
        res.end(result);
    }
  
}).listen(8080);*/

app.listen(80);
