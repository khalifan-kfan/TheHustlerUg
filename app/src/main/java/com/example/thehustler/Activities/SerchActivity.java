package com.example.thehustler.Activities;

import android.app.SearchManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import  com.example.thehustler.Adapter.SectionPagerAdapter;
import com.example.thehustler.R;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;



public class SerchActivity extends AppCompatActivity  implements  SearchView.OnQueryTextListener{

    Toolbar title;
    SectionPagerAdapter sectionsPagerAdapter;
    public TabLayout tabs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serch);
         tabs = findViewById(R.id.tabs);
      //  TabItem tuser = findViewById(R.id.tusers);
        //TabItem tposts = findViewById(R.id.tpost);
        //TabItem twork = findViewById(R.id.twork);

        title = findViewById(R.id.title);

        setSupportActionBar(title);
        getSupportActionBar().setTitle("type..");

         sectionsPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager(),tabs.getTabCount(),SerchActivity.this);
        final ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        tabs.setupWithViewPager(viewPager);
/*
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(),true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {


            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.seach_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem searchMenuItem = menu.findItem(R.id.act_search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        sectionsPagerAdapter.update(query);
        return false;
    }
    @Override
    public boolean onQueryTextChange(String newText) {
        return true;
    }
}
