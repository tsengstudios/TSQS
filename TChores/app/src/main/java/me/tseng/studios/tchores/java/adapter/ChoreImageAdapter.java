package me.tseng.studios.tchores.java.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import me.tseng.studios.tchores.R;

public class ChoreImageAdapter extends ArrayAdapter<Integer> {
    private Integer[] images;

    public ChoreImageAdapter(Context context, Integer[] images) {
        super(context, android.R.layout.simple_spinner_item, images);
        this.images = images;
    }

    public static ChoreImageAdapter getChoreImageAdapter(Context context) {
        return new ChoreImageAdapter(context,
                new Integer[]{
                        R.drawable.chore_png_1,
                        R.drawable.chore_png_2,
                        R.drawable.chore_png_3,
                        R.drawable.chore_png_4,
                        R.drawable.chore_png_5,
                        R.drawable.chore_png_6,
                        R.drawable.chore_png_7,
                        R.drawable.chore_png_8,
                        R.drawable.chore_png_9,
                        R.drawable.chore_png_10,
                        R.drawable.chore_png_11,
                        R.drawable.chore_png_12,
                });
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    private View getImageForPosition(int position) {
        ImageView imageView = new ImageView(getContext());
        imageView.setBackgroundResource(images[position]);
        imageView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return imageView;
    }
}

