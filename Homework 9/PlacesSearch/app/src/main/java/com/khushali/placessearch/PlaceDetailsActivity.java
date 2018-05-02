package com.khushali.placessearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class PlaceDetailsActivity extends AppCompatActivity implements Callback{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    String placeid;
    String jsonData="";
    ProgressDialog progressDialog;
    boolean isFav;
    String address = "", picURL = "", placeName = "", tweetURL = "";
    boolean loaded = false;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        placeid = getIntent().getStringExtra("placeid");
        placeName = getIntent().getStringExtra("placename");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(placeName);
        isFav = getIntent().getBooleanExtra("isFav",false);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!loaded) {
            getPlaceDetails(placeid);
        }
    }

    void getPlaceDetails(String placeid){
        progressDialog = new ProgressDialog(PlaceDetailsActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching details...");
        progressDialog.show();
        HashMap<String,String> params = new HashMap<>();
        params.put("placeid",placeid);
        params.put("key",getString(R.string.key));

        if(ConnectionDetector.checkConnection(getBaseContext())) {
            LoadData loadData = new LoadData(getString(R.string.googleApiURL), params, this);
            loadData.execute();
        }else{
            Toast.makeText(getBaseContext(),"No network available",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void getData(String data) {
        try {
            jsonData = data;
            JSONObject jsonObject = new JSONObject(jsonData);
            jsonObject = jsonObject.getJSONObject("result");
            address = jsonObject.getString("formatted_address");
            picURL = jsonObject.getString("icon");
            if(jsonObject.has("url")){
                tweetURL = jsonObject.getString("url");
            }else if(jsonObject.has("website")){
                tweetURL = jsonObject.getString("website");
            }
            Log.d("PlaceDetailsActicity",jsonData);
            mSectionsPagerAdapter.notifyDataSetChanged();
            mViewPager.setAdapter(mSectionsPagerAdapter);
            loaded = true;
        }catch (Exception e){

        }finally {
            progressDialog.dismiss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place_details, menu);
        MenuItem item = menu.findItem(R.id.action_fav);
        if (isFav)
            item.setIcon(R.drawable.ic_heart_fill_white);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_fav) {
            FavoritesDataSource dataSource = new FavoritesDataSource(getBaseContext());
            dataSource.open();
            if(isFav){
                //remove from favorites
                dataSource.deleteFromFavorites(placeid);
                item.setIcon(R.drawable.ic_heart_outline_white);
                Toast.makeText(getBaseContext(),placeName+" was removed from favorites",Toast.LENGTH_SHORT).show();
                isFav = false;
            }else{
                //add to favorites
                Place place = new Place(placeid,placeName,address,true,picURL);
                dataSource.addToFavorites(place);
                item.setIcon(R.drawable.ic_heart_fill_white);
                Toast.makeText(getBaseContext(),placeName+" was added to favorites",Toast.LENGTH_SHORT).show();
                isFav = true;
            }
            dataSource.close();
            return true;
        }else if(id == R.id.action_share){
            //open twitter url
            String tweetParams = "text="+ URLEncoder.encode("Check out "+placeName+" located at "+address+". Website ")+"&url="+URLEncoder.encode(tweetURL)+"&hashtags=TravelAndEntertainmentSearch";
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?"+tweetParams));
            startActivity(browserIntent);
            return true;
        }else if(id == android.R.id.home){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            Bundle bundle = new Bundle();
            bundle.putString("placeid",placeid);
            bundle.putString("data",jsonData);
            switch (position){
                case 0:
                    InfoFragment fragment = new InfoFragment();
                    fragment.setArguments(bundle);
                    return fragment;
                case 1:
                    PhotosFragment fragment1 = new PhotosFragment();
                    fragment1.setArguments(bundle);
                    return fragment1;
                case 2:
                    MapFragment fragment2 = new MapFragment();
                    fragment2.setArguments(bundle);
                    return fragment2;
                case 3:
                    ReviewFragment fragment3 = new ReviewFragment();
                    fragment3.setArguments(bundle);
                    return fragment3;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }
    }
}
