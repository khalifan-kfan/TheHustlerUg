package com.example.thehustler.Adapter;



import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.thehustler.Activities.SerchActivity;
import com.example.thehustler.Fragments.SerchPostFragment;
import com.example.thehustler.Fragments.SerchUserFragment;
import com.example.thehustler.Fragments.SerchWorkFragment;
import com.example.thehustler.R;
import com.example.thehustler.classes.Update;

public class SectionPagerAdapter extends FragmentStatePagerAdapter {
    private int Numberoftabs;
    String  Seach;
    public Context mContext;

    public SectionPagerAdapter(@NonNull FragmentManager fm,int Numberoftabs,Context context) {
        super(fm,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.Numberoftabs = Numberoftabs;
        mContext = context;

    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                 SerchUserFragment tab1 = SerchUserFragment.newInstance();
                return tab1;
            case 1:
                SerchPostFragment tab2 = SerchPostFragment.newInstance();
                return tab2;
            case 2:
                 SerchWorkFragment tab3 = SerchWorkFragment.newInstance();
                return tab3;
            default:
                return null;
        }

    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (object instanceof Update) {
            //sent to fragments
            ((Update) object).TextChange(Seach);
        }

        return super.getItemPosition(object);
    }

    @Override
    public int getCount() {
        return Numberoftabs;
    }
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.thruuser);
            case 1:
                return mContext.getString(R.string.thrupost);

            case 2:
                return mContext.getString(R.string.thruwork);
            default:
                return null;
        }
    }

    public  void update(String text){
        Seach =text;
        notifyDataSetChanged();
    }


}
