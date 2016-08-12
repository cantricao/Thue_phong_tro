package com.example.caotri.mapapplication;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Cao Tri on 03-Jul-16.
 */
public interface GeocodingListener {
    void onGeocodingStart();
    void onGeocodingSuccess(ArrayList<InfoLocation> infoLocations);
}
