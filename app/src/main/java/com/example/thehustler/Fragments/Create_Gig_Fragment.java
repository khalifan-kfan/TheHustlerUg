package com.example.thehustler.Fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Create_Gig_Fragment extends Fragment {
    private ImageView selectImage,myimage;
    private ProgressBar progressBar;
    private Button SendBtn;
    private EditText post_desc;
    private TextView reqType;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private String myId;

    private static final String IDDD = "RequestType";
    private static final String NAME = "Name";


    private String iddd;

    private Uri ImageUri;
    private String Name;

    public Create_Gig_Fragment() {
        // Required empty public constructor
    }

    public static Create_Gig_Fragment newInstance(String iddd, String name) {
        Create_Gig_Fragment fragment = new Create_Gig_Fragment();
        Bundle args = new Bundle();
        args.putString(IDDD,iddd);
        args.putString(NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            iddd = getArguments().getString(IDDD);
            Name = getArguments().getString(NAME);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create__gig_, container, false);
        reqType= v.findViewById(R.id.request_type);
        selectImage = v.findViewById(R.id.imageButton);
        post_desc = v.findViewById(R.id.Gig_desk);
        myimage = v.findViewById(R.id.selctImage);
        progressBar = v.findViewById(R.id.gig_savin);
        SendBtn = v.findViewById(R.id.send_gig);
        firestore =  FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();
        myId = auth.getCurrentUser().getUid();

        if(iddd.equals("open")){
            reqType.setText(R.string.CreatingopenGig);
        }else{
            reqType.setText(getString(R.string.directto,Name));
        }

        SendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(iddd.equals("open")){
                    PostPublicGig();
                }else{
                    if(!iddd.equals(myId)) {
                        String userid = iddd;
                        SendtoPerson(userid);
                    }else{
                        Toast.makeText(getContext(), "go back!" +
                                "you cant create a gig for you self" , Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 42);
                    }
                    else {
                        ImageCropping();
                    }

                } else {
                    ImageCropping();

                }
            }
        });

        return v;
    }

    private void ImageCropping() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(getActivity());
    }

    private void PostPublicGig() {
        final String description = post_desc.getText().toString();
        if (!TextUtils.isEmpty(description)) {
            progressBar.setVisibility(View.VISIBLE);
            if(ImageUri != null){
                final StorageReference gig = storageReference.child("gig_Images").child(myId)
                        .child(ImageUri.getLastPathSegment()+".jpg");
                gig.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Task<Uri> uriTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        throw task.getException();
                                    }
                                    return gig.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        String [] downloadUri =  new String[1];
                                        downloadUri[0] = task.getResult().toString();
                                        Map<String, Object> GigMap = new HashMap<>();
                                        GigMap.put("gig_description",description);
                                        GigMap.put("gig_date", FieldValue.serverTimestamp());
                                        GigMap.put("gig_image",downloadUri[0]);
                                        GigMap.put("from_id",myId);
                                        GigMap.put("to_id",null);
                                        GigMap.put("status","open");
                                        GigMap.put("end_time",null);
                                        GigMap.put("ref_id",null);
                                        firestore.collection("Gigs").add(GigMap)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>()  {
                                                    @Override
                                                    public void onComplete(@NonNull  Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getContext(), "gig added", Toast.LENGTH_LONG).show();
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            //send to open gigs fragment list

                                                        } else {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(getContext(), "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                                        }
                                                        progressBar.setVisibility(View.INVISIBLE);

                                                    }

                                                });
                                    }
                                }
                            });

                        }

                    }
                });

            }else {

                Map<String, Object> GigMap = new HashMap<>();
                GigMap.put("gig_description",description);
                GigMap.put("gig_date", FieldValue.serverTimestamp());
                GigMap.put("gig_image",null);
                GigMap.put("from_id",myId);
                GigMap.put("to_id",null);
                GigMap.put("status","open");
                GigMap.put("end_time",null);
                GigMap.put("ref_id",null);
                firestore.collection("Gigs").add(GigMap)
                        .addOnCompleteListener(new  OnCompleteListener<DocumentReference>()  {
                            @Override
                            public void onComplete(@NonNull  Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "gig added", Toast.LENGTH_LONG).show();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    //send to open gigs fragment list

                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    String error = task.getException().getMessage();
                                    Toast.makeText(getContext(), "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);

                            }

                        });

            }

        }else {
            Toast.makeText(getContext(), "Must enter good description of your gig", Toast.LENGTH_SHORT)
                    .show();
            post_desc.setHintTextColor(Integer.parseInt("#FF634f"));
        }

    }

    private void SendtoPerson(final String userid) {

        final String randomID = UUID.randomUUID().toString();
        final String description = post_desc.getText().toString();
        if (!TextUtils.isEmpty(description)) {
            progressBar.setVisibility(View.VISIBLE);
            if(ImageUri != null){
                final StorageReference gig = storageReference.child("gig_Images").child(myId)
                        .child(ImageUri.getLastPathSegment()+".jpg");
                gig.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Task<Uri> uriTask = task.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        throw task.getException();
                                    }
                                    return gig.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        String [] downloadUri =  new String[1];
                                        downloadUri[0] = task.getResult().toString();
                                        final Map<String, Object> GigMap = new HashMap<>();
                                        GigMap.put("gig_description",description);
                                        GigMap.put("gig_date", FieldValue.serverTimestamp());
                                        GigMap.put("gig_image",downloadUri[0]);
                                        GigMap.put("to_id",userid);
                                        GigMap.put("from_id",myId);
                                        GigMap.put("status","pending");
                                        GigMap.put("end_time",null);
                                        GigMap.put("ref_id",randomID+myId+userid);
                                        firestore.collection("Users").document(myId).collection("Gigs")
                                                .document(randomID+myId+userid)
                                                .set(GigMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull  Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            firestore.collection("Users").document(userid)
                                                                    .collection("Gigs")
                                                                    .document(randomID+myId+userid)
                                                                    .set(GigMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if(task.isSuccessful()) {
                                                                        if (!userid.equals(myId)) {
                                                                            final String status = "Direct Gig";
                                                                            Map<String, Object> mylikeMap = new HashMap<>();
                                                                            mylikeMap.put("status", status);
                                                                            mylikeMap.put("notId", myId);
                                                                            mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                                            mylikeMap.put("postId", null);
                                                                            firestore.collection("Users/" + userid + "/NotificationBox").document(myId).set(mylikeMap)
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (!task.isSuccessful()) {
                                                                                                Toast.makeText(getContext(), "did not notify",
                                                                                                        Toast.LENGTH_SHORT).show();
                                                                                            } else {
                                                                                                firestore.collection("Users").document(userid)
                                                                                                        .collection("Tokens")
                                                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                                            @Override
                                                                                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                                                for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                                                        String token = doc.getDocument().getString("token");
                                                                                                                        NotSender
                                                                                                                                .sendNotifications(getContext(), token
                                                                                                                                        , status, "New approval Alert");
                                                                                                                    }
                                                                                                                }

                                                                                                            }
                                                                                                        });

                                                                                                NotSender.Updatetoken();
                                                                                            }
                                                                                        }

                                                                                    });
                                                                        }

                                                                        Toast.makeText(getContext(), "gig added", Toast.LENGTH_LONG).show();
                                                                        progressBar.setVisibility(View.INVISIBLE);
                                                                        //send to open gigs fragment list
                                                                    }


                                                                }
                                                            });

                                                        } else {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(getContext(), "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                                        }
                                                        progressBar.setVisibility(View.INVISIBLE);

                                                    }

                                                });
                                    }
                                }
                            });

                        }

                    }
                });

            }else {

                final Map<String, Object> GigMap = new HashMap<>();
                GigMap.put("gig_description",description);
                GigMap.put("gig_date", FieldValue.serverTimestamp());
                GigMap.put("gig_image",null);
                GigMap.put("to_id",userid);
                GigMap.put("from_id",myId);
                GigMap.put("status","pending");
                GigMap.put("end_time",null);
                GigMap.put("ref_id",randomID+myId+userid);
                firestore.collection("Users").document(myId)
                        .collection("Gigs").document(randomID+myId+userid)
                        .set(GigMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firestore.collection("Users").document(userid).collection("Gigs")
                                            .document(randomID+myId+userid)
                                            .set(GigMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                if (!userid.equals(myId)) {
                                                    final String status = "Direct Gig";
                                                    Map<String, Object> mylikeMap = new HashMap<>();
                                                    mylikeMap.put("status", status);
                                                    mylikeMap.put("notId", myId);
                                                    mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                    mylikeMap.put("postId", myId);
                                                    firestore.collection("Users/"+userid+"/NotificationBox")
                                                            .document(myId).set(mylikeMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        Toast.makeText(getContext(), "did not notify",
                                                                                Toast.LENGTH_SHORT).show();
                                                                    } else {
                                                                        firestore.collection("Users").document(userid)
                                                                                .collection("Tokens")
                                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                        for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                                String token = doc.getDocument().getString("token");
                                                                                                NotSender
                                                                                                        .sendNotifications(getContext(), token
                                                                                                                , status, "New Gig Alert");
                                                                                            }
                                                                                        }

                                                                                    }
                                                                                });

                                                                        NotSender.Updatetoken();
                                                                    }
                                                                }

                                                            });
                                                }

                                                Toast.makeText(getContext(), "gig added", Toast.LENGTH_LONG).show();
                                                progressBar.setVisibility(View.INVISIBLE);
                                                //send to open gigs fragment list
                                            }


                                        }
                                    });

                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    String error = task.getException().getMessage();
                                    Toast.makeText(getContext(), "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);

                            }

                        });

            }

        }else {
            Toast.makeText(getContext(), "Must enter good description of your gig", Toast.LENGTH_SHORT)
                    .show();
            post_desc.setHintTextColor(Integer.parseInt("#FF634f"));
        }




    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                ImageUri = result.getUri();
                myimage.setVisibility(View.VISIBLE);
                myimage.setImageURI(ImageUri);

            }else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(getContext(), "Didnt Crop properly"+error,Toast.LENGTH_SHORT).show();
            }
        }


    }
}
