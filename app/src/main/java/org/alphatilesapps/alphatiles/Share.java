package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;

import android.graphics.Bitmap;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Scanner;

public class Share extends AppCompatActivity {
    Context context;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        setContentView(R.layout.share);

        ActivityLayouts.applyEdgeToEdge(this, R.id.shareCL);
        ActivityLayouts.setStatusAndNavColors(this);

        // the link is in the second line of the file.
        Scanner scanner = new Scanner(getResources().openRawResource(R.raw.aa_share));
        scanner.nextLine();
        String link = scanner.nextLine();

        // code taken from Azahar's comment to the question asked here: https://stackoverflow.com/questions/1016896/how-to-get-screen-dimensions-as-pixels-in-android
        int screenWidth;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            // API 30+
            WindowMetrics windowMetrics = getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            screenWidth = windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            screenWidth = displaymetrics.widthPixels;
        }

        // code taken from https://stackoverflow.com/a/25283174. Thank you Stefano
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(link, BarcodeFormat.QR_CODE, 2*screenWidth/3, 2*screenWidth/3);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            ((ImageView) findViewById(R.id.qrCode)).setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }

    }

    public void goBackToEarth(View view) {
        Intent intent = getIntent();
        intent.setClass(context, Earth.class);
        startActivity(intent);
        finish();
    }
}
