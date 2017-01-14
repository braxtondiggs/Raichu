package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Listing {

    @SerializedName("id")
    String mId;
    @SerializedName("domain")
    String mDomain;
    @SerializedName("score")
    int mScore;
    @SerializedName("over_18")
    Boolean mOver_18;
    @SerializedName("thumbnail")
    String mThumbnail;
    @SerializedName("permalink")
    String mPermalink;
    @SerializedName("num_comments")
    int mNum_Comments;
    @SerializedName("created")
    int mCreated;
    @SerializedName("url")
    String mUrl;
    @SerializedName("title")
    String mTitle;
    @SerializedName("author")
    String mAuthor;

    Listing(String id, String domain, int score, Boolean over_18, String thumbnail, String permalink, int num_comments, int created, String url, String title, String author) {
        this.mId = id;
        this.mDomain = domain;
        this.mScore = score;
        this.mOver_18 = over_18;
        this.mThumbnail = thumbnail;
        this.mPermalink = permalink;
        this.mNum_Comments = num_comments;
        this.mUrl = url;
        this.mTitle = title;
        this.mCreated = created;
        this.mAuthor = author;
    }

    public String getID() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getImageUrl() {
        return mUrl;
    }

    public Date getCreatedDate() {
        return new Date(mCreated * 1000);
    }

    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        return sdf.format(this.getCreatedDate());
    }
}
