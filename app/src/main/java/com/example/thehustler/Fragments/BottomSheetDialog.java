package com.example.thehustler.Fragments;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.thehustler.Model.Users;
import com.example.thehustler.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class BottomSheetDialog extends BottomSheetDialogFragment {
    private String postID;
    private BottomSheetListner listner;
    private static final String  POSITION ="postion";



    public BottomSheetDialog(){


    }

    public static BottomSheetDialog newInstance(String postionInt) {
        Bundle args = new Bundle();
        BottomSheetDialog fragment = new BottomSheetDialog();

       args.putString(POSITION,postionInt);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           postID  = getArguments().getString(POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =inflater.inflate(R.layout.bottom_sheet,container,false);
        TextView edit = v.findViewById(R.id.edit);
        TextView just = v.findViewById(R.id.easy);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.onButtonClicked(1,postID);
                dismiss();
            }
        });
        just.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.onButtonClicked(2,postID);
                dismiss();
            }
        });
        return v;
    }

    public interface  BottomSheetListner{
        void onButtonClicked(int k,String  postID);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listner = (BottomSheetListner) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()+"implement bottom listener");
        }
    }
}
