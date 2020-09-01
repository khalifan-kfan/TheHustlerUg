package com.example.thehustler.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iceteck.silicompressorr.FileUtils;
import com.iceteck.silicompressorr.SiliCompressor;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileActivity2 extends AppCompatActivity {

    private ImageView userImage,change;
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
        setContentView(R.layout.activity_profile2);



        Toolbar infor = findViewById(R.id.toolbar_infor2);
        setSupportActionBar(infor);
        getSupportActionBar().setTitle("EDIT");

        radios = findViewById(R.id.radio);

        name2 = findViewById(R.id.PersonName);
        country = findViewById(R.id.Ug);
        city = findViewById(R.id.kla);
        Number = findViewById(R.id.PhoneNumber);
        DOB = findViewById(R.id.editTextDate_of_B);
        work = findViewById(R.id.worket);

        auth = FirebaseAuth.getInstance();
        user_id = auth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        userImage = findViewById(R.id.user__edit);
        change = findViewById(R.id.change_profile_pic_btn2);
        save = findViewById(R.id.buttonsave_edit);
        name = findViewById(R.id.user_name_edit);
        savin = findViewById(R.id.saveProgress);

        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(ProfileActivity2.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ProfileActivity2.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

                    }
                    else {
                        ImageCropping();
                    }

                } else {
                    ImageCropping();

                }
            }

        });

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

                        final String randomName = UUID.randomUUID().toString();
                        user_id = auth.getCurrentUser().getUid();
                        final Uri[] downloadUri = new Uri[1];
                        final StorageReference Image_path = storageReference.child("profile_Images").child(randomName+user_id+".jpg");
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
                                    Toast.makeText(ProfileActivity2.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                }
                            }

                        });

                    }else {
                        ArrayList<String> Names = new ArrayList<>();
                        Names.add(name.getText().toString());
                        Names.add(name2.getText().toString());
                        WriteBatch batch = firestore.batch();
                        DocumentReference userRef = firestore.collection("Users").document(user_id);
                        batch.update(userRef, "name",Names);
                        //batch.update(userRef,"name2",name2.getText().toString());
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
                                Toast.makeText(ProfileActivity2.this, "Succes batch update", Toast.LENGTH_LONG).show();
                                Intent tomain = new Intent(ProfileActivity2.this, MainActivity.class);
                                startActivity(tomain);
                            }
                        });
                        UpdateUser(name.getText().toString(), "");
                    }
                } else {
                    Toast.makeText(ProfileActivity2.this, "All fields are required to be completed", Toast.LENGTH_LONG).show();
                    savin.setVisibility(View.INVISIBLE);
                    save.setEnabled(true);
                }
            }
        });

    }

    private void ImageCropping() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(ProfileActivity2.this);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            savin.setVisibility(View.VISIBLE);
            save.setEnabled(false);
            firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {

                            DocumentSnapshot documentSnapshot= task.getResult();
                            Users user = documentSnapshot.toObject(Users.class);

                            String image =user.getImage();
                            ImageUri = Uri.parse(image);
                            name.setText(user.getName().get(0));
                            name2.setText(user.getName().get(1));
                            city.setText(user.getCity());
                            country.setText(user.getCountry());
                            Number.setText(user.getTele());
                            DOB.setText(user.getDob());
                            sex = (user.getSex());
                            work.setText(user.getWork());

                            SEX = findViewById(radios.getCheckedRadioButtonId());
                            if (SEX.getText().toString().equals(sex)) {
                                SEX.setChecked(true);
                            } else {
                                SEX.setChecked(false);
                            }
                            ImageUri = Uri.parse(image);
                            RequestOptions laceholder = new RequestOptions();
                            laceholder.placeholder(R.drawable.ic_person);
                            Glide.with(ProfileActivity2.this)
                                    .setDefaultRequestOptions(laceholder).load(image).into(userImage);

                        }

                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(ProfileActivity2.this, "retrieve error:" + error, Toast.LENGTH_LONG).show();

                    }
                    savin.setVisibility(View.INVISIBLE);
                    save.setEnabled(true);
                }
            });
        }else {

                Intent login = new Intent(ProfileActivity2.this, LoginActivity.class);
                startActivity(login);
                finish();


        }
    }

    private void putthumb(final String dur) {
        // File imagefile =new File(photoUri.getPath());
        final File Imagefile = new File(SiliCompressor.with(ProfileActivity2.this)
                .compress(FileUtils.getPath(ProfileActivity2.this, ImageUri), new File(ProfileActivity2.this.getCacheDir(), "thumbs")));
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
                //    InforMap.put("name2",name2.getText().toString());
                    InforMap.put("Created", FieldValue.serverTimestamp());
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
                                        Toast.makeText(ProfileActivity2.this, "profile added", Toast.LENGTH_LONG).show();
                                        Intent mainIntent = new Intent(ProfileActivity2.this, MainActivity.class);
                                        startActivity(mainIntent);
                                        finish();
                                    } else {
                                        savin.setVisibility(View.INVISIBLE);
                                        String error = task.getException().getMessage();
                                        Toast.makeText(ProfileActivity2.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                    }
                                    savin.setVisibility(View.INVISIBLE);
                                    save.setEnabled(true);
                                }

                            });

                }
            }
        });

    }

    private void UpdateUser(String toString, String dur) {
        UserProfileChangeRequest updateRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(toString)
                .setPhotoUri(Uri.parse(dur))
                .build();
        auth.getCurrentUser()
                .updateProfile(updateRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //mSavingProgress.setVisibility(View.INVISIBLE);
                        if(task.isSuccessful()){
                            //completed
                            Toast.makeText(ProfileActivity2.this, "Changes saved.",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }else{
                            // If sign in fails, display a message to the user.
                            //     Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(ProfileActivity2.this, "Authentication failed.",
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
        }else if(TextUtils.isEmpty(Number.getText().toString())||(Number.getText().toString().length())<11 ){
            Number.setHintTextColor(Integer.parseInt("#FF634f"));
            Toast.makeText(ProfileActivity2.this, "enter a full phone number",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else  if(ImageUri == null){
            Toast.makeText(ProfileActivity2.this, "select a profile picture",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else return !TextUtils.isEmpty(work.getText().toString());

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