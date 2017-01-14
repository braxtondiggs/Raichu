package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

public class ListingData {
    @SerializedName("kind")
    private String mKind;
    @SerializedName("data")
    private Listing mData;

    public ListingData(String kind, Listing data) {
        this.mKind = kind;
        this.mData = data;
    }

    public Listing getData() {
        return mData;
    }

}
