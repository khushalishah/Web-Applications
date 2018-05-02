package com.khushali.placessearch;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.HashMap;

public class InfoFragment extends Fragment{

    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_info,null);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        String placeid = bundle.getString("placeid");
        setPLaceDetails(bundle.getString("data"));
    }

    void setPLaceDetails(String data){
        try {
            JSONObject jsonObject = new JSONObject(data);
            jsonObject = jsonObject.getJSONObject("result");
            if(jsonObject.has("formatted_address")){
                ((TextView) view.findViewById(R.id.tv_address)).setText(jsonObject.getString("formatted_address"));
            }else{
                ((TextView) view.findViewById(R.id.tv_address)).setText("NA");
            }
            if(jsonObject.has("international_phone_number")){
                ((TextView) view.findViewById(R.id.tv_phone_no)).setText(jsonObject.getString("international_phone_number"));
            }else{
                ((TextView) view.findViewById(R.id.tv_phone_no)).setText("NA");
            }
            if(jsonObject.has("url")){
                ((TextView) view.findViewById(R.id.tv_google_url)).setText(jsonObject.getString("url"));
            }else{
                ((TextView) view.findViewById(R.id.tv_google_url)).setText("NA");
            }
            if(jsonObject.has("website")){
                ((TextView) view.findViewById(R.id.tv_web_url)).setText(jsonObject.getString("website"));
            }else{
                ((TextView) view.findViewById(R.id.tv_web_url)).setText("NA");
            }
            RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating);
            if(jsonObject.has("rating")){
                ratingBar.setRating((float) jsonObject.getDouble("rating"));
                ratingBar.setNumStars((int) Math.ceil(jsonObject.getDouble("rating")));
            }else{
                ratingBar.setRating(0);
                ratingBar.setNumStars(0);
            }
            TextView tvPriceLevel = (TextView) view.findViewById(R.id.tv_price_level);
            if(jsonObject.has("price_level")){
                int priceLevel = jsonObject.getInt("price_level");
                if(priceLevel<=1){
                    tvPriceLevel.setText("$");
                }else if(priceLevel == 2){
                    tvPriceLevel.setText("$$");
                }else if(priceLevel == 3){
                    tvPriceLevel.setText("$$$");
                }else{
                    tvPriceLevel.setText("$$$$");
                }
            }else{
                tvPriceLevel.setText("NA");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
