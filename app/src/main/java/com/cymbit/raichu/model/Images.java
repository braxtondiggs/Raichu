package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

@Parcel(Parcel.Serialization.BEAN)
class Images {
    @SerializedName("source")
    private final Source mSource;

    @ParcelConstructor
    Images(Source source) {
        this.mSource = source;
    }

    public Source getSource() {
        return mSource;
    }
}

