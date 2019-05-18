package com.example.annop.diarygo;


import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class AddPageActivity extends AppCompatActivity {
    String username;
    String email;
    int bookNo;
    double currentLat;
    double currentLng;
    TextView tUsername;
    TextView tBookNo;
    TextView tLat;
    TextView tLng;
    ImageView imageView;
    Button btAdd;
    Button btCancel;
    EditText editText;
    EditText etTopic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_add_page);
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        username = intent.getStringExtra("username");
        bookNo = intent.getIntExtra("countDiary",0)+1;
        currentLat = intent.getDoubleExtra("currentLat",0);
        currentLng = intent.getDoubleExtra("currentLng",0);
        tUsername = (TextView)findViewById(R.id.tUsername);
        tUsername.setText("ID: "+username);
        tLat = (TextView)findViewById(R.id.tLat);
        tLat.setText("Lat: "+currentLat);
        tLng = (TextView)findViewById(R.id.tLng);
        tLng.setText("Lng: "+currentLng);
        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.icon);
        tBookNo = (TextView)findViewById(R.id.tBookNo);
        tBookNo.setText("Book No."+bookNo);
        editText = (EditText)findViewById(R.id.etText);
        etTopic = (EditText)findViewById(R.id.etTopic);
        InputFilter[] filterArray = new InputFilter[1];
        filterArray[0] = new InputFilter.LengthFilter(255);
        editText.setFilters(filterArray);
        btAdd =(Button)findViewById(R.id.btAdd);
        btCancel = (Button)findViewById(R.id.btCancel);
        final Handler handler = new Handler();
        btCancel.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //after click on add bt
                Intent intent = new Intent(AddPageActivity.this, MapsActivity.class);
                intent.putExtra("username", username);
                intent.putExtra("email", email);
                AddPageActivity.this.startActivity(intent);
            }
        });

        btAdd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String name = etTopic.getText().toString().trim();
                final String text = editText.getText().toString().trim();
                final int bookid = bookNo;
                final Double latitude = currentLat;
                final Double longitude = currentLng;
                if((latitude ==0)&&(longitude==0)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPageActivity.this);
                    builder.setMessage("Please Check Your location")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                }
                else {
                    Response.Listener<String> responseListener = new Response.Listener<String>(){

                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonResponse = new JSONObject(response);
                                boolean success = jsonResponse.getBoolean("success");
                                if (success) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPageActivity.this);
                                    builder.setMessage("Add Diary Complete!")
                                            .setNegativeButton("ok", null)
                                            .create()
                                            .show();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(AddPageActivity.this, MapsActivity.class);
                                            intent.putExtra("username", username);
                                            intent.putExtra("email", email);
                                            AddPageActivity.this.startActivity(intent);
                                        }
                                    }, 1000);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(AddPageActivity.this);
                                    builder.setMessage("Location Exist!")
                                            .setNegativeButton("Retry", null)
                                            .create()
                                            .show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    AddRequest addRequest = new AddRequest(username, latitude, longitude,name,text,bookid, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(AddPageActivity.this);
                    queue.add(addRequest);
                }
            }
        });

    }
}
