package com.example.caotri.mapapplication;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

/**
 * Created by Cao Tri on 03-Jul-16.
 */
public class InfoLocation implements Parcelable {
    private String title;
    private String cost;
    private String areas;
    private String address;
    private String detail;
    private LatLng latLng;
    private String route;
    private String location;
    private String format_address;
    private String url;

    public void setFormat_address(String format_address) {
        this.format_address = format_address;
    }

    public String getFormat_address() {
        return format_address;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public InfoLocation(String title, String cost, String areas, String address, String detail) {
        this.title = title;
        this.cost = cost;
        this.areas = areas;
        this.address = address;
        this.detail = detail;

    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    public String getCost() {
        return cost;
    }

    public String getAreas() {
        return areas;
    }

    public String getAddress() {
        return address;
    }

    public String getDetail() {
        return detail;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public String getTitle() {
        return title;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.cost);
        dest.writeString(this.areas);
        dest.writeString(this.address);
        dest.writeString(this.detail);
        dest.writeParcelable(this.latLng, flags);
        dest.writeString(this.route);
        dest.writeString(this.location);
        dest.writeString(this.format_address);
        dest.writeString(this.url);
    }

    protected InfoLocation(Parcel in) {
        this.title = in.readString();
        this.cost = in.readString();
        this.areas = in.readString();
        this.address = in.readString();
        this.detail = in.readString();
        this.latLng = in.readParcelable(LatLng.class.getClassLoader());
        this.route = in.readString();
        this.location = in.readString();
        this.format_address = in.readString();
        this.url = in.readString();
    }

    public static final Creator<InfoLocation> CREATOR = new Creator<InfoLocation>() {
        @Override
        public InfoLocation createFromParcel(Parcel source) {
            return new InfoLocation(source);
        }

        @Override
        public InfoLocation[] newArray(int size) {
            return new InfoLocation[size];
        }
    };
}

