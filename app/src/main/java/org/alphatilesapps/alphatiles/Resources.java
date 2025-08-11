package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Scanner;


public class Resources extends AppCompatActivity {

    Context context;
    String scriptDirection = Start.langInfoList.find("Script direction (LTR or RTL)");

    static int resourcesArraySize;      // the number of resources (plus the header row) in aa_resources.txt, as determined below
    static String[][] resourcesList;     // will capture the name, link and image name [3 items]
    int resourcesInUse; // number of keys in the language's total keyboard
    int resourcesScreenNo = 1; // for languages with more than 6 resources, page 1 will have 6 resources with arrows to advance
    int totalScreens; // the total number of screens required to show all keys
    int partial; // the number of visible keys on final partial screen


    private static final int[] RESOURCES = {
            R.id.resourcePromo01, R.id.resourcePromo02, R.id.resourcePromo03, R.id.resourcePromo04, R.id.resourcePromo05, R.id.resourcePromo06
    };

    private static final int[] RESOURCE_TEXTS = {
            R.id.resourceText01, R.id.resourceText02, R.id.resourceText03, R.id.resourceText04, R.id.resourceText05, R.id.resourceText06
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.resources);

        ActivityLayouts.applyEdgeToEdge(this, R.id.resourcesCL);
        ActivityLayouts.setStatusAndNavColors(this);

        buildResourcesArray();
        loadResources();

        if (scriptDirection.equals("RTL")) {
            ImageView backwardImage = (ImageView) findViewById(R.id.backward);
            ImageView forwardImage = (ImageView) findViewById(R.id.forward);

            backwardImage.setRotationY(180);
            forwardImage.setRotationY(180);
            forceRTLIfSupported();
        } else {
            forceLTRIfSupported();
        }

        int resID = context.getResources().getIdentifier("zzz_resources", "raw", context.getPackageName());
        if (resID == 0) {
            // hide audio instructions icon
            ImageView instructionsButton = (ImageView) findViewById(R.id.instructions);
            instructionsButton.setVisibility(View.GONE);

            ConstraintLayout constraintLayout = findViewById(R.id.resourcesCL);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(constraintLayout);
            constraintSet.centerHorizontally(R.id.gamesHomeImage, R.id.earthCL);
            constraintSet.applyTo(constraintLayout);
        }

    }

    public void buildResourcesArray() {

        boolean header = true;
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_resources));

        String[][] tempResourcesList = new String[30][3];      // arbitrary upper limit of 30 resources

        resourcesArraySize = 0;     // is this necessary?
        while (scanner.hasNext()) {

            if (scanner.hasNextLine()) {
                if (header) {
                    String thisLine = scanner.nextLine();
                    header = false;
                } else {
                    String thisLine = scanner.nextLine();
                    String[] thisLineArray = thisLine.split("\t", 3);
                    tempResourcesList[resourcesArraySize][0] = thisLineArray[0];
                    tempResourcesList[resourcesArraySize][1] = thisLineArray[1];
                    tempResourcesList[resourcesArraySize][2] = thisLineArray[2];
                    resourcesArraySize++;
                }
            }
        }

        resourcesList = new String[resourcesArraySize][3];

        for (int i = 0; i < resourcesArraySize; i++) {

            resourcesList[i][0] = tempResourcesList[i][0];
            resourcesList[i][1] = tempResourcesList[i][1];
            resourcesList[i][2] = tempResourcesList[i][2];

        }

    }

    public void loadResources() {

        resourcesInUse = Resources.resourcesArraySize;
        partial = resourcesInUse % (RESOURCES.length);
        totalScreens = resourcesInUse / (RESOURCES.length);

        if (partial != 0) {
            totalScreens++;
        }

        int visibleResources;

        ImageView goBackward = findViewById(R.id.backward);
        ImageView goForward = findViewById(R.id.forward);

        if (resourcesInUse > RESOURCES.length) {

            visibleResources = RESOURCES.length;
            goBackward.setVisibility(View.VISIBLE);
            goForward.setVisibility(View.VISIBLE);

        } else {

            visibleResources = resourcesInUse;
            goBackward.setVisibility(View.INVISIBLE);
            goForward.setVisibility(View.INVISIBLE);

        }

        for (int r = 0; r < RESOURCES.length; r++) {

            ImageView promotedResource = findViewById(RESOURCES[r]);

            if (r < visibleResources) {

                int resID = getResources().getIdentifier(resourcesList[r][2], "drawable", getPackageName());
                promotedResource.setVisibility(View.VISIBLE);
                promotedResource.setImageResource(resID);
                promotedResource.setVisibility(View.VISIBLE);

                TextView promotedText = findViewById(RESOURCE_TEXTS[r]);
                String httpText = resourcesList[r][1];
                String displayText = resourcesList[r][0];
                String linkText = "<a href=\"" + httpText + "\">" + displayText + "</a>";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  // API 24+
                    promotedText.setText(Html.fromHtml(linkText, Html.FROM_HTML_MODE_LEGACY));
                } else {
                    promotedText.setText(Html.fromHtml(linkText));
                }
                promotedText.setMovementMethod(LinkMovementMethod.getInstance());

            } else {

                promotedResource.setVisibility(View.INVISIBLE);

            }

        }


        for (int r = 0; r < RESOURCES.length; r++) {

            TextView promotedText = findViewById(RESOURCE_TEXTS[r]);

            if (r < visibleResources) {
                promotedText.setVisibility(View.VISIBLE);
                promotedText.setClickable(true);
            } else {
                promotedText.setVisibility(View.INVISIBLE);
                promotedText.setClickable(false);
            }

        }


    }

    private void updateResources() { // This routine will only be called when there are seven or more resources (the layout has space for six)

        int resourcesLimit;
        if (totalScreens == resourcesScreenNo) {
            resourcesLimit = partial;
            for (int r = resourcesLimit; r < (RESOURCES.length); r++) {
                ImageView promotedResource = findViewById(RESOURCES[r]);
                TextView promotedText = findViewById(RESOURCE_TEXTS[r]);
                promotedResource.setVisibility(View.INVISIBLE);
                promotedText.setVisibility(View.INVISIBLE);
            }
        } else {
            resourcesLimit = RESOURCES.length;
        }

        for (int r = 0; r < resourcesLimit; r++) {
            ImageView promotedResource = findViewById(RESOURCES[r]);
            TextView promotedText = findViewById(RESOURCE_TEXTS[r]);

            int resourceIndex = (6 * (resourcesScreenNo - 1)) + r;

            int resID = getResources().getIdentifier(resourcesList[resourceIndex][2], "drawable", getPackageName());
            promotedResource.setImageResource(resID);

            String httpText = resourcesList[resourceIndex][1];
            String displayText = resourcesList[resourceIndex][0];
            String linkText = "<a href=\"" + httpText + "\">" + displayText + "</a>";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {  // API 24+
                promotedText.setText(Html.fromHtml(linkText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                promotedText.setText(Html.fromHtml(linkText));
            }
            promotedText.setMovementMethod(LinkMovementMethod.getInstance());

            promotedResource.setVisibility(View.VISIBLE);
            promotedText.setVisibility(View.VISIBLE);
        }

    }

    public void nextPage(View view) {

        resourcesScreenNo++;
        if (resourcesScreenNo > totalScreens) {
            resourcesScreenNo = totalScreens;
        }

        updateResources();

    }

    public void previousPage(View view) {

        resourcesScreenNo--;
        if (resourcesScreenNo < 1) {
            resourcesScreenNo = 1;
        }

        updateResources();

    }

    public void goBackToEarth(View view) {

        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();

    }

    public void playAudioInstructionsResources(View view) {
        setAllElemsUnclickable();
        int resID = context.getResources().getIdentifier("zzz_resources", "raw", context.getPackageName());
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
        ConstraintLayout parentLayout = findViewById(R.id.resourcesCL);

        // Disable clickability of all child views
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);
            child.setClickable(false);
        }
    }

    protected void setAllElemsClickable() {
        // Get reference to the parent layout container
        ConstraintLayout parentLayout = findViewById(R.id.resourcesCL);

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