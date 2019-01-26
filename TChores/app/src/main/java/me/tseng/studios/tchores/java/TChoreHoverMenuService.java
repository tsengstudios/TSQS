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

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.window.HoverMenuService;
import me.tseng.studios.tchores.R;

/**
 *  originally MultipleSectionsHoverMenuService from hoverdemo-helloworld
 */
public class TChoreHoverMenuService extends HoverMenuService {

    private static final String TAG = "TChore.TChoreHoverMenuService";

    @Override
    protected void onHoverMenuLaunched(@NonNull Intent intent, @NonNull HoverView hoverView) {
        Notification notification = intent.getParcelableExtra(AfterAlarmBR.NOTIFICATION);

        hoverView.setMenu(createHoverMenu(notification));
        hoverView.collapse();
    }

    @NonNull
    private HoverMenu createHoverMenu(Notification notification) {
        return new MultiSectionHoverMenu(getApplicationContext(), notification);
    }

    private static class MultiSectionHoverMenu extends HoverMenu {

        private final Context mContext;
        private final List<Section> mSections;

        public MultiSectionHoverMenu(@NonNull Context context, Notification notification) {
            mContext = context.getApplicationContext();
            String sContentTitle = notification.extras.getString(Notification.EXTRA_TITLE);
            Icon icon = notification.getLargeIcon();

            mSections = Arrays.asList(
                    new Section(
                            new SectionId("1"),
                            createTabView(icon),
                            new HoverMenuScreen(mContext, sContentTitle, icon)
                    ),
                    new Section(
                            new SectionId("2"),
                            createTabView(),
                            new HoverMenuScreen(mContext, "Screen 2", icon)
                    ),
                    new Section(
                            new SectionId("3"),
                            createTabView(),
                            new HoverMenuScreen(mContext, "Screen 3", icon)
                    )
            );
        }

        private View createTabView() {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageResource(R.drawable.fui_ic_check_circle_black_128dp);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            return imageView;
        }

        private View createTabView(Icon icon) {
            ImageView imageView = new ImageView(mContext);
            imageView.setImageIcon(icon);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            return imageView;
        }

        @Override
        public String getId() {
            return "multisectionmenu";
        }

        @Override
        public int getSectionCount() {
            return mSections.size();
        }

        @Nullable
        @Override
        public Section getSection(int index) {
            return mSections.get(index);
        }

        @Nullable
        @Override
        public Section getSection(@NonNull SectionId sectionId) {
            for (Section section : mSections) {
                if (section.getId().equals(sectionId)) {
                    return section;
                }
            }
            return null;
        }

        @NonNull
        @Override
        public List<Section> getSections() {
            return new ArrayList<>(mSections);
        }
    }

}
