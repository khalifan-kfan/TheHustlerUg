package com.example.thehustler.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thehustler.R;


public class MainGigFragment extends Fragment {
    ListView list;



    public MainGigFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_gig, container, false);
        list = v.findViewById(R.id.list);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        Toast.makeText(getContext(),"open gigs",Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        Toast.makeText(getContext(),"on going ",Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(getContext(),"requests",Toast.LENGTH_LONG).show();
                        break;
                }


            }
        });



        return v;



    }
}