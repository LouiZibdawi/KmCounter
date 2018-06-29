package com.example.louizibdawi.kmcounterandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity{

    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        title = (TextView) findViewById(R.id.splashTitle);

        Animation myanim = AnimationUtils.loadAnimation(this, R.anim.mytransition);

        title.startAnimation(myanim);

        Intent main = new Intent(this, MainActivity.class);

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    startActivity(main);
                    finish();
                }
            }
        };

        timer.start();
    }
}
