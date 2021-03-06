package com.example.aula_01.digitalfotos.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    private String url;
    private String Description;

    public Image(String url, String description) {
        this.url = url;
        Description = description;
    }

    protected Image(Parcel in) {
        url = in.readString();
        Description = in.readString();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(Description);
    }
}
