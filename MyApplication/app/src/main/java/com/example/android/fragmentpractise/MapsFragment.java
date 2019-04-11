package com.example.android.fragmentpractise;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.fragmentpractise.Modules.DirectionFinder;
import com.example.android.fragmentpractise.Modules.DirectionFinderListener;
import com.example.android.fragmentpractise.Modules.Route;
import com.example.android.fragmentpractise.Modules.PlacesAutoCompleteAdapter;

import com.example.android.fragmentpractise.Service.LocationService;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.fragmentpractise.R.id.autoCompleteTextView;
import static com.example.android.fragmentpractise.R.id.autocompleteText;

public class MapsFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener,DirectionFinderListener{

    private GoogleMap mMap;


    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location userLocation;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 10 seconds
    //toggle button
    private ToggleButton onOffRoadButton;
    private LatLng userLatLang = null;

    private EditText etDestination;
    private String destination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;

    private View view;
    private View selectedVehicle;
    private TextView speedText;

    //connecting an bound service
    private LocationService locationService;
    private boolean bound=false;
    private boolean permissionGranted=false;
    private boolean progressBarFirst=true;
    private boolean mapZoomFirst;
    private boolean onRoad;
    private Handler handler;
    //creating connection
    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationServiceBinder locationServiceBinder=(LocationService.LocationServiceBinder)service;


            locationService=locationServiceBinder.getLocationService();
            bound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

            bound=false;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_maps, container, false);

        //accesing vehicle choise layout
        /*vehicleLayout=(LinearLayout) view.findViewById(R.id.vehicleLayout);
        vehicleLayout.animate().translationX(500f).setDuration(2000);*/

        //setting onclick listener for vehicle images
        ImageView busImage=(ImageView) view.findViewById(R.id.busImage);
        busImage.setOnClickListener(this);

        ImageView cngImage=(ImageView) view.findViewById(R.id.cngImage);
        cngImage.setOnClickListener(this);

        ImageView tempoImage=(ImageView) view.findViewById(R.id.cycleImage);
        tempoImage.setOnClickListener(this);

        ImageView carImage=(ImageView) view.findViewById(R.id.carImage);
        carImage.setOnClickListener(this);

        ImageView rickshaImage=(ImageView) view.findViewById(R.id.rickshaImage);
        rickshaImage.setOnClickListener(this);

        //auto complete coes
        AutoCompleteTextView autocompleteView = (AutoCompleteTextView) view.findViewById(autoCompleteTextView);
        autocompleteView.setAdapter(new PlacesAutoCompleteAdapter(getActivity(), R.layout.autocomplete_list_item));

        autocompleteView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get data associated with the specified position
                // in the list (AdapterView)
                String description = (String) parent.getItemAtPosition(position);
            }
        });

        speedText=(TextView) view.findViewById(R.id.speedText);

        selectedVehicle=carImage;
        selectedVehicle.setBackgroundColor(Color.YELLOW);
        //on off road toggle button action listener
        onOffRoadButton=(ToggleButton) view.findViewById(R.id.onOffRoadButton);
        onOffRoadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(onOffRoadButton.isChecked())
                {
                    checkAllInput();
                    //vehicleLayout.animate().translationX(-10f).setDuration(2000);
                }
                else
                {
                  /*  if(bound)
                    {
                        getActivity().unbindService(serviceConnection);
                        bound=false;
                    } */
                    locationManager.removeUpdates(locationListener);
                    onRoad=false;
                    mMap.clear();
                }
            }
        });

      /*  // floating button
        FloatingActionButton carButton = (FloatingActionButton) view.findViewById(R.id.carButton);
        carButton.setOnClickListener(this);
        */
        return view;
    }

    private void checkAllInput()
    {
        etDestination=(EditText) view.findViewById(autoCompleteTextView);
        destination = etDestination.getText().toString();

        if (destination.isEmpty()) {
            onOffRoadButton.setChecked(false);
            Toast.makeText(getActivity(), "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }
        else {

            //location manager setup
            mapZoomFirst=true;
            locationManagerSetup();
            //get data with googl direction api and get speed and show
            displayData();
        }
    }


    private void displayData()
    {
        onRoad=true;
        handler=new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                if(onRoad) {
                    //getSpeed from service
                    Location userCurrentLocaton = locationService.getLocation();

                    if (userCurrentLocaton == null) {
                        userCurrentLocaton = userLocation;
                    }
                    else {
                        userLatLang = new LatLng(userCurrentLocaton.getLatitude(), userCurrentLocaton.getLongitude());
                        speedText.setText(userCurrentLocaton.getSpeed() + "m/s`");
                    }

                    Log.e("Location inside fragment", userLatLang.latitude +","+userLatLang.longitude );

                    try {
                        //new DirectionFinder(this, userLatLang.latitude+","+userLatLang.longitude, "kochukhet").execute();
                        if (permissionGranted) {
                            new DirectionFinder(MapsFragment.this, userLatLang.latitude + "," + userLatLang.longitude, destination).execute();
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    handler.postDelayed(this, MIN_TIME_BW_UPDATES);
                }
                else
                {
                    handler.removeCallbacks(this);
                }
            }

        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //loading map in this view
        SupportMapFragment smapf=(SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map1);
        smapf.getMapAsync(this);


        //bind the service when activity created
        Intent intent=new Intent(getActivity(),LocationService.class);
        getActivity().bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


    private void locationManagerSetup()
    {
        //getting the locton listener class from service
        locationListener=locationService.getLocationListener();
        //checking permission need
        // and colect user location
        //initializing locationmanagaaer
        locationManager=(LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if(Build.VERSION.SDK_INT < 23) //if permission not need
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListener);
            Location userLAstKNownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(userLAstKNownLocation!=null)
            {
                userLatLang = new LatLng( userLAstKNownLocation.getLatitude(), userLAstKNownLocation.getLongitude());
                userLocation=userLAstKNownLocation;
                permissionGranted=true;
            }
            else
            {
                userLatLang =new LatLng( 23.7104,90.40744);
                userLocation=userLAstKNownLocation;
                permissionGranted=true;
            }

        }
        else //if permission  need
        {
            //if permission not given
            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED )
            {
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else//if permission given
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_BW_UPDATES,MIN_DISTANCE_CHANGE_FOR_UPDATES,locationListener);
                //setting user last location as current location

                Location userLAstKNownLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(userLAstKNownLocation!=null)
                {
                    userLatLang = new LatLng( userLAstKNownLocation.getLatitude(), userLAstKNownLocation.getLongitude());
                    userLocation=userLAstKNownLocation;
                    permissionGranted=true;
                }
                else
                {
                    userLatLang =new LatLng( 23.7104,90.40744);
                    userLocation=userLAstKNownLocation;
                    permissionGranted=true;
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id=v.getId();
        // checking which image getClocked
        switch (id)
        {
            case R.id.busImage:
                v.setBackgroundColor(Color.YELLOW);
                if(selectedVehicle!=v) {
                    selectedVehicle.setBackgroundColor(Color.TRANSPARENT);
                    selectedVehicle = v;
                }
                break;
            case R.id.cngImage:
                v.setBackgroundColor(Color.YELLOW);
                if(selectedVehicle!=v) {
                    selectedVehicle.setBackgroundColor(Color.TRANSPARENT);
                    selectedVehicle = v;
                }
                break;
            case R.id.carImage:
                v.setBackgroundColor(Color.YELLOW);
                if(selectedVehicle!=v) {
                    selectedVehicle.setBackgroundColor(Color.TRANSPARENT);
                    selectedVehicle = v;
                }
                break;
            case R.id.rickshaImage:
                v.setBackgroundColor(Color.YELLOW);
                if(selectedVehicle!=v) {
                    selectedVehicle.setBackgroundColor(Color.TRANSPARENT);
                    selectedVehicle = v;
                }
                break;
            case R.id.cycleImage:
                v.setBackgroundColor(Color.YELLOW);
                if(selectedVehicle!=v) {
                    selectedVehicle.setBackgroundColor(Color.TRANSPARENT);
                    selectedVehicle = v;
                }
                break;


           /* case R.id.carButton:
                Snackbar.make(v, "Choose the vehicle you are using", Snackbar.LENGTH_LONG).show();
                vehicleLayout.animate().translationX(-10f).setDuration(2000);
                break;*/
        }
    }

    @Override
    public void onDirectionFinderStart() {

        if(progressBarFirst)
        {
            progressDialog = ProgressDialog.show(getActivity(), "Please wait.",
                    "Finding direction..!", true);
            progressBarFirst=false;
        }

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline:polylinePaths ) {
                polyline.remove();
            }
        }
    }

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {

            ((TextView) view.findViewById(R.id.tmDuration)).setText(route.duration.text);
            ((TextView) view.findViewById(R.id.tvDistance)).setText(route.distance.text);


            if(Build.VERSION.SDK_INT <= 20) {
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_start))
                        .title(route.startAddress)
                        .position(route.startLocation)));

                destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_end))
                        .title(route.endAddress)
                        .position(route.endLocation)));
            }
             else {
                Bitmap startBitMap = BitmapFactory.decodeResource(view.getResources(),
                        R.drawable.start_blue);
                Bitmap endBitMap = BitmapFactory.decodeResource(view.getResources(),
                        R.drawable.end_green);

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(startBitMap))
                        .title(route.startAddress)
                        .position(route.startLocation)));

                destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(endBitMap))
                        .title(route.endAddress)
                        .position(route.endLocation)));
            }


            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));

            //for auto zooming
            if(mapZoomFirst) {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(route.startLocation);
                builder.include(route.endLocation);

                LatLngBounds bounds = builder.build();

                int padding = 0; // offset from edges of the map in pixels
                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                mMap.animateCamera(cu);
                mapZoomFirst=false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("destroy","destroy");
        if(bound)
        {
            getActivity().unbindService(serviceConnection);
            bound=false;
        }
    }

    @Override
    public  void onPause()
    {
        super.onPause();
        Log.e("pause","on pause");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("autoCompleteValue",destination );
        Log.e("save ","save");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.e("date retreave ","date retreave");
    }
}
