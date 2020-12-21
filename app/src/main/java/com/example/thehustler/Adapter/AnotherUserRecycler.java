package com.example.thehustler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.thehustler.Activities.AnotherUserAccount;
import com.example.thehustler.Activities.MainActivity;
import com.example.thehustler.Activities.PostActivity;
import com.example.thehustler.Activities.posting;
import com.example.thehustler.Fragments.BottomSheetDialog;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.text.format.DateFormat.format;

public class AnotherUserRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static List<Blogpost> userBlogList;
    private static FirebaseAuth auth;
    private static FirebaseFirestore firestore;

    public static Context context;
    static int original = 1;
    static int re_post = 2;

    public AnotherUserRecycler(List<Blogpost> userBlogList) {
        this.userBlogList = userBlogList;

    }

    @Override
    public int getItemViewType(int position) {
        if(userBlogList.get(position).getRe_postId()==null){
            return original;
        }else{
            return re_post;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();
        if(viewType ==original){
            view = LayoutInflater.from(context)
                    .inflate(R.layout.user_posts, parent, false);
            return new ViewHolder1(view);
        }else{
            view = LayoutInflater.from(context)
                    .inflate(R.layout.re_post_list, parent, false);
            return new ViewHolder2(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        //edits
        holder.setIsRecyclable(false);
        switch (holder.getItemViewType()) {
            case 1: {
                final  ViewHolder1 holder1 = (ViewHolder1) holder;
                String desc_data =userBlogList.get(position).getDescription();
                holder1.setdescriptiontext(desc_data);

                final String CurrentUser = auth.getCurrentUser().getUid();
                final String post_Id =userBlogList.get(position).postId;

                if ((userBlogList.get(position).getImage_url())!=null && (userBlogList.get(position).getPost_image_thumb())!=null){

                    List<String> Image_uri = new ArrayList<>();
                    List<String> thumb_uri = new ArrayList<>();
                    int i = 0;

                    while (i < userBlogList.get(position).getImage_url().size()) {
                        Image_uri.add(i, userBlogList.get(position).getImage_url().get(i));
                        thumb_uri.add(i, userBlogList.get(position).getPost_image_thumb().get(i));
                        i++;
                    }
                    holder1.setBlogimage(Image_uri, thumb_uri);

                }else {
                    holder1.phots.setEnabled(false);
                    holder1.phots.setVisibility(View.GONE);
                }
                final String  user_id = userBlogList.get(position).getUser_id();

                DocumentReference userDocRef = firestore.collection("Users").document(user_id);
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            DocumentSnapshot documentSnapshot = task.getResult();
                            Users user = documentSnapshot.toObject(Users.class);
                            String Name = user.getName().get(0);
                            String profileImageUrl = user.getImage();
                            String profilethmb = user.getThumb();
                            holder1.setOwner(Name, profileImageUrl, profilethmb);
                        } else {
                            String e = task.getException().getMessage();
                            Toast.makeText(context,e+"failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                holder1.popbut.setEnabled(true);
                holder1.popbut.setVisibility(View.VISIBLE);

                try {
                    if(userBlogList.get(position).getTimeStamp() != null) {
                        long milliseconds = userBlogList.get(position).getTimeStamp().getTime();
                        String dateString = DateFormat.format("d/MM/yyyy", new Date(milliseconds)).toString();
                        holder1.setTime(dateString);
                    }else holder1.setTime("0/0/0");
                } catch (Exception e){
                    Toast.makeText(context,"Exception"+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                //likes count
                firestore.collection("Posts/"+post_Id+"/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            int count = queryDocumentSnapshots.size();
                            holder1.updateLikeCount(count);

                        }else{
                            holder1.updateLikeCount(0);
                        }
                    }
                });

                //get likes
                firestore.collection("Posts/"+post_Id+"/likes").document(CurrentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(documentSnapshot.exists()){
                            holder1.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_nfava));
                        }else{
                            holder1.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_fava));
                        }
                    }
                });

                holder1.likebtn.setOnClickListener(new View.OnClickListener() {
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
                                                            firestore.collection("Users/"+user_id+"/NotificationBox")
                                                                    .add(mylikeMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                            if (!task.isSuccessful()) {
                                                                                Toast.makeText(context, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            //send notification now
                                                                            firestore.collection("Users").document(user_id)
                                                                                    .collection("Tokens")
                                                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
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
                                        firestore.collection("Posts/"+post_Id+"/likes").document(CurrentUser).delete();

                                    }
                                }else {
                                    Toast.makeText(context, "your offline", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                holder1.repost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BottomSheetDialog bottomSheetDialog = BottomSheetDialog.newInstance(post_Id, position,3);
                        bottomSheetDialog.show(((MainActivity) context).getSupportFragmentManager(), "bottom Sheet");
                    }
                });
                firestore.collection("Posts/" + post_Id + "/Reposts").whereEqualTo("repost_id", CurrentUser)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {

                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (value.isEmpty()) {
                                    holder1.repost.setImageResource(R.drawable.ic_repost);
                                } else {
                                    holder1.repost.setImageResource(R.drawable.ic_repost_no);

                                }
                            }
                        });
                firestore.collection("Posts/" + post_Id + "/Reposts").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()) {
                            int count = value.size();
                            holder1.repostCounter(count);
                        } else {
                            holder1.repostCounter(0);
                        }
                    }
                });

                holder1.answersbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent answerintent =  new Intent(context, PostActivity.class);
                        answerintent.putExtra("blogPostId",post_Id);
                        answerintent.putExtra("UserId",user_id);
                        context.startActivities(new Intent[]{answerintent});

                    }
                });

                break;
            }
            case 2: {
                final ViewHolder2 holder2 = (ViewHolder2) holder;
                //if desc is not null otherwise view gone
                String desc_data = userBlogList.get(position).getRe_post_desc();
                String desc2 = userBlogList.get(position).getDescription();
                holder2.setdescriptiontext(desc2,desc_data);
                if(desc_data==null){
                    holder2.description2.setVisibility(View.GONE);
                }
                final String CurrentUser = auth.getCurrentUser().getUid();
                final String post_Id = userBlogList.get(position).postId;

                if ((userBlogList.get(position).getRe_image_url()) != null && (userBlogList.get(position).getRe_post_image_thumb()) != null) {
                    List<String> Image_uri = new ArrayList<>();
                    List<String> thumb_uri = new ArrayList<>();
                    int i = 0;
                    while (i < userBlogList.get(position).getRe_post_image_thumb().size()) {
                        Image_uri.add(i, userBlogList.get(position).getRe_image_url().get(i));
                        thumb_uri.add(i, userBlogList.get(position).getRe_post_image_thumb().get(i));
                        i++;
                    }
                    holder2.setBlogimage(Image_uri, thumb_uri,1);
                } else {
                    holder2.phots.setEnabled(false);
                    holder2.phots.setVisibility(View.GONE);
                }
                if ((userBlogList.get(position).getImage_url()) != null && (userBlogList.get(position).getPost_image_thumb()) != null) {
                    List<String> Image_uri = new ArrayList<>();
                    List<String> thumb_uri = new ArrayList<>();
                    int i = 0;
                    while (i < userBlogList.get(position).getImage_url().size()) {
                        Image_uri.add(i, userBlogList.get(position).getImage_url().get(i));
                        thumb_uri.add(i, userBlogList.get(position).getPost_image_thumb().get(i));
                        i++;
                    }
                    holder2.setBlogimage(Image_uri, thumb_uri,2);
                } else {
                    holder2.photos1.setEnabled(false);
                    holder2.photos1.setVisibility(View.GONE);
                }

                final String user_idr = userBlogList.get(position).getRe_postId();
                final String user_id = userBlogList.get(position).getUser_id();

                final DocumentReference userDocRef = firestore.collection("Users").document(user_idr);
                userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            Users user = documentSnapshot.toObject(Users.class);
                            String Name = user.getName().get(0);
                            String profileImageUrl = user.getImage();
                            String profilethmb = user.getThumb();
                            holder2.setOwner(Name, profileImageUrl, profilethmb);
                        } else {
                            String e = task.getException().getMessage();
                            Toast.makeText(context,e+"failed",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                holder2.popbut.setEnabled(true);
                holder2.popbut.setVisibility(View.VISIBLE);
                //set second user info with there id
                holder2.seconduser(user_id);

                if(userBlogList.get(position).getRe_postId().equals(CurrentUser)) {
                    holder2.popbut.setEnabled(true);
                    holder2.popbut.setVisibility(View.VISIBLE);
                    holder2.ownerimage.setEnabled(false);
                }
                try {
                    if (userBlogList.get(position).getTimeStamp()!= null) {
                        long milliseconds = userBlogList.get(position).getTimeStamp().getTime();
                        String dateString = format("d/MM/yyyy", new Date(milliseconds)).toString();
                        holder2.setTime(dateString);
                    } else holder2.setTime("0/0/0");
                } catch (Exception e) {
                    Toast.makeText(context, "Exception" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }


                SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

                if(userBlogList.get(position).getRe_timeStamp() != null) {
                    holder2.date1.setText(format.format(userBlogList.get(position).getRe_timeStamp()));
                }else{
                    holder2.date1.setText(format.format(new Date()));
                }


                //likes count
                firestore.collection("Posts/"+post_Id+"/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            int count = queryDocumentSnapshots.size();
                            holder2.updateLikeCount(count);

                        } else {
                            holder2.updateLikeCount(0);
                        }
                    }
                });
                //comments counter
                firestore.collection("Posts/"+post_Id+"/Answers").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()) {
                            int count = value.size();
                            holder2.commentCount(count);
                        } else {
                            holder2.commentCount(0);
                        }
                    }
                });
                firestore.collection("Posts/" + post_Id + "/Reposts").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()) {
                            int count = value.size();
                            holder2.repostCounter(count);
                        } else {
                            holder2.repostCounter(0);
                        }
                    }
                });


                //get likes
                firestore.collection("Posts/" + post_Id + "/likes").document(CurrentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (documentSnapshot.exists()) {
                            holder2.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_nfava));
                        } else {
                            holder2.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_fava));
                        }
                    }
                });

                firestore.collection("Posts/"+post_Id+"/Reposts").whereEqualTo("repost_id", CurrentUser)
                        .addSnapshotListener(new EventListener<QuerySnapshot>() {

                            @Override
                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                if (value.isEmpty()) {
                                    holder2.repost.setImageResource(R.drawable.ic_repost);
                                } else {
                                    holder2.repost.setImageResource(R.drawable.ic_repost_no);

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
                holder2.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       goToBlogPostFragment(post_Id, user_id);
                    }
                });

                holder2.likebtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        firestore.collection("Posts/" + post_Id + "/likes").document(CurrentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (!task.getResult().exists()) {
                                        Map<String, Object> likeMap = new HashMap<>();
                                        likeMap.put("timestamp", FieldValue.serverTimestamp());
                                        firestore.collection("Posts/" + post_Id + "/likes").document(CurrentUser).set(likeMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (!userBlogList.get(position).getRe_postId().equals(CurrentUser)) {
                                                            final String status = "like";
                                                            Map<String, Object> mylikeMap = new HashMap<>();
                                                            mylikeMap.put("status", status);
                                                            mylikeMap.put("notId", CurrentUser);
                                                            mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                            mylikeMap.put("postId", post_Id);
                                                            firestore.collection("Users/" +userBlogList.get(position).getRe_postId() + "/NotificationBox").add(mylikeMap)
                                                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                            if (!task.isSuccessful()) {
                                                                                Toast.makeText(context, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                            //send notification now
                                                                            firestore.collection("Users").document(userBlogList.get(position).getRe_postId())
                                                                                    .collection("Tokens")
                                                                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                        @Override
                                                                                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                            for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                                    String token = doc.getDocument().getString("token");
                                                                                                    NotSender
                                                                                                            .sendNotifications(context, token
                                                                                                                    , status, "New Like Alert");
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
                                        firestore.collection("Posts/" + post_Id + "/likes").document(CurrentUser).delete();

                                    }
                                } else {
                                    Toast.makeText(context, "your offline", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });


                holder2.answersbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //edits maybe
                        Intent answerintent = new Intent(context, PostActivity.class);
                        answerintent.putExtra("blogPostId", post_Id);
                        answerintent.putExtra("UserId", user_id);
                        context.startActivities(new Intent[]{answerintent});

                    }
                });

                holder2.repost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BottomSheetDialog bottomSheetDialog = BottomSheetDialog.newInstance(post_Id, position,3);
                        bottomSheetDialog.show(((MainActivity) context).getSupportFragmentManager(), "bottom Sheet");
                    }
                });



                holder2.ownerimage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent thereAccount = new Intent(context, AnotherUserAccount.class);
                        thereAccount.putExtra("UserId", user_id);
                        context.startActivity(thereAccount);
                    }
                });

            }
            break;
            default:
                throw new IllegalStateException("Unexpected value: " + holder.getItemViewType());
        }


    }

    private void goToBlogPostFragment(String post_id, String user_id) {
        Intent post = new Intent(context, PostActivity.class);
        post.putExtra("blogPostId", post_id);
        post.putExtra("UserId", user_id);
        context.startActivity(post);
    }

    @Override
    public int getItemCount() {
        return userBlogList.size();
    }

    //@Override
    public static void ButtonClicked(int k, final String PostID, final int position) {
        final String currentId = auth.getCurrentUser().getUid();
        //do somthing according to text
        if(k==1){//edit
            Intent re_post = new Intent(context, posting.class);
            re_post.putExtra("WHICH",PostID);
            context.startActivity(re_post);
        }else if(k==2){
            //just
            HashMap<String,Object> repost = new HashMap<>();
            repost.put("repost_Id",currentId);
            repost.put("repost_time", FieldValue.serverTimestamp());

            firestore.collection("Posts")
                    .document(PostID).collection("Reposts").
                    add(repost).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                @Override
                public void onComplete(@NonNull Task<DocumentReference> task) {
                    if(task.isSuccessful()){
                        firestore.collection("Posts").document(PostID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    final String posterId = (String) task.getResult().get("user_id");
                                    Map<String, Object> postMapn = new HashMap<>();
                                    postMapn.put("re_postId", currentId);
                                    postMapn.put("re_post_desc", null);
                                    postMapn.put("re_image_url", null);
                                    postMapn.put("re_post_image_thumb", null);
                                    postMapn.put("re_timeStamp", userBlogList.get(position).getTimeStamp());
                                    if(userBlogList.get(position).getRe_postId()!=null) {
                                        postMapn.put("image_url", userBlogList.get(position).getRe_image_url());
                                        postMapn.put("post_image_thumb", userBlogList.get(position).getRe_post_image_thumb());
                                        postMapn.put("description", userBlogList.get(position).getRe_post_desc());
                                        postMapn.put("user_id", userBlogList.get(position).getRe_postId());
                                        postMapn.put("timeStamp",FieldValue.serverTimestamp());
                                    }else {
                                        postMapn.put("image_url", userBlogList.get(position).getImage_url());
                                        postMapn.put("post_image_thumb", userBlogList.get(position).getPost_image_thumb());
                                        postMapn.put("description", userBlogList.get(position).getDescription());
                                        postMapn.put("user_id", posterId);
                                        postMapn.put("timeStamp",  FieldValue.serverTimestamp());
                                    }
                                    //new time too
                                    firestore.collection("Posts").add(postMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                            if (task.isSuccessful()) {
                                                final String newPostID = task.getResult().getId();
                                                Map<String, Object> mypostMapn = new HashMap<>();
                                                mypostMapn.put("post_id", newPostID);
                                                mypostMapn.put("author", "re-post");
                                                mypostMapn.put("timeStamp", FieldValue.serverTimestamp());
                                                firestore.collection("Users").document(currentId).collection("Posts")
                                                        .add(mypostMapn).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            if (!posterId.equals(currentId)) {
                                                                final String status = "Re-post";
                                                                Map<String, Object> mylikeMap = new HashMap<>();
                                                                mylikeMap.put("status", status);
                                                                mylikeMap.put("notId", currentId);
                                                                mylikeMap.put("timestamp", FieldValue.serverTimestamp());
                                                                mylikeMap.put("postId", PostID);
                                                                firestore.collection("Users/" + posterId + "/NotificationBox").add(mylikeMap)
                                                                        .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                if (!task.isSuccessful()) {
                                                                                    Toast.makeText(context, "did not properly liked", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                                //send notification now
                                                                                firestore.collection("Users").document(posterId)
                                                                                        .collection("Tokens")
                                                                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                                                                            @Override
                                                                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                                                                for (DocumentChange doc : value.getDocumentChanges()) {
                                                                                                    if (doc.getType() == DocumentChange.Type.ADDED) {
                                                                                                        String token = doc.getDocument().getString("token");
                                                                                                        NotSender
                                                                                                                .sendNotifications(context, token
                                                                                                                        , status, "New Re-post Alert");
                                                                                                    }
                                                                                                }

                                                                                            }
                                                                                        });

                                                                                NotSender.Updatetoken();

                                                                            }
                                                                        });
                                                            }


                                                        } else {
                                                            firestore.collection("Posts").document(newPostID).delete();
                                                            String error = task.getException().getMessage();
                                                            Toast.makeText(context, "Firestore error:" + error, Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            }else {
                                                Toast.makeText(context,"dint repost, something went wrong",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });


                                }else {
                                    Toast.makeText(context,"dint repost, something went wrong",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }else{
                        Toast.makeText(context,"dint repost, something went wrong",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

    public class ViewHolder1 extends RecyclerView.ViewHolder {
        private TextView description;
        private View v;
        private ImageView  likebtn,answersbtn,popbut,repost;
        private TextView postdate,likecount;
        private TextView postOwnerName,repostCounter;
        private CircleImageView ownerimage;

        private ViewPager2 phots;
        public ViewHolder1(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            likebtn = v.findViewById(R.id.likeuserz2);
            popbut = v.findViewById(R.id.popimagedell);
            answersbtn = v.findViewById(R.id.comentbtn);
            postdate = v.findViewById(R.id.datePost);
            likecount = v.findViewById(R.id.likescount23);
            phots = v.findViewById(R.id.pageview);
            repost = v.findViewById(R.id.imageView4);
            repostCounter = v.findViewById(R.id.repostCount);
        }

        public void setBlogimage(List<String> image_uri, List<String> thumb_uri) {
            phots.setAdapter( new ImageAdp(image_uri,thumb_uri,phots));

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

        }

        public void setdescriptiontext(String text) {
            description = v.findViewById(R.id.postdescribe);
            description.setText(text);

        }

        public void setOwner(String name, String profileImageUrl, String profilethmb) {
            postOwnerName = v.findViewById(R.id.Post_user);
            ownerimage =v.findViewById(R.id.circularUser);

            postOwnerName.setText(name);
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);

            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(profileImageUrl).thumbnail(Glide.with(context).load(profilethmb))
                    .into(ownerimage);


        }

        public void setTime(String dateString) {
            postdate.setText(dateString);
        }

        public void updateLikeCount(int count) {
            likecount = v.findViewById(R.id.likescount23);

            if (count > 0)
                likecount.setText(Integer.toString(count));
            else
                likecount.setText(" ");

        }

        public void repostCounter(int i) {
            if(i>0)
                repostCounter.setText(Integer.toString(i));
            else
                repostCounter.setText("0");
        }
    }
    public class ViewHolder2 extends RecyclerView.ViewHolder{
        private TextView description;
        private View v;
        private ImageView likebtn,answersbtn,popbut;
        private TextView postdate,likecount, commentcounter,date1;
        private TextView postOwnerName,owner1name,description2;
        private CircleImageView ownerimage,owner1;
        private CardView cardView,cardowner1;
        private ViewPager2 phots,photos1;
        private TextView repostCounter;
        private ImageView repost;


        public ViewHolder2(@NonNull View itemView) {
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
            repost = v.findViewById(R.id.imageView4);
            repostCounter = v.findViewById(R.id.repostCount);
            description =v.findViewById(R.id.postdescribe);
            //post carried
            date1 = v.findViewById(R.id.repost_date);
            owner1name = v.findViewById(R.id.repost_name);
            cardowner1 = v.findViewById(R.id.repostCard);
            owner1 = v.findViewById(R.id.re_postface);
            description2=v.findViewById(R.id.desc2);
        }


        public void setdescriptiontext(String desc2, String desc1) {
            description2.setText(desc2);
            description.setText(desc1);
        }

        public void setBlogimage(List<String> image_uri, List<String> thumb_uri, int i) {
            if(i==1){
                phots.setAdapter( new ImageAdp(image_uri,thumb_uri,phots));
                phots.setClipToPadding(false);
                phots.setClipChildren(false);
                phots.setOffscreenPageLimit(3);
                phots.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
                CompositePageTransformer cpt = new CompositePageTransformer();
                cpt.addTransformer(new MarginPageTransformer(30));
                cpt.addTransformer((new ViewPager2.PageTransformer() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void transformPage(@NonNull View page, float position) {
                        float r = 1-Math.abs(position);
                        page.setScaleY(0.85F+r*0.15F);
                        page.setForegroundGravity(Gravity.CENTER);
                    }

                }
                ));
                phots.setPageTransformer(cpt);
            }else if(i==2){
                photos1.setAdapter( new ImageAdp(image_uri,thumb_uri,phots));
                photos1.setClipToPadding(false);
                photos1.setClipChildren(false);
                photos1.setOffscreenPageLimit(3);
                photos1.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
                CompositePageTransformer cpt = new CompositePageTransformer();
                cpt.addTransformer(new MarginPageTransformer(30));
                cpt.addTransformer((new ViewPager2.PageTransformer() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void transformPage(@NonNull View page, float position) {
                        float r = 1-Math.abs(position);
                        page.setScaleY(0.65F+r*0.1F);
                        page.setForegroundGravity(Gravity.CENTER);
                    }

                }
                ));
                photos1.setPageTransformer(cpt);
            }

        }
        public void setOwner(String name, String image, String thumb) {
            postOwnerName.setText(name);
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);
            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(image).thumbnail(Glide.with(context).load(thumb))
                    .into(ownerimage);
        }

        public void setTime(String dateString) {
            postdate.setText(dateString);
        }

        public void seconduser(String user_id) {
            firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Users users = task.getResult().toObject(Users.class);
                        owner1name.setText(users.getName().get(0));
                        RequestOptions placeholderOP = new RequestOptions();
                        placeholderOP.placeholder(R.drawable.ic_person);
                        Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(users.getImage()).thumbnail(Glide.with(context)
                                .load(users.getThumb()))
                                .into(owner1);
                    }
                }
            });
        }

        public void updateLikeCount(int count) {
            likecount = v.findViewById(R.id.likescount23);
            if (count > 0)
                likecount.setText(Integer.toString(count));
            else
                likecount.setText(" ");
        }

        public void commentCount(int count) {
            commentcounter = v.findViewById(R.id.Comentcount2);
            if(count>0)
                commentcounter.setText(Integer.toString(count));
            else
                commentcounter.setText(0);

        }

        public void repostCounter(int i) {
            if(i>0)
                repostCounter.setText(Integer.toString(i));
            else
                repostCounter.setText(0);

        }

    }
}
