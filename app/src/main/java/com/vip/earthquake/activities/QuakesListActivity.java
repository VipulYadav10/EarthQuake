package com.vip.earthquake.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vip.earthquake.R;
import com.vip.earthquake.model.EarthQ;
import com.vip.earthquake.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuakesListActivity extends AppCompatActivity {

    private ArrayList<String> arrayList;
    private ListView listView;
    private RequestQueue queue;
    private ArrayAdapter arrayAdapter;
    private List<EarthQ> quakeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quakes_list);

        quakeList = new ArrayList<>();
        listView = findViewById(R.id.listView);

        queue = Volley.newRequestQueue(this);

        arrayList = new ArrayList<>();
        getAllQuakes(Constants.URL);
    }

    void getAllQuakes(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONArray features = response.getJSONArray("features");
                    for (int i = 0; i < features.length(); i++) {
                        EarthQ earthQ = new EarthQ();
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

                        arrayList.add(earthQ.getPlace());
                        quakeList.add(earthQ);
                    }

                    arrayAdapter = new ArrayAdapter<>(QuakesListActivity.this, android.R.layout.simple_list_item_1,
                            android.R.id.text1, arrayList);
                    listView.setAdapter(arrayAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Toast.makeText(getApplicationContext(), "Magnitude: " + quakeList.get(position).getMagnitude(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                    arrayAdapter.notifyDataSetChanged();
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
}