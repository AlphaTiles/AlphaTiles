package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import static org.alphatilesapps.alphatiles.Start.langInfoList;


public class Celebration extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.celebration);


    }


    public void goBackToEarth(View view)
    {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }



}
