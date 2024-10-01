package org.alphatilesapps.alphatiles;

import android.content.Context;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;

import java.util.ArrayList;
import java.util.logging.Logger;

public class TileAdapter extends BaseAdapter {
    ArrayList<ColorTile> tiles;
    float fontScale;

    public float getFontScale() {
        return fontScale;
    }
    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    static Logger LOGGER = Logger.getLogger(TileAdapter.class.getName());
    public TileAdapter(ArrayList<ColorTile> tiles) {
        this.tiles = tiles;
    }
    @Override
    public int getCount() {
        return tiles.size();
    }

    @Override
    public Object getItem(int i) {
        return tiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }
    @Override
    public View getView(int i, View oldView, ViewGroup viewGroup) {
        ColorTile tile = (ColorTile) getItem(i);
        SquareConstraintLayout layout;
        TextView text;
        LinearLayout inner;
        Context context = viewGroup.getContext();
        if(oldView == null) {
            layout = (SquareConstraintLayout) LayoutInflater.from(context).inflate(R.layout.tileadapter_tile, viewGroup, false);
        }
        else {
            layout = (SquareConstraintLayout) oldView;
        }
        inner = (LinearLayout) layout.getChildAt(0);
        text = (TextView) inner.getChildAt(0);
        text.post(new Runnable() {
            @Override
            public void run() {
                text.setTextSize(TypedValue.COMPLEX_UNIT_PX, layout.getWidth() * fontScale);
            }
        });
        inner.setBackgroundColor(tile.color);
        text.setBackgroundColor(tile.color);
        text.setTextColor(tile.fontColor);
        text.setText(tile.text);
        return layout;
    }
    public static class ColorTile {
        public String text;
        @ColorInt
        public int color;
        @ColorInt
        public int fontColor = 0xFFFFFFFF;
        public ColorTile(String text, @ColorInt int color) {
            this.text = text;
            this.color = color;
        }
        public ColorTile(String text, @ColorInt int color, @ColorInt int fontColor) {
            this.text = text;
            this.color = color;
            this.fontColor = fontColor;
        }
    }
}
