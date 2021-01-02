package com.example.thehustler.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.thehustler.Activities.MainActivity;
import com.example.thehustler.Adapter.NotificationAdapter;
import com.example.thehustler.Adapter.TrigRecyclerAdapter;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Notify;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.example.thehustler.Services.BadgeUpdater;
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

import javax.annotation.Nullable;


public class Notification_Fragment extends Fragment {


    private FirebaseFirestore firestore;
    private NotificationAdapter adapter;

    private RecyclerView NotificationView;
    private List<Users> usersList;
    private List<Notify> notifications_list;
    private FirebaseAuth auth;
    private String CurrentUID;
    private Query Notifications;
    private int counter = 0;
    LinearSmoothScroller smoothScroller;
    DocumentSnapshot lastVisible;


    public Notification_Fragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_notification_, container, false);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersList = new ArrayList<>();
        notifications_list= new ArrayList<>();
        NotificationView= v.findViewById(R.id.notification_Recycler);
        NotificationView.setHasFixedSize(true);

        adapter = new NotificationAdapter(notifications_list,usersList);
        NotificationView.setLayoutManager(new LinearLayoutManager(container.getContext()));
        NotificationView.setAdapter(adapter);

        CurrentUID = auth.getCurrentUser().getUid();

       smoothScroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        NotificationView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    LoadmorePosts();
                    //
                  //  if(check==3) {
                    //    check= 0;
                      //  Intent i = new Intent(getContext(), BadgeUpdater.class);
                        //i.putExtra("WHICH","H");
                        //getActivity().startService(i);
                   // }else {
                //        check += 1;
                    //}
                }
                if (dy > 0 && MainActivity.mainBottomNav.isShown()) {
                    MainActivity.mainBottomNav.setVisibility(View.GONE);
                } else if (dy < 0 ) {
                    MainActivity.mainBottomNav.setVisibility(View.VISIBLE);
                }

            }
        });

        Notifications = firestore.collection("Users/"+CurrentUID+"/NotificationBox")
                .orderBy("timestamp", Query.Direction.DESCENDING).limit(7);
        Notifications.addSnapshotListener( new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String postID = doc.getDocument().getId();
                            String seen = doc.getDocument().getString("mark");

                            final Notify notify = doc.getDocument().toObject(Notify.class).withID(postID);
                            final String notId = doc.getDocument().getString("notId");
                            firestore.collection("Users").document(notId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);
                                        usersList.add(users);
                                        notifications_list.add(notify);
                                        adapter.notifyDataSetChanged();
                                    }

                                }
                            });
                            if(seen.equalsIgnoreCase("unseen")){
                                counter++;
                            }
                        }
                    }
                    if(counter>0) {
                        MainActivity m = new MainActivity();
                        m.Updatebadge(counter, 2);
                        //counter = 0;
                    }


                }else {
                    Toast.makeText(getContext(), "nothing here", Toast.LENGTH_LONG).show();
                }
            }

        });

        return v;
    }

    private void LoadmorePosts() {
        Notifications = firestore.collection("Users/"+CurrentUID+"/NotificationBox")
                .orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(7);
        Notifications.addSnapshotListener( new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String postID = doc.getDocument().getId();
                            String seen = doc.getDocument().getString("mark");

                            final Notify notify = doc.getDocument().toObject(Notify.class).withID(postID);
                            final String notId = doc.getDocument().getString("notId");
                            firestore.collection("Users").document(notId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);
                                        usersList.add(users);
                                        notifications_list.add(notify);
                                        adapter.notifyDataSetChanged();
                                    }

                                }
                            });
                            if(seen.equalsIgnoreCase("unseen")){
                                counter++;
                            }
                        }
                    }
                    if(counter>0) {
                        MainActivity m = new MainActivity();
                        m.Updatebadge(counter, 2);
                       // counter = 0;
                    }


                }else {
                    Toast.makeText(getContext(), "nothing here", Toast.LENGTH_LONG).show();
                }
            }

        });
    }
    public void backup() {
        if (NotificationView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) NotificationView.getLayoutManager();
            smoothScroller.setTargetPosition(0);
            layoutManager.startSmoothScroll(smoothScroller);
            // layoutManager.scrollToPositionWithOffset(0,0);

        }
    }


}
