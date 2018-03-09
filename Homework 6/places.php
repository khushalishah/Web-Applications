<?php 
	$data = "";
	$loadPlaces = 1;
	$lattitude = 0;
	$longitude = 0;
	if(isset($_POST["search"])){
		if(isset($_POST["location"]) && $_POST["location"] != ""){
			//get geo location
			$url = "https://maps.googleapis.com/maps/api/geocode/json?address=".urlencode($_POST["location"])."&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
			$data = file_get_contents($url); 
			$characters = json_decode($data);
			$lattitude = $characters->results[0]->geometry->location->lat;
			$longitude = $characters->results[0]->geometry->location->lng;
		}else{
			$lattitude = $_POST['lattitude'];
			$longitude = $_POST['longitude'];
		}
		$url = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='.$lattitude.','.$longitude.'&radius='.($_POST['distance']*1609.34).'&types='.urlencode($_POST['category']).'&keyword='.urlencode($_POST['keyword']).'&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0'; // path to your JSON file
		//echo $url;
		$data = file_get_contents($url);
		//die();
	}else if(isset($_POST["id"])){
		$url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=".$_POST["id"]."&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
		//echo $url;
		$data = file_get_contents($url); 
		$loadPlaces = 0;
		//store first 5 pics
		$characters = json_decode($data);
		if(array_key_exists('photos', $characters->result)){
		$photos = $characters->result->photos;
		for($i=0;$i<count($photos);$i++){
			if($i == 5){
				break;
			}
			
			$photoURL = "https://maps.googleapis.com/maps/api/place/photo?maxwidth=".$photos[$i]->width."&photoreference=".$photos[$i]->photo_reference."&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
			$content = file_get_contents($photoURL);
			file_put_contents('image'.$i.'.jpg', $content);
		}
		}
	}?>
<!DOCTYPE html>
<html>
<head>
<title></title>
<style>
#header{
	font-size:30px;
	font-style: italic;
	text-align:center;
}
#header br{
	display:none;
}
label.tag{
	font-weight:bold;
	margin-bottom:4px;
	display:block;
}
table.data,table.data th,table.data td {
	border-collapse: collapse;
   border: 1px solid black;
}
a{
	text-decoration:none;
	color: black;
}
div.reviews{
	width:100%; 
	border:1px solid #BDBDBD; 
	padding:4px 0px;
}
div.photos{
	width:50%;
	border:1px solid #bdbdbd;
	padding: 16px;
	text-align:center;
	display:inline-block;
}
button.placename {
    background-color: Transparent;
    background-repeat:no-repeat;
    border: none;
    cursor:pointer;
    overflow: hidden;
    outline:none;
}
button.placeadd{
	background-color: Transparent;
    background-repeat:no-repeat;
    border: none;
    cursor:pointer;
    overflow: hidden;
    outline:none;
}
button.placeadd:hover{
	color:#757575;
}
 #map-wrapper {
    width: 400px;
    height: 300px;
    position: relative;
}

#map {
    width: 100%;
    height: 100%;
    display: inline-block;
}

#button-wrapper {
    position: absolute;
    top: 0px;
    left: 0px;
    width: 100%;
    border: 1px solid red;
    display: block;
}
.map-button{
	width: 100%;
	display:inline-block;
	background-color: Transparent;
    background-repeat:no-repeat;
    border: none;
    cursor:pointer;
    overflow: hidden;
    outline:none;
	padding: 8px 0;
}
.map-button:hover{
	background-color:#BDBDBD;
}
</style>
<script type="text/javascript">
var lattitude=0;
var longitude = 0;
var mapLat = 0;
var mapLng = 0;

var getJSON = function(url, callback,isAsync = true,method = 'GET',params = "") {
    var xhr = new XMLHttpRequest();
    xhr.open(method, url, isAsync);
    //xhr.responseType = 'json';
    xhr.onload = function() {
      var status = xhr.status;
      if (status === 200) {
        callback(null, xhr.responseText);
      }else{
		callback(status, xhr.response);
	  }
    };
	xhr.error = function(e) {
            alert("There is some kind of error ");
    };
    xhr.send(params);
};

function radioButtonSelected() {
	var result = document.querySelector('input[name="rbtlocation"]:checked').value;
    if(result=="other"){
		document.getElementById("txtlocation").removeAttribute('disabled');
    }
    else{
        document.getElementById("txtlocation").setAttribute('disabled', true);
		document.getElementById('txtlocation').value = '';
    }
}

function getLocation(){
	getJSON('http://ip-api.com/json',function(err, data) {
		if (err !== null) {
				alert('Something went wrong: ' + err);
			} else {
				var obj = JSON.parse(data);
				lattitude = obj.lat;
				longitude = obj.lon;
				if(lattitude!=0 && longitude!=0){
					var input = document.createElement("input");
					input.setAttribute("type", "hidden");
					input.setAttribute("name", "lattitude");
					input.setAttribute("value", lattitude);
					input.setAttribute("id","lattitude");
					//append to form element that you want .
					document.getElementById("travelform").appendChild(input);
					
					input = document.createElement("input");
					input.setAttribute("type", "hidden");
					input.setAttribute("name", "longitude");
					input.setAttribute("value", longitude);
					input.setAttribute("id","longitude");
					//append to form element that you want .
					document.getElementById("travelform").appendChild(input);

					document.getElementById("btnSearch").removeAttribute('disabled');

					var data = <?php print json_encode($data); ?>;
					var loadPlaces = <?php print $loadPlaces; ?>;
					
					if(data != ""){
						lattitude = <?php print $lattitude; ?>;
						longitude = <?php print $longitude; ?>;
						if(loadPlaces == 1){
							loadPlacesData(data);
							setVariables();
						}else{
							loadReviewsData(data);
							setVariables();
						}
					}
				}
			}
	});
}

function storeVariables(){
			if (typeof(Storage) !== "undefined") {
				localStorage.setItem("keyword", document.getElementById("keyword").value);
				localStorage.setItem("distance",document.getElementById("distance").value);
				localStorage.setItem("location",document.getElementById("txtlocation").value);
				var e = document.getElementById("category");
				var category = e.options[e.selectedIndex].text;
				localStorage.setItem("category",category);
				//document.getElementById("result").innerHTML = localStorage.getItem("lastname");
			}
}

function setVariables(){
		if (typeof(Storage) !== "undefined") {
			document.getElementById('keyword').value = localStorage.getItem("keyword");
			document.getElementById('distance').value = localStorage.getItem("distance");
			document.getElementById('txtlocation').value = localStorage.getItem("location");
			if(localStorage.getItem("location") == ""){
				document.getElementById('here').checked = true;
				document.getElementById('location').checked = false;
				document.getElementById("txtlocation").setAttribute('disabled', true);
			}else{
				document.getElementById('here').checked = false;
				document.getElementById('location').checked = true;
				document.getElementById("txtlocation").removeAttribute('disabled');
			}
			var catObj = document.getElementById('category');
			for (var i = 0; i < catObj.options.length; i++) {
				if (catObj.options[i].text == localStorage.getItem("category")) {
					catObj.options[i].selected = true;
					break;
				}
			}
		}
}

function validate(){
	if(document.getElementById("travelform").checkValidity()){
	var dist = document.getElementById('distance').value;
	if(dist == ''){
		document.getElementById('distance').value = 10;
		storeVariables();
		return true;
	}else{
		if(/^\d+$/.test(dist)){
			if(dist<=0){
				alert('Distance should be greater than zero.');
				return false;
			}
			storeVariables();
			return true;
		}else{
			alert('Distance should be a positive number');
			return false;
		}
	}
	}else{
		return true;
	}
}

function initMap() {
        directionsService = new google.maps.DirectionsService();
		directionsDisplay = new google.maps.DirectionsRenderer();
		var mapCenter = new google.maps.LatLng(mapLat, mapLng);
		var mapOptions = {
			zoom:12,
			center: mapCenter
		}
		var map = new google.maps.Map(document.getElementById('map'), mapOptions);
		directionsDisplay.setMap(map);
		directionsDisplay.setPanel(document.getElementById('directionsPanel'));
		
		var marker = new google.maps.Marker({
          position: mapCenter,
          map: map
        });
}

function calcRoute(mode,start,end) {
  var request = {
    origin:start,
    destination:end,
    travelMode: mode
  };
  directionsService.route(request, function(response, status) {
    if (status == 'OK') {
      directionsDisplay.setDirections(response);
    }
  });
}

function getOffset( el ) {
    var _x = 0;
    var _y = 0;
    while( el && !isNaN( el.offsetLeft ) && !isNaN( el.offsetTop ) ) {
        _x += el.offsetLeft - el.scrollLeft;
        _y += el.offsetTop - el.scrollTop;
        el = el.offsetParent;
    }
    return { top: _y, left: _x };
}

function buildMap(lat,lng,element){
	if(document.getElementById("mainMapDiv") === null){
	mapLat = lat;
	mapLng = lng;
	var mapDiv = document.createElement("div");
	mapDiv.setAttribute("id","mainMapDiv");
	var html = "<div id=\"map-wrapper\"><div id=\"map\"></div>";
    html += "<span style=\"display: block; width: 85px; vertical-align: top; position:absolute; top:0px; left:0px; background: #EDEDED;\">";
    html += "<button class=\"map-button\" onclick=\"calcRoute('WALKING',new google.maps.LatLng("+lattitude+","+longitude+"),new google.maps.LatLng("+lat+","+lng+"))\">Walk there</button>";
	html += "<button class=\"map-button\" onclick=\"calcRoute('BICYCLING',new google.maps.LatLng("+lattitude+","+longitude+"),new google.maps.LatLng("+lat+","+lng+"))\">Bike there</button>";
	html += "<button class=\"map-button\" onclick=\"calcRoute('DRIVING',new google.maps.LatLng("+lattitude+","+longitude+"),new google.maps.LatLng("+lat+","+lng+"))\">Drive there</button>";
	html += "</span></div>";
	mapDiv.innerHTML = html;
	var rect = element.getBoundingClientRect();
	mapDiv.style.position = "absolute";
	mapDiv.style.left = (getOffset(element).left+25)+'px';
	mapDiv.style.top = (getOffset(element).top+25)+'px';
	mapDiv.style.zIndex = 999;
	document.body.appendChild(mapDiv);
	var imported = document.createElement('script');
	imported.src = 'https://maps.googleapis.com/maps/api/js?key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0&callback=initMap';
	imported.defer = true;
	document.head.appendChild(imported);
	}else{
		document.getElementById("mainMapDiv").remove();
	}
}

function loadPlacesData(data){
		var json = JSON.parse(data);
		if(json.results.length==0){
			var html = "<p style=\"background-color:#E0E0E0; border:2px solid #BDBDBD; width:100%; font-weight:500;\">No Records have been found</p>";
		}else{
			var html = "<table style=\"text-align:left; width:100%; margin:16px auto;\" class=\"data\" cellpadding=\"10\"><tr><th>Category</th><th>Name</th><th>Address</th></tr>";
			for(i=0;i<json.results.length;i++){
				var result = json.results[i];
				html += "<tr><td><img src=\""+result.icon+"\" style=\"width:35px; height:35px;\"/></td><td><button class=\"placename\"onClick=\"getReviewsData(\'"+result.place_id+"\')\">"+result.name+"</button></td><td><button class=\"placeadd\"onClick=\"buildMap("+result.geometry.location.lat+","+result.geometry.location.lng+",this)\">"+result.vicinity+"</button></td></tr>";
			}
			html += "</table>";
		}
		document.getElementById('result').innerHTML = html;
}

function getReviewsData(id){
	var f1 = document.createElement("form");
    f1.action="";
	var input = document.createElement("input");
	input.setAttribute("type", "hidden");
	input.setAttribute("name", "id");
	input.setAttribute("value", id);
	input.setAttribute("id","id");
	f1.appendChild(input);     
	f1.method="POST";
	document.body.appendChild(f1);
    f1.submit();
}

function loadReviewsData(data){
	var json = JSON.parse(data);
	var html = "<br/><b>"+json.result.name+"</b><br/>";
	html += "<br/><label>click to show reviews</label><br/><img id=\"reviewImg\" src=\"http://cs-server.usc.edu:45678/hw/hw6/images/arrow_down.png\" style=\"width:30px; height:20px; margin-top:8px; cursor: pointer;\" onclick=\"toggle('reviewImg','reviewSection')\"/><br/><div id=\"reviewSection\" style=\"display:none; margin:8px 0; text-align:center;\">";
	if(!json.result.hasOwnProperty("reviews") || json.result.reviews.length == 0){
		html += "<p style=\"width:100%; border:2px solid #BDBDBD;\"><b>No Reviews Found</b></p>";
	}else{
		for(i=0;i<json.result.reviews.length;i++){
			var review = json.result.reviews[i];
			if(i==5){
				break;
			}
			html += "<div class=\"reviews\">";
			if(review.profile_photo_url != ""){
				html += "<img src=\""+review.profile_photo_url+"\" style=\"width:30px; height:30px;\">";
			}
			html += review.author_name;
			html += "</div><div class=\"reviews\" style=\"text-align:left;\">";
			html += review.text;
			html += "</div>";
		}
	}
	html += "</div>";
	html += "<label style=\"margin-top:24px;\">click to show photos</label><br/><img id=\"photoImg\" src=\"http://cs-server.usc.edu:45678/hw/hw6/images/arrow_down.png\" style=\"width:30px; height:20px; margin-top:8px; cursor: pointer;\" onclick=\"toggle('photoImg','photoSection')\"/></br><div id=\"photoSection\" style=\"display:none; text-align:center;\">";
	if(!json.result.hasOwnProperty("photos") || json.result.photos.length == 0){
		html += "<p style=\"width:100%; border:2px solid #BDBDBD;\"><b>No Photos Found</b></p>";
	}else{
		for(i=0;i<json.result.photos.length;i++){
			var photo = json.result.photos[i];
			if(i==5){
				break;
			}
			html += "<div class=\"photos\"><a href=\"image"+i+".jpg\" target=\"_blank\"><img style=\"width:100%;\" src=\"image"+i+".jpg\"/></a></div>";
		}		
	}
	html += "</div>";
	document.getElementById('result').innerHTML = html;
}

function toggle(imgid,divid) {
	var section = document.getElementById(divid);
	var obj = document.getElementById(imgid);

	if(section.style.display == "inline") {
    	section.style.display = "none";
		obj.src = "http://cs-server.usc.edu:45678/hw/hw6/images/arrow_down.png";
  	}else {
		section.style.display = "inline";
		obj.src = "http://cs-server.usc.edu:45678/hw/hw6/images/arrow_up.png";
		if(imgid == 'reviewImg'){
			if(document.getElementById('photoSection').style.display == "inline"){
				toggle('photoImg','photoSection');
			}
		}else{
			if(document.getElementById('reviewSection').style.display == "inline"){
				toggle('reviewImg','reviewSection');
			}
		}
	}
}

function clearFields(){
	document.getElementById('keyword').value = '';
	document.getElementById('distance').value = '';
	document.getElementById('txtlocation').value = '';
	document.getElementById('here').checked = true;
	document.getElementById('location').checked = false;
	document.getElementById('category').selectedIndex = 0;
	document.getElementById("txtlocation").setAttribute('disabled', true);
	document.getElementById('result').innerHTML = "";
	if(document.getElementById("mainMapDiv") !== null){
		document.getElementById("mainMapDiv").remove();
	}
}
</script>
</head>
<body style="text-align:center;" onLoad="getLocation()">
<div style="padding:0px 8px; background-color:#F5F5F5; width:600px; border:3px solid #E0E0E0; margin:0 auto;">
<div id="header">Travel and Entertainment Search</div>
<hr/>
<form style="text-align:left" method="POST" id="travelform" action="">
<label class="tag">Keyword <input type="text" id="keyword" name="keyword" value="" required/></label>
<label class="tag">Category <select name="category" id="category">
			<option selected="selected">default</option>
			<option>cafe</option>
			<option>backery</option>
			<option>restaurant</option>
			<option>beauty salon</option>
			<option>casino</option>
			<option>movie theater</option>
			<option>lodging</option>
			<option>airport</option>
			<option>train station</option>
			<option>subway station</option>
			<option>bus station</option>
		</select></label>

<label class="tag">Distance (miles) <input type="text" placeholder="10" name="distance" id="distance" value=""/>
from <table style="display:inline; vertical-align: top;"><tr><td><input id="here" type="radio" name="rbtlocation" value="here" onChange="radioButtonSelected()" checked/><span style="font-weight:400">Here</span></td></tr>
<tr><td><input type="radio" id="location" name="rbtlocation" value="other" onChange="radioButtonSelected()"/> 
<input type="text" placeholder="location"  name="location" required disabled id="txtlocation" value=""/></td></tr></table></label>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <input id="btnSearch" type="Submit" value="Search" name="search" onClick="return validate()" disabled/>
<input type="button" value="Clear" onClick="clearFields()"/><br/><br/>
</form>
</div>
<div id="result" style="width:80%; text-align:center; display:inline-block;"></div>
</body>
</html>