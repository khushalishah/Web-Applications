package com.khushali.placessearch;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

public class ReviewFragment extends Fragment implements Callback{

    View view;
    RecyclerView recyclerView;
    String googleReviewsData="", yelpReviewsData="";
    ArrayList<Review> reviewList;
    ReviewAdapter adapter;
    Spinner spReviewType,spSort;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_review,null);
        reviewList = new ArrayList<>();
        initialization();
        googleReviewsData = getArguments().getString("data");
        parseGoogleReviews();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    void initialization(){
        spReviewType = (Spinner) view.findViewById(R.id.sp_review_type);
        spSort = (Spinner) view.findViewById(R.id.sp_sort);

        spReviewType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    parseGoogleReviews();
                }else{
                    if(yelpReviewsData.equals("")){
                        loadYelpReviews();
                    }else{
                        parseYelpReviews();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    sortReviews("default","");
                }else if(position == 1){
                    sortReviews("rating","des");
                }else if(position == 2){
                    sortReviews("rating","asc");
                }else if(position == 3){
                    sortReviews("time","des");
                }else{
                    sortReviews("time","asc");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        adapter = new ReviewAdapter();
        recyclerView.setAdapter(adapter);
    }

    void sortReviews(String type,String order){
        if(type.equals("default")){
            if(spReviewType.getSelectedItemPosition()==0){
                parseGoogleReviews();
            }else {
                parseYelpReviews();
            }
        }else if(type.equals("rating")){
            if(order.equals("des")){
                Collections.sort(reviewList, new Comparator<Review>() {
                    @Override
                    public int compare(Review o1, Review o2) {
                        return Float.compare(o2.getRating(),o1.getRating());
                    }
                });
            }else{
                Collections.sort(reviewList, new Comparator<Review>() {
                    @Override
                    public int compare(Review o1, Review o2) {
                        return Float.compare(o1.getRating(),o2.getRating());
                    }
                });
            }
            adapter.notifyDataSetChanged();
        }else{
            if(order.equals("des")){
                Collections.sort(reviewList, new Comparator<Review>() {
                    @Override
                    public int compare(Review o1, Review o2) {
                        return o2.getDate().compareTo(o1.getDate());
                    }
                });
            }else {
                Collections.sort(reviewList, new Comparator<Review>() {
                    @Override
                    public int compare(Review o1, Review o2) {
                        return o1.getDate().compareTo(o2.getDate());
                    }
                });
            }
            adapter.notifyDataSetChanged();
        }
    }

    void loadYelpReviews(){
        HashMap<String,String> params = new HashMap<>();
        params.put("yelp","true");
        try {
            JSONObject jsonObject = new JSONObject(googleReviewsData);
            jsonObject = jsonObject.getJSONObject("result");
            params.put("name",jsonObject.getString("name"));
            params.put("address1",jsonObject.getString("formatted_address"));

            JSONArray jsonArray = jsonObject.getJSONArray("address_components");
            String city="",state="",country="",postalCode="";

            for(int i=0;i<jsonArray.length();i++){
                JSONObject jo = jsonArray.getJSONObject(i);
                String type = jo.getJSONArray("types").getString(0);
                if(type.equals("administrative_area_level_1")){
                    state = jo.getString("short_name");
                }else if(type.equals("locality")){
                    city = jo.getString("short_name");
                }else if(type.equals("country")){
                    country = jo.getString("short_name");
                }else if(type.equals("postal_code")){
                    postalCode = jo.getString("short_name");
                }
            }
            params.put("city",city);
            params.put("state",state);
            params.put("country",country);
            params.put("postal_code",postalCode);

            if(ConnectionDetector.checkConnection(getActivity())) {
                LoadData loadData = new LoadData(getString(R.string.url), params, this);
                loadData.execute();
            }else{
                Toast.makeText(getActivity(),"No netowrk available",Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    void parseYelpReviews(){
        reviewList.clear();
        try {
            JSONObject jsonObject = new JSONObject(yelpReviewsData);
            JSONArray jsonArray = jsonObject.getJSONArray("reviews");
            if(jsonArray.length()==0){
                recyclerView.setVisibility(View.GONE);
            }else{
                recyclerView.setVisibility(View.VISIBLE);

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jo = jsonArray.getJSONObject(i);
                    Review review = new Review();
                    review.setDate(jo.getString("time_created"));
                    review.setRating((float) jo.getDouble("rating"));
                    review.setAuthURL(jo.getString("url"));
                    review.setPicURL(jo.getJSONObject("user").getString("image_url"));
                    review.setReview(jo.getString("text"));
                    review.setName(jo.getJSONObject("user").getString("name"));

                    reviewList.add(review);
                }
                int position = spSort.getSelectedItemPosition();
                if(position==0){
                    adapter.notifyDataSetChanged();
                }else if(position == 1){
                    sortReviews("rating","des");
                }else if(position == 2){
                    sortReviews("rating","asc");
                }else if(position == 3){
                    sortReviews("time","des");
                }else{
                    sortReviews("time","asc");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void parseGoogleReviews(){
        reviewList.clear();
        try {
            JSONObject jsonObject = new JSONObject(googleReviewsData);
            jsonObject = jsonObject.getJSONObject("result");
            JSONArray jsonArray = jsonObject.getJSONArray("reviews");

            if(jsonArray.length()==0){
                recyclerView.setVisibility(View.GONE);
            }else{
                recyclerView.setVisibility(View.VISIBLE);
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jo = jsonArray.getJSONObject(i);
                    Review review = new Review();
                    review.setName(jo.getString("author_name"));
                    review.setReview(jo.getString("text"));
                    review.setPicURL(jo.getString("profile_photo_url"));
                    review.setAuthURL(jo.getString("author_url"));
                    review.setRating((float)jo.getDouble("rating"));

                    Date date = new Date(jo.getLong("time")*1000);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    review.setDate(sdf.format(date));

                    reviewList.add(review);
                }
                int position = spSort.getSelectedItemPosition();
                if(position==0){
                    adapter.notifyDataSetChanged();
                }else if(position == 1){
                    sortReviews("rating","des");
                }else if(position == 2){
                    sortReviews("rating","asc");
                }else if(position == 3){
                    sortReviews("time","des");
                }else{
                    sortReviews("time","asc");
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void getData(String data) {
        yelpReviewsData = data;
        parseYelpReviews();
    }

    class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>{

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.item_review,parent,false);
            ReviewViewHolder viewHolder = new ReviewViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            final Review review = reviewList.get(position);
            holder.tvName.setText(review.getName());
            holder.tvReview.setText(review.getReview());
            holder.tvDate.setText(review.getDate());
            Picasso.get()
                    .load(review.getPicURL())
                    .into(holder.ivPhoto);
            holder.ratingBar.setRating(review.getRating());
            holder.ratingBar.setNumStars((int) Math.ceil(review.getRating()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(review.getAuthURL()));
                    startActivity(browserIntent);
                }
            });
        }

        class ReviewViewHolder extends RecyclerView.ViewHolder{

            TextView tvName, tvDate, tvReview;
            ImageView ivPhoto;
            RatingBar ratingBar;

            ReviewViewHolder(View itemView){
                super(itemView);

                tvDate = (TextView) itemView.findViewById(R.id.tv_date);
                tvName = (TextView) itemView.findViewById(R.id.tv_name);
                tvReview = (TextView) itemView.findViewById(R.id.tv_review);
                ratingBar = (RatingBar) itemView.findViewById(R.id.rating);
                ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            }
        }
    }
}
