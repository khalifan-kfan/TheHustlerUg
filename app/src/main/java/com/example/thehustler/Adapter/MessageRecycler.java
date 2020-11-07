package com.example.thehustler.Adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.thehustler.Activities.ChatActivity;
import com.example.thehustler.Model.Messages;
import com.example.thehustler.R;
import com.example.thehustler.classes.SquareImageView;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageRecycler extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    static int MY_MESSAGE = 1;
    static int RECEIVED_MESSAGES = 2;
    static int RECEIVED_PHOTOS = 3;
    static int PHOTOS_SENT = 4;
    static int   LOADING_PHOTOS = 5;

    String currentUserId;
    List<Messages> messages;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");
    Context ctx;

    public MessageRecycler(String currentUserId){
        this.currentUserId = currentUserId;
        messages = new ArrayList<>();


    }

    @NonNull
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View chatCards = null;
        ctx = parent.getContext();
        if(viewType == MY_MESSAGE){
            chatCards = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.my_chat_card, parent, false);
        }else if(viewType== RECEIVED_MESSAGES){
            chatCards = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receiver_chat_card, parent, false);
        }else if(viewType== RECEIVED_PHOTOS){
            chatCards = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.recieve_img, parent, false);
        }else if(viewType== LOADING_PHOTOS){
            chatCards = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.loading_photos, parent, false);
        }else if(viewType== PHOTOS_SENT){
            chatCards = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mychatimage, parent, false);
        }
        return new MessagesHolder(chatCards);
    }
    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).getSenderId().equals(currentUserId) && messages.get(position).getType().equals("text")){
            return MY_MESSAGE;
        }else if(messages.get(position).getSenderId().equals(currentUserId) && messages.get(position).getType().equals("sent_images")){
            return PHOTOS_SENT;
        }else if(messages.get(position).getSenderId().equals(currentUserId) && messages.get(position).getType().equals("images")){
            return LOADING_PHOTOS;
        }else if(!messages.get(position).getSenderId().equals(currentUserId) && messages.get(position).getType().equals("text")){
            return RECEIVED_MESSAGES;
        }else if(!messages.get(position).getSenderId().equals(currentUserId) && messages.get(position).getType().equals("sent_images")){
            return RECEIVED_PHOTOS;
        }else return 0;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);
        switch (holder.getItemViewType()) {
            case  1:
            case 2: {
                final MessagesHolder holder1 = (MessagesHolder) holder;
                holder1.bindView(messages.get(position));
                break;
            }
            case 4:
            case 3: {
                photoholder holder2 = (photoholder) holder;
                holder2.bindView(messages.get(position));
                break;
            }
            case 5:
                Unloadedholder holder3 = (Unloadedholder) holder;
                holder3.bindView(messages.get(position),ctx);
                break;
            default:
        }

       //

    }


    @Override
    public int getItemCount() {
        return  messages.size();
    }

    public void setData(List<Messages> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public class MessagesHolder extends RecyclerView.ViewHolder {

        TextView MessageTextView;
        TextView DateSentTextView;
        TextView SendernameTextView;
        private  View v;

        public MessagesHolder(@NonNull View itemView) {
            super(itemView);
            v= itemView;
            MessageTextView = v.findViewById(R.id.message_tv);
            DateSentTextView = v.findViewById(R.id.dateSent_tv);
            SendernameTextView = v.findViewById(R.id.sender_name_tv);
        }
        public void bindView(Messages message) {
            SendernameTextView.setText(message.getSenderName());
            MessageTextView.setText(message.getMessageText());
            if(message.getDateSent() != null) {
                DateSentTextView.setText(format.format(message.getDateSent()));
            }else{
                DateSentTextView.setText(format.format(new Date()));
            }
        }

    }
    public class photoholder extends RecyclerView.ViewHolder {

        TextView MessageTextView;
        TextView DateSentTextView;
       ViewPager2 photos;

        private  View v;
        public photoholder(@NonNull View itemView) {
            super(itemView);
            v= itemView;
            MessageTextView = v.findViewById(R.id.textbox);
            DateSentTextView = v.findViewById(R.id.timesent);
            photos = v.findViewById(R.id.page_chat);
        }
        public void bindView(Messages message) {
           // SendernameTextView.setText(message.getSenderName());
            MessageTextView.setText(message.getMessageText());
            if(message.getDateSent() != null) {
                DateSentTextView.setText(format.format(message.getDateSent()));
            }else{
                DateSentTextView.setText(format.format(new Date()));
            }
            int i = 0;
            ArrayList<String > Image_uri = new ArrayList<>();
            while (i < message.getImages().size()) {
                Image_uri.add(i,  message.getImages().get(i));
                i++;
            }
            photos.setAdapter( new ImageAdp(Image_uri,null,photos));
            photos.setClipToPadding(false);
            photos.setClipChildren(false);
            photos.setOffscreenPageLimit(3);
            photos.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
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
        }

    }
    public class Unloadedholder extends RecyclerView.ViewHolder {

       RecyclerView photos;
        private  View v;
        public Unloadedholder(@NonNull View itemView) {
            super(itemView);
            v= itemView;
           photos = v.findViewById(R.id.photos);

        }
        public void bindView(Messages message, Context ctx) {
            int i = 0;
            ArrayList<Uri > Image_uri = new ArrayList<>();
            while (i < message.getImages().size()) {
                Image_uri.add(i, Uri.parse(message.getImages().get(i)));
                i++;
            }
            photos.setLayoutManager(new LinearLayoutManager(ctx));
            unloadedAdapter adapter = new unloadedAdapter(ctx,Image_uri,message);
            photos.setAdapter(adapter);

        }

    }
}
class unloadedAdapter extends RecyclerView.Adapter<unloadedAdapter.ViewHolder>  {

    Context ctx;
    List<Uri> images;
    List<String> duris;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    Messages messages;

    public unloadedAdapter(Context ctx, List<Uri> images, Messages message) {
        this.ctx = ctx;
        this.images = images;
        messages = message;
       // this.itemlisten = itemlisten;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_images,parent, false);
        ctx = parent.getContext();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        duris = new ArrayList<>();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        // holder.setIsRecyclable(false);
        //
        final Uri imageUri = images.get(position);
        Glide.with(ctx)
                .load(images.get(position))
                .into(holder.photos);
        holder.loading.setVisibility(View.VISIBLE);
                final StorageReference file = storageReference.child(auth.getCurrentUser().getUid()).child("sent_photos")
                        .child(imageUri.getLastPathSegment()+".jpg");
                UploadTask uploadTask = file.putFile(imageUri);
                Task<Uri> urlTask = uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        int currentprogress = (int) progress;
                        holder.loading.setProgress(currentprogress);
                    }
                }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        int currentprogress = (int) progress;
                        holder.loading.setProgress(currentprogress);

                    }
                }).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        // Continue with the task to get the download URL
                        return file.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            String myUrl = downloadUri.toString();
                            duris.add(myUrl);
                            if (duris.size()==images.size()) {
                                //send the message now
                                Messages message = new Messages(messages.getSenderId(), messages.getSenderName(),
                                        messages.getMessageText(),duris,"sent_images");
                                ChatActivity activity = new ChatActivity();
                                activity.sendMessage(message);
                                Toast.makeText(ctx, "All uploaded", Toast.LENGTH_LONG).show();
                            }

                        }
                    }
                });

            }


    @Override
    public int getItemCount() {
        if (images != null) {
            return  images.size();
        } else { return 0; }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        SquareImageView photos;
        ProgressBar loading;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photos = itemView.findViewById(R.id.grid_image);
            loading = itemView.findViewById(R.id.progbar);
        }
    }
}

