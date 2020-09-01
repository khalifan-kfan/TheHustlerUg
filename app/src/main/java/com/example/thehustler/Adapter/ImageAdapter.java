package com.example.thehustler.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.thehustler.R;

import java.util.List;

public class ImageAdapter extends BaseAdapter {

    Context ctx;
    List<Bitmap>images;

    public ImageAdapter(Context ctx, List<Bitmap> images) {
        this.ctx = ctx;
        this.images = images;
    }



    @Override
    public int getCount() {
        if (images != null) return images.size();
        else
            return 0;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView photos;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_images,parent, false);
        ctx = parent.getContext();

        photos = view.findViewById(R.id.grid_image);
        Bitmap d = images.get(position);
        int nh = (int) ( d.getHeight() * (512.0 / d.getWidth()) );
        Bitmap scaled = Bitmap.createScaledBitmap(d, 512, nh, true);
        photos.setImageBitmap(scaled);

        // photos.setImageBitmap(images.get(position));
        return view;
    }
}
