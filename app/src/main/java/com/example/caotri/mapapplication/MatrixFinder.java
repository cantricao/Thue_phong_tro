package com.example.caotri.mapapplication;

import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Cao Tri on 23-Jun-16.
 */
public class MatrixFinder {
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=";
    private static final String GOOGLE_API_KEY = "AIzaSyAhHlh_zrKR0vM2Qs5JRAbJqKRCE6wtqo8";
    private MatrixFinderListener listener;
    private Location origin;
    private List<Marker> destination;
    private List<Route> routes = new ArrayList<>();
    private int j;

    public MatrixFinder(MatrixFinderListener listener, Location origin, List<Marker> destination, int i) {
        this.listener = listener;
        this.origin = origin;
        this.destination = destination;
        this.j = i;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onMatrixFinderStart();
        new DownloadRawData().execute(createUrl());
    }

    private String createUrl() throws UnsupportedEncodingException {
        String sOrigin = String.valueOf(origin.getLatitude() + "," + origin.getLongitude());
        String urlOrigin = URLEncoder.encode(sOrigin, "utf-8");// chuyển string sang Url
        String sDestination = String.valueOf(destination.get(0).getPosition().latitude + "," + destination.get(0).getPosition().longitude);
        for (int i = 1; i < destination.size(); i++) {
            sDestination += String.valueOf("|" + destination.get(i).getPosition().latitude + "," + destination.get(i).getPosition().longitude);
        }
        //String sDestination = encode(destination);
        String urlDestination = URLEncoder.encode(sDestination, "utf-8");
        return DIRECTION_URL_API + urlOrigin + "&destinations=" + urlDestination + "&key=" + GOOGLE_API_KEY;
    }

  /*  @NonNull
    public static String encode(final List<LatLng> path) {
        long lastLat = 0;
        long lastLng = 0;

        final StringBuffer result = new StringBuffer();

        for (final LatLng point : path) {
            long lat = Math.round(point.latitude * 1e5);
            long lng = Math.round(point.longitude * 1e5);

            long dLat = lat - lastLat;
            long dLng = lng - lastLng;

            encode(dLat, result);
            encode(dLng, result);

            lastLat = lat;
            lastLng = lng;
        }
        return result.toString();
    }

    private static void encode(long v, StringBuffer result) {
        v = v < 0 ? ~(v << 1) : v << 1;
        while (v >= 0x20) {
            result.append(Character.toChars((int) ((0x20 | (v & 0x1f)) + 63)));
            v >>= 5;
        }
        result.append(Character.toChars((int) (v + 63)));
    }*/

    private class DownloadRawData extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String link = params[0];
            try {
                URL url = new URL(link);
                InputStream is = url.openConnection().getInputStream();
                StringBuffer buffer = new StringBuffer();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String res) {
            try {
                parseJSon(res);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void parseJSon(String data) throws  JSONException {
        if (data == null)
            return;
        JSONObject jsonData = new JSONObject(data);
        String OK = new String("OK");
        if (jsonData.getString("status").compareTo(OK) != 0)
            return;
        JSONArray jsonDes = jsonData.getJSONArray("destination_addresses");
        JSONArray jsonRows = jsonData.getJSONArray("rows");
        JSONObject jsonRow= jsonRows.getJSONObject(0);
        JSONArray jsonElements = jsonRow.getJSONArray("elements");
        for (int i = 0; i < jsonElements.length(); i++) {
            JSONObject jsonRoute = jsonElements.getJSONObject(i);
            Route route = new Route();
            if (jsonRoute.getString("status").compareTo(OK) != 0)
                return;
            JSONObject jsonDuration = jsonRoute.getJSONObject("duration");
            JSONObject jsonDistance = jsonRoute.getJSONObject("distance");
            route.distance = new Distance(jsonDistance.getString("text"), jsonDistance.getInt("value"));
            route.duration = new Duration(jsonDuration.getString("text"), jsonDuration.getInt("value"));
            routes.add(route);
        }
        listener.onMatrixFinderSuccess(routes,j);
    }
}
