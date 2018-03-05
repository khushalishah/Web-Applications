<?php 
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
		$url = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='.$lattitude.','.$longitude.'&radius='.$_POST['distance'].'&types='.$_POST['category'].'&keyword='.$_POST['keyword'].'&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0'; // path to your JSON file
		//echo $url;
		$data = file_get_contents($url);
		echo json_encode($data);
		die();
	}else{?>
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
</style>
<script type="text/javascript">
var lattitude=0;
var longitude = 0;

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
				}
			}
	});
}

function searchClicked(event){
	event.preventDefault();
	if(document.getElementById('travelform').checkValidity()){
		if(validate()){
			getResultData();
		}
	}
}

function validate(){
	var dist = document.getElementById('distance').value;
	if(dist == ''){
		document.getElementById('distance').value = 10;
		return true;
	}else{
		if(/^\d+$/.test(dist)){
			if(dist<=0){
				alert('Distance should be greater than zero.');
				return false;
			}
			//get json data
			return true;
		}else{
			alert('Distance should be a positive number');
			return false;
		}
	}
}

function parseJSONData(data){
		var res = JSON.stringify(data);
		var json = JSON.parse(data);
		document.getElementById('result').innerHTML = json;
		if(json.results.length==0){
			var html = "<p>No records have been found</p>";
		}else{
			var html = "<table style=\"text-align:left; width:100%; margin:16px auto;\" class=\"data\" cellpadding=\"10\"><tr><th>Category</th><th>Name</th><th>Address</th></tr>";
			for(i=0;i<json.results.length;i++){
				var result = json.results[0];
				html += "<tr><td><img src=\""+result.icon+"\" style=\"width:35px; height:35px;\"/></td><td><a href=\""+result.id+"\">"+result.name+"</a></td><td>"+result.vicinity+"</td></tr>";
			}
			html += "</table>";
		}
		document.getElementById('result').innerHTML = html;
}

function getResultData(){
	var params = "keyword="+document.getElementById('keyword').value;
	params += "&lattitude="+document.getElementById('lattitude').value;
	params += "&longitude="+document.getElementById('longitude').value;
	params += "&location="+document.getElementById('txtlocation').value;
	params += "&distance="+document.getElementById('distance').value;
	var e = document.getElementById("category");
	var category = e.options[e.selectedIndex].text;
	params += "&category="+category;
	var req;
    if (window.XMLHttpRequest) {
        req = new XMLHttpRequest();
        } else if (window.ActiveXObject) {
        req = new ActiveXObject("Microsoft.XMLHTTP");
        }
    if (req != undefined) {
        try {
            req.open("POST", 'places.php', false);
			req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
            }
        catch(err) {
            alert(err.message);
            }
        req.send(params); // param string only used for POST

        if (req.readyState == 4) { // only if req is "loaded"
            if (req.status == 200)  // only if "OK"
                { 
				parseJSONData(req.responseText);
				}
            else    { alert("XHR error: " + req.status +" "+req.statusText); }
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
}
</script>
</head>
<body style="text-align:center;" onLoad="getLocation()">
<div style="padding:0px 8px; background-color:#F5F5F5; width:600px; border:3px solid #E0E0E0; margin:0 auto;">
<div id="header">Travel and Entertainment Search</div>
<hr/>
<form style="text-align:left" method="POST" id="travelform">
<label class="tag">Keyword <input type="text" id="keyword" name="keyword" value="<?php if(isset($_POST['keyword']))echo $_POST['keyword'];?>" required/></label>
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
<script type="text/javascript">
  document.getElementById('category').value = "<?php echo $_POST['category'];?>";
</script>
<label class="tag">Distance (miles) <input type="text" placeholder="10" name="distance" id="distance" value="<?php if(isset($_POST['distance'])) echo $_POST['distance'];?>"/>
from <table style="display:inline; vertical-align: top;"><tr><td><input id="here" type="radio" name="rbtlocation" value="here" onChange="radioButtonSelected()" checked/><span style="font-weight:400">Here</span></td></tr>
<tr><td><input type="radio" id="location" name="rbtlocation" value="other" onChange="radioButtonSelected()"/> 
<input type="text" placeholder="location"  name="location" required disabled id="txtlocation" value="<?php if(isset($_POST['location']))echo $_POST['location'];?>"/></td></tr></table></label>
<script type="text/javascript">
  var radioValue = "<?php echo $_POST['rbtlocation'];?>";
  if(radioValue == 'here'){
	  document.getElementById('here').checked = true;
	  document.getElementById("txtlocation").setAttribute('disabled', true);
  }else{
	  document.getElementById('location').checked = true;
	  document.getElementById('here').checked = false;
	  document.getElementById("txtlocation").removeAttribute('disabled');
  }
</script>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <input id="btnSearch" type="Submit" value="Search" name="search" onClick="searchClicked(event)" disabled/>
<input type="button" value="Clear" onClick="clearFields()"/><br/><br/>
</form>
</div>
<div id="result" style="width:80%; text-align:center; display:inline-block;"></div>
</body>
</html>
<?php } ?>