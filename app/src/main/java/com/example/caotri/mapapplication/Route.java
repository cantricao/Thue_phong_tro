package com.example.caotri.mapapplication;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;


/**
 * Created by Cao Tri on 19-Jun-16.
 */
public class Route {
    public LatLng startLocation;
    public Distance distance;
    public String endAddress;
    public Duration duration;
    public String startAddress;
    public LatLng endLocation;
    public List<LatLng> points;

    public int getDistance() {
        return distance.value;
    }
}
