package io.branch.referral.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.view.View;
import io.branch.referral.SharingHelper.SHARE_WITH;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShareSheetStyle {
    final Context context_;
    private String copyURlText_;
    private Drawable copyUrlIcon_;
    private String defaultURL_;
    private int dividerHeight_ = -1;
    private List<String> excludeFromShareSheet = new ArrayList();
    private List<String> includeInShareSheet = new ArrayList();
    private final String messageBody_;
    private final String messageTitle_;
    private Drawable moreOptionIcon_;
    private String moreOptionText_;
    private final ArrayList<SHARE_WITH> preferredOptions_;
    private boolean setFullWidthStyle_;
    private View sharingTitleView_ = null;
    private String sharingTitle_ = null;
    private int styleResourceID_ = -1;
    private String urlCopiedMessage_;

    public ShareSheetStyle(@NonNull Context context, @NonNull String messageTitle, @NonNull String messageBody) {
        this.context_ = context;
        this.moreOptionIcon_ = null;
        this.moreOptionText_ = null;
        this.copyUrlIcon_ = null;
        this.copyURlText_ = null;
        this.urlCopiedMessage_ = null;
        this.preferredOptions_ = new ArrayList();
        this.defaultURL_ = null;
        this.messageTitle_ = messageTitle;
        this.messageBody_ = messageBody;
    }

    public ShareSheetStyle setDefaultURL(String url) {
        this.defaultURL_ = url;
        return this;
    }

    public ShareSheetStyle setMoreOptionStyle(Drawable icon, String label) {
        this.moreOptionIcon_ = icon;
        this.moreOptionText_ = label;
        return this;
    }

    public ShareSheetStyle setMoreOptionStyle(@DrawableRes int drawableIconID, @StringRes int stringLabelID) {
        this.moreOptionIcon_ = getDrawable(this.context_, drawableIconID);
        this.moreOptionText_ = this.context_.getResources().getString(stringLabelID);
        return this;
    }

    public ShareSheetStyle setCopyUrlStyle(Drawable icon, String label, String message) {
        this.copyUrlIcon_ = icon;
        this.copyURlText_ = label;
        this.urlCopiedMessage_ = message;
        return this;
    }

    public ShareSheetStyle setCopyUrlStyle(@DrawableRes int drawableIconID, @StringRes int stringLabelID, @StringRes int stringMessageID) {
        this.copyUrlIcon_ = getDrawable(this.context_, drawableIconID);
        this.copyURlText_ = this.context_.getResources().getString(stringLabelID);
        this.urlCopiedMessage_ = this.context_.getResources().getString(stringMessageID);
        return this;
    }

    public ShareSheetStyle addPreferredSharingOption(SHARE_WITH preferredOption) {
        this.preferredOptions_.add(preferredOption);
        return this;
    }

    public ShareSheetStyle setStyleResourceID(@StyleRes int styleResourceID) {
        this.styleResourceID_ = styleResourceID;
        return this;
    }

    public ShareSheetStyle setAsFullWidthStyle(boolean setFullWidthStyle) {
        this.setFullWidthStyle_ = setFullWidthStyle;
        return this;
    }

    public ShareSheetStyle setDividerHeight(int height) {
        this.dividerHeight_ = height;
        return this;
    }

    public ShareSheetStyle setSharingTitle(String title) {
        this.sharingTitle_ = title;
        return this;
    }

    public ShareSheetStyle setSharingTitle(View titleView) {
        this.sharingTitleView_ = titleView;
        return this;
    }

    public ShareSheetStyle excludeFromShareSheet(@NonNull String packageName) {
        this.excludeFromShareSheet.add(packageName);
        return this;
    }

    public ShareSheetStyle excludeFromShareSheet(@NonNull String[] packageName) {
        this.excludeFromShareSheet.addAll(Arrays.asList(packageName));
        return this;
    }

    public ShareSheetStyle excludeFromShareSheet(@NonNull List<String> packageNames) {
        this.excludeFromShareSheet.addAll(packageNames);
        return this;
    }

    public ShareSheetStyle includeInShareSheet(@NonNull String packageName) {
        this.includeInShareSheet.add(packageName);
        return this;
    }

    public ShareSheetStyle includeInShareSheet(@NonNull String[] packageName) {
        this.includeInShareSheet.addAll(Arrays.asList(packageName));
        return this;
    }

    public ShareSheetStyle includeInShareSheet(@NonNull List<String> packageNames) {
        this.includeInShareSheet.addAll(packageNames);
        return this;
    }

    public List<String> getExcludedFromShareSheet() {
        return this.excludeFromShareSheet;
    }

    public List<String> getIncludedInShareSheet() {
        return this.includeInShareSheet;
    }

    public ArrayList<SHARE_WITH> getPreferredOptions() {
        return this.preferredOptions_;
    }

    public Drawable getCopyUrlIcon() {
        return this.copyUrlIcon_;
    }

    public Drawable getMoreOptionIcon() {
        return this.moreOptionIcon_;
    }

    public String getMessageBody() {
        return this.messageBody_;
    }

    public String getMessageTitle() {
        return this.messageTitle_;
    }

    public String getCopyURlText() {
        return this.copyURlText_;
    }

    public String getDefaultURL() {
        return this.defaultURL_;
    }

    public String getMoreOptionText() {
        return this.moreOptionText_;
    }

    public String getUrlCopiedMessage() {
        return this.urlCopiedMessage_;
    }

    public int getDividerHeight() {
        return this.dividerHeight_;
    }

    public String getSharingTitle() {
        return this.sharingTitle_;
    }

    public View getSharingTitleView() {
        return this.sharingTitleView_;
    }

    public boolean getIsFullWidthStyle() {
        return this.setFullWidthStyle_;
    }

    private Drawable getDrawable(@NonNull Context context, @DrawableRes int drawableID) {
        if (VERSION.SDK_INT >= 21) {
            return context.getResources().getDrawable(drawableID, context.getTheme());
        }
        return context.getResources().getDrawable(drawableID);
    }

    public int getStyleResourceID() {
        return this.styleResourceID_;
    }
}
