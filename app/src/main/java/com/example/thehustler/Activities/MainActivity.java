package com.example.thehustler.Activities;



import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.thehustler.Adapter.AnotherUserRecycler;
import com.example.thehustler.Adapter.MypostsAdapter;
import com.example.thehustler.Adapter.TrigRecyclerAdapter;
import  com.example.thehustler.Fragments.Account_Fragment;
import com.example.thehustler.Fragments.BottomSheetDialog;
import  com.example.thehustler.Fragments.ChatsFragment;
import  com.example.thehustler.Fragments.Home_Fragment;
import  com.example.thehustler.Fragments.Notification_Fragment;
import com.example.thehustler.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class MainActivity extends AppCompatActivity implements BottomSheetDialog.BottomSheetListner {


    private Toolbar toolbar;
    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String CurrentUserId;

    private DrawerLayout drawerLayout;

    private FloatingActionButton add;
    public static BottomNavigationView mainBottomNav;

    private Home_Fragment homeFragment;
    private Notification_Fragment notificationFragment;
    private Account_Fragment accountFragment;
    private ChatsFragment chatsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        CurrentUserId = auth.getCurrentUser().getUid();
        toolbar = findViewById(R.id.toolbar___);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hustle");
        drawerLayout = findViewById(R.id.drawer);
        ActionBarDrawerToggle adt = new ActionBarDrawerToggle(this,drawerLayout,toolbar,
        R.string.drawer_open,R.string.drawer_close);
        drawerLayout.addDrawerListener(adt);
        adt.syncState();
        NavigationView nv =findViewById(R.id.nav_view);
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.edit_nav:
                        Intent profile = new Intent(MainActivity.this,ProfileActivity2.class);
                        startActivity(profile);
                        return true;
                    case R.id.MyJobs:
                        //lists
                        Intent postIntent = new Intent(MainActivity.this, JobsActivity.class);
                        postIntent.putExtra("CODEWORD","main");
                        startActivity(postIntent);
                        return true;
                    case R.id.List:
                        Intent intent = new Intent(MainActivity.this, JobsActivity.class);
                        intent.putExtra("CODEWORD","history");
                        startActivity(intent);
                        //history list
                        return true;
                    case R.id.reviews:
                        Intent i = new Intent(MainActivity.this,Reviews.class);
                        i.putExtra("UserId",CurrentUserId);
                        startActivity(i);
                        //reviews
                        return true;
                    case R.id.logout:
                        logOut();
                        return true;
                    case R.id.settings:
                        //real settings not yet
                        break;
                    case R.id.share:
                        //link not yet
                        break;
                    case R.id.contact:
                        //email not yet
                        break;
                    case R.id.donate:
                        //donate not yet
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });

        add = findViewById(R.id.fab);
        if(auth.getCurrentUser() != null) {
            //fragments
            homeFragment = new Home_Fragment();
            notificationFragment = new Notification_Fragment();
            accountFragment = new Account_Fragment();
            chatsFragment = new ChatsFragment();
            initializeFragment();

            mainBottomNav = findViewById(R.id.mainBottomNav);
            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                  Fragment  currentFragment = getSupportFragmentManager().findFragmentById(R.id.mainContainer);

                    switch (menuItem.getItemId()) {
                        case R.id.home_id:
                            replaceFragment(homeFragment,currentFragment);
                            return true;
                        case R.id.notification_id:
                            replaceFragment(notificationFragment,currentFragment);
                            return true;
                        case R.id.account_id:
                            replaceFragment(accountFragment,currentFragment);
                            return true;
                        case R.id.message_id:
                            replaceFragment(chatsFragment,currentFragment);
                            return true;
                        default:
                            return false;
                    }

                }
            });

            if(savedInstanceState==null){
                mainBottomNav.setSelectedItemId(R.id.home_id);
            }

            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent postIntent = new Intent(MainActivity.this, posting.class);
                    postIntent.putExtra("WHICH","original");
                    startActivity(postIntent);

                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.out:
                logOut();
                return  true;
            case R.id.set:
                Intent settings = new Intent(MainActivity.this,ProfileActivity2.class);
                startActivity(settings);
                return  true;
            case R.id.search:
                Intent seach = new Intent(MainActivity.this,SerchActivity.class);
                startActivity(seach);
            default:
                return false;

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Tologin();
        }else {
            CurrentUserId = auth.getCurrentUser().getUid();
            firestore.collection("Users").document(CurrentUserId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if(task.isSuccessful()){
                        if(!task.getResult().exists()) {
                            Intent inforIntent = new Intent(MainActivity.this, InforSettings.class);
                            startActivity(inforIntent);
                            finish();
                        }
                    }else {
                        String e = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error:"+e,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    private  void replaceFragment(Fragment newFragment,Fragment currentFragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if(newFragment == homeFragment){

            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(chatsFragment);

        }

        if(newFragment ==accountFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(chatsFragment);
        }
        if(newFragment==chatsFragment){
            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(notificationFragment);
            fragmentTransaction.hide(accountFragment);

        }


        if(newFragment == notificationFragment){

            fragmentTransaction.hide(homeFragment);
            fragmentTransaction.hide(accountFragment);
            fragmentTransaction.hide(chatsFragment);
        }
        fragmentTransaction.show(newFragment);

        //fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commitNow();

    }

    private void Tologin() {
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(login);
        finish();
    }

    private void logOut() {
        auth.signOut();
        Tologin();
    }

    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.mainContainer, homeFragment);
        fragmentTransaction.add(R.id.mainContainer, notificationFragment);
        fragmentTransaction.add(R.id.mainContainer, accountFragment);
        fragmentTransaction.add(R.id.mainContainer, chatsFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);
        fragmentTransaction.hide(chatsFragment);

        fragmentTransaction.commitNow();

    }

    @Override
    public void onButtonClicked(int k, String postID, int postion, int adaptor) {
        if(adaptor==1) {
            TrigRecyclerAdapter.buttonclicked(k, postID, postion);
        }else if(adaptor==2){
            MypostsAdapter.ButtonClicked(k,postID,postion);
        }else if(adaptor==3){
            AnotherUserRecycler.ButtonClicked(k,postID,postion);
        }
    }
}
