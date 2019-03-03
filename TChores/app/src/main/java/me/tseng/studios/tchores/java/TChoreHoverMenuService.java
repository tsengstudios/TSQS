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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.mattcarroll.hover.HoverMenu;
import io.mattcarroll.hover.HoverView;
import io.mattcarroll.hover.window.HoverMenuService;
import me.tseng.studios.tchores.R;

/**
 *  originally MultipleSectionsHoverMenuService from hoverdemo-helloworld
 */
public class TChoreHoverMenuService extends HoverMenuService {

    private static final String TAG = "TChores.TChoreHoverMenuService";

    public static final String KEY_CHORE_RESOLVED = "chore.resolved";
    public static final String KEY_COLLAPSE_CHAT_HEAD = "collapse.chat.head";

    MultiSectionHoverMenu mMultiSectionHoverMenu;


    @Override
    protected void onHoverMenuLaunched(@NonNull Intent intent, @NonNull HoverView hoverView) {
        Notification notification = intent.getParcelableExtra(AfterAlarmBR.KEY_NOTIFICATION);
        String choreId = intent.getStringExtra(ChoreDetailActivity.KEY_CHORE_ID);
        boolean bRemoveChore = intent.getBooleanExtra(KEY_CHORE_RESOLVED, false);
        boolean bCollapseChatHead = intent.getBooleanExtra(KEY_COLLAPSE_CHAT_HEAD, false);
        if (notification == null && bRemoveChore == false) {
            throw new RuntimeException("Notification==null  while choreId=" + choreId + " bRemoveChore=" + bRemoveChore + " bCollapseChatHead=" + bCollapseChatHead);
        }
        if (bCollapseChatHead) {
            hoverView.collapse();
            return;
        }

        if (mMultiSectionHoverMenu == null && notification != null) {
            mMultiSectionHoverMenu = createHoverMenu(notification, choreId, hoverView);
            hoverView.setMenu(mMultiSectionHoverMenu);
            hoverView.collapse();
        } else {
            if (bRemoveChore) {
                if (mMultiSectionHoverMenu == null) {
                    // dead Hover menu
                    return;
                }
                // remove this section choreId
                mMultiSectionHoverMenu.tryRemove(choreId, hoverView);
                if (mMultiSectionHoverMenu.mSections.size() == 0) {

                    // close the chathead
                    mMultiSectionHoverMenu = null;
                    hoverView.close();
                } else {
                    hoverView.collapse();
                }
            } else {
                if (notification == null) {
                    throw new RuntimeException("Notification==null  while choreId=" + choreId + " bRemoveChore=" + bRemoveChore + " bCollapseChatHead=" + bCollapseChatHead);   // should be impossible without the earlier if condition that would throw a runtime exception
                }

                // reentry with new choreId?
                mMultiSectionHoverMenu.tryAdd(notification, choreId, hoverView);
            }
        }
    }

    @NonNull
    private MultiSectionHoverMenu createHoverMenu(Notification notification, String choreId, HoverView hoverView) {
        return new MultiSectionHoverMenu(getApplicationContext(), notification, choreId, hoverView);
    }

    private static class MultiSectionHoverMenu extends HoverMenu {

        private final Context mContext;
        List<Section> mSections = new ArrayList<>();

        public MultiSectionHoverMenu(@NonNull Context context, Notification notification, String choreId, HoverView hoverView) {
            mContext = context.getApplicationContext();

            tryAdd(notification, choreId, hoverView);
        }

        public void tryAdd(Notification notification, String choreId, HoverView hoverView) {
            Section sectionFound = findSection(choreId);
            if (sectionFound == null) {
                final Bundle extras = notification.extras;
                if (extras == null) {
                    throw new RuntimeException("Notification extras==null  while choreId=" + choreId);
                }
                String sContentTitle = extras.getString(Notification.EXTRA_TITLE);
                Icon icon = notification.getLargeIcon();

                Map<String, PendingIntent> mapPendingIntents = getMapPendingIntents(notification.actions);

                sectionFound = new Section(          // TODO what happens when max sections hit?
                        new SectionId(choreId),
                        createTabView(icon),
                        new TChoreHoverMenuScreen(mContext, sContentTitle, icon, mapPendingIntents));
                mSections.add(0, sectionFound);
            } else {
                mSections.remove(sectionFound);
                mSections.add(0, sectionFound);
            }

            // select sectionFound if not already
            //  dangerous cause of crashes    hoverView.mSelectedSectionId = null;
            //  OTHER FAILURES    ((HoverViewStateCollapsed) hoverView.mCollapsed).takeControl(hoverView);
            //  OTHER FAILURES    (HoverViewStateExpanded) hoverView.mCollapsed).expand();

            this.notifyMenuChanged();
        }

        public void tryRemove(String choreId, HoverView hoverView) {
            Section sectionFound = findSection(choreId);
            if (sectionFound == null) {
                return;
            }

            mSections.remove(sectionFound);
            if (mSections.size() != 0) {
                this.notifyMenuChanged();   // Hover can't handle zero sections.  need to exit and close up
            }
        }

        private Section findSection(String choreId) {
            SectionId sId = new SectionId(choreId);
            return getSection(sId);
        }


        private Map<String,PendingIntent> getMapPendingIntents(Notification.Action[] actions) {
            Map<String,PendingIntent> map = new HashMap<>();

            for (Notification.Action a: actions) {
                  map.put(a.title.toString(), a.actionIntent);
            }

            return map;
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
            imageView.setBackgroundResource(R.drawable.border4imageview);
            imageView.setCropToPadding(true);
            imageView.setPadding(4, 4, 4, 4);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
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
            if (index < 0)
                index = 0;
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
