package com.example.thehustler.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.thehustler.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetDialog extends BottomSheetDialogFragment {
    private String postID;
     BottomSheetListner listner ;
    private static final String  POSITION ="postion";
    private int position,adp;
    private static final String  POSTID ="postId";
    private static final String  ADAPTOR ="adaptor_number";


    public BottomSheetDialog(){
    }

    public static BottomSheetDialog newInstance(String postId,int position,int adaptor) {
        Bundle args = new Bundle();
        BottomSheetDialog fragment = new BottomSheetDialog();
        args.putString(POSTID,postId);
       args.putInt(POSITION,position);
       args.putInt(ADAPTOR,adaptor);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
           postID  = getArguments().getString(POSTID);
           position = getArguments().getInt(POSITION);
           adp =getArguments().getInt(ADAPTOR);
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
                listner.onButtonClicked(1,postID,position,adp);
                dismiss();
            }
        });
        just.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listner.onButtonClicked(2,postID,position,adp);
                dismiss();
            }
        });
        return v;
    }

    public interface  BottomSheetListner{
        void onButtonClicked(int k,String  postID,int postion,int adaptor);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listner = (BottomSheetListner) context;
        }catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    +"implement bottom listener");
        }
    }
}
