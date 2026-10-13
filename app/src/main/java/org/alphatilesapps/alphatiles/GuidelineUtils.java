package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import androidx.constraintlayout.widget.Guideline;

public class GuidelineUtils {

    /**
     * Updates guidelines for any Activity layout given an array of View IDs and Dimen IDs.
     */
    public static void applyGuidelines(View rootView, Context context, int[][] guidelineMappings) {
        TypedValue typedValue = new TypedValue();

        for (int[] mapping : guidelineMappings) {
            int viewId = mapping[0];
            int dimenId = mapping[1];

            Guideline guideline = rootView.findViewById(viewId);
            if (guideline != null) {
                context.getResources().getValue(dimenId, typedValue, true);
                guideline.setGuidelinePercent(typedValue.getFloat());
            }
        }
    }
}