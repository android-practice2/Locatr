package com.bignerdranch.android.locatr;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class LocatrFragment extends SupportMapFragment {

    private static final String TAG = "LocatrFragment";
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
//    private ImageView mImageView;

    private GoogleApiClient mClient;
    private GalleryItem mMapItem;
    private Bitmap mMapImage;
    private Location mCurrentLocation;
    private ProgressFragment mProgressFragment;
    private GoogleMap mGoogleMap;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                mGoogleMap = googleMap;
                updateUI();
            }
        });
    }

//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View inflate = inflater.inflate(R.layout.fragment_locatr, container, false);
//        setHasOptionsMenu(true);
//
//        mImageView = inflate.findViewById(R.id.image);
//        return inflate;
//    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);

        MenuItem action_locate = menu.findItem(R.id.action_locate);
        action_locate.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                findImage();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                if (hasLocationPermission()) {
                    findImage();
                    return;
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void updateUI() {
        if (mGoogleMap == null|| mMapImage ==null) {
            return;
        }

        LatLng myPoint = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        LatLng itemPoint = new LatLng(mMapItem.getLat(), mMapItem.getLon());

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions photoMarkerOptions=new MarkerOptions();

        photoMarkerOptions.position(itemPoint)
                .icon(bitmapDescriptor)
        ;

        MarkerOptions myMarkerOptions = new MarkerOptions();
        myMarkerOptions.position(myPoint);

        mGoogleMap.clear();
        mGoogleMap.addMarker(photoMarkerOptions);
        mGoogleMap.addMarker(myMarkerOptions);

        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(myPoint)
                .include(itemPoint)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, (int) getResources().getDimension(R.dimen.map_inset_margin));
        mGoogleMap.animateCamera(cameraUpdate);

    }

    private List<GalleryItem> findImage() {

        if (ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), LOCATION_PERMISSIONS[0])) {
                new PermissionRationaleFragment(new PermissionRationaleFragment.PositiveButtonClickCallback() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);

                    }
                })

                        .show(getFragmentManager(), null);

            } else {
                requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);

            }
        } else {
            mProgressFragment = new ProgressFragment();

            mProgressFragment.show(getChildFragmentManager(), null);

            doFindImage();

        }

        return null;
    }

    @SuppressLint("MissingPermission")
    private void doFindImage() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setNumUpdates(1);
        locationRequest.setInterval(0);

        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                Log.i(TAG, "Got a fix: " + location);
                mCurrentLocation = location;
                new SearchAsyncTask().execute(location);
            }
        });
    }

    private class SearchAsyncTask extends AsyncTask<Location, Void, List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Location... locations) {
            FlickrFetchr flickrFetchr = new FlickrFetchr();
            Location location = locations[0];
            List<GalleryItem> galleryItems = flickrFetchr.searchPhotos(location);
            GalleryItem mapItem = galleryItems.get(0);
            mMapItem = mapItem;

            byte[] urlBytes = new byte[0];
            try {
                urlBytes = flickrFetchr.getUrlBytes(mapItem.getUrl());
            } catch (IOException e) {
                Log.i(TAG, "Unable to download bitmap", e);
                return null;
            }

            mMapImage = BitmapFactory.decodeByteArray(urlBytes, 0, urlBytes.length);

            return galleryItems;
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mProgressFragment.dismiss();
            super.onPostExecute(galleryItems);
//            mImageView.setImageBitmap(mBitmap);
            updateUI();
        }
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}
