package com.cymbit.raichu.model;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

@Table
public class Favorites extends SugarRecord {
    String identifier;
    String domain;
    int score;
    Boolean over_18;
    String thumbnail;
    String permalink;
    int num_comments;
    String source;
    String title;
    int created;
    String author;
    String subreddit;

    public Favorites() {
    }

    public Favorites(Listing listing) {
        this.identifier = listing.getID();
        this.domain = listing.getDomain();
        this.score = listing.getScore();
        this.over_18 = listing.isNSFW();
        //this.thumbnail = listing.;
        this.permalink = listing.getLink();
        this.num_comments = listing.getComments();
        this.source = listing.getImageUrl();
        this.title = listing.getTitle();
        this.created = listing.getCreated();
        this.author = listing.getAuthor();
        this.subreddit = listing.getSub();
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

    public String getImageUrl() {
        return source;
    }

    public String getDomain() {
        return domain;
    }

    public int getCreated() {
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

    public String getSub() {
        return subreddit;
    }

}
