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

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.drawable.Icon;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import io.mattcarroll.hover.Content;
import me.tseng.studios.tchores.R;

/**
 * A screen that is displayed in our Hello World Hover Menu.
 */
public class HoverMenuScreen extends FrameLayout implements Content {

    private static final String TAG = "TChores.HoverMenuScreen";

    private final Context mContext;
    private final String mPageTitle;
    private Icon mIcon;
    private Map<String, PendingIntent> mMapPendingIntents;

    private ImageView mImageViewPhoto;
    private TextView mTextViewName;
    private Button mButtonRefuse;
    private Button mButtonComplete;
    private Button mButtonSnooze;

    public HoverMenuScreen(@NonNull Context context, @NonNull String pageTitle, Icon icon, Map<String, PendingIntent> mapPendingIntents) {
        super(context);
        mContext = context.getApplicationContext();
        mPageTitle = pageTitle;
        mIcon = icon;
        mMapPendingIntents = mapPendingIntents;

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

        hookupButtonWithPendingIntent(mButtonRefuse, ChoreDetailActivity.ACTION_REFUSED_LOCALIZED);
        hookupButtonWithPendingIntent(mButtonComplete, ChoreDetailActivity.ACTION_COMPLETED_LOCALIZED);
        hookupButtonWithPendingIntent(mButtonSnooze, ChoreDetailActivity.ACTION_SNOOZED_LOCALIZED);
        mTextViewName.setText(mPageTitle);
        mImageViewPhoto.setImageIcon(mIcon);  // TODO stuff a better bitmap through all the Intent s  just to get a higher res image here.

    }

    private void hookupButtonWithPendingIntent(Button button, final String actionKey) {
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PendingIntent pi = mMapPendingIntents.get(actionKey);
                if (pi != null) {
                    try {
                        pi.send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "Failed PendingIntent.send() " + e.getLocalizedMessage());
                    }

                }
            }

        });
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
