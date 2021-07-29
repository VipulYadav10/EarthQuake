package com.vip.earthquake;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vip.earthquake.activities.QuakesListActivity;
import com.vip.earthquake.model.EarthQ;
import com.vip.earthquake.ui.CustomInfoWindow;
import com.vip.earthquake.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private RequestQueue queue;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private BitmapDescriptor[] iconColors;
    private Button showListBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showListBtn = findViewById(R.id.showListBtn);

        showListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, QuakesListActivity.class));
            }
        });

        iconColors = new BitmapDescriptor[] {
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE),
                BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)

        };


        queue = Volley.newRequestQueue(this);

        getEarthquakes();
    }

    /*
        Get all earthquakes objects.
     */
    private void getEarthquakes() {
        final EarthQ earthQ = new EarthQ();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.URL,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray features = response.getJSONArray("features");
                            for(int i=0;i<features.length();i++) {
                                JSONObject properties = features.getJSONObject(i).getJSONObject("properties");
//                                if(!properties.getString("place").contains("India"))
//                                    continue;

                                // get Geometry object
                                JSONObject geometry = features.getJSONObject(i).getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");

                                double lon = coordinates.getDouble(0);
                                double lat = coordinates.getDouble(1);

//                                Log.d("PLACE", "onResponse: " + properties.getString("place")  + lon + " " + lat);
                                earthQ.setPlace(properties.getString("place"));
                                earthQ.setType(properties.getString("type"));
                                earthQ.setTime(properties.getLong("time"));
                                earthQ.setLat(lat);
                                earthQ.setLon(lon);
                                earthQ.setMagnitude(properties.getDouble("mag"));
                                earthQ.setDetailLink(properties.getString("detail"));

                                java.text.DateFormat dateFormat = java.text.DateFormat.getDateInstance();
                                String formattedDate = dateFormat.format(new Date(properties.getLong("time"))
                                        .getTime());

                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.icon(iconColors[Constants.randomInt(0, iconColors.length)]);
                                markerOptions.title(earthQ.getPlace());
                                markerOptions.position(new LatLng(lat, lon));
                                markerOptions.snippet("Magnitude: " + earthQ.getMagnitude() + "\n" +
                                        "Date: " + formattedDate);

                                // add circle to markers that have magnitude > x
                                if(earthQ.getMagnitude() >= 2.0) {
                                    CircleOptions circleOptions = new CircleOptions();
                                    circleOptions.center(new LatLng(earthQ.getLat(), earthQ.getLon()));
                                    circleOptions.radius(300);
                                    circleOptions.strokeWidth(3.6f);
                                    circleOptions.fillColor(Color.RED);
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                    mMap.addCircle(circleOptions);
                                }

                                Marker marker = mMap.addMarker(markerOptions);
                                marker.setTag(earthQ.getDetailLink());
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 1));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setInfoWindowAdapter(new CustomInfoWindow(getApplicationContext()));
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMarkerClickListener(this);

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if(Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
//            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            mMap.addMarker(new MarkerOptions()
//                .position(latLng)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
//                    .setTitle("Hello");
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 8));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);


        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        getQuakeDetails(marker.getTag().toString());
//        Toast.makeText(getApplicationContext(), marker.getTag().toString(), Toast.LENGTH_SHORT)
//                .show();
    }

    private void getQuakeDetails(String url) {
                final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                        url, new Response.Listener<JSONObject>() {
                    @Override
            public void onResponse(JSONObject response) {
                String detailsUrl = "";
                try {
                    JSONObject properties = response.getJSONObject("properties");
                    JSONObject products = properties.getJSONObject("products");


                    try {
                        if(products.has("nearby-cities") && products.getString("nearby-cities") != null) {
                            JSONArray nearby = products.getJSONArray("nearby-cities");
                            //                    JSONObject contents = nearby.getJSONObject(0);
                            for (int i = 0; i < nearby.length(); i++) {
                                JSONObject nearbyObj = nearby.getJSONObject(i);

                                JSONObject contents = nearbyObj.getJSONObject("contents");
                                JSONObject nearbyJSON = contents.getJSONObject("nearby-cities.json");

                                detailsUrl = nearbyJSON.getString("url");

                            }
                        }
                        else {
                            Toast.makeText(MapsActivity.this, "No Data Available", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.d("URL: ", detailsUrl);
                    getMoreDetails(detailsUrl);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(jsonObjectRequest);
    }

    public void getMoreDetails(String url) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {

                dialogBuilder = new AlertDialog.Builder(MapsActivity.this);
                View view = getLayoutInflater().inflate(R.layout.popup, null);

                Button dismissButton = view.findViewById(R.id.dismissPop);
                Button dismissButtonTop = view.findViewById(R.id.dismissPopTop);
                TextView popList = view.findViewById(R.id.popList);
                WebView htmlPop = view.findViewById(R.id.htmlWebView);

                StringBuilder stringBuilder = new StringBuilder();

                for(int i=0; i<response.length(); i++) {
                    try {
                        JSONObject citiesObject = response.getJSONObject(i);
                        stringBuilder.append("City: ")
                                .append(citiesObject.getString("name"))
                                .append("\n").append("Distance: ")
                                .append(citiesObject.getString("distance"))
                                .append("\n").append("Population: ")
                                .append(citiesObject.getString("population"));

                        stringBuilder.append("\n\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    popList.setText(stringBuilder);

                    dismissButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dismissButtonTop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    // to remove the parent view
                    if(view.getParent() != null) {
                        ((ViewGroup)view.getParent()).removeView(view);
                    }
                    dialogBuilder.setView(view);
                    dialog = dialogBuilder.create();
                    dialog.show();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(jsonArrayRequest);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}