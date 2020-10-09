package com.example.thehustler.Activities;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.IntentService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.example.thehustler.R;
import com.example.thehustler.Services.LocationService;
import com.example.thehustler.Services.Constants;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;

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
    private EditText name, name2, country, city, Number, work, DOB;
    private Button save;
    private ProgressBar savin;
    private String user_id;
    private Boolean isChanged = false;
    private TextView autoLocate;
    private RadioGroup radios;
    private RadioButton SEX;
    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private ResultReceiver resultReceiver;
    public static final int CODE_PEMISSION_LOCATION = 61;
    TextView code;

    private String sex, country_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infor_settings);

        Toolbar infor = findViewById(R.id.toolbar_infor);
        setSupportActionBar(infor);
        getSupportActionBar().setTitle("User Settings");

        radios = findViewById(R.id.radio);
        resultReceiver = new AddressReciever(new Handler());

        name2 = findViewById(R.id.editTextTextPersonName);
        country = findViewById(R.id.editTextTextPersonName2);
        city = findViewById(R.id.editTextTextPersonName3);
        Number = findViewById(R.id.editTextNumber);
        DOB = findViewById(R.id.editTextDate);
        work = findViewById(R.id.editTextTextMultiLine);

        code = findViewById(R.id.textView6);
        code.setText("+!!");
        auth = FirebaseAuth.getInstance();
        user_id = auth.getCurrentUser().getUid();
        storageReference = FirebaseStorage.getInstance().getReference();
        firestore = FirebaseFirestore.getInstance();

        userImage = findViewById(R.id.user__);
        save = findViewById(R.id.buttonsave);
        name = findViewById(R.id.user_name);
        savin = findViewById(R.id.saveProgress);
        autoLocate = findViewById(R.id.locate);

        autoLocate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED  ) {
                    ActivityCompat.requestPermissions(InforSettings.this, new String[]{permission.ACCESS_FINE_LOCATION}, CODE_PEMISSION_LOCATION);
                } else {
                    getlocation();
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
                        user_id = auth.getCurrentUser().getUid();
                        final Uri[] downloadUri = new Uri[1];
                        final StorageReference Image_path = storageReference.child("profile_Images").child(user_id + ".jpg");
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

                                } else {
                                    save.setEnabled(true);
                                    savin.setVisibility(View.INVISIBLE);
                                    String error = task.getException().getMessage();
                                    Toast.makeText(InforSettings.this, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                }
                            }

                        });

                    }
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
                    if (ContextCompat.checkSelfPermission(InforSettings.this, permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
                        ActivityCompat.requestPermissions(InforSettings.this, new String[]{permission.READ_EXTERNAL_STORAGE}, 1);

                    } else {
                        ImageCropping();
                    }

                } else {
                    ImageCropping();

                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODE_PEMISSION_LOCATION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getlocation();
            } else {
                Toast.makeText(InforSettings.this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getlocation() {
        savin.setVisibility(View.VISIBLE);
        LocationRequest lq = new LocationRequest();
        lq.setInterval(10000);
        lq.setFastestInterval(1000);
        lq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            return;
        }
        final LocationManager manager = (LocationManager) getSystemService( this.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {


            LocationServices.getFusedLocationProviderClient(InforSettings.this)
                    .requestLocationUpdates(lq, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(InforSettings.this)
                                    .removeLocationUpdates(this);
                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                int LatestLocationIndex = locationResult.getLocations().size() - 1;
                                double lat = locationResult.getLocations().get(LatestLocationIndex).getLatitude();
                                double longi = locationResult.getLocations().get(LatestLocationIndex).getLongitude();

                                Location location = new Location("providerN/A");
                                location.setLongitude(longi);
                                location.setLatitude(lat);
                                fetchLontudeAddress(location);
                            } else {
                                savin.setVisibility(View.INVISIBLE);
                            }

                        }
                    }, Looper.getMainLooper());
        }else {
          //  Toast.makeText(this,"turn loction on",Toast.LENGTH_SHORT).show();
            buildAlertMessageNoGps();
        }

    }
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
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
                        InforMap.put("tele",country_code+Number.getText().toString());
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
            Toast.makeText(InforSettings.this, "Click auto locate..",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(country.getText().toString())){
            country.setHintTextColor(Integer.parseInt("#FF634f"));
            Toast.makeText(InforSettings.this, "Click auto locate..",
                    Toast.LENGTH_SHORT).show();
            return false;
        }else if(TextUtils.isEmpty(Number.getText().toString())||(Number.getText().toString().length())<9
        || country_code ==null){
            Number.setHintTextColor(Integer.parseInt("#FF634f"));
            Toast.makeText(InforSettings.this, "enter a full phone number,and turn on location to get country code",
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
    private void  fetchLontudeAddress(Location location){
        Intent intent = new Intent(this, LocationService.class);
        intent.putExtra(Constants.RECEIVER,resultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA,location);
        startService(intent);
    }


    private class  AddressReciever extends ResultReceiver{
        public AddressReciever(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if(resultCode == 1){
               ArrayList<String> getlocality = new ArrayList<>();
               getlocality = resultData.getStringArrayList(Constants.RESULT_DATA_KEY);
               country.setText(getlocality.get(0));
              city.setText(getlocality.get(1));
              country_code = getlocality.get(2);
              code.setText(country_code);
              country.setEnabled(false);
              city.setEnabled(false);
            }else {
                Toast.makeText(InforSettings.this,resultData.getString(Constants.RESULT_ERROR),Toast.LENGTH_SHORT).show();
            }
            savin.setVisibility(View.INVISIBLE);
        }
    }
}
