package com.example.thehustler.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.R;
import com.example.thehustler.classes.SquareImageView;

import java.util.List;

public class ImageAdp  extends RecyclerView.Adapter<ImageAdp.Slider> {
   List<String> Images;
   List<String> thumbs;
   private ViewPager2 viewPager2;
    public Context context;

    public ImageAdp(List<String> images, List<String> thumbs, ViewPager2 viewPager2) {
        Images = images;
        this.thumbs = thumbs;
        this.viewPager2 = viewPager2;
    }

    @NonNull
    @Override
    public Slider onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_images,parent, false);
        context = parent.getContext();
        return new ImageAdp.Slider(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Slider holder, int position) {
        holder.setImage(Images.get(position),thumbs.get(position));
    }

    @Override
    public int getItemCount() {
        return Images.size();
    }

    class Slider extends RecyclerView.ViewHolder {
        private SquareImageView postIm;


       Slider(@NonNull View itemView) {
            super(itemView);
            postIm = itemView.findViewById(R.id.grid_image);

        }
        void setImage(String s, String s1){

            RequestOptions placRO = new RequestOptions();
            placRO.placeholder(R.drawable.ic_place);
             Glide.with(context).applyDefaultRequestOptions(placRO)
                     .load(s).thumbnail(Glide.with(context).load(s1))
                   .into(postIm);


            //postIm.setImageResource(item.getImage());
        }
    }


}
