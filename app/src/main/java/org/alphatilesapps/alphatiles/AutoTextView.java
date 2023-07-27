package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;


import androidx.annotation.Nullable;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AutoTextView extends androidx.appcompat.widget.AppCompatTextView {
    static Logger LOGGER = Logger.getLogger(AutoTextView.class.getName());
    float baselinePercent = 0.7f;
    float maximumDescend = 0.95f;
    float maximumAscend = 0.05f;
    float leftMargin = 0.03f, rightMargin = 0.03f, bottomMargin = 0.03f, topMargin = 0.03f;
    public AutoTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                update = true;
                text = getText();
            }
        });
    }


    public float getBaselinePercent() {
        return baselinePercent;
    }

    public float getBottomMargin() {
        return bottomMargin;
    }

    public float getLeftMargin() {
        return leftMargin;
    }

    public float getMaximumAscend() {
        return maximumAscend;
    }

    public float getMaximumDescend() {
        return maximumDescend;
    }

    public void setBaselinePercent(float baselinePercent) {
        update = true;
        this.baselinePercent = baselinePercent;
    }

    public void setBottomMargin(float bottomMargin) {
        update = true;
        this.bottomMargin = bottomMargin;
    }

    public void setLeftMargin(float leftMargin) {
        update = true;
        this.leftMargin = leftMargin;
    }

    public void setMaximumAscend(float maximumAscend) {
        update = true;
        this.maximumAscend = maximumAscend;
    }

    public void setMaximumDescend(float maximumDescend) {
        update = true;
        this.maximumDescend = maximumDescend;
    }

    public void setRightMargin(float rightMargin) {
        update = true;
        this.rightMargin = rightMargin;
    }

    public void setTopMargin(float topMargin) {
        update = true;
        this.topMargin = topMargin;
    }
    float textX = 0;
    float textY = 0;
    int lastWidth = 0;
    int lastHeight = 0;
    boolean update = true;
    CharSequence text;
    @Override
    public void draw(Canvas canvas) {
        if(update || lastWidth != getWidth() || lastHeight != getHeight()) {
            float textSize = getTextSize();
            setTextSize(0.0f);
            super.draw(canvas);
            setTextSize(textSize);
            update = false;
            lastWidth = getWidth();
            lastHeight = getHeight();
            Paint paint = getPaint();
            Rect r = new Rect();
            paint.setTextSize(getWidth());
            paint.getTextBounds(text.toString(), 0, text.length(), r);
            float width = r.width();
            float leftM = leftMargin * getWidth();
            float rightM = rightMargin * getWidth();
            float topM = topMargin * getHeight();
            float bottomM = bottomMargin * getHeight();
            float ascent = r.top;
            float descent = r.bottom;
            textY = (getHeight() - topM - bottomM) * baselinePercent + topM;
            float descentY = (getHeight() - topM - bottomM) * maximumDescend + topM;
            float ascentY = (getHeight() - topM - bottomM) * maximumAscend + topM;
            float descentScale = Math.abs((textY-descentY)/descent);
            float ascentScale = Math.abs((textY-ascentY)/ascent);
            float widthScale = (getWidth()-leftM-rightM)/width;
            float scale = Math.min(descentScale, Math.min(ascentScale, widthScale));
            width *= scale;
            textX = leftM;
            if(getTextAlignment() == TEXT_ALIGNMENT_CENTER) {
                textX += (getWidth() - leftM - rightM - width)/2;
            }
            paint.setTextSize(paint.getTextSize() * scale);
        }
        canvas.drawText(text, 0, text.length(), textX, textY, getPaint());
    }

}
