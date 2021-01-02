package com.example.thehustler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Activities.AnotherUserAccount;
import com.example.thehustler.Activities.PostActivity;
import com.example.thehustler.Model.Notify;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    String status1 ="Comment";
    String status2 ="like";
    String status3 ="Approval";
    String status4 ="Review";

    private List<Notify> notifyList;
    private  List<Users> usersList;
    public Context context;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

    public NotificationAdapter(List<Notify> notifyList, List<Users> usersList) {
        this.notifyList = notifyList;
        this.usersList = usersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notify_list,parent, false);
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final String CurrtId = auth.getCurrentUser().getUid();

        final String Postid= notifyList.get(position).getPostId();
        final String Uid = notifyList.get(position).getNotId();
        final String name = usersList.get(position).getName().get(0);
        String image = usersList.get(position).getImage();
        String thumb = usersList.get(position).getThumb();

        final String mark = notifyList.get(position).getMark();
        if(mark.equalsIgnoreCase("unseen")){
            holder.mark.setBackgroundResource(R.drawable.ic_unmarked);
            holder.lay.setBackgroundColor(Color.parseColor(String.valueOf(R.color.ligth)));
        }else {
            holder.mark.setEnabled(false);
            holder.mark.setBackgroundResource(R.drawable.ic_marked);
        }
       holder.setowner(image,thumb,name);

        Date time = notifyList.get(position).getTimestamp();
        String Status = notifyList.get(position).getStatus();
        holder.setdate(time);
        holder.setmessage(Status);
        holder.infor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Postid==null){
                    if(mark.equalsIgnoreCase("unseen")) {
                        firestore.collection("Users/" + CurrtId + "/NotificationBox")
                                .document(notifyList.get(position).Userid).update("mark", "seen").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    holder.mark.setBackgroundResource(R.drawable.ic_marked);
                                    holder.lay.setBackgroundColor(Color.parseColor(String.valueOf(R.color.mywhite)));
                                    toUser(Uid);
                                } else {
                                    Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        toUser(Uid);
                    }
                }else{
                    if(mark.equalsIgnoreCase("unseen")) {
                        firestore.collection("Users/" + CurrtId + "/NotificationBox")
                                .document(notifyList.get(position).Userid).update("mark", "seen").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    holder.mark.setBackgroundResource(R.drawable.ic_marked);
                                    holder.lay.setBackgroundColor(Color.parseColor(String.valueOf(R.color.mywhite)));
                                    Intent comLik = new Intent(context, PostActivity.class);
                                    comLik.putExtra("UserId", Uid);
                                    comLik.putExtra("blogPostId", Postid);
                                    context.startActivity(comLik);
                                } else {
                                    Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }else {
                        Intent comLik = new Intent(context, PostActivity.class);
                        comLik.putExtra("UserId", Uid);
                        comLik.putExtra("blogPostId", Postid);
                        context.startActivity(comLik);
                    }
                }
            }
        });
        holder.mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore.collection("Users/" + CurrtId + "/NotificationBox")
                        .document(notifyList.get(position).Userid).update("mark", "seen").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            holder.mark.setBackgroundResource(R.drawable.ic_marked);
                            holder.mark.setEnabled(false);
                            holder.lay.setBackgroundColor(Color.parseColor(String.valueOf(R.color.mywhite)));
                        } else {
                            Toast.makeText(context, "try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
        if(Uid.equals(CurrtId)) {
            holder.mname.setEnabled(false);
            holder.guyzImage.setEnabled(false);

        }
        holder.mname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toUser(Uid);
            }
        });
        holder.guyzImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toUser(Uid);
            }
        });
    }

    private void toUser(String uid) {
        Intent touser = new Intent(context, AnotherUserAccount.class);
        touser.putExtra("UserId",uid);
        context.startActivity(touser);
    }

    @Override
    public int getItemCount() {
        return notifyList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView infor,mname,mtime;
        private CircleImageView guyzImage;
        private ImageView mark;
        private ConstraintLayout lay;
        View v;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            lay = v.findViewById(R.id.cons_not);
            infor = v.findViewById(R.id.infor);
            mname = v.findViewById(R.id.not_name);
            guyzImage = v.findViewById(R.id.not_image);
            mtime = v.findViewById(R.id.timeNot);
            mark = v.findViewById(R.id.imageView5);
        }
        public void setowner(String image, String thumb, String name) {
           mname.setText(name);
            Glide.with(context).load(image).thumbnail(Glide.with(context).load(thumb))
                    .into(guyzImage);
        }

        public void setdate(Date time) {
            if(time != null) {
                mtime.setText(format.format(time));
            }else{
                mtime.setText(format.format(new Date()));
            }
        }

        public void setmessage(String status) {
            if(status.equals(status1)){
                infor.setText("Commented on your post");
            }else if(status.equals(status2)){
                infor.setText("liked on your post");
            }else if(status.equals(status3)){
                infor.setText("Approved you");
            }else {
                infor.setText("reviewed you");
            }
        }
    }
}
