package com.example.thehustler.Fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thehustler.Adapter.TrigRecyclerAdapter;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.OpenGigs;
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

import java.util.ArrayList;
import java.util.List;

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
                            String bloguser_id = doc.getDocument().getString("from_id");
                            firestore.collection("Users").document(bloguser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {



    }

    @Override
    public int getItemCount() {
        return gigsList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        CircleImageView user;
        TextView byName,post_date,description,interests;
        ImageView star,descImage;
        View v;

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
        }
    }
}



class HistoryAdaptor extends RecyclerView.Adapter {
    public HistoryAdaptor(List<OpenGigs> gigs_lists, List<Users> usersListl) {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
class OngoingAdaptor extends RecyclerView.Adapter {
    public OngoingAdaptor(List<OpenGigs> gigs_lists, List<Users> usersListl) {

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
class RequestAdaptor extends RecyclerView.Adapter {
    public RequestAdaptor(List<OpenGigs> gigs_lists, List<Users> usersListl) {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}