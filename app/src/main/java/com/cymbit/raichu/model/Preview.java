package com.cymbit.raichu.model;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
class Preview {
    @SerializedName("images")
    List<Images> mImages;

    public Preview() {

    }

    Preview(List<Images> images) {
        this.mImages = images;
    }

    public Images getImages() {
        return (!mImages.isEmpty()) ? mImages.get(0) : null;
    }
}
