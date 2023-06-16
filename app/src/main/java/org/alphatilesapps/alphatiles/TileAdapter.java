package org.alphatilesapps.alphatiles;

import android.annotation.SuppressLint;
import android.content.Context;

import android.graphics.Rect;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TileAdapter extends BaseAdapter {
    ArrayList<ColorTile> tiles;
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
        AutoTextView text;
        LinearLayout inner;
        Context context = viewGroup.getContext();
        if(oldView == null) {
            layout = (SquareConstraintLayout) LayoutInflater.from(context).inflate(R.layout.tileadapter_tile, viewGroup, false);
            inner = (LinearLayout) layout.getChildAt(0);
            text = (AutoTextView)inner.getChildAt(0);
        }
        else {
            layout = (SquareConstraintLayout) oldView;
            inner = (LinearLayout) layout.getChildAt(0);
            text = (AutoTextView)inner.getChildAt(0);
        }
        inner.setBackgroundColor(tile.color);
        text.setBackgroundColor(tile.color);
        text.setText(tile.text);
        return layout;
    }
    public static class ColorTile {
        public String text;
        @ColorInt
        public int color;

        public ColorTile(String text, @ColorInt int color) {
            this.text = text;
            this.color = color;
        }
    }
}
