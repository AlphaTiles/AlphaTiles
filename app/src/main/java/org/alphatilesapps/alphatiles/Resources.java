package org.alphatilesapps.alphatiles;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Scanner;
import java.util.logging.Logger;

import static org.alphatilesapps.alphatiles.Settings.forceRTL;

public class Resources extends AppCompatActivity {

    Context context;

    static int resourcesArraySize;      // the number of resources (plus the header row) in aa_resources.txt, as determined below
    static String[][] resourcesList;     // will capture the name, link and image name [3 items]

    private static final int[] RESOURCES = {
            R.id.resourcePromo01, R.id.resourcePromo02, R.id.resourcePromo03, R.id.resourcePromo04, R.id.resourcePromo05, R.id.resourcePromo06
    };

    private static final int[] RESOURCE_TEXTS = {
            R.id.resourceText01, R.id.resourceText02, R.id.resourceText03, R.id.resourceText04, R.id.resourceText05, R.id.resourceText06
    };

    private static final Logger LOGGER = Logger.getLogger( Resources.class.getName() );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.resources);

        setTitle(Start.localAppName);

        buildResourcesArray();
        loadResources();

        if(forceRTL){
            forceRTLIfSupported();
        }

    }

    @Override
    public void onBackPressed() {
        // no action
    }

    public void buildResourcesArray() {

        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_resources)); // prep scan of aa_resources.txt

        String[][] tempResourcesList = new String[8][3];      // 7 = hard-coded at six until you make a scrollable window for more links

        resourcesArraySize = 0;     // is this necessary?
        while(scanner.hasNext()) {

            if (scanner.hasNextLine()) {
                String thisLine = scanner.nextLine();
                String[] thisLineArray = thisLine.split("\t", 3);
                tempResourcesList[resourcesArraySize][0] = thisLineArray[0];
                tempResourcesList[resourcesArraySize][1] = thisLineArray[1];
                tempResourcesList[resourcesArraySize][2] = thisLineArray[2];
                resourcesArraySize++;
            }
        }

        resourcesList = new String [resourcesArraySize][3];

        for (int i = 0; i < resourcesArraySize; i++ ) {

            resourcesList[i][0] = tempResourcesList[i][0];
            resourcesList[i][1] = tempResourcesList[i][1];
            resourcesList[i][2] = tempResourcesList[i][2];

        }

    }

    public void loadResources() {

        for (int r = 0; r < RESOURCES.length; r++ ) {

            LOGGER.info("Remember: r = " + r);

            ImageView promotedResource = findViewById(RESOURCES[r]);
            TextView promotedText = findViewById(RESOURCE_TEXTS[r]);
            if (r + 1 < resourcesArraySize) {
                int resID = getResources().getIdentifier(resourcesList[r + 1][2], "drawable", getPackageName());
                promotedResource.setImageResource(resID);
                promotedResource.setVisibility(View.VISIBLE);

                String httpText = resourcesList[r + 1][1];
                String displayText = resourcesList[r + 1][0];

                String linkText = "<a href=\"" + httpText + "\">" + displayText + "</a>";

                promotedText.setText(Html.fromHtml(linkText));
                promotedText.setMovementMethod(LinkMovementMethod.getInstance());

            } else {
                promotedResource.setVisibility(View.INVISIBLE);
                promotedText.setText("");
            }

        }
    }

    public void goBackToEarth(View view) {

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

}