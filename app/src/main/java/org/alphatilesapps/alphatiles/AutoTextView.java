package org.alphatilesapps.alphatiles;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;


import androidx.annotation.Nullable;

public class AutoTextView extends androidx.appcompat.widget.AppCompatTextView {
    float baselinePercent = 0.8f;
    float maximumDescend = 0.95f;
    float maximumAscend = 0.05f;
    float leftMargin = 0.03f, rightMargin = 0.03f, bottomMargin = 0.03f, topMargin = 0.03f;

    public AutoTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
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
        this.baselinePercent = baselinePercent;
    }

    public void setBottomMargin(float bottomMargin) {
        this.bottomMargin = bottomMargin;
    }

    public void setLeftMargin(float leftMargin) {
        this.leftMargin = leftMargin;
    }

    public void setMaximumAscend(float maximumAscend) {
        this.maximumAscend = maximumAscend;
    }

    public void setMaximumDescend(float maximumDescend) {
        this.maximumDescend = maximumDescend;
    }

    public void setRightMargin(float rightMargin) {
        this.rightMargin = rightMargin;
    }

    public void setTopMargin(float topMargin) {
        this.topMargin = topMargin;
    }

    @Override
    public void draw(Canvas canvas) {
        CharSequence text = getText();
        setText("");
        super.draw(canvas);
        setText(text);
        Paint paint = getPaint();
        Rect r = new Rect();
        paint.setTextSize(getWidth());
        paint.getTextBounds(text.toString(), 0, text.length(), r);
        float width = r.width();
        paint.getTextBounds("ABCDjgy", 0, 7, r);
        float leftM = leftMargin * getWidth();
        float rightM = rightMargin * getWidth();
        float topM = topMargin * getHeight();
        float bottomM = bottomMargin * getHeight();
        float ascent = r.top;
        float descent = r.bottom;
        float textY = (getHeight() - topM - bottomM) * baselinePercent + topM;
        float descentY = (getHeight() - topM - bottomM) * maximumDescend + topM;
        float ascentY = (getHeight() - topM - bottomM) * maximumAscend + topM;
        float descentScale = Math.abs((textY-descentY)/descent);
        float ascentScale = Math.abs((textY-ascentY)/ascent);
        float widthScale = (getWidth()-leftM-rightM)/width;
        float scale = Math.min(descentScale, Math.min(ascentScale, widthScale));
        width *= scale;
        float textX = leftM;
        if(getTextAlignment() == TEXT_ALIGNMENT_CENTER) {
            textX += (getWidth() - leftM - rightM - width)/2;
        }
        paint.setTextSize(paint.getTextSize() * scale);
        canvas.drawText(text, 0, text.length(), textX, textY, getPaint());
    }

}
