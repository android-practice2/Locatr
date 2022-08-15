package com.bignerdranch.android.locatr;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {
    public static final int PAGE_SIZE = 10;
    private static final String TAG = "FlickrFetchr";
    public static final String API_KEY = "268c20779d60b65d5b5d0da6f334b3d8";
    private static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s,geo")
            .appendQueryParameter("per_page", String.valueOf(PAGE_SIZE))
            .build();

    public List<GalleryItem> fetchRecentPhotos(Integer page) {
        String url = buildUrl(FETCH_RECENTS_METHOD, null,page);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(Integer page,String query) {
        String url = buildUrl(SEARCH_METHOD, query,page);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> searchPhotos(Location location) {
        String url = buildUrl(location);
        return downloadGalleryItems(url);
    }
    public List<GalleryItem> downloadGalleryItems(String url) {
        Log.i(TAG, "download: " + url);
        List<GalleryItem> list = new ArrayList<>();

        try {



            String result = getUrlString(                    url);

            Log.i(TAG, "Received JSON: " + result);

            parseItems(list, result);

        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch URL: ", e);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse JSON", e);
        }
        return list;
    }

    private String buildUrl(String method, String query,Integer page) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        if (page != null) {
            uriBuilder.appendQueryParameter("page", page.toString());
        }
        return uriBuilder.build().toString();
    }

    private String buildUrl(Location location) {
        return
                ENDPOINT.buildUpon()
                        .appendQueryParameter("method", SEARCH_METHOD)
                        .appendQueryParameter("lat", "" + location.getLatitude())
                        .appendQueryParameter("lon", "" + location.getLongitude())
                        .build().toString();
    }


    private void parseItems(List<GalleryItem> items, String result)
            throws IOException, JSONException {
        JSONObject jsonObject = new JSONObject(result);
        JSONObject photosJsonObject = jsonObject.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");
        for (int i = 0; i < photoJsonArray.length(); i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);
            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));
            item.setUrl(photoJsonObject.getString("url_s"));
            item.setOwner(photoJsonObject.getString("owner"));
            item.setLat(photoJsonObject.getDouble("latitude"));
            item.setLon(photoJsonObject.getDouble("longitude"));


            items.add(item);
        }
    }

    public byte[] getUrlBytes(String urlSpec) throws IOException {

        URL url = new URL(urlSpec);

//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1081));
//        HttpURLConnection urlConnection =(HttpURLConnection) url.openConnection(proxy);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = urlConnection.getInputStream();

        if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException(urlConnection.getResponseMessage() + ": with " + urlSpec);
        }

        try {

            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = inputStream.read(buffer)) > 0) {
                byteArrayOutputStream.write(buffer, 0, size);
            }

        } finally {
            inputStream.close();
            byteArrayOutputStream.close();
            urlConnection.disconnect();
        }

        return byteArrayOutputStream.toByteArray();

    }

    public String getUrlString(String urlSpec) throws IOException {
        byte[] urlBytes = getUrlBytes(urlSpec);
        return new String(urlBytes, StandardCharsets.UTF_8);
    }
}
