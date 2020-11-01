package com.example.thehustler.Activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thehustler.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PickImages   extends AppCompatActivity implements Adapter.itemlistener{
    Uri iui;

    ArrayList<Uri> photoUri;

    Adapter imageAdapter;
    int replace_position;
    private RecyclerView images;
    CircleImageView capture;
    ImageView select;
    private Button Send;
    private EditText caption;
    private static final int permsCAM=331;
    private static final int permsCHOOSE=332;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pick_images);
        select = findViewById(R.id.addphoto);
        capture = findViewById(R.id.circleImageView);
        images = findViewById(R.id.imageViewRv);
        caption = findViewById(R.id.messo);
        Send = findViewById(R.id.button);
        photoUri =  new ArrayList<>();


        imageAdapter = new Adapter(this, photoUri,this);
        //  images.setHasFixedSize(true);
        int columns=2;
        images.setLayoutManager(new GridLayoutManager(PickImages.this,columns));
        images.setAdapter(imageAdapter);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               pickImagePErm(permsCHOOSE);
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImagePErm(permsCAM);
            }
        });
        Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cap = caption.getText().toString();
                if (TextUtils.isEmpty(cap)) {
                    cap = "";
                }
                if(photoUri.isEmpty()) {
                    Intent back = new Intent(PickImages.this,ChatActivity.class);
                    setResult(RESULT_CANCELED,back);
                    finish();
                }else {
                    goback(cap);
                }
            }
        });

    }


    private void pickImage(int i) {
        if(i==2) {
            // CropImage.startPickImageActivity(this);
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setAction(Intent.ACTION_PICK);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 517);
        }else {
            cameraz();
            //camera
        }
    }

    private void cameraz() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"picture new");
        values.put(MediaStore.Images.Media.DESCRIPTION,"picture new");
        iui = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,iui);
        startActivityForResult(cameraIntent, 554);
    }

    public void pickImagePErm(int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(PickImages.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(PickImages.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(PickImages.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                        , Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, code);

            } else {
                if(code==permsCAM){
                   pickImage(1);
                }else if(code==permsCHOOSE){
                    pickImage(2);
                }
            }

        } else {
            if(code==permsCAM){
                pickImage(1);
            }else if(code==permsCHOOSE){
                pickImage(2);
            }
        }


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if( grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(requestCode ==permsCAM ){
                pickImage(1);
            }else if(requestCode ==permsCHOOSE){
                pickImage(2);
            }
        }else {
            Toast.makeText(PickImages.this, "Denied permission",Toast.LENGTH_SHORT).show();
        }
    }
    public void cropRq(Uri iuiu){
        //impotant
        CropImage.activity(iuiu)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1,1)
                .start(this);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if(result.getUri()!=null) {
                    Uri selectedImage = result.getUri();
                    photoUri.set(replace_position,selectedImage);
                    imageAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(this,"uri is null",Toast.LENGTH_SHORT).show();
                }
                //Uri selectedImage = CropImage.getPickImageResultUri(this,data);
            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
               Toast.makeText(PickImages.this,result.getError().toString(),Toast.LENGTH_LONG).show();
            }

        }
        // for camera
        if(requestCode == 554 && resultCode==RESULT_OK){
            //camera
            // Uri image = data.getData();
            photoUri.add(iui);
            imageAdapter.notifyDataSetChanged();
            //cropRq(image);
        } else if (requestCode == 517 && resultCode == RESULT_OK) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int k = 0; k < clipData.getItemCount(); k++) {
                    Uri images = clipData.getItemAt(k).getUri();
                    photoUri.add(images);
                    imageAdapter.notifyDataSetChanged();
                }
            } else {
                Uri image = data.getData();
                photoUri.add(image);
                imageAdapter.notifyDataSetChanged();
            }
        }

    }
    public void goback(String cap){
        Intent back = new Intent(this,ChatActivity.class);
       // Bundle b = new Bundle();
       //b.putParcelableArrayList("PHOTOS",photoUri);
        //back.putExtra("BUNDLE",b);
        back.putParcelableArrayListExtra("PHOTOS",photoUri);
        back.putExtra("CAPTION",cap);
        setResult(RESULT_OK,back);
        finish();

    }

    @Override
    public void picture(int k, int position, Uri image) {
        replace_position = position;
        if(k==1){
            //crop
            cropRq(image);
        }else{
            //remove
            photoUri.remove(position);
        }
    }
}


class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    Context ctx;
    List<Uri> images;
    itemlistener itemlisten;
    public Adapter(Context ctx, List<Uri> images, itemlistener itemlisten) {
        this.ctx = ctx;
        this.images = images;
        this.itemlisten = itemlisten;
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.editable_images,parent, false);
        ctx = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, final int position) {
        // holder.setIsRecyclable(false);
        //
        Glide.with(ctx)
                .load(images.get(position))
                .into(holder.photos);

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemlisten.picture(2,position,images.get(position));
                notifyItemRemoved(position);
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemlisten.picture(1,position,images.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        if (images != null) {
            return  images.size();
        } else { return 0; }
    }
    public interface  itemlistener {
        public void picture(int k,int position,Uri image);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView photos;
        ImageButton cancel,edit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photos = itemView.findViewById(R.id.imageView2);
            edit = itemView.findViewById(R.id.crop);
            cancel = itemView.findViewById(R.id.cancel);

        }
    }
}

