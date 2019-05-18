package com.example.annop.diarygo;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.appdatasearch.GetRecentContextCall;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    String showUrl = "http://diarygo.esy.es/GetMarker.php";
    RequestQueue requestQueue;
    ImageButton profileview;
    String username;
    String messageText;
    String email;
    String currentUser;
    TextView di1;
    TextView di2;
    TextView di3;
    TextView di4;
    TextView di5;
    TextView di6;
    TextView di7;
    TextView di8;
    TextView td;
    Double currentLat;
    Double currentLng;
    int currentBookId;
    int countDiary;
    int diaryno;
    ArrayList<Sqlmarker> userBookList = new ArrayList<>();
    ArrayList<Sqlmarker> allBookData = new ArrayList<>();
    static final int CAM_REQUEST = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        final TextView gtext = (TextView) findViewById(R.id.gtext);
        countDiary = 0;
        td = (TextView) findViewById(R.id.tTotalDiary);
        di1 = (TextView) findViewById(R.id.di1);
        di2 = (TextView) findViewById(R.id.di2);
        di3 = (TextView) findViewById(R.id.di3);
        di4 = (TextView) findViewById(R.id.di4);
        di5 = (TextView) findViewById(R.id.di5);
        di6 = (TextView) findViewById(R.id.di6);
        di7 = (TextView) findViewById(R.id.di7);
        di8 = (TextView) findViewById(R.id.di8);
        final TextView diNum = (TextView) findViewById(R.id.diNum);
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        username = intent.getStringExtra("username");
        String message = "User : " + username.toUpperCase();
        gtext.setText(message);
        verifyStoragePermissions(this);
        //camera
        profileview = (ImageButton) findViewById(R.id.profileView) ;
       profileview.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
               File file = getFile();
               camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
               startActivityForResult(camera_intent,CAM_REQUEST);
           }
       });
        //show profile at start
        String path = fileLocation + "/"+ username + "_profile.jpg";
        File image_file = new File(path);
        if(image_file.exists()){
            profileview.setImageDrawable(Drawable.createFromPath(path));
            profileview.setRotation(90f);
            profileview.setScaleType(ImageButton.ScaleType.FIT_XY);
        }else{
            profileview.setImageDrawable(getResources().getDrawable(R.drawable.noprofile));
            profileview.setScaleType(ImageButton.ScaleType.FIT_XY);
        }
        //showmarker
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                showUrl,(JSONObject)null, new Response.Listener<JSONObject>(){
        @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray marker = response.getJSONArray("marker");
                    //Sqlmarker[] sqlmarker = new Sqlmarker[marker.length()];
                    ArrayList<Sqlmarker> sqlmarker = new ArrayList<>();
                    for(int i = 0 ; i < marker.length(); i++){
                        JSONObject markers = marker.getJSONObject(i);
                         sqlmarker.add(new Sqlmarker(markers.getInt("temp_id"),markers.getString("username"),markers.getDouble("latitude"),
                                markers.getDouble("longitude"),markers.getString("name"),markers.getString("text"),markers.getInt("bookid")));
                        //count diary
                        allBookData.add(sqlmarker.get(i));
                        if(username.equals(sqlmarker.get(i).getUsername())){
                            userBookList.add(sqlmarker.get(i));
                            countDiary = countDiary+1;
                        }
                    }

                    //sort by book id
                    ArrayList<Sqlmarker> userBookListTemp = new ArrayList<>();
                    for(int i = 0 ; i < userBookList.size(); i++){
                        for(int j = 0 ; j < userBookList.size(); j++) {
                            if (userBookList.get(j).getBookID() == i+1) {
                                userBookListTemp.add(userBookList.get(j));
                            }
                        }
                    }
                    for(int i = 0 ; i < userBookList.size(); i++){
                        userBookList.set(i,userBookListTemp.get(i));
                    }

                    //set diary name text
                    if(countDiary >0){
                        di1.setText(isTooLong(userBookList.get(0).getBookName()));
                        if(countDiary>1){
                            di2.setText(isTooLong(userBookList.get(1).getBookName()));
                            if(countDiary>2){
                                di3.setText(isTooLong(userBookList.get(2).getBookName()));
                                if(countDiary>3){
                                    di4.setText(isTooLong(userBookList.get(3).getBookName()));
                                    if(countDiary>4){
                                        di5.setText(isTooLong(userBookList.get(4).getBookName()));
                                        if(countDiary>5){
                                            di6.setText(isTooLong(userBookList.get(5).getBookName()));
                                            if(countDiary>6){
                                                di7.setText(isTooLong(userBookList.get(6).getBookName()));
                                                if(countDiary>7){
                                                    di8.setText(isTooLong(userBookList.get(7).getBookName()));
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    td.setText(""+sqlmarker.size()+" Diaries on server");
                    diaryno = countDiary;
                    if (countDiary > 1) {
                        diNum.setText("you have " + countDiary + " Diaries");
                    }
                    else{
                        diNum.setText("you have " + countDiary + " Diary");
                    }
                    di1.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            if(countDiary>0) {
                                LatLng latLng = new LatLng(userBookList.get(0).getLatitude(), userBookList.get(0).getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                            }
                        }
                    });
                    di2.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            if(countDiary>1) {
                                LatLng latLng = new LatLng(userBookList.get(1).getLatitude(), userBookList.get(1).getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                            }
                        }
                    });
                    di3.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                            if(countDiary>2) {
                                LatLng latLng = new LatLng(userBookList.get(2).getLatitude(), userBookList.get(2).getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                            }
                        }
                    });
                    di4.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                                if(countDiary>3) {
                                    LatLng latLng = new LatLng(userBookList.get(3).getLatitude(), userBookList.get(3).getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                                }
                        }
                    });
                    di5.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                                if(countDiary>4) {
                                    LatLng latLng = new LatLng(userBookList.get(4).getLatitude(), userBookList.get(4).getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                                }
                        }
                    });
                    di6.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                                if(countDiary>5) {
                                    LatLng latLng = new LatLng(userBookList.get(5).getLatitude(), userBookList.get(5).getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                                }
                        }
                    });
                    di7.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                                if(countDiary>6) {
                                    LatLng latLng = new LatLng(userBookList.get(6).getLatitude(), userBookList.get(6).getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                                }
                        }
                    });
                    di8.setOnClickListener(new View.OnClickListener(){
                        public void onClick(View v){
                                if(countDiary>7) {
                                    LatLng latLng = new LatLng(userBookList.get(7).getLatitude(), userBookList.get(7).getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                                }
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

            }
        });
        requestQueue.add(jsonObjectRequest);
        //map
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final ImageButton btadd = (ImageButton) findViewById(R.id.btadd);
        btadd.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //after click on add bt
                if(countDiary<8) {
                    Intent intent = new Intent(MapsActivity.this, AddPageActivity.class);
                    intent.putExtra("username", username);
                    intent.putExtra("email", email);
                    intent.putExtra("countDiary", countDiary);
                    intent.putExtra("currentLat", currentLat);
                    intent.putExtra("currentLng", currentLng);
                    MapsActivity.this.startActivity(intent);
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setMessage("You can not save more than 8 diaries, Please delete another diary before add a new diary")
                            .setNegativeButton("Ok", null)
                            .create()
                            .show();
                }
            }
        });
        final ImageButton btlogout = (ImageButton) findViewById(R.id.btlogout);
        btlogout.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //after click on add bt
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }
    //rotate method
    String fileLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) +"";
    private File getFile(){
        File folder = new File(fileLocation);
        if(!folder.exists()){
            folder.mkdir();
        }
        File image_file = new File(folder,username + "_profile.jpg");
        return image_file;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String path = fileLocation + "/"+ username + "_profile.jpg";
        profileview.setImageDrawable(Drawable.createFromPath(path));
        profileview.setRotation(90f);
        profileview.setScaleType(ImageButton.ScaleType.FIT_XY);
    }
    public String isTooLong(String string){
        if(string.length()<15){
            return string;
        }
     return string.substring(0,14)+"..";
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydneyะห, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        final ArrayList<Sqlmarker> sqlmarker = new ArrayList<>();
        mMap = googleMap;
        if(mMap != null){
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter(){

                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    final View v = getLayoutInflater().inflate(R.layout.info_window,null);
                    TextView tTopic = (TextView) v.findViewById(R.id.tTopic);
                    final TextView tUser = (TextView) v.findViewById(R.id.tUser);
                    ImageButton iRead = (ImageButton) v.findViewById(R.id.btRead);
                    iRead.setImageResource(R.mipmap.icon_read);
                    if(marker.getTitle().length()<15) {
                        tTopic.setText(" " + marker.getTitle());
                    }
                    else{
                        tTopic.setText(" " + marker.getTitle().substring(0,14)+"..");
                    }
                    LatLng ll = marker.getPosition();
                    double lat = ll.latitude;
                    double lng = ll.longitude;
                    for(int i = 0 ;i < allBookData.size();i++){
                        if(  (lat == allBookData.get(i).getLatitude()) && (lng == allBookData.get(i).getLongitude()) && (allBookData.get(i).getBookName().equals(marker.getTitle()))){
                            tUser.setText(" ID: "+ allBookData.get(i).getUsername());
                            currentUser =allBookData.get(i).getUsername();
                            messageText = allBookData.get(i).getBookText();
                            currentBookId = allBookData.get(i).getBookID();
                        }
                    }
                    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                        public void onInfoWindowClick(Marker marker)
                        {
                            showDialog(v,username,marker.getTitle(),messageText,currentBookId,currentUser);
                    }});
                    return v;
                }
            });
        }
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        final TextView di2 = (TextView) findViewById(R.id.di2);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                LatLng startLocation = new LatLng(13.734641,100.530260);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(startLocation));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
                requestQueue = Volley.newRequestQueue(getApplicationContext());
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                        showUrl,(JSONObject)null, new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray marker = response.getJSONArray("marker");
                            for(int i = 0 ; i < marker.length(); i++){
                                JSONObject markers = marker.getJSONObject(i);
                                sqlmarker.add(new Sqlmarker(markers.getInt("temp_id"),markers.getString("username"),markers.getDouble("latitude"),
                                        markers.getDouble("longitude"),markers.getString("name"),markers.getString("text"),markers.getInt("bookid")));
                            }
                            for(int i = 0; i < sqlmarker.size();i++){
                                LatLng latLng = new LatLng(sqlmarker.get(i).getLatitude(), sqlmarker.get(i).getLongitude());
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(sqlmarker.get(i).getBookName());
                                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.icon_diary));
                                mMap.addMarker(markerOptions);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                    }
                });
                requestQueue.add(jsonObjectRequest);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        currentLat = location.getLatitude();
        currentLng = location.getLongitude();
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
       // mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

//        //stop location updates
//        if (mGoogleApiClient != null) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//        }

    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }
    public void showDialog(View v,String username, String name,String text,int bookid,String currentUser){
//        MyDialog dialog = new MyDialog(username,name,text,bookid,currentUser);
        MyDialog dialog = MyDialog.newInstance(username,name,text,bookid,currentUser);
        dialog.show(getFragmentManager(),"show_dialog");
    }
    public void refresh(){
        //refresh
                Intent intent = getIntent();
                finish();
                startActivity(intent);
    }
}
class Sqlmarker
{
    public int temp_id;
    public String username;
    public double latitude;
    public double longitude;
    public String bookName;
    public String bookText;
    public int bookID;

    public Sqlmarker(){
        this.temp_id = -1;
        this.username = null;
        this.latitude = -1d;
        this.longitude = -1d;
        this.bookName = null;
        this.bookText = null;
        this.bookID = -1;
    }

    public Sqlmarker(int temp_id,String username,double latitude,double longitude,String bookName,String bookText,int bookID){
        this.temp_id = temp_id;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bookName = bookName;
        this.bookText = bookText;
        this.bookID = bookID;
    }

    public void copy(Sqlmarker obj){
        this.temp_id = obj.temp_id;
        this.username = obj.username;
        this.latitude = obj.latitude;
        this.longitude = obj.longitude;
        this.bookName = obj.bookName;
        this.bookText = obj.bookText;
        this.bookID = obj.bookID;
    }


    public double getLongitude() {
        return longitude;
    }
    public int getBookID() {
        return bookID;
    }
    public int getTemp_id() {
        return temp_id;
    }
    public String getBookName() {
        return bookName;
    }
    public String getBookText() {
        return bookText;
    }
    public String getUsername() {
        return username;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setBookID(int bookID) {
        this.bookID = bookID;
    }
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    public void setBookText(String bookText) {
        this.bookText = bookText;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public void setTemp_id(int temp_id) {
        this.temp_id = temp_id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    // Add constructor, get, set, as needed.
}