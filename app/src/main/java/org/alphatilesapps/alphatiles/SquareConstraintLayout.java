package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SquareConstraintLayout extends ConstraintLayout {
    static Logger LOGGER = Logger.getLogger(SquareConstraintLayout.class.getName());
    public SquareConstraintLayout(Context context) {
        super(context);
    }
    public SquareConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
