package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListingsData {
    @SerializedName("after")
    private String mAfter;
    @SerializedName("children")
    private List<ListingData> mChildren;

    public ListingsData(String after, List<ListingData> children) {
        this.mAfter = after;
        this.mChildren = children;
    }

    public String getAfter() {
        return mAfter;
    }

    public void setAfter(String after) {
        mAfter = after;
    }

    public List<ListingData> getChildren() {
        return mChildren;
    }

    public class ListingData {
        @SerializedName("data")
        private Listing mData;

        public ListingData(Listing data) {
            this.mData = data;
        }

        public Listing getData() {
            return mData;
        }

    }

}
