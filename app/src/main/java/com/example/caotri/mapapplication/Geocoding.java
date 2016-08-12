package com.example.caotri.mapapplication;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Cao Tri on 03-Jul-16.
 */
public class Geocoding  extends Activity{
    private static final String DIRECTION_URL_API = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private static final String GOOGLE_API_KEY = "AIzaSyAhHlh_zrKR0vM2Qs5JRAbJqKRCE6wtqo8";
    private GeocodingListener listener;
    ArrayList<InfoLocation> infoLocations = new ArrayList<>();

    public Geocoding(GeocodingListener listener) {
        this.listener = listener;
    }

    public void execute() throws UnsupportedEncodingException {
        listener.onGeocodingStart();
        new LoadFromUrl().execute();
    }

    private class LoadFromUrl extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            final String url = "http://thuephongtro.com";
            Document TPHCM = null;
            try {
                TPHCM = Jsoup.connect(url).get();
                for (int i = 0; i < TPHCM.getElementsByClass("d2").size(); i++) {
                    Element ele3 = TPHCM.getElementsByClass("d2").get(i);
                    String link_nha = url + ele3.select("a").first().attr("href");
                    Document doc4 = Jsoup.connect(link_nha)
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:25.0) Gecko/20100101 Firefox/25.0")
                            .referrer("http://www.google.com")
                            .timeout(1000 * 5) //it's in milliseconds, so this means 5 seconds.
                            .ignoreHttpErrors(true)
                            .get();
                    HtmlToPlainText toPlainText = new HtmlToPlainText();
                    if ((doc4.select("b").size() > 8) && (doc4.getElementsByClass("divms").size() == 0)) {
                        InfoLocation infoLocation = new InfoLocation(doc4.title(),
                                toPlainText.getPlainText(doc4.getElementsByClass("r").first()),
                                toPlainText.getPlainText(doc4.select("i").get(2)),
                                toPlainText.getPlainText(doc4.select("b").get(7)),
                                toPlainText.getPlainText(doc4.getElementsByClass("chitiet").first()));
                        infoLocation.setUrl(link_nha);
                        if (findlocation(infoLocation) != null) {
                            infoLocations.add(findlocation(infoLocation));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            listener.onGeocodingSuccess(infoLocations);
        }

        @Nullable
        private InfoLocation findlocation(InfoLocation infoLocation) throws IOException, JSONException {
            String link = DIRECTION_URL_API + URLEncoder.encode(infoLocation.getAddress(), "utf-8") + "&key=" + GOOGLE_API_KEY;
            URL url = new URL(link);
            InputStream is = url.openConnection().getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            JSONObject jsonData = new JSONObject(buffer.toString());
            String OK = new String("OK");
            if (jsonData.getString("status").compareTo(OK) == 0) {
                JSONArray jsonResults = jsonData.getJSONArray("results");
                JSONObject jsonResult = jsonResults.getJSONObject(0);
                JSONObject jsonGeometry = jsonResult.getJSONObject("geometry");
                JSONObject jsonLocation = jsonGeometry.getJSONObject("location");
                infoLocation.setLatLng(new LatLng(jsonLocation.getDouble("lat"), jsonLocation.getDouble("lng")));
                infoLocation.setFormat_address(jsonResult.getString("formatted_address"));
                return infoLocation;
            }
            return null;
        }

    }

}

