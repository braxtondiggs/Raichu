package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Parcel
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
    String mSource;
    @SerializedName("title")
    String mTitle;
    @SerializedName("author")
    String mAuthor;
    @SerializedName("subreddit")
    String mSubReddit;
    /*@SerializedName("preview")
    Preview mPreview;*/


    Listing() {

    }

    public Listing(String id, String domain, int score, Boolean over_18, String thumbnail, String permalink, int num_comments, int created, String source, String title, String author, String subreddit) {
        this.mId = id;
        this.mDomain = domain;
        this.mScore = score;
        this.mOver_18 = over_18;
        this.mThumbnail = thumbnail;
        this.mPermalink = permalink;
        this.mNum_Comments = num_comments;
        this.mSource = source;
        this.mTitle = title;
        this.mCreated = created;
        this.mAuthor = author;
        this.mSubReddit = subreddit;
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
        //return (mPreview.getImages().getSource().getUrl() != null) ? mPreview.getImages().getSource().getUrl() : mSource;
        return mSource;
    }

    public int getCreated() {
        return mCreated;
    }

    private Date getCreatedDate() {
        return new Date(mCreated * 1000);
    }

    public String getLink() {
        return mPermalink;
    }

    public Boolean isNSFW() {
        return mOver_18;
    }

    public int getComments() {
        return mNum_Comments;
    }

    public int getScore() {
        return mScore;
    }

    public String getSub() {
        return "/r/" + mSubReddit;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE,MMMM d,yyyy h:mm,a", Locale.ENGLISH);
        return sdf.format(this.getCreatedDate());
    }
}
/*
@Parcel
public class Preview {
    @SerializedName("images")
    Images mImages;

    public Preview() {

    }

    Preview(Images images) {
        this.mImages = images;
    }

    public Images getImages() {
        return mImages;
    }
}

class Images {
    @SerializedName("source")
    Source mSource;

    Images(Source source) {
        this.mSource = source;
    }

    public Source getSource() {
        return mSource;
    }
}

class Source {
    @SerializedName("url")
    String mUrl;

    Source(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }
}*/