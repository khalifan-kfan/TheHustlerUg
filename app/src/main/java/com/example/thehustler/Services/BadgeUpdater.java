package com.example.thehustler.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.thehustler.Activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BadgeUpdater extends Service {
    Date  check_time;
    SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss a, dd-MM-yy");
    private FirebaseFirestore firestore;
    Boolean checking = false;

    public BadgeUpdater() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        check_time= new Date();
        firestore = FirebaseFirestore.getInstance();
        //format.format(new Date())
        FirebaseAuth auth = FirebaseAuth.getInstance();
        MainActivity m;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
      //  throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        String which = (String) bundle.get("WHICH");
        long time = (long) bundle.get("MILI");
        if(time == 0 ) {
            if (which.equalsIgnoreCase("h")) {
                Query Firstquery = firestore.collection("Posts")
                        .whereGreaterThan("timeStamp", check_time)
                        //.orderBy("timeStamp", Query.Direction.DESCENDING)
                        .limit(9);
                Firstquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value.isEmpty()) {
                            stopSelf();
                        } else {
                            MainActivity m = new MainActivity();
                            m.Updatebadge(value.size(), 1);
                            stopSelf();
                        }
                    }
                });
            } else if (which.equalsIgnoreCase("n")) {

            }
        }else if(which.equalsIgnoreCase("s")){
            Date date = new Date(time);
            Query Firstquery = firestore.collection("Posts")
                    .whereGreaterThan("timeStamp", date)
                    //.orderBy("timeStamp", Query.Direction.DESCENDING)
                    .limit(9);
            Firstquery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                    if (!value.isEmpty()) {
                        MainActivity m = new MainActivity();
                        m.Updatebadge(value.size(), 1);
                    }
                    stopSelf();
                }
            });
        }
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}