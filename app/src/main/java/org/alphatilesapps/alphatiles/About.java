package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
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

        ActivityLayouts.applyEdgeToEdge(this, R.id.aboutCL);
        ActivityLayouts.setStatusAndNavColors(this);

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

        TextView photoAudioCredits2 = findViewById(R.id.photoAudioCredits2);
        String mediaTwo = Start.langInfoList.find("Audio and image credits (lang 2)");
        if (mediaTwo.equals("none") || mediaTwo.equals(null)|| mediaTwo.equals("")) {
            photoAudioCredits2.setText("");
        } else {
            photoAudioCredits2.setText(mediaTwo);
            photoAudioCredits2.setMovementMethod(new ScrollingMovementMethod());
            ConstraintLayout constraintLayout = findViewById(R.id.aboutCL);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(photoAudioCredits.getId(),ConstraintSet.BOTTOM,R.id.guidelineH8,ConstraintSet.TOP,0);
//            constraintSet.connect(R.id.guidelineH7,ConstraintSet.TOP,R.id.guidelineH8,ConstraintSet.BOTTOM,0);
            constraintSet.applyTo(constraintLayout);
        }

        TextView email = findViewById(R.id.email);
        email.setAutoLinkMask(Linkify.EMAIL_ADDRESSES);
        String contactEmail = Start.langInfoList.find("Email");
    if (contactEmail.equals("none") || contactEmail.equals(null)|| contactEmail.equals("")) {
            email.setText("");
        } else {
            email.setText(contactEmail);
            email.setMovementMethod(LinkMovementMethod.getInstance());
        }

        TextView privacyPolicy = findViewById(R.id.privacyPolicy);

        String httpText = Start.langInfoList.find("Privacy Policy");
        String linkText = "<a href=\"" + httpText + "\">" + "Privacy Policy" + "</a>";
        CharSequence styledText = Html.fromHtml(linkText);
        privacyPolicy.setText(styledText);
        privacyPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        hidePrivacyPolicy = Boolean.parseBoolean(hidePrivacyPolicySetting);
        if (hidePrivacyPolicy) {
            privacyPolicy.setVisibility(View.GONE);
        } else{
            privacyPolicy.setVisibility(View.VISIBLE);
        }

        String verName = BuildConfig.VERSION_NAME;
        TextView verInfo = findViewById(R.id.appVersionInEnglish);
        verInfo.setText(getString(R.string.ver_info, verName));

        if (scriptDirection.equals("RTL")) {
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        if (!hideSILlogoSetting.equals("")) {
            hideSILlogo = Boolean.parseBoolean(hideSILlogoSetting);

            if (hideSILlogo) {

                ImageView SILlogoImage = (ImageView) findViewById(R.id.logoSILImage);
                SILlogoImage.setVisibility(View.GONE);

                ConstraintLayout constraintLayout = findViewById(R.id.aboutCL);
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                constraintSet.centerHorizontally(R.id.gamesHomeImage, R.id.aboutCL);
                constraintSet.applyTo(constraintLayout);
            }
        } else {//default
            hideSILlogo = false;
        }

        int resID = context.getResources().getIdentifier("zzz_about", "raw", context.getPackageName());
        if (resID == 0) {
            // hide audio instructions icon
            ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
            instructionsButton.setVisibility(View.GONE);

            ConstraintLayout constraintLayout = findViewById(R.id.aboutCL);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.connect(R.id.gamesHomeImage, ConstraintSet.END, R.id.logoSILImage, ConstraintSet.START, 0);
            constraintSet.connect(R.id.logoSILImage, ConstraintSet.START, R.id.gamesHomeImage, ConstraintSet.END, 0);
            constraintSet.applyTo(constraintLayout);
        }

    }

    public void goBackToEarth(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }

    public void playAudioInstructionsAbout(View view) {
        setAllElemsUnclickable();
        int resID = context.getResources().getIdentifier("zzz_about", "raw", context.getPackageName());
        MediaPlayer mp3 = MediaPlayer.create(this, resID);
        mp3.start();
        mp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp3) {
                setAllElemsClickable();
                mp3.release();
            }
        });

    }

    protected void setAllElemsUnclickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.aboutCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(false);
        }
    }

    protected void setAllElemsClickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.aboutCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(true);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceRTLIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void forceLTRIfSupported() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        }
    }

}
