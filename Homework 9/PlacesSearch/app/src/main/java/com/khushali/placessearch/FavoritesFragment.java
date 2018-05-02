package com.khushali.placessearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    View view;
    FavoritesDataSource dataSource;
    int pageNo = 1;
    List<Place> placeList;
    RecyclerView recyclerView;
    FavViewAdapter adapter;
    int totalPages = 0;
    Button btnPrevious, btnNext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_recyclerview,null);
        placeList = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        btnPrevious = (Button) view.findViewById(R.id.btn_previous);
        btnNext = (Button) view.findViewById(R.id.btn_next);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(lm);
        adapter = new FavViewAdapter();
        recyclerView.setAdapter(adapter);
        dataSource = new FavoritesDataSource(getActivity());
        dataSource.open();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("called","called");
        loadPage();
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNo--;
                loadPage();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNo++;
                loadPage();
            }
        });
    }

    void loadPage(){
        int totalCount = dataSource.getFavoritesCount();
        totalPages = totalCount/20;
        if(pageNo == 1){
            btnPrevious.setEnabled(false);
            btnNext.setEnabled(true);
        }else if(pageNo == totalCount){
            btnNext.setEnabled(false);
            btnPrevious.setEnabled(true);
        }
        if(totalCount%20!=0)
            totalPages++;
        placeList = dataSource.getAllFavorites((pageNo-1)*10,20);
        if(totalCount==0){
            view.findViewById(R.id.ll_data).setVisibility(View.GONE);
            ((TextView) view.findViewById(R.id.tv_no_data)).setText("No Favorites");
        }else{
            view.findViewById(R.id.ll_data).setVisibility(View.VISIBLE);
            if(totalCount<=20){
                view.findViewById(R.id.ll_pagination).setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }

    class FavViewAdapter extends RecyclerView.Adapter<FavViewAdapter.FavViewHolder>{

        @Override
        public void onBindViewHolder(@NonNull FavViewHolder holder, int position) {
            final Place place = placeList.get(position);
            holder.tvPlaceName.setText(place.getName());
            holder.tvPlaceAddress.setText(place.getAddress());
            holder.tgFavorite.setChecked(true);

            Picasso.get()
                    .load(place.getPicURL())
                    .into(holder.ivIcon);

            holder.tgFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(buttonView.isPressed()) {
                            //remove item from favorite list
                            dataSource.deleteFromFavorites(place.getPlaceid());
                            loadPage();
                            Toast.makeText(getActivity(), place.getName() + " was removed from favorites", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open place details activity
                    Intent intent = new Intent(getActivity(),PlaceDetailsActivity.class);
                    intent.putExtra("placeid",place.getPlaceid());
                    intent.putExtra("placename",place.getName());
                    intent.putExtra("isFav",place.isFavoriteItem());
                    startActivity(intent);
                }
            });

        }

        @NonNull
        @Override
        public FavViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_place,parent,false);
            FavViewHolder holder = new FavViewHolder(view);
            return holder;
        }

        @Override
        public int getItemCount() {
            return placeList.size();
        }

        class FavViewHolder extends RecyclerView.ViewHolder{

            TextView tvPlaceName, tvPlaceAddress;
            ImageView ivIcon;
            ToggleButton tgFavorite;

            FavViewHolder(View itemView){
                super(itemView);
                tvPlaceName = (TextView) itemView.findViewById(R.id.tv_place);
                tvPlaceAddress = (TextView) itemView.findViewById(R.id.tv_address);
                tgFavorite = (ToggleButton) itemView.findViewById(R.id.tg_favorite);
                ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            }
        }
    }
}
