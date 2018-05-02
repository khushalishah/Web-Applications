var https = require("https");
var url = require('url');
var cors = require('cors')
var express = require('express')
'use strict';
const yelp = require('yelp-fusion');

var app = express()
app.use(cors())
//var port = process.env.PORT || 3000;
var port=9512;

app.get('/', function(req, res, next) {
	var q = url.parse(req.url, true).query;
	var longitude = 0;
	var latitude = 0;
	var result = "";
	const client = yelp.client('BMOC3hUa_qKZSjZVJuIF0A94asFLjjyXa3mucPiUIZRmnciV3Rl6mdGbixBI4ofmHBhxDu_dH3nx9seXs0ew765Uja8kAl2Q5Z1G9QDrfG4lOmzyCfIJLeFLsHrGWnYx');

	
	if(q.yelp != null){
		//call yelp api
		console.log('Name: '+q.name+", City: "+q.city+", State: "+q.state);
		// matchType can be 'lookup' or 'best'
		client.businessMatch('best', {
			name: q.name,
			city: q.city,
			state: q.state,
			postal_code: q.postal_code,
			address1: q.address1,
			country: q.country
		}).then(response => {
			//res.json(response);
			if(response.jsonBody.businesses.length != 0){
				var businessid = response.jsonBody.businesses[0].id;
				getYelpReviews(businessid);
			}else{
				res.json({reviews:[]});
			}
			//console.log(response);
		}).catch(e => {
			console.log(e);
		});
		
	}else if(q.placeid != null){
		var placeDetailsURL = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+q.placeid+"&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
		https.get(placeDetailsURL, res => {
			res.setEncoding("utf8");
			let body = "";
			res.on("data", data => {
				result += data;
			});
			res.on('end', sendResponse);
		});
	}else{
		//check if pagination should be performed or not
		if(q.pagetoken == null){
			//check if location is empty
			if(q.location != null && q.location != ''){
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
	
	function getYelpReviews(businessid){
		client.reviews(businessid).then(response => {
			res.json(response.jsonBody);
			//console.log(response.jsonBody.reviews[0].text);
		}).catch(e => {
			console.log(e);
		});
	}
	
	
});

app.listen(port);
