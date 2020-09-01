package com.example.thehustler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thehustler.Activities.AnotherUserAccount;
import com.example.thehustler.Model.ReviewModel;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private List<ReviewModel> review_list;
    private List<Users> user_list;

    public Context context;
    private FirebaseAuth auth;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");


    public ReviewAdapter(List<ReviewModel> review_list, List<Users> user_list) {
        this.review_list = review_list;
        this.user_list = user_list;
    }

    public ReviewAdapter() {
    }


    @NonNull
    @Override
    public ReviewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rewiew_list,parent, false);
        context = parent.getContext();

        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapter.ViewHolder holder, int position) {

        String CurrtId = auth.getCurrentUser().getUid();
        final String Uid = review_list.get(position).getReviewerId();
        final String name = user_list.get(position).getName().get(0);
        String image = user_list.get(position).getImage();
        String thumb = user_list.get(position).getThumb();
        holder.setreviewer(name,image,thumb);
        if(Uid.equals(CurrtId)) {
            holder.mname.setEnabled(false);
            holder.photo.setEnabled(false);
        }
        String review = review_list.get(position).getReviewText();
        holder.review_msg.setText(review);
        Date time = review_list.get(position).getReviewdate();
        holder.setdate(time);

        holder.mname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toUser(Uid);
            }
        });
        holder.mname.setOnClickListener(new View.OnClickListener() {
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
        return review_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView review_msg,review_time,mname;
        private CircleImageView photo;
        private View v;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v= itemView;
            review_msg = v.findViewById(R.id.rewiewMsg);
            review_time= v.findViewById(R.id.reviewtime);
            mname =v.findViewById(R.id.Uname);
            photo = v.findViewById(R.id.face);


        }

        public void setreviewer(String name, String image, String thumb) {
            mname.setText(name);
            Glide.with(context).load(image).thumbnail(Glide.with(context).load(thumb))
                    .into(photo);
        }

        public void setdate(Date time) {
            if(time != null) {
                review_time.setText(format.format(time));
            }else{
                review_time.setText(format.format(new Date()));
            }


        }
    }
}
