package com.example.thehustler.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thehustler.Activities.AnotherUserAccount;
import com.example.thehustler.Model.Comments;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.List;

public class CommentsRecyclerAdaptor extends RecyclerView.Adapter<CommentsRecyclerAdaptor.ViewHolder> {

    List<Comments> Answerslist;
    public Context context;
    private FirebaseFirestore firestore;
    List<Users> usersList;
    private FirebaseAuth auth;


    public CommentsRecyclerAdaptor(List<Comments> answerslist, List<Users> UsersList) {
        Answerslist = answerslist;
        usersList = UsersList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comments_list,parent, false);
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        auth =FirebaseAuth.getInstance();


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        String answermsg =Answerslist.get(position).getAnswer();
        holder.comment_message.setText(answermsg);

        String CurrtUser = auth.getCurrentUser().getUid();

        final String user_id = Answerslist.get(position).getUserID();

        final String Name = usersList.get(position).getName().get(0);
        holder.name.setText(Name);

        if(CurrtUser.equals(user_id)){
            holder.name.setEnabled(false);
        }


        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent thereAccount = new Intent(context, AnotherUserAccount.class);
                thereAccount.putExtra("UserId",user_id);
                context.startActivity(thereAccount);
            }
        });

        try {
            if(Answerslist.get(position).getTimestamp() != null) {
                long milliseconds = Answerslist.get(position).getTimestamp().getTime();
                String dateString = DateFormat.format("d/MM/yyyy", new Date(milliseconds)).toString();
                holder.date.setText(dateString);
            }else holder.date.setText("0/0/0");
        } catch (Exception e){
            Toast.makeText(context,"Exception"+e.getMessage(),Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public int getItemCount() {
        if (Answerslist != null) {
            return  Answerslist.size();
        } else { return 0; }
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private  View v;
        private TextView date;
        private TextView name;
        private TextView comment_message;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            v = itemView;
            date = v.findViewById(R.id.time_stamp);
            name = v.findViewById(R.id.usernameans);
            comment_message = v.findViewById(R.id.loadMessage);
        }


    }
}
