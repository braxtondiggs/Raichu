package com.cymbit.raichu.model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.dsl.Table;

@Table
public class Favorites extends SugarRecord {
    private String identifier;
    private String domain;
    private int score;
    private Boolean over_18;
    private String permalink;
    private int num_comments;
    private String source;
    private String title;
    private Long created;
    private String author;
    private String subreddit;
    @Ignore
    private Listing.Preview preview;
    private String imageUrl;

    Favorites() {
    }

    public Favorites(Listing listing) {
        this.identifier = listing.getID();
        this.domain = listing.getDomain();
        this.score = listing.getScore();
        this.over_18 = listing.isNSFW();
        this.permalink = listing.getLink();
        this.num_comments = listing.getComments();
        this.source = listing.getSource();
        this.title = listing.getTitle();
        this.created = listing.getCreated();
        this.author = listing.getAuthor();
        this.subreddit = listing.getSub();
        this.preview = listing.getPreview();
        this.imageUrl = listing.getImageUrl();
    }

    public String getID() {
        return identifier;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getSource() {
        return source;
    }

    public String getDomain() {
        return domain;
    }

    public Long getCreated() {
        return created;
    }

    public String getLink() {
        return permalink;
    }

    public Boolean isNSFW() {
        return over_18;
    }

    public int getComments() {
        return num_comments;
    }

    public int getScore() {
        return score;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSub() {
        return subreddit;
    }

}
