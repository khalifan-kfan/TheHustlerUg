package com.example.thehustler.Adapter;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.view.Gravity;
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
import androidx.cardview.widget.CardView;
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

import static android.text.format.DateFormat.format;

public class TrigRecyclerAdapter extends RecyclerView.Adapter<TrigRecyclerAdapter.ViewHolder> {

    List<Blogpost> Postlist;
    List<Users> usersList;
    public Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;



    public TrigRecyclerAdapter(List<Blogpost> Postlist, List<Users>usersList){
        this.Postlist = Postlist;
        this.usersList = usersList;
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

        String desc_data = Postlist.get(position).getDescription();
        holder.setdescriptiontext(desc_data);

        final String CurrentUser = auth.getCurrentUser().getUid();
        final String post_Id = Postlist.get(position).postId;


        if ((Postlist.get(position).getImage_url())!=null && (Postlist.get(position).getPost_image_thumb())!=null){

        List<String> Image_uri = new ArrayList<>();
        List<String> thumb_uri = new ArrayList<>();
        int i = 0;

        while (i < Postlist.get(position).getImage_url().size()) {
            Image_uri.add(i, Postlist.get(position).getImage_url().get(i));
            thumb_uri.add(i, Postlist.get(position).getPost_image_thumb().get(i));
            i++;
        }
        holder.setBlogimage(Image_uri, thumb_uri);

        }else {

            holder.phots.setEnabled(false);
            holder.phots.setVisibility(View.GONE);
        }

        final String  user_id = Postlist.get(position).getUser_id();
        final String name = usersList.get(position).getName().get(0);
        String image = usersList.get(position).getImage();
        String thumb = usersList.get(position).getThumb();
        holder.setOwner(name,image,thumb);


        if(user_id.equals(CurrentUser)) {
            holder.popbut.setEnabled(true);
            holder.popbut.setVisibility(View.VISIBLE);
            holder.ownerimage.setEnabled(false);
        }


        try {
        if(Postlist.get(position).getTimeStamp() != null) {
            long milliseconds = Postlist.get(position).getTimeStamp().getTime();
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
        //comments counter
        firestore.collection("Posts/"+post_Id+"/Answers").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
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
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
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
                Intent answerintent =  new Intent(context, PostActivity.class);
                answerintent.putExtra("blogPostId",post_Id);
                answerintent.putExtra("UserId",user_id);
                context.startActivities(new Intent[]{answerintent});

            }
        });
        holder.popbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.pop(user_id,Postlist,usersList,position);
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
        return Postlist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
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


