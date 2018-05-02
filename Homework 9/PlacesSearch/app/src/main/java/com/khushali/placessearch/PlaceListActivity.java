package com.khushali.placessearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class PlaceListActivity extends AppCompatActivity implements Callback{

    String token = "";
    int pageNo = 0;
    HashMap<String,String>[] parameters;
    ProgressDialog progressDialog;
    Button btnNext, btnPrevious;
    RecyclerView recyclerView;
    PlacesViewAdapter adapter;
    ArrayList<Place> placesList;
    FavoritesDataSource dataSource;
    Set<String> ids;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recyclerview);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Search Results");

        Intent intent = getIntent();
        placesList = intent.getParcelableArrayListExtra("places");
        token = intent.getStringExtra("token");
        parameters = new HashMap[3];
        parameters[0] = (HashMap<String, String>) intent.getSerializableExtra("parameters");

        if(token.equals("")){
            findViewById(R.id.ll_pagination).setVisibility(View.GONE);
        }else{
            HashMap<String,String> params = new HashMap<>();
            params.put("pagetoken",token);
            parameters[1] = params;
        }

        if(placesList.size() == 0){
            findViewById(R.id.ll_data).setVisibility(View.GONE);
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getBaseContext());
        recyclerView.setLayoutManager(lm);

    }

    @Override
    protected void onStart() {
        super.onStart();

        dataSource = new FavoritesDataSource(getBaseContext());
        dataSource.open();
        ids = dataSource.getAllPlaceIds();

        adapter = new PlacesViewAdapter();
        recyclerView.setAdapter(adapter);

        btnPrevious = (Button) findViewById(R.id.btn_previous);
        btnNext = (Button) findViewById(R.id.btn_next);

        if(pageNo == 0){
            btnPrevious.setEnabled(false);
        }

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNo--;
                if(pageNo==0){
                    btnPrevious.setEnabled(false);
                }else {
                    btnPrevious.setEnabled(true);
                }
                loadData(false);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNo++;
                loadData(true);
                btnPrevious.setEnabled(true);
            }
        });

    }

    void loadData(boolean isNextPage){
        String msg = "Fetching previous page...";
        if(isNextPage){
            msg = "Fetching next page";
        }
        progressDialog = new ProgressDialog(PlaceListActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
        placesList.clear();
        if(ConnectionDetector.checkConnection(getBaseContext())) {
            LoadData loadData = new LoadData(getString(R.string.url), parameters[pageNo], this);
            loadData.execute();
        }else{
            Toast.makeText(getBaseContext(),"No network available",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void getData(String data) {
        try{
            JSONObject jsonObject = new JSONObject(data);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            if(jsonArray.length()==0){
                findViewById(R.id.ll_data).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.tv_no_data)).setText("No results");
            }else {
                for (int i = 0; i < jsonArray.length(); i++) {
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

                    placesList.add(place);
                }
                if (jsonObject.has("next_page_token")) {
                    token = jsonObject.getString("next_page_token");
                    HashMap<String, String> params = new HashMap<>();
                    params.put("pagetoken", token);
                    parameters[pageNo + 1] = params;
                    btnNext.setEnabled(true);
                } else {
                    btnNext.setEnabled(false);
                }
                adapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            progressDialog.dismiss();
        }
    }

    class PlacesViewAdapter extends RecyclerView.Adapter<PlacesViewAdapter.PlaceViewHolder>{

        @Override
        public void onBindViewHolder(@NonNull PlaceViewHolder holder, int position) {
            final Place place = placesList.get(position);
            holder.tvPlaceName.setText(place.getName());
            holder.tvPlaceAddress.setText(place.getAddress());
            if(ids.contains(place.getPlaceid())){
                holder.tgFavorite.setChecked(true);
                place.setFavoriteItem(true);
            }else{
                place.setFavoriteItem(false);
                holder.tgFavorite.setChecked(false);
            }

            Picasso.get()
                    .load(place.getPicURL())
                    .into(holder.ivIcon);

            holder.tgFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(buttonView.isPressed()) {
                        if (isChecked) {
                            //add item to favorite list
                            place.setFavoriteItem(true);
                            dataSource.addToFavorites(place);
                            Toast.makeText(getBaseContext(), place.getName() + " was added to favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            //remove item from favorite list
                            place.setFavoriteItem(false);
                            dataSource.deleteFromFavorites(place.getPlaceid());
                            Toast.makeText(getBaseContext(), place.getName() + " was removed from favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open place details activity
                    Intent intent = new Intent(PlaceListActivity.this,PlaceDetailsActivity.class);
                    intent.putExtra("placeid",place.getPlaceid());
                    intent.putExtra("placename",place.getName());
                    intent.putExtra("isFav",place.isFavoriteItem());
                    startActivity(intent);
                }
            });

        }

        @NonNull
        @Override
        public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_place,parent,false);
            PlaceViewHolder holder = new PlaceViewHolder(view);
            return holder;
        }

        @Override
        public int getItemCount() {
            return placesList.size();
        }

        class PlaceViewHolder extends RecyclerView.ViewHolder{

            TextView tvPlaceName, tvPlaceAddress;
            ImageView ivIcon;
            ToggleButton tgFavorite;

            PlaceViewHolder(View itemView){
                super(itemView);
                tvPlaceName = (TextView) itemView.findViewById(R.id.tv_place);
                tvPlaceAddress = (TextView) itemView.findViewById(R.id.tv_address);
                tgFavorite = (ToggleButton) itemView.findViewById(R.id.tg_favorite);
                ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
