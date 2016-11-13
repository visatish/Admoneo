package com.example.vsatish.groceryapp;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by VSatish on 11/12/16.
 */

public class CustomService extends Service {
    public static ArrayList<Store> stores;
    public NotificationManager mNotificationManager;
    public Notification temp;
    public Context my_context;
    public LocationManager mLocationManager;
    public String my_location = "-8.783195,-124.508523"; //middle of the Pacific Ocean so first call will not return any valid places
    public double location_delta = 0;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);

        //Restart the service once it has been killed android


        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 100, restartServicePI);

    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            double original_lat = Double.parseDouble(my_location.split(",")[0]);
            double original_long = Double.parseDouble(my_location.split(",")[1]);

//            double end_lat = location.getLatitude();
//            double end_long = location.getLongitude();
//
//            double lat_difference = original_lat - end_lat;
//            double long_difference = original_long - end_long;
//
//            double a = Math.pow(Math.sin(Math.toRadians(lat_difference/2)), 2) +
//                       Math.cos(Math.toRadians(original_lat)) * Math.cos(Math.toRadians(end_lat)) *
//                       Math.pow(Math.sin(Math.toRadians(long_difference/2)), 2);
//            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//            double d = 6371009 * c;

            float [] distance = new float[1];
            Location.distanceBetween(original_lat, original_long, location.getLatitude(), location.getLongitude(), distance);
            location_delta = distance[0];
            my_location = "" + location.getLatitude() + "," + location.getLongitude();


        }

        @Override
        public void onStatusChanged(String s, int i, Bundle b) {
            //your code here
        }

        @Override
        public void onProviderEnabled(String s) {
            //your code here
        }

        @Override
        public void onProviderDisabled(String s) {
            //your code here
        }

    };

    public static JSONObject parseString(String s) {
        JSONObject object = null;
        try {
            object = (JSONObject) new JSONTokener(s).nextValue();
        } catch (JSONException e) {
            Log.d("ClothingSelector", "JSON Exception: " + e.getMessage());
        }
        return object;
    }
    public ArrayList<Store> read() {
        FileInputStream fis;
        ObjectInputStream ois=null;
        try {
            fis=openFileInput("stores");
            ois=new ObjectInputStream(fis);
            return (ArrayList<Store>)ois.readObject();
        }
        catch(ClassNotFoundException ex)
        {
            Log.e("add customers","serialization problem",ex);
        }
        catch(IOException ex)
        {
            Log.e("add customers","No customers file",ex);
        }
        finally
        {
            try {
                if (ois != null) {
                    ois.close();
                }
        } catch(IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        my_context = getApplicationContext();
        mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setContentTitle("Place Nearby")
//                .setContentText("Download in progress")
//                .setSmallIcon(R.drawable.ic_launcher);
        Log.d("Custom Service", "Service Started");
//        Log.d("Custom Service", stores.toString());

        if (Build.VERSION.SDK_INT >= 24 &&
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
                0, mLocationListener);
        //start a separate thread and start listening to your network object


        Runnable myRunnable = new Runnable() {
            int i = 0;

            @Override
            public void run() {
                Log.d("CustomService", "Location: " + my_location);
                String radius = "1609"; // one mile
                String keyword = "";
                String url = "";
                stores = read();
                if (location_delta > 1610) {
                    for (int i = 0; i <= 1; i++) {
                        Store store = stores.get(i);
                        keyword = store.name.replaceAll("\\s+", "");
                        url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + my_location + "&radius=" + radius + "&keyword=" + keyword + "&key=AIzaSyDtVSemZclXeP0ja7-qHArCrczq9rt-TGI&opennow=true";
                        Log.d("CustomService", "url: " + url);
                        RequestQueue queue = Volley.newRequestQueue(my_context);
                        StringRequest sr = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("CustomService", "Response: " + response);
                                JSONObject parsedResponse = parseString(response);
                                try {
                                    JSONArray results = parsedResponse.getJSONArray("results");
                                    if (results.length() > 0) {
                                        JSONObject place = (JSONObject) results.get(0);
                                        String name = place.getString("name");

                                        //get String of items
                                        String items = "";
                                        for (int i = 0; i < stores.size(); i++) {
                                            Store s = stores.get(i);
                                            if (s.name.equals(name)) {
                                                items = s.items.toString().replaceAll("[\\[\\](){}]", ""); // remove brackets
                                            }
                                        }
                                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(my_context);
                                        mBuilder.setContentTitle(name + " is nearby")
                                                .setContentText("Tasks: " + items)
                                                .setSmallIcon(R.drawable.ic_launcher)
                                                .setAutoCancel(true);

                                        // Creates an explicit intent for an Activity in your app
                                        Intent resultIntent = new Intent(my_context, Pass_Go.class);
                                        resultIntent.putExtra("Name", name);
                                        resultIntent.putExtra("Stores", stores);

                                        // The stack builder object will contain an artificial back stack for the
                                        // started Activity.
                                        // This ensures that navigating backward from the Activity leads out of
                                        // your application to the Home screen.
                                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(my_context);
                                        // Adds the back stack for the Intent (but not the Intent itself)
                                        stackBuilder.addParentStack(Pass_Go.class);
                                        // Adds the Intent that starts the Activity to the top of the stack
                                        stackBuilder.addNextIntent(resultIntent);
                                        PendingIntent resultPendingIntent =
                                                stackBuilder.getPendingIntent(
                                                        0,
                                                        PendingIntent.FLAG_UPDATE_CURRENT
                                                );
                                        mBuilder.setContentIntent(resultPendingIntent);

                                        Random random = new Random();
                                        int m = random.nextInt(9999 - 1000) + 1000;

                                        mNotificationManager.notify(m, mBuilder.build());
                                        Log.d("Custom Service", "Name of Place: " + name);
                                    }
                                } catch (JSONException e) {
                                    Log.d("ClothingSelector", "JSON Exception: " + e.getMessage());
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
//                mPostCommentResponse.requestEndedWithError(error);
//                String body = "Empty";
//                //get status code here
//                String statusCode = String.valueOf(error.networkResponse.statusCode);
//                //get response body and parse with appropriate encoding
//                if(error.networkResponse.data!=null) {
//                    try {
//                        body = new String(error.networkResponse.data,"UTF-8");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
//                }
                                Log.d("ClothingSelector", "Error!");
                                Log.d("ClothingSelector", "Error: " + error);

                                //return control to main activity
//                switchBack();
                            }
                        }) {
                            @Override
                            protected Map<String, String> getParams() {
                                Map<String, String> params = new HashMap<String, String>();
//                                        params.put("location","37.8710434,-122.2507729");
//                                        params.put("radius","8000");
//                                        params.put("type","bakery");
//                                        params.put("keyword","cream berkeley");
//                                        params.put("key","AIzaSyDtVSemZclXeP0ja7-qHArCrczq9rt-TGI");
//                                        params.put("opennow", "true");
                                return params;
                            }

                            //                                    @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
//                                        params.put("location","37.8710434,-122.2507729");
//                                        params.put("radius","8000");
//                                        params.put("type","bakery");
//                                        params.put("keyword","cream berkeley");
//                                        params.put("key","AIzaSyDtVSemZclXeP0ja7-qHArCrczq9rt-TGI");
//                                        params.put("opennow", "true");
                                return params;
                            }
                        };
                        sr.setRetryPolicy(new RetryPolicy() {
                            @Override
                            public int getCurrentTimeout() {
                                return 50000;
                            }

                            @Override
                            public int getCurrentRetryCount() {
                                return 50000;
                            }

                            @Override
                            public void retry(VolleyError error) throws VolleyError {
                                Log.d("ClothingSelector", "Retry Error: " + error);
                            }
                        });

                        queue.add(sr);
                    }
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(myRunnable, 0, 20, TimeUnit.SECONDS);

    }

}
