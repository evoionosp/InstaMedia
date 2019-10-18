package com.developer.devshubhpatel.instamedia.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;

/**
 * Created by patel on 20-04-2017.
 */

public class FMedia {

    private String mediaId,shortCode,profileURL,downloadURL,displayURL, authorId,fullName;
    private boolean isStar,isVideo;
    private int height,width;
    private long timeStamp;
    @Exclude
    private int viewType;

    public FMedia() {
        timeStamp = System.currentTimeMillis();
        this.viewType = 1;
    }

    public FMedia(int i){
        this.viewType = i;
    }


    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public String getDisplayURL() {
        return displayURL;
    }

    public void setDisplayURL(String displayURL) {
        this.displayURL = displayURL;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isStar() {
        return isStar;
    }

    public void setStar(boolean star) {
        isStar = star;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Exclude
    public int getViewType() {
        return viewType;
    }

    @Exclude
    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
