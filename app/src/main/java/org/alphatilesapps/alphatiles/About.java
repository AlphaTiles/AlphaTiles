package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.segment.analytics.Analytics;

import static org.alphatilesapps.alphatiles.Settings.forceRTL;

public class About extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.about);

        setTitle(Start.localAppName);

        TextView localName = findViewById(R.id.gameNameInLOP);
        localName.setText(Start.localAppName);
        TextView lgNamesPlusCountry = findViewById(R.id.langNamesPlusCountry);
        if (Start.langInfoList.find("(In Local Lang)").equals(Start.langInfoList.find("(In English)"))) {
            lgNamesPlusCountry.setText(context.getString(R.string.names_plus_countryB,
                    Start.langInfoList.find("(In Local Lang)"),
                    Start.langInfoList.find("(In English)"),
                    Start.langInfoList.find("Country"))); // RR, KP
        } else {
            lgNamesPlusCountry.setText(context.getString(R.string.names_plus_countryA,
                    Start.langInfoList.find("(In Local Lang)"),
                    Start.langInfoList.find("(In English)"),
                    Start.langInfoList.find("Country"))); // RR, KP
        }

        TextView photoAudioCredits = findViewById(R.id.photoAudioCredits);
        photoAudioCredits.setText(Start.langInfoList.find("Audio and image credits"));

        String verName = BuildConfig.VERSION_NAME;
        TextView verInfo = findViewById(R.id.appVersionInEnglish);
        verInfo.setText(getString(R.string.ver_info, verName));

        if(forceRTL){
            forceRTLIfSupported();
        }
        else{
            forceLTRIfSupported();
        }

    }

    public void goBackToEarth(View view)
    {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceLTRIfSupported()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

}
