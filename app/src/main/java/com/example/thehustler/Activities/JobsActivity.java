package com.example.thehustler.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;

import com.example.thehustler.Fragments.Create_Gig_Fragment;
import com.example.thehustler.Fragments.Gig_lists_Fragment;
import com.example.thehustler.Fragments.MainGigFragment;
import com.example.thehustler.R;

import java.util.jar.Attributes;

public class JobsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private String codeWord;
    private String Name;
    private String id;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        toolbar = findViewById(R.id.toolbar_Gigs);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GIGS");
        Intent intent = getIntent();
        //get account user id
        codeWord = intent.getStringExtra("CODEWORD");
        id = intent.getStringExtra("ID");
        Name = intent.getStringExtra("NAME");

        switch (codeWord){
            case "open":
                status = "open";
                toLists(status);
                break;
            case "requests":
                status = "pending";
                toLists(status);
                break;
            case "ongoing":
                status = "ongoing";
                toLists(status);
                break;
            case "main":
                startmainfrag();
                break;
            case "history":
                status = "done";
                toLists(status);
                break;
            case "create_open":
               createOpen();
                break;
            case "create_direct":
              toDisrect();
                break;
            default:
                break;
        }
    }

    private void toLists(String status) {
        Gig_lists_Fragment fragment = Gig_lists_Fragment.newInstance(status);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.Gigs_container,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    private void toDisrect() {
        Create_Gig_Fragment fragment = Create_Gig_Fragment.newInstance(id,Name);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.Gigs_container,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    public void createOpen(){
        Create_Gig_Fragment frag = Create_Gig_Fragment.newInstance("open",null);
       FragmentManager manager = getSupportFragmentManager();
       FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.Gigs_container,frag);
        transaction.addToBackStack(null);
        transaction.commit();
    }
    public void startmainfrag(){
        MainGigFragment mainGigFragment = new MainGigFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.Gigs_container, mainGigFragment);
        fragmentTransaction.commitNow();
    }

    @Override
    public void onBackPressed() {
        if(getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
        else {
            getFragmentManager().popBackStack();
        }
    }
}