package com.example.thehustler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Activities.AnotherUserAccount;

import com.example.thehustler.Activities.PostActivity;
import com.example.thehustler.Model.Blogpost;
import com.example.thehustler.Model.Users;
import com.example.thehustler.NotifyHandler.NotSender;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MypostsAdapter  extends RecyclerView.Adapter<MypostsAdapter.ViewHolder> {

    public List<Blogpost> userBlogList;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public Context context;


    public MypostsAdapter(List<Blogpost> userBlogList) {
        this.userBlogList = userBlogList;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_posts,parent, false);
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();
        return new ViewHolder(view);


    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.setIsRecyclable(false);

        String desc_data =userBlogList.get(position).getDescription();
        holder.setdescriptiontext(desc_data);

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
            holder.setBlogimage(Image_uri, thumb_uri);

        }else {
            holder.phots.setEnabled(false);
            holder.phots.setVisibility(View.GONE);
        }


        final String  user_id = userBlogList.get(position).getUser_id();

        DocumentReference userDocRef = firestore.collection("Users").document(CurrentUser);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot documentSnapshot = task.getResult();
                    Users user = documentSnapshot.toObject(Users.class);
                    String Name = user.getName().get(0);
                    String profileImageUrl = user.getImage();
                    String profilethmb = user.getThumb();
                    holder.setOwner(Name, profileImageUrl, profilethmb);
                } else {
                    String e = task.getException().getMessage();
                    Toast.makeText(context,e+"failed",Toast.LENGTH_LONG).show();
                }
            }
        });

        holder.popbut.setEnabled(true);
        holder.popbut.setVisibility(View.VISIBLE);

        try {
            if(userBlogList.get(position).getTimeStamp() != null) {
                long milliseconds = userBlogList.get(position).getTimeStamp().getTime();
                String dateString = DateFormat.format("d/MM/yyyy", new Date(milliseconds)).toString();
                holder.setTime(dateString);
            }else holder.setTime("0/0/0");
        } catch (Exception e){
            Toast.makeText(context,"Exception"+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        //likes count
        firestore.collection("Posts/"+post_Id+"/likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    holder.updateLikeCount(count);

                }else{
                    holder.updateLikeCount(0);
                }
            }
        });

        //get likes
        firestore.collection("Posts/"+post_Id+"/likes").document(CurrentUser).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    holder.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_nfava));
                }else{
                    holder.likebtn.setImageDrawable(context.getDrawable(R.drawable.ic_fava));
                }
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

        holder.answersbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent answerintent =  new Intent(context, PostActivity.class);
                answerintent.putExtra("blogPostId",post_Id);
                answerintent.putExtra("UserId",user_id);
                context.startActivities(new Intent[]{answerintent});

            }
        });
        holder.popbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.pop(user_id,userBlogList,position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return userBlogList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView description;
        private View v;
        private ImageView likebtn,answersbtn,popbut;
        private TextView postdate,likecount;
        private TextView postOwnerName;
        private CircleImageView ownerimage;
        private ViewPager2 phots;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            likebtn = v.findViewById(R.id.likeuserz2);
            popbut = v.findViewById(R.id.popimagedell);
            answersbtn = v.findViewById(R.id.comentbtn);
            postdate = v.findViewById(R.id.datePost);
            phots =  v.findViewById(R.id.pageview);

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
        }

        public void setTime(String date){

            postdate.setText(date);
        }

        public void setOwner(String name,String image,String thumb){
            postOwnerName = v.findViewById(R.id.Post_user);
            ownerimage =v.findViewById(R.id.circularUser);

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
        public  void pop(final String user_id, final List<Blogpost> postlist , final int position){
            PopupMenu popupMenu = new PopupMenu(context,popbut);
            popupMenu.getMenuInflater().inflate(R.menu.popdown, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    firestore.collection("Posts").document(user_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            postlist.remove(position);
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
}

