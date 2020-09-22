package com.example.thehustler.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.thehustler.Activities.AnotherUserAccount;
import com.example.thehustler.Model.OpenGigs;
import com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;




public class Gig_lists_Fragment extends Fragment {

    private static final String STATUS = "status";
    private String Status;

    private RecyclerView views;

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<OpenGigs> gigs_lists;
    private  List<Users> usersListl;
    private DocumentSnapshot lastVisible;
    private Boolean firstload = true;
    private String myId;
    private OpenGigsAdapter adapterO_G;
    private RequestAdaptor adaptorR_Q;
    private HistoryAdaptor historyAdaptor;
    private  OngoingAdaptor ongoingAdaptor;
      public ProgressBar bar;


    public Gig_lists_Fragment() {
        // Required empty public constructor
    }


    public static Gig_lists_Fragment newInstance(String status) {
        Gig_lists_Fragment fragment = new Gig_lists_Fragment();
        Bundle args = new Bundle();
        args.putString(STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Status = getArguments().getString(STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gig_lists_, container, false);
        views = v.findViewById(R.id.rv_gigs);
        gigs_lists = new ArrayList<>();
        usersListl = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        myId = auth.getCurrentUser().getUid();
       bar = v.findViewById(R.id.progressBar);


        //Statuses,,,

        //open for open gig
        //pending for requests not yet accepted
        //ongoing for active gigs
        //done for history

        switch (Status){
            case "open":
                firstload = true;
                lastVisible = null;
                getOpenGigs(container);
                break;
            case "pending":
                firstload = true;
                lastVisible = null;
                getRequests(container);
                break;
            case "ongoing":
                firstload = true;
                lastVisible = null;
                getActive(container);
                break;
            case "done":
                firstload = true; lastVisible = null;
                getHistory(container);
                break;
            default:
                break;

        }

        return v;
    }

    private void getHistory(ViewGroup container) {
        final String currentStatus ="done";
        historyAdaptor = new HistoryAdaptor(gigs_lists,usersListl);
        views.setLayoutManager(new LinearLayoutManager(container.getContext()));
        views.setAdapter(historyAdaptor);

        views.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    More(currentStatus);
                }
            }

        });
        views.setHasFixedSize(true);


        Query first = firestore.collection("Users").document(myId)
                .collection("Gigs")
                .whereEqualTo("status",currentStatus)
                .orderBy("gig_date", Query.Direction.DESCENDING)
                .limit(4);

        first.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (!value.isEmpty()) {
                    if (firstload) {
                        lastVisible = value.getDocuments().get(value.size() - 1);
                        gigs_lists.clear();
                        usersListl.clear();
                    }
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String gigID = doc.getDocument().getId();

                            final OpenGigs openGigs = doc.getDocument().toObject(OpenGigs.class).withID(gigID);
                            String from_id = doc.getDocument().getString("from_id");
                            String to_id =doc.getDocument().getString("to_id");
                            String sendId;
                            if(from_id.equals(myId)){
                                sendId = to_id;
                            }else {
                                sendId = from_id;
                            }
                            firestore.collection("Users").document(sendId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);

                                        if (firstload) {
                                            usersListl.add(users);
                                            gigs_lists.add(openGigs);

                                        } else {
                                            usersListl.add(0, users);
                                            gigs_lists.add(0, openGigs);
                                        }
                                        adaptorR_Q.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                    firstload = false;
                }else {
                    Toast.makeText(getContext(), "no Past Activity", Toast.LENGTH_LONG).show();
                }
            }

        });


    }

    private void getActive(ViewGroup container) {
        final String currentStatus ="ongoing";
        ongoingAdaptor = new OngoingAdaptor(gigs_lists,usersListl);
        views.setLayoutManager(new LinearLayoutManager(container.getContext()));
        views.setAdapter(ongoingAdaptor);

        views.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    More(currentStatus);
                }
            }

        });
        views.setHasFixedSize(true);
        Query first = firestore.collection("Users").document(myId)
                .collection("Gigs")
                .whereEqualTo("status",currentStatus)
                .orderBy("gig_date", Query.Direction.DESCENDING)
                .limit(4);

        first.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (!value.isEmpty()) {
                    if (firstload) {
                        lastVisible = value.getDocuments().get(value.size() - 1);
                        gigs_lists.clear();
                        usersListl.clear();
                    }
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String gigID = doc.getDocument().getId();

                            final OpenGigs openGigs = doc.getDocument().toObject(OpenGigs.class).withID(gigID);
                            String from_id = doc.getDocument().getString("from_id");
                            String to_id =doc.getDocument().getString("to_id");
                            String sendId;
                            if(from_id.equals(myId)){
                                sendId = to_id;
                            }else {
                                sendId = from_id;
                            }
                            firestore.collection("Users").document(sendId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);

                                        if (firstload) {
                                            usersListl.add(users);
                                            gigs_lists.add(openGigs);

                                        } else {
                                            usersListl.add(0, users);
                                            gigs_lists.add(0, openGigs);
                                        }
                                        adaptorR_Q.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                    firstload = false;
                }else {
                    Toast.makeText(getContext(), "No Activity", Toast.LENGTH_LONG).show();
                }
            }

        });


    }

    private void getRequests(ViewGroup container) {
        final String currentStatus ="pending";
        adaptorR_Q = new RequestAdaptor(gigs_lists,usersListl);
        views.setLayoutManager(new LinearLayoutManager(container.getContext()));
        views.setAdapter(adaptorR_Q);

        views.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                   More(currentStatus);
                }
            }

        });
        views.setHasFixedSize(true);


        Query first = firestore.collection("Users").document(myId)
                .collection("Gigs")
              .whereEqualTo("status",currentStatus)
              .orderBy("gig_date", Query.Direction.DESCENDING)
              .limit(4);

      first.addSnapshotListener(new EventListener<QuerySnapshot>() {
          @Override
          public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

              if (!value.isEmpty()) {
                  if (firstload) {
                      lastVisible = value.getDocuments().get(value.size() - 1);
                      gigs_lists.clear();
                      usersListl.clear();
                  }
                  for (DocumentChange doc : value.getDocumentChanges()) {
                      if (doc.getType() == DocumentChange.Type.ADDED) {
                          String gigID = doc.getDocument().getId();

                          final OpenGigs openGigs = doc.getDocument().toObject(OpenGigs.class).withID(gigID);
                          String from_id = doc.getDocument().getString("from_id");
                          String to_id =doc.getDocument().getString("to_id");
                          String sendId;
                          if(from_id.equals(myId)){
                              sendId = to_id;
                          }else {
                              sendId = from_id;
                          }
                          firestore.collection("Users").document(sendId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                              @Override
                              public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                  if (task.isSuccessful()) {
                                      Users users = task.getResult().toObject(Users.class);

                                      if (firstload) {
                                          usersListl.add(users);
                                          gigs_lists.add(openGigs);

                                      } else {
                                          usersListl.add(0, users);
                                          gigs_lists.add(0, openGigs);
                                      }
                                      adaptorR_Q.notifyDataSetChanged();
                                  }
                              }
                          });
                      }
                  }
                  firstload = false;
              }else {
                  Toast.makeText(getContext(), "no Requests found", Toast.LENGTH_LONG).show();
              }
          }

      });

    }

    private void More(final String currentStatus) {
        Query next = firestore.collection("Users").document(myId)
                .collection("Gigs")
                .whereEqualTo("status",currentStatus)
                .startAfter(lastVisible)
                .orderBy("gig_date", Query.Direction.DESCENDING)
                .limit(4);

        next.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (!value.isEmpty()) {
                    lastVisible = value.getDocuments()
                            .get(value.size() - 1);
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String gigID = doc.getDocument().getId();

                            final OpenGigs openGigs = doc.getDocument().toObject(OpenGigs.class).withID(gigID);
                            String from_id = doc.getDocument().getString("from_id");
                            String to_id =doc.getDocument().getString("to_id");
                            String sendId;
                            if(from_id.equals(myId)){
                                sendId = to_id;
                            }else {
                                sendId = from_id;
                            }
                            firestore.collection("Users").document(sendId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);
                                            usersListl.add(users);
                                            gigs_lists.add(openGigs);
                                            if(currentStatus.equals("pending")) {
                                                adaptorR_Q.notifyDataSetChanged();
                                            }else if(currentStatus.equals("ongoing")){
                                                ongoingAdaptor.notifyDataSetChanged();
                                            }else {
                                                historyAdaptor.notifyDataSetChanged();
                                            }
                                    }
                                }
                            });
                        }
                    }

                }else {
                    Toast.makeText(getContext(), "Thats it", Toast.LENGTH_LONG).show();
                }
            }
        });


    }

    private void getOpenGigs(ViewGroup container) {
        adapterO_G = new OpenGigsAdapter(gigs_lists,usersListl);
        views.setLayoutManager(new LinearLayoutManager(container.getContext()));
        views.setAdapter(adapterO_G);

        views.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    moreOpenGigs();
                }
            }

        });
        views.setHasFixedSize(true);


        Query Openquery= firestore.collection("Gigs")
                .orderBy("gig_date", Query.Direction.DESCENDING)
                .limit(3);
        Openquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (!value.isEmpty()) {
                    if (firstload) {
                        lastVisible = value.getDocuments().get(value.size() - 1);
                       gigs_lists.clear();
                       usersListl.clear();
                    }
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String gigID = doc.getDocument().getId();

                            final OpenGigs openGigs = doc.getDocument().toObject(OpenGigs.class).withID(gigID);
                            String bloguser_id = doc.getDocument().getString("from_id");
                            firestore.collection("Users").document(bloguser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);

                                            if (firstload) {
                                                usersListl.add(users);
                                                gigs_lists.add(openGigs);

                                            } else {
                                                usersListl.add(0, users);
                                                gigs_lists.add(0, openGigs);
                                            }
                                            adapterO_G.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                    firstload = false;
                }else {
                    Toast.makeText(getContext(), "no open Gigs found", Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    private void moreOpenGigs() {
        Query Nextquery= firestore.collection("Gigs")
                .orderBy("gig_date", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(3);
        Nextquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if (!value.isEmpty()) {
                    lastVisible = value.getDocuments()
                            .get(value.size() - 1);
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String gigID = doc.getDocument().getId();

                            final OpenGigs openGigs = doc.getDocument().toObject(OpenGigs.class).withID(gigID);
                            String bloguser_id = doc.getDocument().getString("from_id");
                            firestore.collection("Users").document(bloguser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);
                                            usersListl.add(users);
                                            gigs_lists.add(openGigs);

                                        adapterO_G.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }

                }else {
                    Toast.makeText(getContext(), "no more Open gigs ", Toast.LENGTH_LONG).show();
                }


            }
        });

    }


}







class OpenGigsAdapter extends RecyclerView.Adapter<OpenGigsAdapter.ViewHolder>{
    List<OpenGigs> gigsList;
    List<Users> usersList;
    public Context context;
    private FirebaseAuth auth;
    private  FirebaseFirestore firestore;
    private String  myid;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");
    public OpenGigsAdapter(List<OpenGigs> gigs_lists, List<Users> usersListl) {
        gigsList= gigs_lists;
        usersList = usersListl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.open_gigs,parent, false);
        context = parent.getContext();
        auth =FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        myid = auth.getCurrentUser().getUid();
        String  gig_image = gigsList.get(position).getGig_image();
        final String from_id=gigsList.get(position).getFrom_id();
        String  gig_date = gigsList.get(position).getGig_date();
        String gig_desk = gigsList.get(position).getGig_description();
        final String gigId = gigsList.get(position).Userid;

        String name = usersList.get(position).getName().get(0);
        String userImage_thumb = usersList.get(position).getThumb();
        String userImage = usersList.get(position).getImage();
        holder.setOwner(name,userImage_thumb,userImage,from_id);
        holder.setGig(gig_desk,gig_date,gig_image);


       // holder.section.setlon

        holder.star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection("Gigs/"+gigId+"/Interests").document(myid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().exists()) {
                                Map<String, Object> likeMap = new HashMap<>();
                                likeMap.put("timestamp", FieldValue.serverTimestamp());
                                firestore.collection("Gigs/"+gigId+"/Interests").document(myid).set(likeMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!myid.equals(myid)) {
                                                    final String status = "Interests";
                                                    Map<String, Object> mylikeMap = new HashMap<>();
                                                    mylikeMap.put("status", status);
                                                    mylikeMap.put("notId", myid);
                                                    mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                    mylikeMap.put("postId", from_id);
                                                    firestore.collection("Users/"+from_id+"/NotificationBox").document(myid).set(mylikeMap)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful()) {
                                                                        Toast.makeText(context, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                    //send notification now
                                                                    firestore.collection("Users").document(from_id)
                                                                            .collection("Tokens")
                                                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                    for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                            String token = doc.getDocument().getString("token");
                                                                                            NotSender
                                                                                                    .sendNotifications(context,token
                                                                                                            ,status,"New Interest Alert");
                                                                                        }
                                                                                    }
                                                                                }
                                                                            });
                                                                    NotSender.Updatetoken();
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            } else {
                                firestore.collection("Gigs/"+gigId+"/Interests").document(myid).delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!from_id.equals(myid)) {
                                                    firestore.collection("Users/"+from_id+"/NotificationBox")
                                                            .document(myid).delete()
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Toast.makeText(context, "did not delete properly" + e, Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }else {
                            Toast.makeText(context, "your offline", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });



        //get favorites
        firestore.collection("Gigs/"+gigId+"/Interests").document(myid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    holder.star.setImageDrawable(context.getDrawable(R.drawable.ic_favestar));
                }else{
                    holder.star.setImageDrawable(context.getDrawable(R.drawable.ic_n_Istar));
                }
            }
        });
        //likes count
        firestore.collection("Gigs/"+gigId+"/Interests").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    holder.updateCount(count);
                }else{
                    holder.updateCount(0);
                }
            }
        });
    }
    @Override
    public int getItemCount() {
        return gigsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        CircleImageView user;
        TextView byName,post_date,description,interests;
        ImageView star,descImage;
        View v;
        RelativeLayout section;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            user = v.findViewById(R.id.userCir);//onclick
            byName = v.findViewById(R.id.by_name);//onClick
            post_date = v.findViewById(R.id.start_date);
            description= v.findViewById(R.id.gig_describe);
            interests = v.findViewById(R.id.interests_);//counter , onclick
            descImage = v.findViewById(R.id.imageView);
            star = v.findViewById(R.id.imageView_interest);//check
            //onclick//notify
            section = v.findViewById(R.id.rl_section);
        }

        private void setOwner(String name, String userImage_thumb, String userImage,String from_id) {

            if(from_id.equals(myid)){
                byName.setText(R.string.byyou);
            }else {
                byName.setText(context.getString(R.string.Postedby,name));
            }
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);
            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(userImage).thumbnail(Glide.with(context).load(userImage_thumb))
                    .into(user);
        }

        public void setGig(String gig_desk, String gig_date, String gig_image) {
            description.setText(gig_desk);
            if( gig_date != null) {
                post_date.setText(format.format(gig_date));
            }else{
                post_date.setText(format.format(new Date()));
            }

            if(gig_image !=null){
                RequestOptions placeholderOP = new RequestOptions();
                placeholderOP.placeholder(R.drawable.rounded_rectangle);
                Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(gig_image)
                        .into(user);
            }else{
                descImage.setVisibility(View.GONE);
                descImage.setEnabled(false);
            }
        }

        public void updateCount(int count) {
            if (count > 0)
                interests.setText(Integer.toString(count));
            else
                interests.setText("");
        }
    }
}

class HistoryAdaptor extends RecyclerView.Adapter<HistoryAdaptor.ViewHolder> {
    List<OpenGigs> gigsList;
    List<Users> usersList;
    public Context context;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String myid;
    String  check_id;
    Gig_lists_Fragment fragment;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

    public HistoryAdaptor(List<OpenGigs> gigs_lists, List<Users> usersListl) {
        gigsList = gigs_lists;
        usersList = usersListl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.on_going_gig, parent, false);
        context = parent.getContext();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        holder.reviewCheck.setVisibility(View.VISIBLE);
        holder.reviewCheck.setEnabled(true);
        holder.Done.setVisibility(View.GONE);
        holder.Done.setEnabled(false);


        myid = auth.getCurrentUser().getUid();
        String gig_image = gigsList.get(position).getGig_image();
        final String from_id = gigsList.get(position).getFrom_id();
        String gig_date = gigsList.get(position).getEnd_time();
        final String ref_id = gigsList.get(position).getRef_id();
        final String to_id = gigsList.get(position).getTo_id();
        String gig_desk = gigsList.get(position).getGig_description();
        final String gigId = gigsList.get(position).Userid;
        final String name = usersList.get(position).getName().get(0);
        String userImage_thumb = usersList.get(position).getThumb();
        String userImage = usersList.get(position).getImage();
        holder.setOwner(name, userImage_thumb, userImage, from_id);
        holder.setGig(gig_desk, gig_date, gig_image);
        String send_id;

       if(from_id.equals(myid)){
           send_id = to_id;
           check_id = myid;
       }else {//am the to_id
           check_id = from_id;
           send_id = myid;
       }
       //check for review
        firestore.collection("Users/"+send_id+"/Reviews")
                .whereEqualTo("reviewerId",check_id).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.isEmpty()){

                    if(from_id.equals(myid)) {
                        holder.reviewCheck.setText(context.getString(R.string.please_review, name));
                    }else
                        holder.reviewCheck.setText(context.getString(R.string.checkme, name));

                }else{
                    if(from_id.equals(myid)) {
                        holder.reviewCheck.setText(R.string.reviewed);
                    }else
                        holder.reviewCheck.setText(R.string.reviewed);

                }
            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build an AlertDialog

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete gig record");
                builder.setMessage("confirm to delete?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        fragment.bar.setVisibility(View.VISIBLE);
                        firestore.collection("Users").document(myid).collection("Gigs")
                                .document(gigId)
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                gigsList.remove(position);
                                usersList.remove(position);
                                notifyItemRemoved(position);
                            }
                        });


                    }
                });
                // Set the alert dialog no button click listener
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }

    @Override
    public int getItemCount() {
        return gigsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user;
        TextView name, date, end_time, desc,reviewCheck;
        ImageView Done, cancel, decImage;
        View v;
        RelativeLayout rl;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            end_time = v.findViewById(R.id.st);
            user = v.findViewById(R.id.userCir);//onclick
            name = v.findViewById(R.id.by_name);//onClick
            date = v.findViewById(R.id.start_date);
            desc = v.findViewById(R.id.gig_describe);
            decImage = v.findViewById(R.id.imageView);//counter , onclick
            cancel = v.findViewById(R.id.imageView3);
            Done = v.findViewById(R.id.imageView2);//check
            //onclick//notify
            rl = v.findViewById(R.id.mylay);
            reviewCheck = v.findViewById(R.id.Review__);
        }

        public void setGig(String gig_desk, String gig_date, String gig_image) {
            end_time.setText(R.string.ended);
            desc.setText(gig_desk);
            if (gig_date != null) {
                date.setText(format.format(gig_date));
            } else {
                date.setText(format.format(new Date()));
            }
            if (gig_image != null) {
                RequestOptions placeholderOP = new RequestOptions();
                placeholderOP.placeholder(R.drawable.rounded_rectangle);
                Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(gig_image)
                        .into(user);
            } else {
                decImage.setVisibility(View.GONE);
                decImage.setEnabled(false);
            }


        }

        public void setOwner(String Name, String userImage_thumb, String userImage, String from_id) {
            if(from_id.equals(myid)){
                name.setText(context.getString(R.string.byyou,Name));
            }else {
                name.setText(context.getString(R.string.Sentby,Name));
            }
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);
            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(userImage).thumbnail(Glide.with(context).load(userImage_thumb))
                    .into(user);
        }
    }
}


class OngoingAdaptor extends RecyclerView.Adapter<OngoingAdaptor.ViewHolder> {
    List<OpenGigs> gigsList;
    List<Users> usersList;
    public Context context;
    private FirebaseAuth auth;
    private  FirebaseFirestore firestore;
     Gig_lists_Fragment fragment;
    private String  myid;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

    public OngoingAdaptor(List<OpenGigs> gigs_lists, List<Users> usersListl) {
        gigsList= gigs_lists;
        usersList = usersListl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.on_going_gig,parent, false);
        context = parent.getContext();
        auth =FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position){


        myid = auth.getCurrentUser().getUid();
        String  gig_image = gigsList.get(position).getGig_image();
        final String from_id=gigsList.get(position).getFrom_id();
        final String to_id = gigsList.get(position).getTo_id();
        final String  gig_date = gigsList.get(position).getStart_time();
        String gig_desk = gigsList.get(position).getGig_description();
        final String gigId = gigsList.get(position).Userid;
        String name = usersList.get(position).getName().get(0);
        String userImage_thumb = usersList.get(position).getThumb();
        String userImage = usersList.get(position).getImage();
        final String ref = gigsList.get(position).getRef_id();

        holder.setOwner(name,userImage_thumb,userImage,from_id);
        holder.setGig(gig_desk,gig_date,gig_image);

        if(from_id.equals(myid)){
         holder.Done.setEnabled(false);
         holder.Done.setVisibility(View.GONE);
        }
        holder.user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String send_id;
                if (from_id.equals(myid)){
                    send_id = to_id;
                }else send_id = from_id;
                Intent thereAccount = new Intent(context, AnotherUserAccount.class);
                thereAccount.putExtra("UserId",send_id);
                context.startActivity(thereAccount);
            }
        });

        holder.Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build an AlertDialog
                // should only occur if i dint send the gig
                if(!from_id.equals(myid)){
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Finishing gig");
                builder.setMessage("Are you really done?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        fragment.bar.setVisibility(View.VISIBLE);
                        final Map<String, Object> GigMap = new HashMap<>();
                        GigMap.put("end_time", FieldValue.serverTimestamp());
                        GigMap.put("status", "done");
                        firestore.collection("Users").document(myid).collection("Gigs")
                                .document(gigId).update(GigMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    firestore.collection("Users")
                                            .document(from_id).collection("Gigs")
                                            .document(ref).update(GigMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            fragment.bar.setVisibility(View.INVISIBLE);
                                            gigsList.remove(position);
                                            usersList.remove(position);
                                            notifyItemRemoved(position);

                                            final String status = "Done Gig";
                                            Map<String, Object> mylikeMap = new HashMap<>();
                                            mylikeMap.put("status", status);
                                            mylikeMap.put("notId", myid);
                                            mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                            mylikeMap.put("postId",null);
                                            firestore.collection("Users/"+from_id+"/NotificationBox")
                                                    .document(myid).set(mylikeMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (!task.isSuccessful()) {
                                                                Toast.makeText(context, "did not notify",
                                                                        Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                firestore.collection("Users").document(from_id)
                                                                        .collection("Tokens")
                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                            @Override
                                                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                        String token = doc.getDocument().getString("token");
                                                                                        NotSender
                                                                                                .sendNotifications(context, token
                                                                                                        , status, "Done Gig Alert");
                                                                                    }
                                                                                }

                                                                            }
                                                                        });

                                                                NotSender.Updatetoken();
                                                            }
                                                        }

                                                    });

                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "something went wrong, try again", Toast.LENGTH_SHORT).show();
                                    fragment.bar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            }
        });

        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Build an AlertDialog

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete gig");
                builder.setMessage("confirm to delete?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        fragment.bar.setVisibility(View.VISIBLE);
                        firestore.collection("Users").document(myid).collection("Gigs")
                                .document(gigId)
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                final String send_id;
                                if (from_id.equals(myid)){
                                    send_id = to_id;
                                }else send_id = from_id;

                                firestore.collection("Users")
                                        .document(send_id).collection("Gigs")
                                        .document(ref)
                                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        fragment.bar.setVisibility(View.INVISIBLE);
                                        //not
                                        final String status = "Canceled Gig";
                                        Map<String, Object> mylikeMap = new HashMap<>();
                                        mylikeMap.put("status", status);
                                        mylikeMap.put("notId", myid);
                                        mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                        mylikeMap.put("postId",null);
                                        firestore.collection("Users/"+send_id+"/NotificationBox")
                                                .document(myid).set(mylikeMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!task.isSuccessful()) {
                                                            Toast.makeText(context, "did not notify",
                                                                    Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            firestore.collection("Users").document(send_id)
                                                                    .collection("Tokens")
                                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                        @Override
                                                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                            for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                    String token = doc.getDocument().getString("token");
                                                                                    NotSender
                                                                                            .sendNotifications(context, token
                                                                                                    , status, "Canceled Gig Alert");
                                                                                }
                                                                            }

                                                                        }
                                                                    });

                                                            NotSender.Updatetoken();
                                                        }
                                                    }

                                                });

                                    }
                                });

                                gigsList.remove(position);
                                usersList.remove(position);
                                notifyItemRemoved(position);
                            }
                        });


                    }
                });
                // Set the alert dialog no button click listener
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return gigsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user;
        TextView name,date,end_time,desc;
        ImageView Done,cancel,decImage;
        View v;
        RelativeLayout rl;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            v = itemView;
            end_time = v.findViewById(R.id.st);
            user = v.findViewById(R.id.userCir);//onclick
            name = v.findViewById(R.id.by_name);//onClick
            date = v.findViewById(R.id.start_date);
            desc= v.findViewById(R.id.gig_describe);
            decImage = v.findViewById(R.id.imageView);//counter , onclick
            cancel = v.findViewById(R.id.imageView3);
            Done= v.findViewById(R.id.imageView2);//check
            //onclick//notify
            rl = v.findViewById(R.id.mylay);
        }

        private void setOwner(String Name, String userImage_thumb, String userImage,String from_id) {
            if(from_id.equals(myid)){
                name.setText(context.getString(R.string.workingOn,Name));
            }else {
                name.setText(context.getString(R.string.Sentby,Name));
            }
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);
            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(userImage).thumbnail(Glide.with(context).load(userImage_thumb))
                    .into(user);
        }

        public void setGig(String gig_desk, String gig_date, String gig_image) {
            desc.setText(gig_desk);
            if( gig_date != null) {
                date.setText(format.format(gig_date));
            }else{
                date.setText(format.format(new Date()));
            }
            if(gig_image !=null){
                RequestOptions placeholderOP = new RequestOptions();
                placeholderOP.placeholder(R.drawable.rounded_rectangle);
                Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(gig_image)
                        .into(user);
            }else{
                decImage.setVisibility(View.GONE);
                decImage.setEnabled(false);
            }
        }
    }
}


class RequestAdaptor extends RecyclerView.Adapter<RequestAdaptor.ViewHolder> {
    List<OpenGigs> gigsList;
    List<Users> usersList;
    public Context context;
    private FirebaseAuth auth;
    private  FirebaseFirestore firestore;
    Gig_lists_Fragment fragment;
    private String  myid;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

    public RequestAdaptor(List<OpenGigs> gigs_lists, List<Users> usersListl) {
        gigsList= gigs_lists;
        usersList = usersListl;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gig_request,parent, false);
        context = parent.getContext();
        auth =FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {


        myid = auth.getCurrentUser().getUid();
        String gig_image = gigsList.get(position).getGig_image();
        final String from_id = gigsList.get(position).getFrom_id();
        final String ref_id = gigsList.get(position).getRef_id();
        final String to_id = gigsList.get(position).getTo_id();
        String gig_desk = gigsList.get(position).getGig_description();
        final String gigId = gigsList.get(position).Userid;
        final String name = usersList.get(position).getName().get(0);
        String userImage_thumb = usersList.get(position).getThumb();
        String userImage = usersList.get(position).getImage();
        holder.setOwner(name, userImage_thumb, userImage, from_id);
        holder.setGig(gig_desk,gig_image);

        if(from_id.equals(myid)){
          // holder.cancel.setEnabled(false);
           //holder.cancel.setVisibility(View.GONE);
           holder.accept.setVisibility(View.GONE);
           holder.accept.setEnabled(false);
        }
        holder.user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String send_id;
                if (from_id.equals(myid)){
                    send_id = to_id;
                }else send_id = from_id;
                Intent thereAccount = new Intent(context, AnotherUserAccount.class);
                thereAccount.putExtra("UserId",send_id);
                context.startActivity(thereAccount);
            }
        });
       holder.accept.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               fragment.bar.setVisibility(View.VISIBLE);
               final Map<String, Object> GigMap = new HashMap<>();
               GigMap.put("start_time", FieldValue.serverTimestamp());
               GigMap.put("status", "ongoing");
               firestore.collection("Users").document(myid).collection("Gigs")
                       .document(gigId).update(GigMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful()) {
                           firestore.collection("Users")
                                   .document(from_id).collection("Gigs")
                                   .document(ref_id).update(GigMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   fragment.bar.setVisibility(View.INVISIBLE);
                                   gigsList.remove(position);
                                   usersList.remove(position);
                                   notifyItemRemoved(position);

                                   final String status = "Accepted Gig";
                                   Map<String, Object> mylikeMap = new HashMap<>();
                                   mylikeMap.put("status", status);
                                   mylikeMap.put("notId", myid);
                                   mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                   mylikeMap.put("postId",null);
                                   firestore.collection("Users/"+from_id+"/NotificationBox")
                                           .document(myid).set(mylikeMap)
                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                               @Override
                                               public void onComplete(@NonNull Task<Void> task) {
                                                   if (!task.isSuccessful()) {
                                                       Toast.makeText(context, "did not notify",
                                                               Toast.LENGTH_SHORT).show();
                                                   } else {
                                                       firestore.collection("Users").document(from_id)
                                                               .collection("Tokens")
                                                               .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                   @Override
                                                                   public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                       for (DocumentChange doc : value.getDocumentChanges()) {
                                                                           if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                               String token = doc.getDocument().getString("token");
                                                                               NotSender
                                                                                       .sendNotifications(context, token
                                                                                               , status, "Gig Start Alert");
                                                                           }
                                                                       }

                                                                   }
                                                               });

                                                       NotSender.Updatetoken();
                                                   }
                                               }

                                           });

                               }
                           });
                       } else {
                           Toast.makeText(context, "something went wrong, try again", Toast.LENGTH_SHORT).show();
                           fragment.bar.setVisibility(View.INVISIBLE);
                       }
                   }
               });


           }
       });
        holder.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.bar.setVisibility(View.VISIBLE);
                firestore.collection("Users").document(myid).collection("Gigs")
                        .document(gigId)
                        .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        final String send_id;
                        if (from_id.equals(myid)){
                            send_id = to_id;
                        }else send_id = from_id;

                        firestore.collection("Users")
                                .document(send_id).collection("Gigs")
                                .document(ref_id)
                                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                fragment.bar.setVisibility(View.INVISIBLE);
                                //not
                                final String status = "Canceled Gig";
                                Map<String, Object> mylikeMap = new HashMap<>();
                                mylikeMap.put("status", status);
                                mylikeMap.put("notId", myid);
                                mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                mylikeMap.put("postId",null);
                                firestore.collection("Users/"+send_id+"/NotificationBox")
                                        .document(myid).set(mylikeMap)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (!task.isSuccessful()) {
                                                    Toast.makeText(context, "did not notify",
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    firestore.collection("Users").document(send_id)
                                                            .collection("Tokens")
                                                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                    for (DocumentChange doc : value.getDocumentChanges()) {
                                                                        if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                            String token = doc.getDocument().getString("token");
                                                                            NotSender
                                                                                    .sendNotifications(context, token
                                                                                            , status, "Canceled Gig Alert");
                                                                        }
                                                                    }

                                                                }
                                                            });

                                                    NotSender.Updatetoken();
                                                }
                                            }

                                        });

                            }
                        });
                        gigsList.remove(position);
                        usersList.remove(position);
                        notifyItemRemoved(position);
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return gigsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView user;
        TextView name,gigdesc;
        Button accept,cancel;
        ImageView gig_photo;
        View v;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
            user = v.findViewById(R.id.circularUser);
            name = v.findViewById(R.id.gig_user);
            gigdesc = v.findViewById(R.id.gig_describe);
            accept = v.findViewById(R.id.accept_btn);
            cancel= v.findViewById(R.id.cancel_button);
            gig_photo = v.findViewById(R.id.imageView);

        }

        private void setOwner(String Name, String userImage_thumb, String userImage,String from_id) {
            if(from_id.equals(myid)){
                name.setText(context.getString(R.string.youRequsted,Name));
            }else {
                name.setText(context.getString(R.string.Requestedby,Name));
            }
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);
            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(userImage).thumbnail(Glide.with(context).load(userImage_thumb))
                    .into(user);
        }

        public void setGig(String gig_desk,String gig_image) {
            gigdesc.setText(gig_desk);

            if(gig_image !=null){
                RequestOptions placeholderOP = new RequestOptions();
                placeholderOP.placeholder(R.drawable.rounded_rectangle);
                Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(gig_image)
                        .into(user);
            }else{
                gig_photo.setVisibility(View.GONE);
                gig_photo.setEnabled(false);
            }
        }

    }
}