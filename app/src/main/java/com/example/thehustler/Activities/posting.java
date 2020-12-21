package com.example.thehustler.Activities;
import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import  com.example.thehustler.Adapter.ImageAdapter;
import com.example.thehustler.Adapter.ImageAdp;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;


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
    private String  which;

    private TextView description;
    private TextView postdate;
    private TextView postOwnerName;
    private CircleImageView ownerimage;
    private CardView cardView;
    private ViewPager2 phots;
    private  Users users;
    private Blogpost blogpost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        toolbar = findViewById(R.id.toolbar_post);
        setSupportActionBar(toolbar);
       toolbar.setTitle("post here");
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

        phots = findViewById(R.id.pageview);
        cardView = findViewById(R.id.Card2);
        postOwnerName = findViewById(R.id.Post_user);
        ownerimage =findViewById(R.id.circularUser);
        postdate = findViewById(R.id.datePost);
        description = findViewById(R.id.postdescribe);

        Intent intent = getIntent();
        which = intent.getStringExtra("WHICH");
        if(!which.equals("original")){
            cardView.setVisibility(View.VISIBLE);
            cardView.setEnabled(false);
            //set up user
            firestore.collection("Posts").document(which).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){

                        blogpost = task.getResult().toObject(Blogpost.class).withID( task.getResult().getId());
                       description.setText(blogpost.getDescription());
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

                        Date datesent = blogpost.getTimeStamp();
                        if(datesent != null) {
                            postdate.setText(format.format(datesent));
                        }else{
                            postdate.setText(format.format(new Date()));
                        }
                        if ((blogpost.getImage_url())!=null && (blogpost.getPost_image_thumb())!=null){

                            List<String> Image_uri = new ArrayList<>();
                            List<String> thumb_uri = new ArrayList<>();
                            int i = 0;

                            while (i < blogpost.getImage_url().size()) {
                                Image_uri.add(i, blogpost.getImage_url().get(i));
                                thumb_uri.add(i, blogpost.getPost_image_thumb().get(i));
                                i++;
                            }
                            setBlogimage(Image_uri, thumb_uri);

                        }else {
                            phots.setEnabled(false);
                            phots.setVisibility(View.GONE);
                        }

                        String owner = blogpost.getUser_id();
                        firestore.collection("Users").document(owner).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                users = task.getResult().toObject(Users.class);
                                postOwnerName.setText(users.getName().get(0));
                                RequestOptions placeholderOP = new RequestOptions();
                                placeholderOP.placeholder(R.drawable.ic_person);
                                Glide.with(posting.this).
                                        applyDefaultRequestOptions(placeholderOP).
                                        load(users.getImage()).thumbnail(Glide.with(posting.this)
                                        .load(users.getThumb()))
                                        .into(ownerimage);
                            }
                        });
                    }
                }
            });
        }


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
                if (which.equals("original")) {
                    if (!TextUtils.isEmpty(description)) {
                        postin.setVisibility(View.VISIBLE);
                        if (photoUri != null) {
                            storethumb(description);
                        } else {
                            Map<String, Object> postMapn = new HashMap<>();
                            postMapn.put("re_postId", null);
                            postMapn.put("image_url", null);
                            postMapn.put("post_image_thumb", null);
                            postMapn.put("description", description);
                            postMapn.put("user_id", UserId);
                            postMapn.put("timeStamp", FieldValue.serverTimestamp());
                            firestore.collection("Posts").add(postMapn).addOnCompleteListener(
                                    new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                final String post_id = task.getResult().getId();
                                                Map<String, Object> mypostMapn = new HashMap<>();
                                                mypostMapn.put("post_id", post_id);
                                                mypostMapn.put("author", "original");
                                                mypostMapn.put("timeStamp", FieldValue.serverTimestamp());
                                                firestore.collection("Users").document(UserId).collection("Posts")
                                                        .add(mypostMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(posting.this, "post added", Toast.LENGTH_LONG).show();
                                                            Intent mainIntent = new Intent(posting.this, MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();
                                                        } else {
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
                    } else {
                        Toast.makeText(posting.this, "add text", Toast.LENGTH_LONG).show();
                    }
                }else {

                    if (!TextUtils.isEmpty(description)) {
                        postin.setVisibility(View.VISIBLE);
                        if (photoUri != null) {
                            //diff method or edit post thumb
                            storethumb(description);
                        } else {
                            Map<String, Object> postMapn = new HashMap<>();

                            postMapn.put("re_postId", UserId);
                           postMapn.put("re_post_desc",description);
                           postMapn.put("re_image_url",null);
                           postMapn.put("re_post_image_thumb",null);
                           postMapn.put("re_timeStamp",blogpost.getTimeStamp());

                           if(blogpost.getRe_postId()!=null) {
                               postMapn.put("image_url", null);
                               postMapn.put("post_image_thumb", null);
                               postMapn.put("description", blogpost.getRe_post_desc());
                               postMapn.put("user_id", blogpost.getRe_postId());
                               postMapn.put("timeStamp",FieldValue.serverTimestamp());
                           }else {
                               postMapn.put("image_url", null);
                               postMapn.put("post_image_thumb", null);
                               postMapn.put("description", blogpost.getDescription());
                               postMapn.put("user_id", blogpost.getUser_id());
                               postMapn.put("timeStamp",FieldValue.serverTimestamp() );
                           }
                            firestore.collection("Posts").add(postMapn).addOnCompleteListener(
                                    new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                final String post_id = task.getResult().getId();
                                                Map<String, Object> mypostMapn = new HashMap<>();
                                                mypostMapn.put("post_id", post_id);
                                                mypostMapn.put("author", "re-post");
                                                mypostMapn.put("timeStamp", FieldValue.serverTimestamp());
                                                firestore.collection("Users").document(UserId).collection("Posts")
                                                        .add(mypostMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            if (!blogpost.getUser_id().equals(UserId)) {
                                                                final String status = "Re-post";
                                                                Map<String, Object> mylikeMap = new HashMap<>();
                                                                mylikeMap.put("status", status);
                                                                mylikeMap.put("notId", blogpost.getUser_id());
                                                                mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                                mylikeMap.put("postId", blogpost.postId);
                                                                firestore.collection("Users/"+blogpost.getUser_id()+"/NotificationBox").add(mylikeMap)
                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                if (!task.isSuccessful()) {
                                                                                    Toast.makeText(posting.this, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                                //send notification now
                                                                                firestore.collection("Users").document(blogpost.getUser_id())
                                                                                        .collection("Tokens")
                                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                                for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                                        String token = doc.getDocument().getString("token");
                                                                                                        NotSender
                                                                                                                .sendNotifications(posting.this, token
                                                                                                                        , status, "New Re-post Alert");
                                                                                                    }
                                                                                                }

                                                                                            }
                                                                                        });

                                                                                NotSender.Updatetoken();

                                                                            }
                                                                        });
                                                            }

                                                            Toast.makeText(posting.this, "post added", Toast.LENGTH_LONG).show();
                                                            Intent mainIntent = new Intent(posting.this, MainActivity.class);
                                                            startActivity(mainIntent);
                                                            finish();
                                                        } else {
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
                    } else {
                        Toast.makeText(posting.this, "add repost edit", Toast.LENGTH_LONG).show();
                    }


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
            final StorageReference file = storageReference.child("re_postImages")
                    .child(randomName + ".jpg");
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
                        Toast.makeText(posting.this, "myUrl" + myUrl, Toast.LENGTH_LONG).show();

                        if (photoUri.isEmpty() && !duris.isEmpty()) {
                            Map<String, Object> postMap = new HashMap<>();
                            if(which.equals("original")) {
                                postMap.put("re_postId", null);
                                postMap.put("image_url", duris);
                                postMap.put("post_image_thumb", thumbs);
                                postMap.put("description", description);
                                postMap.put("user_id", UserId);
                                postMap.put("timeStamp", FieldValue.serverTimestamp());
                                firestore.collection("Posts").add(postMap).addOnCompleteListener(
                                        new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    final String post_id = task.getResult().getId();
                                                    Map<String, Object> mypostMapn = new HashMap<>();
                                                    mypostMapn.put("post_id", post_id);
                                                    mypostMapn.put("author", "original");
                                                    mypostMapn.put("timeStamp", FieldValue.serverTimestamp());
                                                    firestore.collection("Users").document(UserId).collection("Posts")
                                                          .add(mypostMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(posting.this, "post added", Toast.LENGTH_LONG).show();
                                                                Intent mainIntent = new Intent(posting.this, MainActivity.class);
                                                                startActivity(mainIntent);
                                                                finish();
                                                            } else {
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
                                postMap.put("re_postId", UserId);
                                postMap.put("re_post_desc",description);
                                postMap.put("re_image_url",duris);
                                postMap.put("re_post_image_thumb",thumbs);
                                postMap.put("re_timeStamp",blogpost.getRe_timeStamp());
                                if(blogpost.getRe_postId()!=null) {
                                    postMap.put("image_url", blogpost.getRe_image_url());
                                    postMap.put("post_image_thumb", blogpost.getRe_post_image_thumb());
                                    postMap.put("description", blogpost.getRe_post_desc());
                                    postMap.put("user_id", blogpost.getRe_postId());
                                    postMap.put("timeStamp", FieldValue.serverTimestamp());
                                }else {
                                    postMap.put("image_url", blogpost.getRe_image_url());
                                    postMap.put("post_image_thumb",blogpost.getRe_image_url());
                                    postMap.put("description", blogpost.getDescription());
                                    postMap.put("user_id", blogpost.getUser_id());
                                    postMap.put("timeStamp", FieldValue.serverTimestamp());
                                }
                                firestore.collection("Posts").add(postMap).addOnCompleteListener(
                                        new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if (task.isSuccessful()) {
                                                    final String post_id = task.getResult().getId();
                                                    Map<String, Object> mypostMapn = new HashMap<>();
                                                    mypostMapn.put("post_id", post_id);
                                                    mypostMapn.put("author", "re-post");
                                                    mypostMapn.put("timeStamp", FieldValue.serverTimestamp());
                                                    firestore.collection("Users").document(UserId).collection("Posts")
                                                            .add(mypostMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()) {
                                                                if (!blogpost.getUser_id().equals(UserId)) {
                                                                    final String status = "Re-post";
                                                                    Map<String, Object> mylikeMap = new HashMap<>();
                                                                    mylikeMap.put("status", status);
                                                                    mylikeMap.put("notId", blogpost.getUser_id());
                                                                    mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                                    mylikeMap.put("postId", blogpost.postId);
                                                                    firestore.collection("Users/"+blogpost.getUser_id()+"/NotificationBox").add(mylikeMap)
                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                    if (!task.isSuccessful()) {
                                                                                        Toast.makeText(posting.this, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                    //send notification now
                                                                                    firestore.collection("Users").document(blogpost.getUser_id())
                                                                                            .collection("Tokens")
                                                                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                                @Override
                                                                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                                    for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                                            String token = doc.getDocument().getString("token");
                                                                                                            NotSender
                                                                                                                    .sendNotifications(posting.this, token
                                                                                                                            , status, "New Re-post Alert");
                                                                                                        }
                                                                                                    }

                                                                                                }
                                                                                            });

                                                                                    NotSender.Updatetoken();

                                                                                }
                                                                            });
                                                                }
                                                                Toast.makeText(posting.this, "post added", Toast.LENGTH_LONG).show();
                                                                Intent mainIntent = new Intent(posting.this, MainActivity.class);
                                                                startActivity(mainIntent);
                                                                finish();
                                                            } else {
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

                        } else {
                            Toast.makeText(posting.this, "Download uri is empty" + duris.size()
                                            + "photouri" + photoUri.size() + "thumbs" + thumbs.size()
                                    , Toast.LENGTH_LONG).show();
                            postin.setVisibility(View.INVISIBLE);
                        }
                        uploadImageToFirebaseStorage(description);
                    }
                }
            });
        }
    }

    public void setBlogimage(List<String> downloadUri, List<String> thumb_uri){

        phots.setAdapter( new ImageAdp(downloadUri,thumb_uri,phots));

        phots.setClipToPadding(false);
        phots.setClipChildren(false);
        phots.setOffscreenPageLimit(3);
        phots.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        CompositePageTransformer cpt = new CompositePageTransformer();
        cpt.addTransformer(new MarginPageTransformer(30));
        cpt.addTransformer((new ViewPager2.PageTransformer() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1-Math.abs(position);
                page.setScaleY(0.85F+r*0.15F);
                page.setForegroundGravity(Gravity.CENTER);
            }

        }
        ));
        phots.setPageTransformer(cpt);
        //RequestOptions placRO = new RequestOptions();
        //placRO.placeholder(R.drawable.ic_post);
        // Glide.with(context).applyDefaultRequestOptions(placRO).load(downloadUri).thumbnail(Glide.with(context).load(thumb_uri))
        //       .into(blogimage);
    }

    private void storethumb(final String description) {

        for (final Uri khali : photoUri) {
            final String randomName = UUID.randomUUID().toString();
            // File imagefile =new File(photoUri.getPath());
            File Imagefile = new File(SiliCompressor.with(posting.this)
                    .compress(FileUtils.getPath(posting.this, khali), new File(posting.this.getCacheDir(), "thumbs2")));
            Uri Thumburi = Uri.fromFile(Imagefile);
            final StorageReference thumbpath = storageReference.child("re_post_image_thumb").child(randomName+"thumb.jpg");
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















