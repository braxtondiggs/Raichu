package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.cymbit.raichu.utils.Utilities.capitalize;

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
    @SerializedName("created_utc")
    Long mCreated;
    @SerializedName("url")
    String mSource;
    @SerializedName("title")
    String mTitle;
    @SerializedName("author")
    String mAuthor;
    @SerializedName("subreddit")
    String mSubReddit;
    @SerializedName("preview")
    Preview Preview;

    Listing() {

    }

    public Listing(String id, String domain, int score, Boolean over_18, String thumbnail, String permalink, int num_comments, Long created, String source, String title, String author, String subreddit, Preview preview) {
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
        this.Preview = preview;
    }

    public String getID() {
        return mId;
    }

    public String getTitle() {
        return capitalize(mTitle);
    }

    public String getAuthor() {
        return capitalize(mAuthor);
    }

    public String getImageUrl() {
        return (Preview != null && Preview.getImages() != null) ? Preview.getImages().getSource().getUrl() : null;
    }

    public String getSource() {
        return mSource;
    }

    Long getCreated() {
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
        return capitalize(mSubReddit);
    }

    public String getSubLink() {
        return "/r/" + mSubReddit;
    }

    Preview getPreview() {
        return Preview;
    }

    String getDomain() {
        return mDomain;
    }

    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy h:mma", Locale.ENGLISH);
        return sdf.format(this.getCreatedDate());
    }
}
