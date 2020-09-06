package com.example.thehustler.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.thehustler.Fragments.MainGigFragment;
import com.example.thehustler.R;

public class JobsActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jobs);
        toolbar = findViewById(R.id.toolbar_Gigs);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GIGS");

       startmainfrag();

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