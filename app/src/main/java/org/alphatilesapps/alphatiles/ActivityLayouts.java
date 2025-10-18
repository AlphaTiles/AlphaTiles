package org.alphatilesapps.alphatiles;

import static com.google.android.material.color.MaterialColors.isColorLight;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public final class ActivityLayouts {

    public static void applyEdgeToEdge(ComponentActivity activity, int fittedViewId) {

        View view = activity.findViewById(fittedViewId);

        EdgeToEdge.enable(activity);

        // Set Listener
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());

            // Example: only add padding to the bottom if your content needs to avoid the nav bar
            v.setPadding(
                    insets.left,
                    insets.top,    // ✅ Add top padding so content starts below the status bar
                    insets.right,
                    insets.bottom  // ✅ Keep bottom padding for nav bar
            );

            return windowInsets;
        });

    }
    public static void setStatusAndNavColors(Activity activity) {
        Window window = activity.getWindow();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // On R+ / Android 11+ / API 30+, use WindowInsetsController
            window.setDecorFitsSystemWindows(false);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            // Detect background color under the transparent status bar
            View root = window.getDecorView().findViewById(android.R.id.content);
            int statusColor = Color.TRANSPARENT; // default

            Drawable bg = root.getBackground();
            if (bg instanceof ColorDrawable) {
                statusColor = ((ColorDrawable) bg).getColor();
            }

            // Fallback if still transparent or no drawable
            if (statusColor == Color.TRANSPARENT) {
                statusColor = ContextCompat.getColor(activity, android.R.color.background_light);
            }

            // Determine if background is light
            boolean isLightBackground = isColorLight(statusColor);

            // If background is light, force dark icons
            WindowInsetsControllerCompat insetsControllerCompat =
                    new WindowInsetsControllerCompat(window, window.getDecorView());
            insetsControllerCompat.setAppearanceLightStatusBars(isLightBackground);
            insetsControllerCompat.setAppearanceLightNavigationBars(isLightBackground);

        } else {
            // Pre-Android 11
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(activity, R.color.themeBlue));
            window.setNavigationBarColor(ContextCompat.getColor(activity, R.color.themeBlue));
        }
    }
}

