package com.fallproofgithub.fallproof;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {

    String url = "http://172.29.9.87:1323/info/";

    private Timer timer = new Timer();

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendRequestToServer();   //Your code here
            }
        }, 0, 10*1000);//5 Minutes

    }

    public void sendRequestToServer() {

        final String PREFS_NAME = "MyPrefsFile";
        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String username = settings.getString("username", null);

        RequestQueue requestQueue = Volley.newRequestQueue(MyService.this);

        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, url + username, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean fell = response.getBoolean("fell");
                            double ainfo = response.getDouble("ainfo");
                            String time = response.getString("time");
                            boolean emergency = response.getBoolean("emergency");
                            Log.d("----> FELL : ", String.valueOf(fell));
                            Log.d("----> AINFO : ", String.valueOf(ainfo));
                            Log.d("----> TIME : ", String.valueOf(time));
                            Log.d("----> EMERGENCY : ", String.valueOf(emergency));
                            settings.edit().putFloat("ainfo", (float) ainfo).apply();
                            settings.edit().putBoolean("fell", fell).apply();
                            settings.edit().putString("time", time).apply();
                            settings.edit().putBoolean("emergency", emergency).apply();

                            if (fell) {
                                addNotification(time);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e("HttpClient", "success! response: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("HttpClient", "error: " + error.toString());

                    }
                });

        requestQueue.add(sr);
    }

    private void addNotification(String time) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.danger)
                        .setContentTitle("DANGER")
                        .setContentText("Your elderly fell and can be in danger.");

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
    }

}
