package com.example.annop.diarygo;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;
/**
 * Created by Annop on 11/19/2016.
 */

public class AddRequest extends StringRequest {
  private static final String ADD_REQUEST_URL = "http://diarygo.esy.es/Add.php";
    private Map<String, String> params;

    public AddRequest(String username, Double latitude, Double longitude,String name,String text,int bookid, Response.Listener<String> listener){
        super(Request.Method.POST, ADD_REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("username", username);
        params.put("latitude", latitude+"");
        params.put("longitude", longitude+"");
        params.put("name", name);
        params.put("text", text);
        params.put("bookid", bookid+"");
    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
