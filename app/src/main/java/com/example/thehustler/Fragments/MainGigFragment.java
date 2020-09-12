package com.example.thehustler.Fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
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
                Fragment newFragment = Create_Gig_Fragment.newInstance(null,"open");
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


                        // Toast.makeText(getContext(),"open gigs",Toast.LENGTH_LONG).show();
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