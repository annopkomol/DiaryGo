package com.example.annop.diarygo;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Annop on 11/19/2016.
 */

public class DeleteRequest extends StringRequest {
    private static final String DELETE_REQUEST_URL = "http://diarygo.esy.es/Delete.php";
    private Map<String, String> params;

    public DeleteRequest(String username,int bookid, Response.Listener<String> listener){
        super(Request.Method.POST, DELETE_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("username", username);
        params.put("bookid", bookid+"");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
