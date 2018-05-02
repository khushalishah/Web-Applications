package com.khushali.placessearch;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback, Callback {


    View view;
    double latitude, longitude;
    MapView mapView;
    static GoogleMap mMap;
    String placeName = "";
    AutoCompleteTextView etFromLocation;
    String mode = "driving";
    String oPlaceId, dPlaceId;
    LatLng latLng;
    String oName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_map, null);
        String data = getArguments().getString("data");
        dPlaceId = getArguments().getString("placeid");
        try {
            JSONObject jsonObject = new JSONObject(data);
            jsonObject = jsonObject.getJSONObject("result");
            placeName = jsonObject.getString("name");
            jsonObject = jsonObject.getJSONObject("geometry");
            jsonObject = jsonObject.getJSONObject("location");
            latitude = jsonObject.getDouble("lat");
            longitude = jsonObject.getDouble("lng");
        } catch (Exception e) {
        }

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mapView.getMapAsync(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initialization();
    }

    void initialization() {
        etFromLocation = (AutoCompleteTextView) view.findViewById(R.id.et_from_location);
        CustomAutoCompleteAdapter adapter = new CustomAutoCompleteAdapter(getActivity());
        etFromLocation.setAdapter(adapter);
        etFromLocation.setOnItemClickListener(onItemClickListener);
        final Spinner spinner = (Spinner) view.findViewById(R.id.sp_travel_mode);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //get map directions
                if(!etFromLocation.getText().toString().equals("")){
                    mode = spinner.getSelectedItem().toString().toLowerCase();
                    setGoogleMapDirections();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //do something with the selection
                    GooglePlace p = (GooglePlace) adapterView.getItemAtPosition(i);
                    etFromLocation.setText(p.getPlaceText());
                    oPlaceId = p.getPlaceId();
                    //get directions in map
                    GeoDataClient geoDataClient = Places.getGeoDataClient(getActivity(), null);
                    geoDataClient.getPlaceById(p.getPlaceId()).addOnCompleteListener(new OnCompleteListener<PlaceBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
                            if (task.isSuccessful()) {
                                PlaceBufferResponse places = task.getResult();
                                Place myPlace = places.get(0);
                                oName = myPlace.getName().toString();
                                latLng = myPlace.getLatLng();
                                setGoogleMapDirections();
                                //Log.i(TAG, "Place found: " + myPlace.getName());
                                places.release();
                            } else {
                                //Log.e(TAG, "Place not found.");
                            }
                        }
                    });
                }
            };

    void setGoogleMapDirections() {
        mMap.clear();
        // Creating MarkerOptions
        /*MarkerOptions options = new MarkerOptions();

        // Setting the position of the marker
        options.position(latLng);


        // Add new marker to the Google Map Android API V2
        mMap.addMarker(options);
        options.position(new LatLng(latitude,longitude));
        mMap.addMarker(options);*/

        // Getting URL to the Google Directions API
        String url = getURL();
        Log.d("onMapClick", url.toString());

        if(ConnectionDetector.checkConnection(getActivity())) {
            LoadData loadData = new LoadData(url, null, this);
            loadData.execute();
        }else{
            Toast.makeText(getActivity(),"No network available",Toast.LENGTH_SHORT).show();
        }
    }

    public String getURL() {
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=place_id:");// from
        urlString.append(oPlaceId);
        urlString.append("&destination=place_id:");// to
        urlString.append(dPlaceId);
        urlString.append("&mode=");
        urlString.append(mode);
        urlString.append("&key=");
        urlString.append(getString(R.string.key));
        return urlString.toString();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            mMap.setMyLocationEnabled(true);
            LatLng place = new LatLng(latitude, longitude);
            Marker marker = mMap.addMarker(new MarkerOptions().position(place)
                    .title(placeName));
            marker.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place, 12.0f));

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getData(String data) {
        try {
            //Transform the string into a json object
            final JSONObject json = new JSONObject(data);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(10)
                        .color(Color.BLUE).geodesic(true));

                if(z==0){
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(src.latitude,src.longitude)).title(oName));
                    marker.showInfoWindow();
                }else if(z==list.size()-2){
                    mMap.addMarker(new MarkerOptions().position(new LatLng(dest.latitude,dest.longitude)));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }


}
