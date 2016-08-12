package com.example.caotri.mapapplication;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.multiwindow.SMultiWindow;

import org.json.JSONArray;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback , GoogleMap.InfoWindowAdapter
        , GoogleMap.OnInfoWindowClickListener
        , ActivityCompat.OnRequestPermissionsResultCallback
        , MatrixFinderListener, GeocodingListener
        , DirectionFinderListener{

    private static final int MY_LOCATION_REQUEST_CODE = 100;
    private GoogleMap mMap;
    private List<Marker> destinationMarkers = new ArrayList<>();
    private ProgressDialog progressDialog;
    Location mLastLocation;
    private SMultiWindow mMultiWindow = null;
    private HashMap<Marker,InfoLocation> hashMap = new HashMap<>();
    private List<Polyline> polylinePaths;
    private InfoLocation details;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        try {
            new Geocoding(this).execute();
        }
         catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        FloatingActionButton fab_matrix = (FloatingActionButton) findViewById(R.id.fab);


        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        mMultiWindow = new SMultiWindow();
        try {
            mMultiWindow.initialize(this);
        } catch (SsdkUnsupportedException e) {
            // / TODO Auto-generated catch block
            e.printStackTrace();
            // In this case, application should be stopped, because this device can't support this feature.
        }
    }
    public void onFabClick(View v) {
        int size = destinationMarkers.size();
        for (int i = 0; i < size; i += 23) {
            if (i + 23 < size) {
                try {
                    new MatrixFinder(MapsActivity.this, mLastLocation, destinationMarkers.subList(i, i + 23), i).execute();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                if (i != size)
                    try {
                        new MatrixFinder(MapsActivity.this, mLastLocation, destinationMarkers.subList(i, size), i).execute();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length == 1 &&
                    permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                // Permission was denied. Display an error message.
                return;
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setInfoWindowAdapter(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android
                .Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    , MY_LOCATION_REQUEST_CODE);
            // here to request the missing permissions, and then overriding
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            return;
        }
        mMap.setMyLocationEnabled(true);
        mLastLocation = getLocation();
        if (mLastLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                    // Sets the center of the map to location user
                    .zoom(11)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    //.tilt(0)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        mMap.setOnInfoWindowClickListener(this);
    }


    @Override
    public View getInfoWindow(Marker marker) {
        //return null;
        return prepareInfoView(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
        //return prepareInfoView(marker);
    }

    private View prepareInfoView(final Marker marker) {
        //prepare InfoView programmatically
        LinearLayout infoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams infoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        infoView.setOrientation(LinearLayout.HORIZONTAL);
        infoView.setLayoutParams(infoViewParams);

        ImageView infoImageView = new ImageView(MapsActivity.this);
        //Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        Drawable drawable = getResources().getDrawable(android.R.drawable.ic_dialog_map);
        infoImageView.setImageDrawable(drawable);
        infoView.addView(infoImageView);

        LinearLayout subInfoView = new LinearLayout(MapsActivity.this);
        LinearLayout.LayoutParams subInfoViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        subInfoView.setOrientation(LinearLayout.VERTICAL);
        subInfoView.setLayoutParams(subInfoViewParams);
        TextView subInfoTitle = new TextView(MapsActivity.this);
        TextView subInfoSnippnet = new TextView(MapsActivity.this);
        subInfoSnippnet.setText(marker.getSnippet());
        subInfoTitle.setText(marker.getTitle());
        subInfoView.addView(subInfoTitle);
        subInfoView.addView(subInfoSnippnet);
        infoView.addView(subInfoView);

        return infoView;
    }

    @Override
    public void onMatrixFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Tính khoảng cách từ đây dến các địa chỉ", true);
    }

    @Override
    public void onMatrixFinderSuccess(List<Route> routes, int j) {
        if(routes.size() == 0)
        {
            Toast.makeText(MapsActivity.this,"Matrix key hết trong ngày hôm nay",Toast.LENGTH_LONG);
        }
        for (int i = 0; i < routes.size(); i++) {
            Route route = routes.get(i);
            String distance = "Distance: " + route.distance.text;
            String duration = "Duration: " + route.duration.text;
            String snappet = distance + "\n" + duration;
            hashMap.get(destinationMarkers.get(i+j)).setRoute(snappet);
        }
        progressDialog.dismiss();
    }

    public Location getLocation() {
        boolean gps_enabled;
        boolean network_enabled;

        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Location net_loc = null, gps_loc = null, finalLoc = null;

        if (gps_enabled)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST_CODE);
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
        gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (network_enabled)
            net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (gps_loc != null && net_loc != null) {

            if (gps_loc.getAccuracy() >= net_loc.getAccuracy())
                finalLoc = gps_loc;
            else
                finalLoc = net_loc;

            // I used this just to get an idea (if both avail, its upto you which you want to take as I taken location with more accuracy)

        } else {

            if (gps_loc != null) {
                finalLoc = net_loc;
            } else if (net_loc != null) {
                finalLoc = gps_loc;
            }
        }
        return finalLoc;
    }

    @Override
    public void onGeocodingStart() {
        progressDialog = ProgressDialog.show(this, "Please wait.",
                "Load data!", true);
    }

    @Override
    public void onGeocodingSuccess(ArrayList<InfoLocation> infoLocations) {
        if(infoLocations.size() == 0)
        {
            Toast.makeText(MapsActivity.this,"Geocoding key hết trong ngày hôm nay",Toast.LENGTH_LONG);
        }
        for (int i = 0; i < infoLocations.size(); i++) {
            InfoLocation infoLocation = infoLocations.get(i);
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(infoLocation.getTitle())
                    .position(infoLocation.getLatLng())
                    .snippet(infoLocation.getCost())));
            hashMap.put(destinationMarkers.get(i),infoLocation);
        }

        progressDialog.dismiss();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        details = hashMap.get(marker);
        if (mLastLocation != null)
            details.setLocation(String.valueOf(mLastLocation.getLatitude() + "," + mLastLocation.getLongitude()));
        Intent intent = new Intent(this, DetailLocation.class);
        intent.putExtra("details",details);
        startActivityForResult(intent,2);
    }

    protected void Direction(int requestCode, int resultCode){
        if (requestCode != resultCode) {
            return;
        }
        String des = String.valueOf(details.getLatLng().latitude + ","
                + details.getLatLng().longitude);
        String org = details.getLocation();
        try {
            new DirectionFinder(this, org, des).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Direction(requestCode,resultCode);
    }

    @Override
    public void onDirectionFinderStart() {
        progressDialog = ProgressDialog.show(this, "Please wait", "Finding direction...", true);
        if (polylinePaths != null) {
            for (Polyline polylinePath : polylinePaths) {
                polylinePath.remove();
            }
            polylinePaths = null;
        }

    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();
        PolylineOptions polylineOptions = new PolylineOptions()
                .geodesic(true)
                .color(Color.BLUE)
                .width(10);
        for (Route route : routes) {
            for (int i = 0; i < route.points.size(); i++) {
                polylineOptions.add(route.points.get(i));
            }
        }
        polylinePaths.add(mMap.addPolyline(polylineOptions));
        progressDialog.dismiss();
    }
}

