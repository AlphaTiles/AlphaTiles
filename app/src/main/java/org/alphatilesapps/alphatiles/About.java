package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class About extends AppCompatActivity {

    Context context;
    String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");
    String hideSILlogoSetting = Start.settingsList.find("Hide SIL logo");
    String hidePrivacyPolicySetting = Start.settingsList.find("Hide privacy policy");
    Boolean hideSILlogo;
    Boolean hidePrivacyPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.about);

        setTitle(Start.localAppName);

        TextView localName = findViewById(R.id.gameNameInLOP);
        localName.setText(Start.localAppName);
        TextView lgNamesPlusCountry = findViewById(R.id.langNamesPlusCountry);
        if (Start.langInfoList.find("Lang Name (In Local Lang)").equals(Start.langInfoList.find("Lang Name (In English)"))) {
            lgNamesPlusCountry.setText(context.getString(R.string.names_plus_countryB,
                    Start.langInfoList.find("Lang Name (In Local Lang)"),
                    Start.langInfoList.find("Lang Name (In English)"),
                    Start.langInfoList.find("Country"))); // RR, KP
        } else {
            lgNamesPlusCountry.setText(context.getString(R.string.names_plus_countryA,
                    Start.langInfoList.find("Lang Name (In Local Lang)"),
                    Start.langInfoList.find("Lang Name (In English)"),
                    Start.langInfoList.find("Country"))); // RR, KP
        }

        TextView photoAudioCredits = findViewById(R.id.photoAudioCredits);
        photoAudioCredits.setText(Start.langInfoList.find("Audio and image credits"));
        photoAudioCredits.setMovementMethod(new ScrollingMovementMethod());

        TextView privacyPolicy = findViewById(R.id.privacyPolicy);

        String httpText = "https://alphatilesapps.org/privacypolicy.html";
        String displayText = "Alpha Tiles Privacy Policy";
        String linkText = "<a href=\"" + httpText + "\">" + displayText + "</a>";
        privacyPolicy.setText(Html.fromHtml(linkText));
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        hidePrivacyPolicy = Boolean.parseBoolean(hidePrivacyPolicySetting);
        if(hidePrivacyPolicy) {
            privacyPolicy.setVisibility(View.GONE);
        }

        String verName = BuildConfig.VERSION_NAME;
        TextView verInfo = findViewById(R.id.appVersionInEnglish);
        verInfo.setText(getString(R.string.ver_info, verName));

        if(scriptDirection.compareTo("RTL") == 0){
            forceRTLIfSupported();
        }
        else{
            forceLTRIfSupported();
        }

        if(hideSILlogoSetting.compareTo("")!=0){
            hideSILlogo = Boolean.parseBoolean(hideSILlogoSetting);

            if(hideSILlogo){

                ImageView SILlogoImage = (ImageView) findViewById(R.id.logoSILImage);
                SILlogoImage.setVisibility(View.GONE);

                ConstraintLayout constraintLayout = findViewById(R.id.aboutCL);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.centerHorizontally(R.id.gamesHomeImage, R.id.aboutCL);
                constraintSet.applyTo(constraintLayout);
            }
        }
        else{//default
            hideSILlogo = false;
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
