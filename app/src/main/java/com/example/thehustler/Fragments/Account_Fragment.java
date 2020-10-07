package com.example.thehustler.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Activities.ProfileActivity2;
import com.example.thehustler.Activities.Reviews;
import com.example.thehustler.Adapter.MypostsAdapter;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class Account_Fragment extends Fragment {


    private RecyclerView postsView;

    private List<Blogpost> Postlist;
    private FirebaseFirestore firestore;
    private MypostsAdapter mypostsAdapter;

    private  String currentID;
    private DocumentSnapshot lastVisible;
    private TextView gigCount;

    private Button Review,edit;

    private FirebaseAuth auth;

    private CircleImageView userImage;
    private TextView Name1,name2,sex,city,country,work, aprd,aprl;

    private Boolean loadFirst= true;


    public Account_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(
                R.layout.fragment_account_, container, false);

        userImage = view.findViewById(R.id.toolface);
        Name1 = view.findViewById(R.id.myname);
        Review = view.findViewById(R.id.Review);
        name2 = view.findViewById(R.id.textView10);
        sex = view.findViewById(R.id.sex);
        city =view.findViewById(R.id.city);
        country =view.findViewById(R.id.Country);
        work =view.findViewById(R.id.Mywork);

        edit = view.findViewById(R.id.edit_);
        aprd= view.findViewById(R.id.approvedNumber);
        aprl=view.findViewById(R.id.approvalnumber);
        gigCount = view.findViewById(R.id.finGigs);


        auth = FirebaseAuth.getInstance();
        firestore= FirebaseFirestore.getInstance();

        Postlist = new ArrayList<>();
        postsView = view.findViewById(R.id.myPosts);
        postsView.setHasFixedSize(true);
        postsView.setNestedScrollingEnabled(false);

        mypostsAdapter = new MypostsAdapter(Postlist);
        postsView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        postsView.setAdapter(mypostsAdapter);

        currentID= auth.getCurrentUser().getUid();
        postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    LoadmorePosts();
                }

            }
        });

        firestore.collection("Users/"+currentID+"/Approvals").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count = value.size();
                    aprl.setText(Integer.toString(count));

                }else{
                    aprl.setText("0");
                }

            }
        });
        firestore.collection("Users/"+currentID+"/Gigs")
                .whereEqualTo("status","done")
                .whereEqualTo("to_id",currentID)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                        if(!value.isEmpty()){
                            int count = value.size();
                            gigCount.setText(Integer.toString(count));
                        }else{
                            gigCount.setText(0);
                        }
                    }
                });

        firestore.collection("Users/"+currentID+"/Approved").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count = value.size();
                    aprd.setText(Integer.toString(count));

                }else{
                    aprd.setText("0");
                }

            }
        });


        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reviewIntent = new Intent(getContext(), ProfileActivity2.class);
                startActivity(reviewIntent);
            }
        });


        Review.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent reviewIntent = new Intent(getContext(), Reviews.class);
                reviewIntent.putExtra("UserId",currentID);
                startActivity(reviewIntent);
            }
        });

            return view;



    }

    @Override
    public void onStart() {
        super.onStart();
        DocumentReference userDocRef = firestore.collection("Users").document(currentID);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    DocumentSnapshot documentSnapshot= task.getResult();
                    Users user = documentSnapshot.toObject(Users.class);
                    Name1.setText(user.getName().get(0));
                    name2.setText(user.getName().get(1));
                    sex.setText(user.getSex());
                    country.setText(user.getCountry());
                    city.setText(user.getCity());
                    work.setText(user.getWork());

                    String profileImageUrl = user.getImage();
                    String profilethmb = user.getThumb();
                    RequestOptions placeholderOption = new RequestOptions();
                    placeholderOption.placeholder(R.drawable.ic_person);
                    Glide.with(getContext()).applyDefaultRequestOptions(placeholderOption).load(profileImageUrl).thumbnail(Glide.with(getContext()).load(profilethmb))
                            .into(userImage);



                }else {
                    String e = task.getException().getMessage();
                    Toast.makeText(getContext(),e+"failed",Toast.LENGTH_LONG).show();
                }
            }
        });
        if (auth.getCurrentUser() != null) {

            Query Firstquery = firestore.collection("Users")
                    .document(currentID).collection("Posts")
            .orderBy("timeStamp", Query.Direction.DESCENDING).limit(4);
            Firstquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                    if(e==null) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (loadFirst) {
                                lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                                Postlist.clear();
                                //usersList.clear();
                            }
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    final String docID = doc.getDocument().getId();
                                    final String postID = doc.getDocument().getString("post_id");
                                    firestore.collection("Posts").document(postID).get()
                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if(task.getResult().exists()) {
                                                    Blogpost blogPost = task.getResult().toObject(Blogpost.class).withID(postID);
                                                    if (loadFirst) {
                                                        // usersList.add(users);
                                                        Postlist.add(blogPost);
                                                    } else {
                                                        // usersList.add(0, users);
                                                        Postlist.add(0, blogPost);
                                                    }
                                                    // userBlogList.add(blogPost);
                                                    mypostsAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                });
                            }
                            }
                            loadFirst = false;
                        } else {
                            Toast.makeText(getContext(), "nothing here", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        Toast.makeText(getContext(),"error:"+e,Toast.LENGTH_LONG).show();
                    }
                }
            });

        }

    }

    public void LoadmorePosts() {
        if (auth.getCurrentUser() != null) {
            Query Nextquery =  firestore.collection("Users")
                    .document(currentID).collection("Posts")
                    .orderBy("timeStamp", Query.Direction.DESCENDING)
                    .startAfter(lastVisible)
                    .limit(4);
            Nextquery.addSnapshotListener( new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);

                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                final String docID = doc.getDocument().getId();
                                final String postID = doc.getDocument().getString("post_id");
                                firestore.collection("Posts").document(postID).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if(task.isSuccessful()){
                                                    if(task.getResult().exists()) {
                                                        Blogpost blogPost = task.getResult().toObject(Blogpost.class).withID(postID);
                                                        // usersList.add(users);
                                                        Postlist.add(blogPost);
                                                        // userBlogList.add(blogPost);
                                                        mypostsAdapter.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                }
            });


        }
    }


}