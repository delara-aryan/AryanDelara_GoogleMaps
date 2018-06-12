package com.example.aryand2799.mymapsapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean canGetLocation = false;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;
    private ArrayList<LatLng> locationList;
    private boolean locationOnTime;
    private boolean notTrackingMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Add a marker on the map that shows your place of birth.
        // and displays the message "born here" when tapped.
        LatLng scripps = new LatLng(32.8850, -117.2255);
        mMap.addMarker(new MarkerOptions().position(scripps).title("born here"));
        mMap.moveCamera((CameraUpdateFactory.newLatLng(scripps)));

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed FINE permission check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed COARSE permission check");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) ||
                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        }

        locationSearch = (EditText) findViewById(R.id.editText_addr);
        locationSearch = (EditText) findViewById(R.id.editText_addr);
        locationOnTime = false;
        getLocation();
    }

    public void onSearch(View view) {
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //Use LocationManager for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);
        Log.d("MyMapsApp", "onSearch: Location= " + location);
        Log.d("MyMapsApp", "onSearch: Provider= " + provider);

        LatLng userLocation = null;
        try {
            //check the last known location, ned to specifically list the provider network or gps
            if (locationManager != null) {
                Log.d("MyMapsApp", "onSearch: locationManager not null");
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using NETWORK_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else if ((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null) {
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "onSearch: using GPS_PROVIDER userLocation is: " + myLocation.getLatitude() + " " + myLocation.getLongitude());
                    Toast.makeText(this, "UserLoc: " + myLocation.getLatitude() + " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("MyMapsApp", "onSearch: myLocation is null");
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            Log.d("MyMapsApp", "Exception on getLastKnownLocation");
        }
        if (!location.matches("")) {
            //Create Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try {
                //Get a list of addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0 / 60.0),
                        userLocation.longitude - (5.0 / 60.0),
                        userLocation.latitude + (5.0 / 60.0),
                        userLocation.longitude + (5.0 / 60.0));
                Log.d("MyMapsApp", "created addressList");

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (!addressList.isEmpty()) {
                Log.d("MyMapsApp", "Address list size: " + addressList.size());
                for (int i = 0; i < addressList.size(); i++) {
                    Address address = addressList.get(i);
                    LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latlng).title(i + ": " + address.getSubThoroughfare()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));
                }
            }
        }
    }

    // Add View button and method (changeView) to switch between satellite and map views.
    public void changeView(View view) {
        Log.d("MyMapsApp", "changeView: changing view");
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    //Method getLocation to place a marker at current location
    public void getLocation() {

        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            //get GPS status
            //isProiderEnabled returns true if user has enabled gps on phone
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            if (isGPSEnabled) {
                Log.d("MyMapsApp", "getLocation: GPS is enabled");
            }
            //get Network status
            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
            if (isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: Network is enabled");
            }

            if (!isGPSEnabled && !isNetworkEnabled) {
                Log.d("MyMapsApp", "getLocation: no provider is enabled");
            } else {
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
                }
            }
        } catch (Exception e) {
            Log.d("MyMapsApp", "getLocation: Caught exception");
            e.printStackTrace();
        }
    }

    //LocationListener is an anonymous inner class
    //Setup for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.NETWORK_PROVIDER);

            //Check if doing one time via onMapReady, if so remove updates to both gps and network
            if (locationOnTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                locationOnTime = true;
            } else {
                //if here then tracking so relaunch request for network
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status changed");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            dropAmarker(LocationManager.GPS_PROVIDER);
            //Check if doing one time via onMapReady, if so remove updates to both gps and network
            if (locationOnTime == false) {
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                locationOnTime = true;
            } else {
                //if here then tracking so relaunch request for network
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListenerNetwork: status change");

            switch(status) {
                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp", "locationListenerNetwork: LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("MyMaps", "locationListenerNetwork: LocationProvider.OUT_OF_SERVICE");
                    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATE, locationListenerNetwork);
                    break;

            }

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    public void dropAmarker(String provider) {
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            myLocation = locationManager.getLastKnownLocation(provider);
            LatLng userLocation = null;
            if (myLocation == null) {
                Log.d("MyMapsApp", "dropAMarker: location is null");

            } else {
                userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);  // its set to 17
                if (provider == LocationManager.GPS_PROVIDER) {
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.RED));
                } else {
                    mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));

                }

                locationList.add(userLocation);
                if(locationList.size()>2)
                {
                    locationList.remove(0);
                }
                mMap.animateCamera(update);
            }
        }


    }

    public void trackMyLocation (View view) {

        //Kick off the location tracker using getLocation which starts the LocationListeners
        // If notTrackingMyLocation (instance var) then call getLocation
        // else removeUpdates for both Network and GPS LocationListeners
        Log.d("MyMaps", "Tracking now");

        if (notTrackingMyLocation) {
            getLocation();  //updates private var myLocation
            Toast.makeText(this, "trackMyLocation: tracking is ON", Toast.LENGTH_SHORT).show();
            Log.d("MyMaps","trackMyLocation: tracking is ON");
            notTrackingMyLocation = false;
        }else {
            locationManager.removeUpdates(locationListenerGPS);
            locationManager.removeUpdates(locationListenerNetwork);
            Toast.makeText(this, "trackMyLocation: tracking is OFF", Toast.LENGTH_SHORT).show();
            Log.d("MyMaps","trackMyLocation: tracking is OFF");
            notTrackingMyLocation = true;
        }
    }

    public void clearMarkers (View view){
        mMap.clear();
    }
}
