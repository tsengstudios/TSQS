/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.tseng.studios.tchores.java;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.Random;

import io.mattcarroll.hover.Content;
import me.tseng.studios.tchores.R;
import me.tseng.studios.tchores.java.util.RestaurantUtil;

/**
 * A screen that is displayed in our Hello World Hover Menu.
 */
public class HoverMenuScreen extends FrameLayout implements Content {

    private final Context mContext;
    private final String mPageTitle;

    private ImageView mImageViewPhoto;
    private TextView mTextViewName;
    private Button mButtonRefuse;
    private Button mButtonComplete;
    private Button mButtonSnooze;

    public HoverMenuScreen(@NonNull Context context, @NonNull String pageTitle) {
        super(context);
        mContext = context.getApplicationContext();
        mPageTitle = pageTitle;

        init();
    }

    @NonNull
    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.chore_actions, this, true);

        mImageViewPhoto = (ImageView) findViewById(R.id.ca_chore_image);
        mTextViewName = (TextView) findViewById(R.id.ca_chore_name);
        mButtonRefuse = (Button) findViewById(R.id.ca_button_refuse);
        mButtonComplete = (Button) findViewById(R.id.ca_button_complete);
        mButtonSnooze = (Button) findViewById(R.id.ca_button_snooze);

        mTextViewName.setText(mPageTitle);

        String tempPhoto = RestaurantUtil.getRandomImageUrl(new Random(), getContext());
        if (RestaurantUtil.isURL(tempPhoto)) {
            Glide.with(mImageViewPhoto.getContext())
                    .load(tempPhoto)
                    .into(mImageViewPhoto);
        } else {
            try {
                int tp = Integer.valueOf(tempPhoto);
                mImageViewPhoto.setImageResource(tp);
            } catch (Exception e) {
                // not an int or not a resource number; use default image
            }
        }

    }

    // Make sure that this method returns the SAME View.  It should NOT create a new View each time
    // that it is invoked.
    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean isFullscreen() {
        return true;
    }

    @Override
    public void onShown() {
        // No-op.
    }

    @Override
    public void onHidden() {
        // No-op.
    }
}
