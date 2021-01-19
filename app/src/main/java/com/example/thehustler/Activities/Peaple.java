package com.example.thehustler.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Adapter.MessageRecycler;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class Peaple extends AppCompatActivity {
    static String which;
    static final int like =1;
    static final int comment =2;
    static final int repost =3;
    static final int interst=4;
    static final int approvals =5;
    static final int approved =6;

    ArrayList<Users> people;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    private   RecyclerView views;
    private boolean first_load = true;
    DocumentSnapshot lastVisible;
    peapleRecycler adaptor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peaple);
        Toolbar toolbar = findViewById(R.id.toolbar_p);
        setSupportActionBar(toolbar);

        views = findViewById(R.id.pev);
        getSupportActionBar().setTitle("Activity");
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        //get account user id
        //scnarios
        //for post id i have likes.coments and reposts
        // for gig id i have interests
        String Name = null;
        String id = null;
      //  String codeWord = intent.getStringExtra("CODEWORD");
        if(intent.getStringExtra("ID") != null && intent.getStringExtra("POGI")!=null) {
            id = intent.getStringExtra("ID");
          Name = intent.getStringExtra("POGI");
        }

        if(Name!= null || id!= null ) {
            final Query likes = firestore.collection("Posts/" + id+ "/likes").limit(10);//key id,1
            final Query comments = firestore.collection("Posts/"+id+"/Answers").limit(10);//field user_id
            final Query Reposts =  firestore.collection("Posts").document(id).collection("Reposts").limit(10);//repost_Id
            final Query interests = firestore.collection("Gigs/"+id+"/Interests").limit(10);//key id
            final Query aprovals = firestore.collection("Users/"+id+"/Approvals").limit(10);//key id
            final Query approveds = firestore.collection("Users/"+id+"/Approved").limit(10);//key id
            if (Name.equals("Interests")) {
                adaptor = new peapleRecycler(people,2);
            }else {
                adaptor = new peapleRecycler(people,1);
            }
        views.setLayoutManager(new LinearLayoutManager(Peaple.this));
        views.setAdapter(adaptor);

            final String finalName = Name;
            views.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    if (finalName.equals("Likes")) { More(likes,like);
                    }else if(finalName.equals("Comments")){  More(comments,comment);
                    }else if(finalName.equals("Reposts")){ More(Reposts,repost);
                    }else if(finalName.equals("Interests")){ More(interests,interst);
                    }else if(finalName.equals("Approvals")){ More(aprovals,approvals);
                    }else if(finalName.equals("Approved")){ More(approveds,approved);
                    }
                }
            }

        });
        views.setHasFixedSize(true);
            if (Name.equals("Likes")) { pull_list(likes,like);
            }else if(Name.equals("Comments")){ pull_list(comments,comment);
            }else if(Name.equals("Reposts")){pull_list(Reposts,repost);
            }else if(Name.equals("Interests")){pull_list(interests,interst);
            }else if(Name.equals("Approvals")){pull_list(aprovals,approvals);
            }else if(Name.equals("Approved")){pull_list(approveds,approved);
            }
        }

    }

    private void pull_list(final Query q, final int num) {
        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (first_load) {
                            lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                            people.clear();
                        }
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String needed_id = "";
                                final String docID = doc.getDocument().getId();
                                if(num == like || num == interst || num == approvals|| num == approved){
                                    needed_id = docID;
                                }else if (num ==  repost){
                                    needed_id = doc.getDocument().getString("repost_Id");
                                }else if (num == comment){
                                    needed_id = doc.getDocument().getString("user_id");
                                }
                                firestore.collection("Users").document(needed_id).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if(task.getResult().exists()) {
                                                        Users user = task.getResult().toObject(Users.class);

                                                        if (first_load) {
                                                           people.add(user);
                                                        } else {
                                                            people.add(0, user);
                                                        }

                                                       adaptor.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                        first_load = false;
                    } else {
                        Toast.makeText(Peaple.this, "no peaple here", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Peaple.this, "error:" + error, Toast.LENGTH_LONG).show();
                }

            }
        });
    }


    public void More(Query q, final int num){
        q.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if (error == null) {
                    if (!queryDocumentSnapshots.isEmpty()) {

                        lastVisible = queryDocumentSnapshots.getDocuments()
                                .get(queryDocumentSnapshots.size() - 1);
                        for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String needed_id = "";
                                final String docID = doc.getDocument().getId();
                                if(num == like || num == interst || num == approvals|| num == approved){
                                    needed_id = docID;
                                }else if (num ==  repost){
                                    needed_id = doc.getDocument().getString("repost_Id");
                                }else if (num == comment){
                                    needed_id = doc.getDocument().getString("user_id");
                                }
                                firestore.collection("Users").document(needed_id).get()
                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    if(task.getResult().exists()) {
                                                        Users user = task.getResult().toObject(Users.class);
                                                        people.add(user);
                                                        adaptor.notifyDataSetChanged();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }

                    } else {
                        Toast.makeText(Peaple.this, "no peaple here", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(Peaple.this, "error:" + error, Toast.LENGTH_LONG).show();
                }

            }
        });
    }

}
class peapleRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<Users> people;
    public Context context;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String myid;
    static int intrest = 2;
    static int others = 1;
    int type;

    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");
    public peapleRecycler(ArrayList<Users> people, int type) {
        this.people = people;
        this.type = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = null;
        context = parent.getContext();
        if (viewType == intrest) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.interest_lists, parent, false);
            return new intrestholder(v);
        } else if (viewType == others) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chats_list, parent, false);
            return new OthersHolder(v);
        }else return null;

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        switch (holder.getItemViewType()) {
            case 1:
                {
                    final OthersHolder holder1 = (OthersHolder) holder;
                    final String CurrentUser = auth.getCurrentUser().getUid();
                    final String ChatterId = people.get(position).Userid;



                    String Image_uri = people.get(position).getImage();
                    String Name =people.get(position).getName().get(0);
                    String Name2 = people.get(position).getName().get(1);
                    holder1.setUser(Name,Name2,Image_uri,people.get(position).getCity());


                    holder1.section.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (CurrentUser.equals(ChatterId)) {
                                Toast.makeText(context,"thats you",Toast.LENGTH_SHORT).show();
                            }else gotochatActivity(ChatterId);

                        }
                    });

                    break;
                }
            case 2:
            {
                final intrestholder holder2 = (intrestholder) holder;
                final String CurrentUser = auth.getCurrentUser().getUid();
                final String ChatterId = people.get(position).Userid;
                holder2.name.setText(people.get(position).getName().get(1));

                if (CurrentUser.equals(ChatterId)){
                    holder2.send_gig.setVisibility(View.VISIBLE);
                    holder2.send_gig.setEnabled(true);
                }else {
                    holder2.send_gig.setVisibility(View.GONE);
                    holder2.send_gig.setEnabled(false);
                }

                firestore.collection("Users/"+ChatterId+"/Approvals").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(!value.isEmpty()){
                            int count = value.size();
                            holder2.aprrovals.setText(new StringBuilder().append(Integer.toString(count)).append("aprs").toString());

                        }else{
                            holder2.aprrovals.setText(new StringBuilder().append(Integer.toString(0)).append("aprs").toString());
                        }

                    }
                });

                holder2.name.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(CurrentUser.equals(ChatterId)) {
                            Toast.makeText(context,"Thats you",Toast.LENGTH_SHORT).show();
                        }else gotochatActivity(ChatterId);
                    }
                });
                holder2.reviews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent reviewIntent = new Intent(context,Reviews.class);
                        reviewIntent.putExtra("UserId",ChatterId);
                        context.startActivity(reviewIntent);
                    }
                });

                holder2.send_gig.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       // tbd
                    }
                });
            }

        }

    }

    private void gotochatActivity(String chatterId) {
        Intent message = new Intent(context, AnotherUserAccount.class);
        message.putExtra("UserId",chatterId);
        context.startActivity(message);
    }


    @Override
    public int getItemViewType(int position) {
        if (type == intrest) {
            return intrest;
        } else return others;
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    public static class intrestholder extends RecyclerView.ViewHolder {

        TextView name, aprrovals;
        Button reviews;
        ImageButton send_gig;

        public intrestholder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.int_name);
            aprrovals = itemView.findViewById(R.id.aprrovals);
            reviews = itemView.findViewById(R.id.btn_rv);
            send_gig = itemView.findViewById(R.id.send_btn);

        }
    }
    public class OthersHolder extends RecyclerView.ViewHolder{
        CircleImageView Chatterydp;
        TextView ChatterName,new_date,name2;
        View v;
        ConstraintLayout section;
        public OthersHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
         //   badge = v.findViewById(R.id.textView13);
            new_date = v.findViewById(R.id.new_date);
            Chatterydp = v.findViewById(R.id.chatterImage);
            ChatterName = v. findViewById(R.id.chat_name);
            section = v.findViewById(R.id.ViewC);
            name2 = v.findViewById(R.id.name2);

        }
        public void setUser(String s, String name, String image_uri,String city) {
            ChatterName.setText(s);
            name2.setText(name);
            new_date.setText(city);
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);

            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(image_uri)
                    .into(Chatterydp);
        }



    }



}