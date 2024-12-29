package org.alphatilesapps.alphatiles;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// https://developer.android.com/develop/ui/views/layout/edge-to-edge-manually
// https://stackoverflow.com/questions/76868700/set-insets-for-all-activities-android
// https://stackoverflow.com/questions/57293449/go-edge-to-edge-on-android-correctly-with-windowinsets

public final class ActivityLayouts {

    public static void applyEdgeToEdge(ComponentActivity activity, int fittedViewId) {

        View view = activity.findViewById(fittedViewId);

        EdgeToEdge.enable(activity);

        // Set Listener
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            mlp.bottomMargin = insets.bottom;
            mlp.leftMargin = insets.left;
            mlp.rightMargin = insets.right;
            v.setLayoutParams(mlp);
            return windowInsets;
        });

    }
    public static void setStatusAndNavColors (Activity activity) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(activity.getResources().getColor(R.color.themeBlue));
        window.setNavigationBarColor(activity.getResources().getColor(R.color.themeBlue));
    }

}