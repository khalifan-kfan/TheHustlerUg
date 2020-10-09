package com.example.thehustler.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
    TextView create_opengig,create_directgig;

    // private static final String ARG_PARAM2 = "RequestType";
    //private static final String ARG_PARAM1 = "Name";


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
        create_directgig = v.findViewById(R.id.directgig);
        create_opengig = v.findViewById(R.id.OpenGig);

        create_directgig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment frag = new DirectGigUserFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.Gigs_container,frag);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        create_opengig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment newFragment = Create_Gig_Fragment.newInstance("open",null);
                FragmentTransaction transaction =getFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack if needed
                transaction.replace(R.id.Gigs_container, newFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                       Fragment newFrag =  Gig_lists_Fragment.newInstance("open");
                        FragmentTransaction transaction =getFragmentManager().beginTransaction();
                        transaction.replace(R.id.Gigs_container, newFrag);
                        transaction.addToBackStack(null);
                        transaction.commit();
                        break;
                    case 1:
                        Fragment newFrag2 =  Gig_lists_Fragment.newInstance("ongoing");
                        FragmentTransaction transaction2 =getFragmentManager().beginTransaction();
                        transaction2.replace(R.id.Gigs_container, newFrag2);
                        transaction2.addToBackStack(null);
                        transaction2.commit();
                        //Toast.makeText(getContext(),"on going ",Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Fragment newFrag3 =  Gig_lists_Fragment.newInstance("pending");
                        FragmentTransaction transaction3 =getFragmentManager().beginTransaction();
                        transaction3.replace(R.id.Gigs_container, newFrag3);
                        transaction3.addToBackStack(null);
                        transaction3.commit();
                       // Toast.makeText(getContext(),"requests",Toast.LENGTH_LONG).show();
                        break;
                }


            }
        });



        return v;



    }
}