package com.example.thehustler.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.thehustler.Activities.AnotherUserAccount;
import com.example.thehustler.Activities.SerchActivity;
import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import de.hdodenhof.circleimageview.CircleImageView;


public class DirectGigUserFragment extends Fragment {
    private String newname ;
    private RecyclerView views;
    private SelectedUser adapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private List<Users> users_lists;
    private DocumentSnapshot lastVisible;
    private Boolean firstload = true;
    private ImageButton search;
    private EditText name_et;
    private Boolean namechange = false;


    public DirectGigUserFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

       View v =inflater.inflate(R.layout.fragment_direct_gig_user, container, false);
        views = v.findViewById(R.id.gig_user_rv);
        search = v.findViewById(R.id.imageButton_search);
        name_et = v.findViewById(R.id.editTextTextPersonName4);
        users_lists = new ArrayList<>();
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        adapter = new SelectedUser(users_lists);
        views.setLayoutManager(new LinearLayoutManager(container.getContext()));
        views.setAdapter(adapter);
        firestore = FirebaseFirestore.getInstance();
        views.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Boolean atBottom = !recyclerView.canScrollVertically(-1);
                if (atBottom) {
                    //prog.setVisibility(View.VISIBLE);
                    loadmore();
                }
            }
        });
       if(name_et.getText().toString().isEmpty()){
           search.setEnabled(false);
       }

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newname =  name_et.getText().toString();
                if(TextUtils.isEmpty(newname)){
                        Toast.makeText(getContext(),"please enter a name",Toast.LENGTH_SHORT).show();
                }else {
                    namechange= true;
                    drive();
                }

            }
        });

        return v;
    }

    private void drive(){
        if ((newname.length()>0)){
            if(namechange) {
                namechange= false;
                users_lists.clear();
                adapter.notifyItemRangeRemoved(0,users_lists.size());
            }
            //prog.setVisibility(View.VISIBLE);
            fistfive();
        }else{
            Toast.makeText(getContext(), "no searches yet", Toast.LENGTH_LONG).show();
        }

    }

    private void fistfive() {
        Query Firstquery = firestore.collection("Users")
                .whereArrayContains("name",newname)
                .limit(5);
        Firstquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null) {
                    if (!value.isEmpty()) {
                        if (firstload) {
                            lastVisible =value.getDocuments().get(value.size() - 1);
                            users_lists.clear();
                        }

                        for (DocumentChange doc:value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String ID = doc.getDocument().getId();
                                Users users = doc.getDocument().toObject(Users.class).withID(ID);
                                if (firstload) {
                                    users_lists.add(users);
                                } else {
                                    users_lists.add(0, users);
                                }
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                   //     prog.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "no name like that", Toast.LENGTH_LONG).show();
                    }
                }else {
                   // prog.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"error:"+error,Toast.LENGTH_LONG).show();
                }// prog.setVisibility(View.GONE);
            }
        });

    }

    private void loadmore() {
        Query Nextquery = firestore.collection("Users")
                .whereArrayContains("name",newname)
                .startAfter(lastVisible)
                .limit(5);
        Nextquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null) {
                    if (!value.isEmpty()) {
                        lastVisible = value.getDocuments()
                                .get(value.size() - 1);

                        for (DocumentChange doc:value.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String ID = doc.getDocument().getId();
                                Users users = doc.getDocument().toObject(Users.class);
                                users_lists.add(users);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    } else {
                   //     prog.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "no name like that", Toast.LENGTH_LONG).show();
                    }
                }else {
                 //   prog.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"error:"+error,Toast.LENGTH_LONG).show();
                }
               // prog.setVisibility(View.GONE);
            }
        });

    }

}
class SelectedUser  extends RecyclerView.Adapter<SelectedUser.ViewHolder> {


    private List<Users> users_lists;
    String Name,Name2;
    FirebaseAuth auth;
    public Context context;

    public SelectedUser(List<Users> users_lists) {
        this.users_lists = users_lists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chats_list,parent, false);
        context = parent.getContext();
        //firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String CurrentUser = auth.getCurrentUser().getUid();

        final String id = users_lists.get(position).Userid;

        String Image_uri = users_lists.get(position).getImage();
        Name =users_lists.get(position).getName().get(0);
         Name2 = users_lists.get(position).getName().get(1);
        String wk =users_lists.get(position).getWork();
        holder.setUser(Name,Name2,wk,Image_uri);

        holder.section.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               generateForUser(id,Name);
            }
        });
        holder.dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(id.equals(CurrentUser)){
                   Toast.makeText(context,
                            "you cant create a gig for you self" , Toast.LENGTH_LONG).show();

                } else{
                Intent thereAccount = new Intent(context, AnotherUserAccount.class);
                thereAccount.putExtra("UserId",id);
                context.startActivity(thereAccount);
                }
            }
        });

    }

    private void generateForUser(String id, String name) {
        AppCompatActivity activity = (AppCompatActivity) context;
        Fragment Fragment  = Create_Gig_Fragment.newInstance(id,name);
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.Gigs_container, Fragment).addToBackStack(null).commit();
    }

    @Override
    public int getItemCount() {
        return users_lists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView dp;
        TextView ChatterName,work,name2;
        View v;
        ConstraintLayout section;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            v=itemView;
            work = v.findViewById(R.id.new_date);
            dp = v.findViewById(R.id.chatterImage);
            ChatterName = v. findViewById(R.id.chat_name);
            section = v.findViewById(R.id.ViewC);
            name2 = v.findViewById(R.id.name2);
        }

        public void setUser(String s, String name,String work2, String image_uri) {

            ChatterName.setText(s);
            name2.setText(name);
            work.setText(work2);
            RequestOptions placeholderOP = new RequestOptions();
            placeholderOP.placeholder(R.drawable.ic_person);
            Glide.with(context).applyDefaultRequestOptions(placeholderOP).load(image_uri)
                    .into(dp);

        }
    }
}