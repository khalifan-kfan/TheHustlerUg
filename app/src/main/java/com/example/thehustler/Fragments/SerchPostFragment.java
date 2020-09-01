package com.example.thehustler.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Activities.AnotherUserAccount;
import com.example.thehustler.Activities.CommentActivity;
import com.example.thehustler.Activities.MainActivity;
import com.example.thehustler.Activities.PostActivity;
import com.example.thehustler.Adapter.ImageAdp;
import com.example.thehustler.Adapter.TrigRecyclerAdapter;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.example.thehustler.classes.Update;
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
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;


public class SerchPostFragment extends Fragment implements Update {
    private String  Sterm;
    private RecyclerView views;
    private PostAdapter adapter;
    private List<Blogpost> Postlist;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<Users> users_lists;
    private DocumentSnapshot lastVisible;
    private Boolean firstload = true;
    private ProgressBar prog;
    private Boolean wordchange = false;


    public SerchPostFragment() {
        // Required empty public constructor
    }


    public static SerchPostFragment newInstance() {
        SerchPostFragment fragment = new SerchPostFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_serch_post, container, false);
        views = v.findViewById(R.id.Recyclerspost);
        prog = v.findViewById(R.id.prog_Seach);
        users_lists = new ArrayList<>();
        Postlist = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        adapter = new PostAdapter(Postlist,users_lists);
       views.setLayoutManager(new LinearLayoutManager(container.getContext()));
        views.setAdapter(adapter);

        return v;
    }

    private void drive(){
        if ((Sterm.length()>0) ){
            firestore = FirebaseFirestore.getInstance();
            views.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    Boolean atBottom = !recyclerView.canScrollVertically(-1);
                    if (atBottom) {
                        prog.setVisibility(View.VISIBLE);
                        loadmore();
                    }
                }
            });
            if(wordchange) {
                wordchange= false;
                Postlist.clear();
                users_lists.clear();
                adapter.notifyItemRangeRemoved(0,users_lists.size());

            }
            prog.setVisibility(View.VISIBLE);
            fistfive();
        }else{
            Toast.makeText(getContext(), "no searches yet", Toast.LENGTH_LONG).show();
        }

    }

    private void fistfive() {
        Query Firstquery = firestore.collection("Posts")
                .orderBy("description").startAt(Sterm)
                .endAt(Sterm+"\uf8ff")
                .limit(3);
        Firstquery.addSnapshotListener( new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (!queryDocumentSnapshots.isEmpty()) {
                    prog.setVisibility(View.GONE);
                    if (firstload) {
                        lastVisible = queryDocumentSnapshots.getDocuments().get(queryDocumentSnapshots.size() - 1);
                        Postlist.clear();
                        users_lists.clear();
                    }
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String postID = doc.getDocument().getId();

                            final Blogpost blogpost = doc.getDocument().toObject(Blogpost.class).withID(postID);
                            String bloguser_id = doc.getDocument().getString("user_id");
                            firestore.collection("Users").document(bloguser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);
                                        if(blogpost.getDescription().contains(Sterm.toLowerCase())){
                                            if (firstload) {
                                                users_lists.add(users);
                                                Postlist.add(blogpost);

                                            } else {
                                                users_lists.add(0, users);
                                                Postlist.add(0, blogpost);
                                            }
                                            adapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }
                    firstload = false;

                }else {
                    prog.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "no posts found", Toast.LENGTH_LONG).show();
                }
            }

        });
        // registration.remove();

    }

    @Override
    public void TextChange(String seach) {
        Sterm = seach;
        wordchange = true;
        drive();
    }
    public void loadmore(){
        Query Nextquery = firestore.collection("Posts")
                .orderBy("description").startAfter(lastVisible)
                .endAt(Sterm+"\uf8ff")
                .limit(3);

        Nextquery.addSnapshotListener( new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                prog.setVisibility(View.GONE);
                if (!queryDocumentSnapshots.isEmpty()) {

                    lastVisible = queryDocumentSnapshots.getDocuments()
                            .get(queryDocumentSnapshots.size() - 1);
                    for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {

                            String postID = doc.getDocument().getId();
                            final Blogpost blogpost = doc.getDocument().toObject(Blogpost.class).withID(postID);
                            String bloguser_id = doc.getDocument().getString("user_id");
                            firestore.collection("Users").document(bloguser_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Users users = task.getResult().toObject(Users.class);
                                        if(blogpost.getDescription().contains(Sterm)){
                                        users_lists.add(users);
                                        Postlist.add(blogpost);
                                       adapter.notifyDataSetChanged();
                                    }}
                                }
                            });
                        }
                    }

                }
            }
        });
    }

}
class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder>{
    List<Blogpost> postlist;
    List<Users> users_lists;
    public Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    public PostAdapter(List<Blogpost> postlist, List<Users> users_lists) {
        this.postlist = postlist;
        this.users_lists =users_lists;

    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_posts,parent, false);
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostAdapter.ViewHolder holder, final int position) {

            holder.setIsRecyclable(false);

            String desc_data = postlist.get(position).getDescription();
            holder.setdescriptiontext(desc_data);

            final String CurrentUser = auth.getCurrentUser().getUid();
            final String post_Id = postlist.get(position).postId;


            if ((postlist.get(position).getImage_url())!=null && (postlist.get(position).getPost_image_thumb())!=null){

                List<String> Image_uri = new ArrayList<>();
                List<String> thumb_uri = new ArrayList<>();
                int i = 0;

                while (i < postlist.get(position).getImage_url().size()) {
                    Image_uri.add(i, postlist.get(position).getImage_url().get(i));
                    thumb_uri.add(i, postlist.get(position).getPost_image_thumb().get(i));
                    i++;
                }
                holder.setBlogimage(Image_uri, thumb_uri);

            }else {

                holder.phots.setEnabled(false);
                holder.phots.setVisibility(View.GONE);
            }

            final String  user_id = postlist.get(position).getUser_id();
            final String name = users_lists.get(position).getName().get(0);
            String image = users_lists.get(position).getImage();
            String thumb = users_lists.get(position).getThumb();
            holder.setOwner(name,image,thumb);


            if(user_id.equals(CurrentUser)) {
                holder.popbut.setEnabled(true);
                holder.popbut.setVisibility(View.VISIBLE);
                holder.ownerimage.setEnabled(false);
            }


            try {
                if(postlist.get(position).getTimeStamp() != null) {
                    long milliseconds = postlist.get(position).getTimeStamp().getTime();
                    String dateString = DateFormat.format("d/MM/yyyy", new Date(milliseconds)).toString();
                    holder.setTime(dateString);
                }else holder.setTime("0/0/0");
            } catch (Exception e){
                Toast.makeText(context,"Exception"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
            //likes count
            firestore.collection("Posts/"+post_Id+"/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@androidx.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                    if(!queryDocumentSnapshots.isEmpty()){
                        int count = queryDocumentSnapshots.size();
                        holder.updateLikeCount(count);

                    }else{
                        holder.updateLikeCount(0);
                    }
                }
            });
            //comments counter
            firestore.collection("Posts/"+post_Id+"/Answers").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                    if(!value.isEmpty()){
                        int count = value.size();
                        holder.commentCount(count);

                    }else{
                        holder.commentCount(0);
                    }

                }
            });


            //get likes
            firestore.collection("Posts/"+post_Id+"/likes").document(CurrentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onEvent(@androidx.annotation.Nullable DocumentSnapshot documentSnapshot, @androidx.annotation.Nullable FirebaseFirestoreException e) {
                    if(documentSnapshot.exists()){
                        holder.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_nfava));
                    }else{
                        holder.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_fava));
                    }
                }
            });
/*
        holder.phots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToBlogPostFragment(post_Id, user_id);
            }

        });
*/
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToBlogPostFragment(post_Id, user_id);
                }
            });

            holder.likebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    firestore.collection("Posts/"+post_Id+"/likes").document(CurrentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().exists()) {
                                    Map<String, Object> likeMap = new HashMap<>();
                                    likeMap.put("timestamp", FieldValue.serverTimestamp());
                                    firestore.collection("Posts/"+post_Id+"/likes").document(CurrentUser).set(likeMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (!user_id.equals(CurrentUser)) {
                                                        final String status = "like";
                                                        Map<String, Object> mylikeMap = new HashMap<>();
                                                        mylikeMap.put("status", status);
                                                        mylikeMap.put("notId", CurrentUser);
                                                        mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                        mylikeMap.put("postId", post_Id);
                                                        firestore.collection("Users/"+user_id+"/NotificationBox").document(CurrentUser).set(mylikeMap)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (!task.isSuccessful()) {
                                                                            Toast.makeText(context, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        firestore.collection("Users").document(user_id)
                                                                                .collection("Tokens")
                                                                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                    @Override
                                                                                    public void onEvent(@androidx.annotation.Nullable QuerySnapshot value, @androidx.annotation.Nullable FirebaseFirestoreException error) {
                                                                                        for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                                String token = doc.getDocument().getString("token");
                                                                                                NotSender
                                                                                                        .sendNotifications(context,token
                                                                                                                ,status,"New approval Alert");
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
                                    firestore.collection("Posts/"+post_Id+"/likes").document(CurrentUser).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (!user_id.equals(CurrentUser)) {
                                                        firestore.collection("Users/" + user_id + "/NotificationBox")
                                                                .document(CurrentUser).delete()
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


            holder.answersbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent answerintent =  new Intent(context, CommentActivity.class);
                    answerintent.putExtra("post_Id",post_Id);
                    answerintent.putExtra("postownerId",user_id);
                    context.startActivities(new Intent[]{answerintent});

                }
            });
            holder.popbut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.pop(user_id,postlist,users_lists,position);
                }
            });

            holder.ownerimage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent thereAccount = new Intent(context, AnotherUserAccount.class);
                    thereAccount.putExtra("UserId",user_id);
                    context.startActivity(thereAccount);
                }
            });

        }
    @Override
    public int getItemCount() {
        return postlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView description;
        private View v;
        private ImageView likebtn,answersbtn,popbut;
        private TextView postdate,likecount, commentcounter;
        private TextView postOwnerName;
        private CircleImageView ownerimage;
        private CardView cardView;
        private ViewPager2 phots;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            phots = v.findViewById(R.id.pageview);
            cardView = v.findViewById(R.id.Card2);
            likebtn = v.findViewById(R.id.likeuserz2);
            popbut = v.findViewById(R.id.popimagedell);
            answersbtn = v.findViewById(R.id.comentbtn);
            postOwnerName = v.findViewById(R.id.Post_user);
            ownerimage =v.findViewById(R.id.circularUser);
            postdate = v.findViewById(R.id.datePost);

        }
        public void setdescriptiontext(String text){
            description = v.findViewById(R.id.postdescribe);
            description.setText(text);
        }

        public void setBlogimage(List<String> downloadUri, List<String> thumb_uri){


            phots.setAdapter( new ImageAdp(downloadUri,thumb_uri,phots));

            phots.setClipToPadding(false);
            phots.setClipChildren(false);
            phots.setOffscreenPageLimit(3);
            phots.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

            CompositePageTransformer cpt = new CompositePageTransformer();
            cpt.addTransformer(new MarginPageTransformer(40));
            cpt.addTransformer((new ViewPager2.PageTransformer() {
                @Override
                public void transformPage(@NonNull View page, float position) {
                    float r = 1-Math.abs(position);
                    page.setScaleY(0.75F+r*0.15F);
                }

            }
            ));
            phots.setPageTransformer(cpt);
            //RequestOptions placRO = new RequestOptions();
            //placRO.placeholder(R.drawable.ic_post);
            // Glide.with(context).applyDefaultRequestOptions(placRO).load(downloadUri).thumbnail(Glide.with(context).load(thumb_uri))
            //       .into(blogimage);
        }

        public void setTime(String date){
            postdate.setText(date);
        }

        public void setOwner(String name,String image,String thumb){

            postOwnerName.setText(name);
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);

            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(image).thumbnail(Glide.with(context).load(thumb))
                    .into(ownerimage);
        }
        public  void  updateLikeCount(int count){
            likecount = v.findViewById(R.id.likescount23);

            if (count > 0)
                likecount.setText(Integer.toString(count));
            else
                likecount.setText(" ");
        }
        public void commentCount(int count){
            commentcounter = v.findViewById(R.id.Comentcount2);

            if(count>0)
                commentcounter.setText(Integer.toString(count));
            else
                commentcounter.setText(" ");

        }

        public  void pop(final String user_id, final List<Blogpost> postlist, final List<Users> usersList, final int position){
            PopupMenu popupMenu = new PopupMenu(context,popbut);
            popupMenu.getMenuInflater().inflate(R.menu.popdown, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    firestore.collection("Posts").document(user_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            postlist.remove(position);
                            usersList.remove(position);
                            Toast.makeText(context, "deleteted....",Toast.LENGTH_SHORT).show();
                        }
                    });

                    return true;
                }
            });
            popupMenu.show();
            notifyItemRemoved(position);
        }

    }
    public void goToBlogPostFragment(String post_Id, String user_id) {
        Intent post = new Intent(context, PostActivity.class);
        post.putExtra("blogPostId", post_Id);
        post.putExtra("UserId", user_id);
        context.startActivity(post);

    }

}