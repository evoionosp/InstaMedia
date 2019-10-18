
package com.developer.devshubhpatel.instamedia.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ShortcodeMedia {



    @SerializedName("edge_sidecar_to_children")
    @Expose
    private EdgeSidecarToChildren edgeSidecarToChildren;
    @SerializedName("video_url")
    @Expose
    private String videoUrl;
    @SerializedName("video_view_count")
    @Expose
    private Integer videoViewCount;
    @SerializedName("__typename")
    @Expose
    private String typename;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("shortcode")
    @Expose
    private String shortcode;
    @SerializedName("dimensions")
    @Expose
    private Dimensions dimensions;
    @SerializedName("display_url")
    @Expose
    private String displayUrl;
    @SerializedName("is_video")
    @Expose
    private Boolean isVideo;
    @SerializedName("taken_at_timestamp")
    @Expose
    private Integer takenAtTimestamp;
    @SerializedName("owner")
    @Expose
    private Owner owner;
    @SerializedName("is_ad")
    @Expose
    private Boolean isAd;




    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public Integer getVideoViewCount() {
        return videoViewCount;
    }

    public void setVideoViewCount(Integer videoViewCount) {
        this.videoViewCount = videoViewCount;
    }


    public String getTypename() {
        return typename;
    }

    public void setTypename(String typename) {
        this.typename = typename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public Boolean getVideo() {
        return isVideo;
    }

    public void setVideo(Boolean video) {
        isVideo = video;
    }

    public Boolean getAd() {
        return isAd;
    }

    public void setAd(Boolean ad) {
        isAd = ad;
    }

    public void setDisplayUrl(String displayUrl) {
        this.displayUrl = displayUrl;
    }

    public Boolean getIsVideo() {
        return isVideo;
    }

    public void setIsVideo(Boolean isVideo) {
        this.isVideo = isVideo;
    }

    public Integer getTakenAtTimestamp() {
        return takenAtTimestamp;
    }

    public void setTakenAtTimestamp(Integer takenAtTimestamp) {
        this.takenAtTimestamp = takenAtTimestamp;
    }
    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Boolean getIsAd() {
        return isAd;
    }

    public void setIsAd(Boolean isAd) {
        this.isAd = isAd;
    }


    public EdgeSidecarToChildren getEdgeSidecarToChildren() {
        return edgeSidecarToChildren;
    }

    public void setEdgeSidecarToChildren(EdgeSidecarToChildren edgeSidecarToChildren) {
        this.edgeSidecarToChildren = edgeSidecarToChildren;
    }

}
