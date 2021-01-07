package com.example.thehustler.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.thehustler.Adapter.ChatListsAdapter;

import com.example.thehustler.Model.Chats;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView chatview;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    ChatListsAdapter adapter;
    List<Chats>chatsList;
    List<Users> chatters_list;




    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v =inflater.inflate(R.layout.fragment_chats, container, false);

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        chatview = v.findViewById(R.id.chatsView);
        chatsList = new ArrayList<>();
        chatters_list = new ArrayList<>();
        chatview.setHasFixedSize(true);

        adapter = new ChatListsAdapter(chatters_list,chatsList);
        chatview.setLayoutManager(new LinearLayoutManager(container.getContext()));
        chatview.setAdapter(adapter);

        final String curtID = auth.getCurrentUser().getUid();


        Query IdQuery = firestore.collection("Users/"+curtID+"/Chats")
                .orderBy("latest_update",Query.Direction.DESCENDING);

        IdQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(!value.isEmpty()){
                    for (DocumentChange doc : value.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            final String usId = doc.getDocument().getId();


                            final Chats chats = doc.getDocument().toObject(Chats.class);
                            final String Chatuser_id = doc.getDocument().getString("ChatterId");
                            DocumentReference userDocRef = firestore.collection("Users").document(Chatuser_id);
                            userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        Users users = documentSnapshot.toObject(Users.class).withID(Chatuser_id);
                                        chatters_list.add(users);
                                        chatsList.add(chats);
                                        adapter.notifyDataSetChanged();
                                    } else {
                                        String e = task.getException().getMessage();
                                        Toast.makeText(getContext(),e+"failed to get users",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        }
                    }
                    Collections.sort(chatsList, new Comparator<Chats>() {
                        @Override
                        public int compare(Chats o1, Chats o2) {
                            return o2.getLatest_update().compareTo(o1.getLatest_update());
                        }
                    });

                }else {
                    Toast.makeText(getContext(),"no chats",Toast.LENGTH_LONG).show();
                }

            }
        });


        return v;
    }
}