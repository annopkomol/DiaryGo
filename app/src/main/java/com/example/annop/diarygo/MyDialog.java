package com.example.annop.diarygo;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Annop on 11/19/2016.
 */

public class MyDialog extends DialogFragment{
    LayoutInflater inflater;
    View v;
    String username;
    String name;
    String text;
    String currentUser;
    int bookid;
    TextView tTopic2,tText2,tUsername2;
    ImageView iBin,iClose;

    public static final MyDialog newInstance(String username, String name,String text,int bookid,String currentUser){
        MyDialog adf =  new MyDialog();
        Bundle bundle = new Bundle(5);
        bundle.putString("username", username);
        bundle.putString("name", name);
        bundle.putString("text", text);
        bundle.putString("currentUser", currentUser);
        bundle.putInt("bookid", bookid);
        adf.setArguments(bundle);
        return adf;
    }


//    public MyDialog(String username, String name,String text,int bookid,String currentUser){
//        this.username = username;
//        this.name = name;
//        this.text = text;
//        this.bookid = bookid;
//        this.currentUser = currentUser;
//    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        username = getArguments().getString("username");
        name = getArguments().getString("name");
        text = getArguments().getString("text");
        currentUser = getArguments().getString("currentUser");
        bookid = getArguments().getInt("bookid");
        inflater = getActivity().getLayoutInflater();
        v = inflater.inflate(R.layout.show_dialog, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        tTopic2 =(TextView)v.findViewById(R.id.tTopic2);
        tText2 =(TextView)v.findViewById(R.id.tText2);
        tUsername2 =(TextView)v.findViewById(R.id.tUsername2);
        iBin =(ImageView) v.findViewById(R.id.iBin);
        if(username.equals(currentUser)) {
            iBin.setImageResource(R.drawable.bin);
        }
        iClose =(ImageView) v.findViewById(R.id.iClose);
        iClose.setImageResource(R.drawable.closebt);
        tTopic2.setText(name);
        tText2.setText(text);
        tUsername2.setText("Writtten by "+ currentUser);
        iClose.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                dismiss();
            }
        });
        iBin.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(username.equals(currentUser)) {
                    //delete
                    Response.Listener<String> responseListener = new Response.Listener<String>(){

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                        dismiss();
                                        Intent intent = getActivity().getIntent();
                                        getActivity().finish();
                                        startActivity(intent);
                                } else {
                                    dismiss();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    DeleteRequest deleteRequest = new DeleteRequest(username,bookid, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    queue.add(deleteRequest);
                    //refresh
                }
            }
        });

        builder.setView(v);
        return builder.create();
    }


}
