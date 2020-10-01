package com.example.thehustler.Activities;
import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import  com.example.thehustler.Adapter.ImageAdapter;
import com.example.thehustler.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;



public class posting extends AppCompatActivity {
    private Toolbar toolbar;

    private ImageView photo;
    private List <Uri> photoUri;

    private EditText describe;
    private Button submit;
    private ProgressBar postin;
    private GridView images;
    ImageAdapter imageAdapter;

    private List<String > duris;
    private List<String > thumbs;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String UserId;
    private List<Bitmap> bitmaps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        toolbar = findViewById(R.id.toolbar_post);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("post here");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        duris = new ArrayList<>();
        thumbs = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        UserId = auth.getCurrentUser().getUid();



        images = findViewById(R.id._dynamic);
        photo = findViewById(R.id.image_post);
        submit = findViewById(R.id._post);
        describe = findViewById(R.id.Posttxt);
        postin = findViewById(R.id.progress_post);


        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            /*    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(1, 1)
                        .start(posting.this);*/


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(posting.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(posting.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);

                    } else {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setAction(Intent.ACTION_PICK);
                        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                    }

                } else {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                      intent.setAction(Intent.ACTION_PICK);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

                }

            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String description = describe.getText().toString();

                if (!TextUtils.isEmpty(description)) {
                    postin.setVisibility(View.VISIBLE);
                    if (photoUri!=null) {
                        storethumb(description);
                    } else {
                        Map<String, Object> postMapn = new HashMap<>();
                        postMapn.put("re_postId",null);
                        postMapn.put("image_url", null);
                        postMapn.put("post_image_thumb", null);
                        postMapn.put("description", description);
                        postMapn.put("user_id", UserId);
                        postMapn.put("timeStamp", FieldValue.serverTimestamp());
                        firestore.collection("Posts").add(postMapn).addOnCompleteListener(
                                new OnCompleteListener<DocumentReference>()  {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    final String post_id = task.getResult().getId();
                                    Map<String, Object> mypostMapn = new HashMap<>();
                                    mypostMapn.put("post_id",post_id);
                                    mypostMapn.put("author","original");
                                    mypostMapn.put("timeStamp", FieldValue.serverTimestamp());
                                    firestore.collection("Users").document(UserId).collection("Posts")
                                            .add(mypostMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if(task.isSuccessful()) {
                                                Toast.makeText(posting.this, "post added", Toast.LENGTH_LONG).show();
                                                Intent mainIntent = new Intent(posting.this, MainActivity.class);
                                                startActivity(mainIntent);
                                                finish();
                                            }else{
                                                postin.setVisibility(View.INVISIBLE);
                                                firestore.collection("Posts").document(post_id).delete();
                                                String error = task.getException().getMessage();
                                                Toast.makeText(posting.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                } else {
                                    postin.setVisibility(View.INVISIBLE);
                                    String error = task.getException().getMessage();
                                    Toast.makeText(posting.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                }
                                postin.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                }else{
                    Toast.makeText(posting.this, "add text", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            bitmaps = new ArrayList<>();
            photoUri = new ArrayList<>();
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                for (int k = 0; k < clipData.getItemCount(); k++) {
                    Uri images = clipData.getItemAt(k).getUri();
                    photoUri.add(images);
                    try {
                        InputStream is = getContentResolver().openInputStream(images);
                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        bitmaps.add(bitmap);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Uri image = data.getData();
                photoUri.add(image);
                try {
                    InputStream is = getContentResolver().openInputStream(image);
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    bitmaps.add(bitmap);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
            imageAdapter = new ImageAdapter(this, bitmaps);
            images.setAdapter(imageAdapter);
        }

    }
    private void uploadImageToFirebaseStorage(final String description) {
        if (photoUri.size() > 0) {
            final String randomName = UUID.randomUUID().toString();
            final Uri imageUri = photoUri.get(0);
            final StorageReference file = storageReference.child("postImages")
                    .child(randomName+".jpg");
            photoUri.remove(0);
           UploadTask uploadTask = file.putFile(imageUri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return file.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();
                        duris.add(myUrl);
                        Toast.makeText(posting.this,"myUrl"+myUrl,Toast.LENGTH_LONG).show();

                        if(photoUri.isEmpty() && !duris.isEmpty()) {
                            Map<String, Object> postMap = new HashMap<>();
                            postMap.put("re_postId",null);
                            postMap.put("image_url",duris);
                            postMap.put("post_image_thumb",thumbs);
                            postMap.put("description",description);
                            postMap.put("user_id",UserId);
                            postMap.put("timeStamp", FieldValue.serverTimestamp());
                            firestore.collection("Posts").add(postMap).addOnCompleteListener(
                                    new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if (task.isSuccessful()) {
                                        final String post_id = task.getResult().getId();
                                        Map<String, Object> mypostMapn = new HashMap<>();
                                        mypostMapn.put("post_id",post_id);
                                        mypostMapn.put("author","original");
                                        mypostMapn.put("timeStamp", FieldValue.serverTimestamp());
                                        firestore.collection("Users").document(UserId).collection("Posts")
                                                .add(mypostMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful()) {
                                                    Toast.makeText(posting.this, "post added", Toast.LENGTH_LONG).show();
                                                    Intent mainIntent = new Intent(posting.this, MainActivity.class);
                                                    startActivity(mainIntent);
                                                    finish();
                                                }else{
                                                    postin.setVisibility(View.INVISIBLE);
                                                    firestore.collection("Posts").document(post_id).delete();
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(posting.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else {
                                        postin.setVisibility(View.INVISIBLE);
                                        String error = task.getException().getMessage();
                                        Toast.makeText(posting.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();

                                    }
                                    postin.setVisibility(View.INVISIBLE);
                                }
                            });
                        }else {
                            Toast.makeText(posting.this, "Download uri is empty"+duris.size()
                                            +"photouri"+photoUri.size()+"thumbs"+thumbs.size()
                                    ,Toast.LENGTH_LONG).show();
                            postin.setVisibility(View.INVISIBLE);
                        }

                        uploadImageToFirebaseStorage(description);
                    }
                }
            });
        }
    }

    private void storethumb(final String description) {

        for (final Uri khali : photoUri) {
            final String randomName = UUID.randomUUID().toString();
            // File imagefile =new File(photoUri.getPath());
            File Imagefile = new File(SiliCompressor.with(posting.this)
                    .compress(FileUtils.getPath(posting.this, khali), new File(posting.this.getCacheDir(), "thumbs2")));
            Uri Thumburi = Uri.fromFile(Imagefile);
            final StorageReference thumbpath = storageReference.child("post_image_thumb").child(randomName+"thumb.jpg");
            UploadTask uploadTask = thumbpath.putFile(Thumburi);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return thumbpath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String myUrl = downloadUri.toString();
                        thumbs.add(myUrl);
                        Toast.makeText(posting.this,"myUrl"+myUrl,Toast.LENGTH_LONG).show();
                        if(khali.equals(photoUri.get(photoUri.size()-1))&&!thumbs.isEmpty()){
                            uploadImageToFirebaseStorage(description);
                        }
                    }
                }
            });
            if(khali.equals(photoUri.get(photoUri.size()-1))){
                break;
            }
        }
    }
}















