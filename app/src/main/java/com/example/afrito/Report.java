package com.example.afrito;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Report implements Parcelable {
    public static final int POI = 0;
    public static final int ENV = 1;
    public static final int HZD = 2;
    public static final int INF = 3;

    private String title;
    private String desc;
    private int type;
    private double [] latLng;
    private Bitmap img;

    public Report(String title, String desc, int type, double[] latLng, Bitmap img) {
        this.title = title;
        this.desc = desc;
        this.type = type;
        this.latLng = latLng;
        this.img = img;
    }

    protected Report(Parcel in) {
        title = in.readString();
        desc = in.readString();
        type = in.readInt();
        latLng = in.createDoubleArray();
        img = in.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel in) {
            return new Report(in);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setLatLng(double[] latLng) {
        this.latLng = latLng;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public double[] getLatLng() {
        return latLng;
    }

    public Bitmap getImg() {
        return img;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(desc);
        parcel.writeInt(type);
        parcel.writeDoubleArray(latLng);
        parcel.writeParcelable(img, i);
    }
}
