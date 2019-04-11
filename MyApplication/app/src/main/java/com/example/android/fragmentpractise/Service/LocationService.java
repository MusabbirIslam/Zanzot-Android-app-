package com.example.android.fragmentpractise.Service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import static com.example.android.fragmentpractise.R.id.speedText;

public class LocationService extends Service {

    private LocationListener locationListener;
    private Location userLocation;

    private final IBinder binder=new LocationServiceBinder();

    public class LocationServiceBinder extends Binder{
        public LocationService getLocationService()
        {
            return LocationService.this;
        }

    }

    public LocationListener getLocationListener()
    {
        Log.e("inside service listener","getting location class");
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                userLocation=location;
                Toast.makeText(getApplicationContext(),userLocation.getSpeed()+" m/s",Toast.LENGTH_SHORT).show();
                Log.e("inside service listener","dataaaaaaaaaaaaaaa");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
         };
        return locationListener;
    }

    @Override
    public void onCreate()
    {
        Log.e("created","created");
        //location inner class for receiving location change
        //creating locationlistener inner class and crating object for locationlistenr

    }

    public Location getLocation()
    {
        if(userLocation!=null)
        Log.e("Location inside service", userLocation.getLatitude()+","+userLocation.getLongitude());
        return userLocation;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
