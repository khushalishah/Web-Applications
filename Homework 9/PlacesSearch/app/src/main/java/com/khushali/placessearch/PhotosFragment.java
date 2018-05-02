package com.khushali.placessearch;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class PhotosFragment extends Fragment {

    View view;
    private GeoDataClient mGeoDataClient;
    PlacePhotoMetadataBuffer photoMetadataBuffer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_photos,null);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        getPhotos(getArguments().getString("placeid"));

    }

    // Request photos and metadata for the specified place.
    private void getPhotos(String placeId) {
        mGeoDataClient = Places.getGeoDataClient(getActivity(), null);
        final Task<PlacePhotoMetadataResponse> photoMetadataResponse = mGeoDataClient.getPlacePhotos(placeId);
        photoMetadataResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                // Get the list of photos.
                PlacePhotoMetadataResponse photos = task.getResult();
                photoMetadataBuffer = photos.getPhotoMetadata();
                setPhotos();
            }
        });
    }

    void setPhotos(){
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        if(photoMetadataBuffer.getCount()==0){
            recyclerView.setVisibility(View.GONE);
        }else {
            LinearLayoutManager lm = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(lm);
            recyclerView.setAdapter(new PhotosAdapter());
        }
    }

    class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotoViewHolder>{

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.item_photo,parent,false);
            PhotoViewHolder viewHolder = new PhotoViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull final PhotoViewHolder holder, int position) {
            // Get the first photo in the list.
            PlacePhotoMetadata photoMetadata = photoMetadataBuffer.get(position);
            // Get the attribution text.
            CharSequence attribution = photoMetadata.getAttributions();
            // Get a full-size bitmap for the photo.
            Task<PlacePhotoResponse> photoResponse = mGeoDataClient.getPhoto(photoMetadata);
            photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                    PlacePhotoResponse photo = task.getResult();
                    Bitmap bitmap = photo.getBitmap();
                    holder.ivPhoto.setImageBitmap(bitmap);
                }
            });
        }

        @Override
        public int getItemCount() {
            return photoMetadataBuffer.getCount();
        }

        class PhotoViewHolder extends RecyclerView.ViewHolder{

            ImageView ivPhoto;

            PhotoViewHolder(View itemView){
                super(itemView);
                ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            }

        }
    }

}
