package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListingsData {
    @SerializedName("after")
    private String mAfter;
    @SerializedName("before")
    private String mBefore;
    @SerializedName("children")
    private List<ListingData> mChildren;

    public ListingsData(String after, String before, List<ListingData> children) {
        this.mAfter = after;
        this.mBefore = before;
        this.mChildren = children;
    }

    public String getAfter() {
        return mAfter;
    }

    public String getBefore() {
        return mBefore;
    }

    public List<ListingData> getChildren() {
        return mChildren;
    }
}
