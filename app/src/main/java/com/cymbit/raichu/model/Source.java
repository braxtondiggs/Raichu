package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel(Parcel.Serialization.BEAN)
public class Source {
    @SerializedName("url")
    private String mUrl;

    @ParcelConstructor
    Source(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }
}
