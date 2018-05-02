package com.khushali.placessearch;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class SearchFragment extends Fragment implements Callback {

    View view;
    EditText etDistance, etKeyword;
    AutoCompleteTextView etLocation;
    RadioButton rbtCLocation, rbtOLocation;
    RadioGroup radioGroup;
    TextView tvErrorKeyword, tvErrorLocation;
    ArrayList<Place> listPlaces;
    Spinner spCategory;
    ProgressDialog progressDialog;
    static int MY_PERMISSION_ACCESS_COURSE_LOCATION = 10;
    double latitude, longitude;
    HashMap<String, String> parameters;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, null);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        initializeComponents();
    }

    void initializeComponents() {
        etDistance = (EditText) view.findViewById(R.id.et_distance);
        etKeyword = (EditText) view.findViewById(R.id.et_keyword);
        etLocation = (AutoCompleteTextView) view.findViewById(R.id.et_location);
        radioGroup = (RadioGroup) view.findViewById(R.id.rg_from);
        rbtCLocation = (RadioButton) view.findViewById(R.id.rbt_clocation);
        rbtOLocation = (RadioButton) view.findViewById(R.id.rbt_olocation);
        tvErrorKeyword = (TextView) view.findViewById(R.id.tv_error_keyword);
        tvErrorLocation = (TextView) view.findViewById(R.id.tv_error_location);
        spCategory = (Spinner) view.findViewById(R.id.sp_category);

        //Set adapter for autocomplete text view

        CustomAutoCompleteAdapter adapter =  new CustomAutoCompleteAdapter(getActivity());
        etLocation.setAdapter(adapter);
        etLocation.setOnItemClickListener(onItemClickListener);
        listPlaces = new ArrayList<>();

        //radio group click listener
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbtCLocation.isChecked()) {
                    etLocation.setFocusableInTouchMode(false);
                    etLocation.setFocusable(false);
                    etLocation.setText("");
                    tvErrorLocation.setVisibility(View.GONE);
                } else {
                    etLocation.setFocusableInTouchMode(true);
                    etLocation.setFocusable(true);
                }
            }
        });

        //search button click listener
        getActivity().findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchButtonClicked();
            }
        });

        view.findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearForm();
            }
        });
    }

    void searchButtonClicked() {
        //check validation
        if (checkValidation()) {
            checkPermission();
        } else {
            Toast.makeText(getActivity(), "Please fix all fields with errors", Toast.LENGTH_SHORT).show();
        }
    }

    void checkPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_COURSE_LOCATION);
        } else {
            getLocation();
        }
    }

    void getLocation(){
        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Location location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if(location != null) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        Log.d("Location: ",""+latitude+" "+longitude);

        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 10, locationListener);
        getPlaceSearches();
        }else{
            Toast.makeText(getActivity(),"Can not get location of device",Toast.LENGTH_SHORT).show();
        }
    }

    void getPlaceSearches() {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching results");
        progressDialog.show();

        String distance = "10";
        if (!etDistance.getText().toString().equals("")) {
            distance = etDistance.getText().toString();
        }
        String location = "";
        if (rbtOLocation.isChecked()) {
            location = etLocation.getText().toString();
        }

        parameters = new HashMap<>();
        parameters.put("keyword", etKeyword.getText().toString());
        parameters.put("category", spCategory.getSelectedItem().toString());
        parameters.put("distance", distance);
        parameters.put("lattitude", latitude + "");
        parameters.put("longitude", longitude + "");
        parameters.put("location", location);

        if(ConnectionDetector.checkConnection(getActivity())) {
            LoadData loadData = new LoadData(getString(R.string.url), parameters, this);
            loadData.execute();
        }else{
            Toast.makeText(getActivity(),"No network available",Toast.LENGTH_SHORT).show();
        }
        }

    boolean checkValidation() {
        boolean result = true;
        if (etKeyword.getText().toString().trim().equals("")) {
            tvErrorKeyword.setVisibility(View.VISIBLE);
            result = false;
        }
        if (etLocation.isFocusableInTouchMode() && etLocation.isFocusable()) {
            if (etLocation.getText().toString().trim().equals("")) {
                tvErrorLocation.setVisibility(View.VISIBLE);
                result = false;
            }
        }
        return result;
    }

    void clearForm() {
        etKeyword.setText("");
        etLocation.setText("");
        etDistance.setText("");
        tvErrorLocation.setVisibility(View.GONE);
        tvErrorKeyword.setVisibility(View.GONE);
        rbtCLocation.setChecked(true);
        spCategory.setSelection(0);
    }

    @Override
    public void getData(String data) {
        FavoritesDataSource dataSource = new FavoritesDataSource(getActivity());
        dataSource.open();
        Set<String> ids = dataSource.getAllPlaceIds();
        dataSource.close();
        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject res = jsonArray.getJSONObject(i);
                Place place = new Place();
                place.setPlaceid(res.getString("place_id"));
                place.setName(res.getString("name"));
                place.setAddress(res.getString("vicinity"));
                place.setPicURL(res.getString("icon"));
                if(ids.contains(res.getString("place_id")))
                    place.setFavoriteItem(true);
                else
                    place.setFavoriteItem(false);

                listPlaces.add(place);
            }
            String token = "";
            if(jsonObject.has("next_page_token")){
                token = jsonObject.getString("next_page_token");
            }

            Intent intent = new Intent(getActivity(),PlaceListActivity.class);
            intent.putParcelableArrayListExtra("places",listPlaces);
            intent.putExtra("token",token);
            intent.putExtra("parameters",parameters);
            startActivity(intent);
        } catch (Exception e) {

        } finally {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_ACCESS_COURSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }else{
                Toast.makeText(getActivity(),"Search will not work unless you give permission for location",Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private AdapterView.OnItemClickListener onItemClickListener =
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    //do something with the selection
                    etLocation.setText(((GooglePlace)adapterView.
                            getItemAtPosition(i)).getPlaceText());
                    //searchScreen();
                }
            };

    public void searchScreen(){
        Intent i = new Intent();
        i.setClass(getActivity(), MainActivity.class);
        startActivity(i);
    }
}
