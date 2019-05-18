package com.example.annop.diarygo;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {
    private Thread thread;
    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher m = p.matcher(email);
        return m.matches();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText etUsername = (EditText) findViewById(R.id.etUsername);
        final EditText etPassword = (EditText) findViewById(R.id.etPassword);
        final EditText etRePassword = (EditText) findViewById(R.id.etRePassword);
        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        final Button btRegister = (Button) findViewById(R.id.btRegister);
        final Handler handler = new Handler();

        btRegister.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final String username = etUsername.getText().toString();
                final String password = etPassword.getText().toString();
                final String repassword = etRePassword.getText().toString();
                final String email = etEmail.getText().toString();
               // boolean cpasscheck = false;
               // boolean passcheck = false;
               // boolean idcheck = false;
                if((password.equals(repassword))==false){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("Password does not match the confirm-password")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                }
                else if (username.length()>12 || username.length() <4){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("username must be at least 4 but no more than 12 characters")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                }
                else if (password.length()>12 || password.length() <4){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("password must be at least 4 but no more than 12 characters")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                }
                else if (isValidEmailAddress(email)==false){
                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                    builder.setMessage("invalid email address")
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
                                int success = jsonResponse.getInt("success");
                                if (success == 2) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                    builder.setMessage("registration complete!")
                                            .setNegativeButton("ok", null)
                                            .create()
                                            .show();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                            RegisterActivity.this.startActivity(intent);
                                        }
                                    }, 3000);
                                } else if(success == 1){
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setMessage("email exists")
                                        .setNegativeButton("Retry", null)
                                        .create()
                                        .show();
                            }else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                                builder.setMessage("username exists")
                                        .setNegativeButton("Retry", null)
                                        .create()
                                        .show();
                            }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    RegisterRequest registerRequest = new RegisterRequest(username, password, email, responseListener);
                    RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                    queue.add(registerRequest);
               }
            }
        });
    }
}
