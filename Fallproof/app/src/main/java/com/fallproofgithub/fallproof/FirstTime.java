package com.fallproofgithub.fallproof;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class FirstTime extends AppCompatActivity {

    Button buttonSubmit;
    EditText username;
    EditText deviceId;

    String url = "http://172.29.9.87:1323/register/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_time);

        username = findViewById(R.id.inputUsername);
        deviceId = findViewById(R.id.deviceId);

        buttonSubmit = findViewById(R.id.buttonSubmit);

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context context = getApplicationContext();
                int duration = Toast.LENGTH_SHORT;

                if (username.getText().length() == 0) {
                    CharSequence text = "Please provide an username.";
                    Toast.makeText(context, text, duration).show();
                } else if (deviceId.getText().length() == 0) {
                    CharSequence text = "Please provide the correct device id";
                    Toast.makeText(context, text, duration).show();
                } else {

                    RequestQueue requestQueue = Volley.newRequestQueue(FirstTime.this);

                    StringRequest sr = new StringRequest(Request.Method.GET, url + username.getText().toString() + "/" + deviceId.getText().toString(),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
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

                    final String PREFS_NAME = "MyPrefsFile";
                    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                    settings.edit().putString("username", username.getText().toString()).apply();
                    Intent intent = new Intent(FirstTime.this, MainActivity.class);
                    startActivity(intent);

                }
            }
        });

    }
}
