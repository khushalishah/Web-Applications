<?php 
	$data = "";
	$loadPlaces = 1;
	$lattitude = 0;
	$longitude = 0;
	if(isset($_GET["search"])){
		if(isset($_GET["location"]) && $_GET["location"] != ""){
			//get geo location
			$url = "https://maps.googleapis.com/maps/api/geocode/json?address=".urlencode($_GET["location"])."&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0";
			$data = file_get_contents($url); 
			$characters = json_decode($data);
			$lattitude = $characters->results[0]->geometry->location->lat;
			$longitude = $characters->results[0]->geometry->location->lng;
		}else{
			$lattitude = $_GET['lattitude'];
			$longitude = $_GET['longitude'];
		}
		$url = 'https://maps.googleapis.com/maps/api/place/nearbysearch/json?location='.$lattitude.','.$longitude.'&radius='.($_GET['distance']*1609.34).'&types='.urlencode($_GET['category']).'&keyword='.urlencode($_GET['keyword']).'&key=AIzaSyBcrBxlXi_PvW591GLcKs4St2gkJK_V4I0';
		$data = file_get_contents($url);
		echo $data;
		//die();
	}
?>