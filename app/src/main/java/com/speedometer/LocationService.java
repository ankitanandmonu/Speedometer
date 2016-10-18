package com.speedometer;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by user
 * Date: 10/18/2016
 * Speedometer
 */
public class LocationService extends Service implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final long INTERVAL = 1000 * 2;
    private static final long FASTEST_INTERVAL = 1000 * 1;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation, lStart, lEnd;
    static double distance = 0;
    double speed;

    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return mBinder;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException e){

        }

    }

    protected void stopLocationUpdates(){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        distance = 0;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        MainActivity.progressDialog.dismiss();
        mCurrentLocation = location;
        if (lStart == null){
            lStart = mCurrentLocation;
            lEnd = mCurrentLocation;
        } else {
            lEnd = mCurrentLocation;
            //Calling below method updates the  live status of distance and speed in the textview
            updateUI();
            //calculating speed with getSpeed method it returns speed in mps so we are converting it into kmph
            speed = location.getSpeed() * 18 / 5;
        }

    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }


    private void updateUI() {
        if (MainActivity.p == 0){
            distance = distance + (lStart.distanceTo(lEnd)/ 1000.00);
            MainActivity.endTime = System.currentTimeMillis();
            long difference = MainActivity.endTime - MainActivity.startTime;
            difference = TimeUnit.MILLISECONDS.toMinutes(difference);
            MainActivity.time.setText(difference + " minutes");
            if (speed > 0.0)
                MainActivity.speed.setText(new DecimalFormat("#.##").format(speed) + " km/hr");
            else
                MainActivity.speed.setText(".......");

            MainActivity.distance.setText(new DecimalFormat("#.###").format(distance) + " Km's.");

            lStart = lEnd;
        }
    }
    @Override
    public boolean onUnbind(Intent intent) {
        stopLocationUpdates();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        lStart = null;
        lEnd = null;
        distance = 0;
        return super.onUnbind(intent);
    }

}
