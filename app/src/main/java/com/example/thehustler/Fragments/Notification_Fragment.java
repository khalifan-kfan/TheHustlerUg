package com.example.thehustler.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.thehustler.Adapter.NotificationAdapter;
import com.example.thehustler.Adapter.TrigRecyclerAdapter;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Notify;
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

import javax.annotation.Nullable;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Notification_Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Notification_Fragment extends Fragment {


    private FirebaseFirestore firestore;
    private NotificationAdapter adapter;

    private RecyclerView NotificationView;
    private List<Users> usersList;
    private List<Notify> notifications_list;
    private FirebaseAuth auth;
    private String CurrentUID;
    private Query Notifications;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Notification_Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Notification_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Notification_Fragment newInstance(String param1, String param2) {
        Notification_Fragment fragment = new Notification_Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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

        Notifications = firestore.collection("Users/"+CurrentUID+"/NotificationBox")
                .orderBy("timestamp", Query.Direction.DESCENDING);
        Notifications.addSnapshotListener( new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {

                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String postID = doc.getDocument().getId();
                            final Notify notify = doc.getDocument().toObject(Notify.class);
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

                        }
                    }


                }else {
                    Toast.makeText(getContext(), "nothing here", Toast.LENGTH_LONG).show();
                }
            }

        });

        return v;
    }


}
