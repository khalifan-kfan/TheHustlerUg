package com.example.thehustler.Activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.Manifest.*;

public class InforSettings extends AppCompatActivity {

    private ImageView userImage;
    private Uri ImageUri = null;
    private EditText name,name2,country,city,Number,work,DOB;
    private Button save;
    private ProgressBar savin;
    private String user_id;
    private  Boolean isChanged = false;

    private RadioGroup radios;
    private RadioButton SEX;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private String sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infor_settings);

        Toolbar infor = findViewById(R.id.toolbar_infor);
        setSupportActionBar(infor);
        getSupportActionBar().setTitle("User Settings");

        radios = findViewById(R.id.radio);

        name2 = findViewById(R.id.editTextTextPersonName);
        country = findViewById(R.id.editTextTextPersonName2);
        city = findViewById(R.id.editTextTextPersonName3);
        Number = findViewById(R.id.editTextNumber);
        DOB = findViewById(R.id.editTextDate);
        work = findViewById(R.id.editTextTextMultiLine);
        
        auth = FirebaseAuth.getInstance();
       user_id = auth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        userImage = findViewById(R.id.user__);
        save = findViewById(R.id.buttonsave);
        name = findViewById(R.id.user_name);
        savin = findViewById(R.id.saveProgress);



        save.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                save.setEnabled(false);
                SEX = findViewById(radios.getCheckedRadioButtonId());
                sex = SEX.getText().toString();

                if (check()) {
                    savin.setVisibility(View.VISIBLE);
                    if (isChanged) {
                        user_id = auth.getCurrentUser().getUid();
                        final Uri[] downloadUri = new Uri[1];
                        final StorageReference Image_path = storageReference.child("profile_Images").child(user_id+".jpg");
                       Image_path.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                           @Override
                           public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                               if (task.isSuccessful()) {
                                   Task<Uri> task_uri = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                       @Override
                                       public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                           if (!task.isSuccessful()) {
                                               throw task.getException();
                                           }
                                           return Image_path.getDownloadUrl();
                                       }
                                   }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                       @Override
                                       public void onComplete(@NonNull Task<Uri> task) {
                                           if (task.isSuccessful()) {
                                               downloadUri[0] = task.getResult();
                                               String dur = downloadUri[0].toString();
                                               putthumb(dur);
                                           }
                                       }
                                   });

                               }else {
                                   save.setEnabled(true);
                                   savin.setVisibility(View.INVISIBLE);
                                   String error = task.getException().getMessage();
                                   Toast.makeText(InforSettings.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                               }
                           }

                       });

                    }
                    /*else {
                        WriteBatch batch = firestore.batch();
                        DocumentReference userRef = firestore.collection("Users").document(user_id);
                        batch.update(userRef, "name1", name.getText().toString());
                        batch.update(userRef, "name1", name.getText().toString());
                        batch.update(userRef,"name2",name2.getText().toString());
                        batch.update(userRef,"Created", FieldValue.serverTimestamp());
                        batch.update(userRef,"country",country.getText().toString());
                        batch.update(userRef,"city",city.getText().toString());
                        batch.update(userRef,"tele",Number.getText().toString());
                        batch.update(userRef,"work",work.getText().toString());
                        batch.update(userRef,"dob",DOB.getText().toString());
                        batch.update(userRef,"sex",sex);

                        // Commit the batch
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(InforSettings.this, "Succes batch update", Toast.LENGTH_LONG).show();
                                Intent tomain = new Intent(InforSettings.this, MainActivity.class);
                                startActivity(tomain);
                            }
                        });
                        UpdateUser(name.getText().toString(), "");
                    }*/
                } else {
                    Toast.makeText(InforSettings.this, "All fields are required to be completed", Toast.LENGTH_LONG).show();
                    savin.setVisibility(View.INVISIBLE);
                    save.setEnabled(true);
                }

            }
        });


        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(InforSettings.this, permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(InforSettings.this, new String[]{permission.READ_EXTERNAL_STORAGE}, 1);

                    }
                    else {
                        ImageCropping();
                    }

                } else {
                    ImageCropping();

                }
            }
        });
    }

    private void putthumb(final String dur) {
        // File imagefile =new File(photoUri.getPath());
        final File Imagefile = new File(SiliCompressor.with(InforSettings.this)
                .compress(FileUtils.getPath(InforSettings.this, ImageUri), new File(InforSettings.this.getCacheDir(), "thumbs")));
        Uri Thumburi = Uri.fromFile(Imagefile);
        final StorageReference thumbpath = storageReference.child("profile_image_thumbs").child(user_id+"thumb.jpg");
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
                    ArrayList<String> Names = new ArrayList<>();
                        Names.add(name.getText().toString());
                        Names.add(name2.getText().toString());

                        Map<String, Object> InforMap = new HashMap<>();
                        InforMap.put("image",dur);
                        InforMap.put("thumb",myUrl);
                        InforMap.put("name",Names);
                       // InforMap.put("name2",name2.getText().toString());
                        InforMap.put("created", FieldValue.serverTimestamp());
                        InforMap.put("country",country.getText().toString());
                        InforMap.put("city",city.getText().toString());
                        InforMap.put("tele",Number.getText().toString());
                        InforMap.put("work",work.getText().toString());
                        InforMap.put("dob",DOB.getText().toString());
                        InforMap.put("sex",sex);

                        firestore.collection("Users").document(user_id).set(InforMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        UpdateUser(name.getText().toString(), dur);
                                        Toast.makeText(InforSettings.this, "profile added", Toast.LENGTH_LONG).show();
                                        Intent mainIntent = new Intent(InforSettings.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    } else {
                                        savin.setVisibility(View.INVISIBLE);
                                        String error = task.getException().getMessage();
                                        Toast.makeText(InforSettings.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                    }
                                    savin.setVisibility(View.INVISIBLE);
                                    save.setEnabled(true);
                                }

                            });

                }
            }
        });


    }

    private void UpdateUser(String username, String s) {
        UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .setPhotoUri(Uri.parse(s))
                .build();
        auth.getCurrentUser()
                .updateProfile(updateRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //mSavingProgress.setVisibility(View.INVISIBLE);
                        if(task.isSuccessful()){
                            //completed
                            Toast.makeText(InforSettings.this, "Changes saved.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            // If sign in fails, display a message to the user.
                       //     Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(InforSettings.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private Boolean check(){
        if(TextUtils.isEmpty(name.getText().toString())) {
            name.setHintTextColor(Integer.parseInt("#FF634f"));
            return false;
        }else if(TextUtils.isEmpty(name2.getText().toString())){
            name2.setHintTextColor(Integer.parseInt("#FF634f"));
            return false;
        }else if (TextUtils.isEmpty(city.getText().toString())){
            city.setHintTextColor(Integer.parseInt("#FF634f"));
            return false;
        }else if(TextUtils.isEmpty(country.getText().toString())){
            country.setHintTextColor(Integer.parseInt("#FF634f"));
            return false;
        }else if(TextUtils.isEmpty(Number.getText().toString())||(Number.getText().toString().length())<10 ){
            Number.setHintTextColor(Integer.parseInt("#FF634f"));
            Toast.makeText(InforSettings.this, "enter a full phone number",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else  if(ImageUri == null){
            Toast.makeText(InforSettings.this, "select a profile picture",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else return !TextUtils.isEmpty(work.getText().toString());

    }


    private void ImageCropping() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(InforSettings.this);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                ImageUri = result.getUri();
                userImage.setImageURI(ImageUri);
                isChanged = true;

            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }


    }
}
