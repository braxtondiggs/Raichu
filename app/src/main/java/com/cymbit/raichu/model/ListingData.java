package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

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
