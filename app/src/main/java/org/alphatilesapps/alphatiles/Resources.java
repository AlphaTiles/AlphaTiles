package org.alphatilesapps.alphatiles;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
import java.util.logging.Logger;

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

//        ImageView avatar = findViewById(R.id.activePlayerImage);
//        int resID = getResources().getIdentifier(String.valueOf(Start.AVATAR_IDS[Start.playerNumber - 1]), "drawable", getPackageName());
//        avatar.setImageResource(resID);

        buildResourcesArray();
        loadResources();

        String fileName = "okToDeleteMe2.txt";
        String text = "burrito";

//        writeFile(fileName, text);
//        writeFile2(fileName, text);

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

//                String linkText = "<a href=\"example.jsp?channel=" + val + "&date=" + date + "\">" + val + "</a>";


//                String linkText = "<a href='http://stackoverflow.com'>StackOverflow</a>";
//                promotedText.setText(resourcesList[r + 1][2]);

                promotedText.setText(Html.fromHtml(linkText));
                promotedText.setMovementMethod(LinkMovementMethod.getInstance());


                //                TextView link = findViewById(R.id.resourceText01);
//                String linkText = "<a href='http://stackoverflow.com'>StackOverflow</a>";
//                link.setText(Html.fromHtml(linkText));
//                link.setMovementMethod(LinkMovementMethod.getInstance());

            } else {
                promotedResource.setVisibility(View.INVISIBLE);
                promotedText.setText("");
            }

//            TextView link = findViewById(R.id.resourceText01);
//            String linkText = "<a href='http://stackoverflow.com'>StackOverflow</a>";
//            link.setText(Html.fromHtml(linkText));
//            link.setMovementMethod(LinkMovementMethod.getInstance());

        }
    }

    public void goBackToEarth(View view) {

        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();

    }

    public void writeFile2(String fileName, String text) {
        // https://developer.android.com/training/permissions/requesting
        // Check if the write permission is already available
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Writer permission is already available, execute the write task
                File textFile = new File(Environment.getExternalStorageDirectory(), fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(textFile);
                    fos.write(text.getBytes());
                    fos.close();

                    Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(this, "This is required to export results to a .txt file", Toast.LENGTH_SHORT).show();
                }

                // Request write permission
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

    }

    public void writeFile(String fileName, String text) {
        LOGGER.info("Remember: isExternalStorageWriteable() = " + isExternalStorageWriteable());
        LOGGER.info("Remember: checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) = " + checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));

        if (isExternalStorageWriteable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            File textFile = new File(Environment.getExternalStorageDirectory(), fileName);
            try {
                FileOutputStream fos = new FileOutputStream(textFile);
                fos.write(text.getBytes());
                fos.close();

                Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Cannot write to external storage", Toast.LENGTH_SHORT).show();
        }
    };

    private boolean isExternalStorageWriteable() {
        // https://www.youtube.com/watch?v=7CEcevGbIZU
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        } else {
            return false;
        }
    }


    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        LOGGER.info("Remember: ContextCompat.checkSelfPermission(this, permission) = " + ContextCompat.checkSelfPermission(this, permission));
        LOGGER.info("Remember: PackageManager.PERMISSION_GRANTED = " + PackageManager.PERMISSION_GRANTED);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

}