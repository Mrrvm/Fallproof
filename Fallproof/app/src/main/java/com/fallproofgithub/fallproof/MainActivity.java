package com.fallproofgithub.fallproof;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    Button buttonSos;
    Button buttonOnit;
    boolean isPressed = false;
    boolean isDown = false;
    boolean emergency = false;
    boolean emergencyColor = false;
    TextView emergencyText;
    Button outEmergency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String PREFS_NAME = "MyPrefsFile";

        final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        buttonSos = (Button) findViewById(R.id.buttonSos);
        buttonOnit = (Button) findViewById(R.id.buttonOnit);
        emergencyText = (TextView) findViewById(R.id.textView6);
        outEmergency = (Button) findViewById(R.id.buttonOutEmergency);

        buttonOnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPressed=true;
            }
        });

        buttonSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Calling SOS!", Toast.LENGTH_SHORT).show();
            }
        });

        outEmergency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emergency = false;
            }
        });

        if (settings.getBoolean("my_first_time", true)) {

            // first time task
            // record the fact that the app has been started at least once
            settings.edit().putBoolean("my_first_time", false).commit();
            Intent intent = new Intent(this, MainFirstTime.class);
            startActivity(intent);
        } else {
            // use this to start and trigger a service
            Context context = MainActivity.this;
            Intent i = new Intent(context, MyService.class);
            context.startService(i);

            Thread t = new Thread() {

                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            Thread.sleep(500);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final String PREFS_NAME = "MyPrefsFile";

                                    final SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                                    // update TextView here!
                                    TextView textView = (TextView) findViewById(R.id.textView2Update);
                                    //TextView textViewTime = (TextView) findViewById(R.id.textViewTime);


                                    //textView.setText(String.valueOf(settings.getFloat("ainfo", (float) 0)));


                                    if (emergency) {
                                        ConstraintLayout cl = findViewById(R.id.mainProcess);
                                        if (emergencyColor) {
                                            cl.setBackgroundColor(Color.RED);
                                        } else {
                                            cl.setBackgroundColor(Color.BLUE);
                                        }
                                        emergencyColor = !emergencyColor;
                                        emergencyText.setText("António's Current state is");
                                    } else {
                                        outEmergency.setVisibility(View.INVISIBLE);
                                        if (settings.getBoolean("emergency", false)) {
                                            emergencyText.setText("ANTÓNIO IS IN DANGER!");
                                            textView.setText("112 IS ON THE WAY");
                                            ConstraintLayout cl = findViewById(R.id.mainProcess);
                                            emergency = true;
                                            outEmergency.setVisibility(View.VISIBLE);
                                            if (emergencyColor) {
                                                cl.setBackgroundColor(Color.RED);
                                            } else {
                                                cl.setBackgroundColor(Color.BLUE);
                                            }
                                            emergencyColor = !emergencyColor;
                                        } else {
                                            boolean fell = settings.getBoolean("fell", false);
                                            if (fell) {
                                                isDown = true;
                                            }

                                            if (isPressed || !isDown) {
                                                textView.setText("Stable");
                                                buttonOnit.setVisibility(View.INVISIBLE);
                                                buttonSos.setVisibility(View.INVISIBLE);
                                                ConstraintLayout cl = findViewById(R.id.mainProcess);
                                                cl.setBackgroundColor(Color.parseColor("#33cfe8"));
                                                isDown = false;
                                            }

                                            if (isDown) {
                                                textView.setText("Unstable");
                                                buttonOnit.setVisibility(View.VISIBLE);
                                                buttonSos.setVisibility(View.VISIBLE);
                                                ConstraintLayout cl = findViewById(R.id.mainProcess);
                                                cl.setBackgroundColor(Color.RED);
                                            }
                                        }
                                    }
                                }

                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };

            t.start();
        }

    }
}
