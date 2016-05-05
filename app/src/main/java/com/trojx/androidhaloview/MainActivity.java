package com.trojx.androidhaloview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HaloBackground haloBackground= (HaloBackground) findViewById(R.id.halo);
        haloBackground.startHaloAnimation();
    }
}
