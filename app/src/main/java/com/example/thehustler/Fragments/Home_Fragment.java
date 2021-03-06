package com.example.thehustler.Fragments;



import android.content.Intent;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thehustler.Activities.MainActivity;
import com.example.thehustler.Adapter.TrigRecyclerAdapter;
import com.example.thehustler.Model.Blogpost;
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
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;





public class Home_Fragment extends Fragment {
   RecyclerView postsView;

  private List<Blogpost> Postlist;
  private FirebaseFirestore firestore;
  private TrigRecyclerAdapter trigRecyclerAdaptor;

  private DocumentSnapshot lastVisible;

  private  List<Users> usersList;
  RecyclerView.SmoothScroller smoothScroller;
  private FirebaseAuth auth;
  private long countposts;
  Date lastTym;
  private Boolean loadFirst= true;
  private int check=0;

  public Home_Fragment() {
    // Required empty public constructor
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(
            R.layout.fragment_home_, container, false);


    auth = FirebaseAuth.getInstance();
    firestore = FirebaseFirestore.getInstance();
    usersList = new ArrayList<>();
    Postlist = new ArrayList<>();
    postsView = view.findViewById(R.id.posts__);
    postsView.setHasFixedSize(true);
    trigRecyclerAdaptor = new TrigRecyclerAdapter(Postlist);
    postsView.setLayoutManager(new LinearLayoutManager(container.getContext()));
    postsView.setAdapter(trigRecyclerAdaptor);

    if (auth.getCurrentUser() != null) {
      firestore = FirebaseFirestore.getInstance();

       smoothScroller = new LinearSmoothScroller(getContext()){
        @Override protected int getVerticalSnapPreference(){
          return LinearSmoothScroller.SNAP_TO_START;
        }
      };

      postsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
          super.onScrolled(recyclerView, dx, dy);

          Boolean atBottom = !recyclerView.canScrollVertically(-1);
          if (atBottom) {
            LoadmorePosts();
            //
            if(check==3) {
              check= 0;
              Intent i = new Intent(getContext(), BadgeUpdater.class);
              i.putExtra("WHICH","H");
              getActivity().startService(i);
            }else {
              check += 1;
            }
          }
          if (dy > 0 && MainActivity.mainBottomNav.isShown()) {
            MainActivity.mainBottomNav.setVisibility(View.GONE);
          } else if (dy < 0 ) {
            MainActivity.mainBottomNav.setVisibility(View.VISIBLE);
          }

        }
      });

      Query Firstquery = firestore.collection("Posts")
              .orderBy("timeStamp", Query.Direction.DESCENDING)
              .limit(3);
      Firstquery.addSnapshotListener( new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

          if (!queryDocumentSnapshots.isEmpty()) {
            if (loadFirst) {
              lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
              Postlist.clear();
              usersList.clear();
            }
            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
              if (doc.getType() == DocumentChange.Type.ADDED) {
                String postID = doc.getDocument().getId();
                String bloguser_id;
                final Blogpost blogpost = doc.getDocument().toObject(Blogpost.class).withID(postID);
                if (loadFirst) {
                  Postlist.add(blogpost);
                } else {
                  Postlist.add(0, blogpost);
                }

                trigRecyclerAdaptor.notifyDataSetChanged();

               /* if(doc.getDocument().getString("re_postId")==null) {
                  bloguser_id = doc.getDocument().getString("user_id");
                }else {
                  bloguser_id = doc.getDocument().getString("re_postId");
                }
                firestore.collection("Users").document(bloguser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                  @Override
                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                      Users users = task.getResult().toObject(Users.class);

                      if (loadFirst) {
                        usersList.add(users);
                        Postlist.add(blogpost);
                      } else {
                        usersList.add(0, users);
                        Postlist.add(0, blogpost);
                      }

                      trigRecyclerAdaptor.notifyDataSetChanged();
                    }

                  }
                });*/

              }
            }
            loadFirst = false;

          }else {
            Toast.makeText(getContext(), "nothing here", Toast.LENGTH_LONG).show();
          }
        }

      });
      // registration.remove();
    }

    return view ;
  }



  public void LoadmorePosts() {
    if (auth.getCurrentUser() != null) {
      Query Nextquery = firestore.collection("Posts")
              .orderBy("timeStamp", Query.Direction.DESCENDING)
              .startAfter(lastVisible)
              .limit(3);

      Nextquery.addSnapshotListener( new EventListener<QuerySnapshot>() {
        @Override
        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
          if (!queryDocumentSnapshots.isEmpty()) {


            lastVisible = queryDocumentSnapshots.getDocuments()
                    .get(queryDocumentSnapshots.size() - 1);


            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

              if (doc.getType() == DocumentChange.Type.ADDED) {
                String bloguser_id;
                String postID = doc.getDocument().getId();
                final Blogpost blogpost = doc.getDocument().toObject(Blogpost.class).withID(postID);
                Postlist.add(blogpost);
                trigRecyclerAdaptor.notifyDataSetChanged();

              /*  if(doc.getDocument().getString("re_postId")==null) {
                  bloguser_id = doc.getDocument().getString("user_id");
                }else {
                  bloguser_id = doc.getDocument().getString("re_postId");
                }
                firestore.collection("Users").document(bloguser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                  @Override
                  public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                      Users users = task.getResult().toObject(Users.class);

                      usersList.add(users);
                      Postlist.add(blogpost);
                      trigRecyclerAdaptor.notifyDataSetChanged();
                    }

                  }
                });*/
              }
            }
          }
        }
      });
      // registration.remove();
    }
  }

  public void backup() {
    if (postsView != null) {
      LinearLayoutManager layoutManager = (LinearLayoutManager) postsView.getLayoutManager();
      smoothScroller.setTargetPosition(0);
      layoutManager.startSmoothScroll(smoothScroller);
       // layoutManager.scrollToPositionWithOffset(0,0);

    }


  }
}
