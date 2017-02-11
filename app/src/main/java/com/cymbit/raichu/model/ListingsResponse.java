package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

public class ListingsResponse {
    @SerializedName("data")
    private ListingsData mData;

    public ListingsResponse(ListingsData data) {
        this.mData = data;
    }

    public ListingsData getData() {
        return mData;
    }
}

