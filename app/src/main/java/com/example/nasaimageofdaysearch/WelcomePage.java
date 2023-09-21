package com.example.nasaimageofdaysearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class WelcomePage extends Activity {
    private static final int SPLASH_DISPLAY_LENGTH = 2000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(WelcomePage.this, MainActivity.class);
                WelcomePage.this.startActivity(mainIntent);
                WelcomePage.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
