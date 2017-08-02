package demo.free.com.demomap;
// Created by $USER_NAME on 25-04-2017.

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapClusterView extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    MarkerOptions markerOptions;
    LatLng dragPosition;
    String latlng,statusflag,statuscode,response_msg ;
    List<LocationData> locationDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationDataList = new ArrayList<>();
        GetAllShopList();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
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
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        LocationData ld = new LocationData();
        //Place current location marker
        dragPosition = new LatLng(location.getLatitude(), location.getLongitude());
        latlng = dragPosition.toString();
        markerOptions = new MarkerOptions();
        markerOptions.position(dragPosition);
        markerOptions.title(latlng);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(dragPosition));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
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
        } else    {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
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


    private void GetAllShopList() {

        String Shop_All_url = "https://www.yeldi.com/andappservices/FolkConsumerService.svc/UserAppMerchantShops";
        Log.d("Shop_All_url", Shop_All_url);
        JsonObjectRequest strreq = new JsonObjectRequest(Request.Method.POST, Shop_All_url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                // TODO Auto-generated method stub

                Log.d("profile success respons", "" + response);

                try {

                    JSONObject jsonobj = response.getJSONObject("UserAppMerchantShops");

                    statusflag = jsonobj.optString("result");
                    statuscode = jsonobj.optString("statuscode");
                    response_msg = jsonobj.optString("message");

                    Log.d("Result ", jsonobj.optString("result"));
                    Log.d("code ", jsonobj.optString("statuscode"));
                    Log.d("totalrecords ", jsonobj.optString("totalrecords"));

                    if (statusflag.equalsIgnoreCase("success")) {

                        JSONArray shoplist = jsonobj.getJSONArray("shops");
                        Log.d("length", "" + shoplist.length());
                        for (int i = 0; i < shoplist.length(); i++) {
                            LocationData ld = new LocationData();
                            JSONObject json = null;
                            try {
                                json = shoplist.getJSONObject(i);

                                String lat = json.getString("geolatitude");
                                String longi = json.getString("geolongitude");
                                Log.d("Lat&Long", "" + lat + ", "+longi);
                                double latitude = Double.parseDouble(lat);
                                double longitude = Double.parseDouble(longi);
                                LatLng latLng = new LatLng(latitude,longitude);

                                //move CameraPosition on first result
                                if (i == 0) {
                                    CameraPosition cameraPosition = new CameraPosition.Builder()
                                            .target(latLng).zoom(13).build();

                                    mMap.animateCamera(CameraUpdateFactory
                                            .newCameraPosition(cameraPosition));
                                }
                                 if(json.getString("category").equalsIgnoreCase("Grooming")){
                                     mMap.addMarker(new MarkerOptions()
                                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                                             .title(json.getString("name"))
                                             .snippet("")
                                             .position(latLng));
                                 }else if(json.getString("category").equalsIgnoreCase("F&B")){
                                     mMap.addMarker(new MarkerOptions()
                                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                             .title(json.getString("name"))
                                             .snippet("")
                                             .position(latLng));
                                 }else if(json.getString("category").equalsIgnoreCase("Fashion")){
                                     mMap.addMarker(new MarkerOptions()
                                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                             .title(json.getString("name"))
                                             .snippet("")
                                             .position(latLng));
                                 }else if(json.getString("category").equalsIgnoreCase("Health")){
                                     mMap.addMarker(new MarkerOptions()
                                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                             .title(json.getString("name"))
                                             .snippet("")
                                             .position(latLng));
                                 }else{
                                     mMap.addMarker(new MarkerOptions()
                                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                                             .title(json.getString("name"))
                                             .snippet("")
                                             .position(latLng));
                                 }

                                /*mMap.addMarker(new MarkerOptions()
                                        .title(json.getString("name"))
                                        .position(new LatLng(latitude,longitude)));*/
                              /*  ld.setGeolat(json.getDouble("geolatitude"));
                                ld.setGeolong(json.getDouble("geolongitude"));
                                locationDataList.add(ld);*/
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                    } else if (statusflag.equalsIgnoreCase("failed")) {
                        Toast.makeText(MapClusterView.this, ""+response_msg, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(MapClusterView.this, ""+response_msg, Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception ex) {

                    Log.d("Shoplist Exception", ex.toString());
                    // Toast.makeText(New_ShoppinglistActivity.this, "No more shop to Load", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MapClusterView.this, "please try again later", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub

                Log.d("Error_response", error.toString());
                if (error instanceof NetworkError) {
                    Toast.makeText(MapClusterView.this, "Network Error", Toast.LENGTH_LONG).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(MapClusterView.this, "ServerError", Toast.LENGTH_LONG).show();
                } else if (error instanceof AuthFailureError) {
                } else if (error instanceof ParseError) {
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(MapClusterView.this, "NoConnectionError", Toast.LENGTH_LONG).show();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(MapClusterView.this, "TimeoutError", Toast.LENGTH_LONG).show();

                }
            }
        }) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject mParams = new JSONObject();
                    mParams.put("location", "Arumbakkam,Anna Nagar,Choolaimedu");
                    mParams.put("category", "");
                    mParams.put("sessionid", "e5122271-8f68-43f7-88d0-710ce1339f02");
                    mParams.put("deviceinfo", "000000000000000");

                    JSONObject message = new JSONObject();
                    message.put("userapp", mParams);
                    Log.d("jsonobject", message.toString());
                    return message.toString().getBytes("utf-8");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        };
        strreq.setRetryPolicy(new DefaultRetryPolicy(60000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(strreq);
    }
}
