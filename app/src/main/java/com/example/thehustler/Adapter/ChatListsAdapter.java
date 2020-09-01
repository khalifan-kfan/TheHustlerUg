package com.example.thehustler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Activities.ChatActivity;

import com.example.thehustler.Model.Chats;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListsAdapter extends RecyclerView.Adapter<ChatListsAdapter.ViewHolder> {
    public List<Users> usersList;
    public List<Chats> chatsList;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    public Context context;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");

    public ChatListsAdapter(List<Users> usersList,List<Chats> chatsList) {
        this.usersList = usersList;
        this.chatsList = chatsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_list,parent, false);
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        final String CurrentUser = auth.getCurrentUser().getUid();
        //final String ChatterId = usersList.get(position).Userid;

        final  String ChatterId = chatsList.get(position).getChatterId();
       Date latest_time = chatsList.get(position).getLatest_update();
       holder.data(latest_time);

        String Image_uri = usersList.get(position).getImage();
        String Name =usersList.get(position).getName().get(0);
        String Name2 = usersList.get(position).getName().get(1);
        holder.setUser(Name,Name2,Image_uri);


        holder.section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotochatActivity(ChatterId);

            }
        });
        holder.ChatterName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotochatActivity(ChatterId);
            }
        });


    }
    private void gotochatActivity(final  String chatterId) {

        Intent message = new Intent(context, ChatActivity.class);
        message.putExtra("ReceiverUID",chatterId);
        context.startActivity(message);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView Chatterydp;
        TextView ChatterName,new_date,name2;
        View v;
        ConstraintLayout section;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v=itemView;
            new_date = v.findViewById(R.id.new_date);
            Chatterydp = v.findViewById(R.id.chatterImage);
            ChatterName = v. findViewById(R.id.chat_name);
            section = v.findViewById(R.id.ViewC);
            name2 = v.findViewById(R.id.name2);
        }

        public void setUser(String s, String name, String image_uri) {

           ChatterName.setText(s);
           name2.setText(name);
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);

            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(image_uri)
                    .into(Chatterydp);

        }

        public void data(Date latest_time) {
            if(latest_time != null) {
                new_date.setText(format.format(latest_time));
            }else{
               new_date.setText(format.format(new Date()));
            }


        }
    }
}
