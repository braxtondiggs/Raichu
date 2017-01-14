package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

public class ListingsResponse {
    @SerializedName("kind")
    private String mKind;
    @SerializedName("data")
    private ListingsData mData;

    public ListingsResponse(String kind, ListingsData data) {
        this.mKind = kind;
        this.mData = data;
    }

    public String getKind() {
        return mKind;
    }

    public ListingsData getData() {
        return mData;
    }
}

