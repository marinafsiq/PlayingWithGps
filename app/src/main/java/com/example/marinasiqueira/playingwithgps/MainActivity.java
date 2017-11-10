package com.example.marinasiqueira.playingwithgps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GnssMeasurement;
import android.location.GnssMeasurementsEvent;
import android.location.GnssNavigationMessage;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.OnNmeaMessageListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    private final int ACCESS_REQUEST = 10;
    LocationManager locationManager;
    LocationListener locationListener;
    private GnssStatus mGnssStatus;
    private GnssStatus.Callback mGnssStatusListener;
    private GnssMeasurementsEvent.Callback mGnssMeasurementsListener; // For SNRs
    private OnNmeaMessageListener mOnNmeaMessageListener;
    private GnssNavigationMessage.Callback mGnssNavMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListenerTest();
        Log.d("Marina-mainActivity", "location listener instantiated");
        // Register the listener with the Location Manager to receive location updates
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Marina-mainActivity", "does not have all permission, let's ask them");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_REQUEST);

        } else {
            Log.d("Marina-mainActivity", "already have the permissions. Lets request location manager");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

        addGnssStatusListener();
        addGnssMeasurementsListener();
        addNavigationMessageListener();
        addGnssNavigationMsg();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("Marina-ois", "olás");
        Log.d("Marina-mainActivity", "on request permission callback");
        if (requestCode == ACCESS_REQUEST) {
            Log.d("Marina-mainActivity", "inside requestCode. Now will check if permissions were granted");
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    class LocationListenerTest implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("Marina-LocationListener", "on Location Changed");
            TextView tv_Latitude = (TextView) findViewById(R.id.tv_latitude);
            TextView tv_Longitude = (TextView) findViewById(R.id.tv_longitude);
            TextView tv_Provider = (TextView) findViewById(R.id.tv_provider);
            TextView tv_Time = (TextView) findViewById(R.id.tv_time);
            TextView tv_Accuracy = (TextView) findViewById(R.id.tv_accuracy);

            tv_Latitude.setText(location.getLatitude() + "");
            tv_Longitude.setText(location.getLongitude() + "");
            tv_Provider.setText(location.getProvider());
            tv_Time.setText(location.getTime() + "");
            tv_Accuracy.setText(location.getAccuracy() + "");

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("Marina-LocationListener", "on Status Changed");
            Log.d("Marina-LocationListener", "provider: " + provider);
            Log.d("Marina-LocationListener", "extras: " + extras.toString());

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }


    private void addGnssStatusListener() {
        Log.d("Marina-GnssStatus", "starting");
        mGnssStatusListener = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                super.onSatelliteStatusChanged(status);
                Log.d("Marina-GnssStatus", "on Satellite Status Changed");
                displayGnssStatus(status);
            }

            @Override
            public void onFirstFix(int ttffMillis) {
                super.onFirstFix(ttffMillis);
                Log.d("Marina-GnssStatus", "on First Fix");
            }

            @Override
            public void onStarted() {
                super.onStarted();
                Log.d("Marina-GnssStatus", "on Started");
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d("Marina-GnssStatus", "on Stopped");
            }
        };
        try{
            locationManager.registerGnssStatusCallback(mGnssStatusListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    private void addGnssMeasurementsListener() {
        Log.d("Marina-GnssMeasureEvt", "addGnssMeasurementsListener");
        mGnssMeasurementsListener = new GnssMeasurementsEvent.Callback() {
            @Override
            public void onGnssMeasurementsReceived(GnssMeasurementsEvent eventArgs) {
                super.onGnssMeasurementsReceived(eventArgs);
                Log.d("Marina-GnssMeasureEvt", "onGnssMeasurementsReceived");
                displayGnssInfo(eventArgs);
            }

            @Override
            public void onStatusChanged(int status) {
                final String statusMessage;
                switch (status) {
                    case STATUS_LOCATION_DISABLED:
                        statusMessage = "STATUS_LOCATION_DISABLED";
                        break;
                    case STATUS_NOT_SUPPORTED:
                        statusMessage = "STATUS_NOT_SUPPORTED";
                        TextView tv = (TextView)findViewById(R.id.tv_satelite);
                        tv.setText(statusMessage);
                        tv.setTextColor(Color.RED);
                        break;
                    case STATUS_READY:
                        statusMessage = "STATUS_READY";
                        break;
                    default:
                        statusMessage = "gnss_status_unknown";
                }
                Log.d("Marina-GnssMeasureEvt", "on Status Changed: " + statusMessage);
            }
        };
        Boolean res;
        try {
            res = locationManager.registerGnssMeasurementsCallback(mGnssMeasurementsListener);
            Log.d("Marina-GnssMeasureEvt", "registerGnssMeasurementsCallback = " + res);

        }catch (SecurityException e){
            e.printStackTrace();
        }

    }

    public void addNavigationMessageListener(){
        Log.d("Marina-NavigationMsg", "starting");
        mOnNmeaMessageListener = new OnNmeaMessageListener() {
            @Override
            public void onNmeaMessage(String s, long l) {
                Log.d("Marina-Nmea", "onNmeaMessage");
                displayNmea(s);
            }
        };
        try{
            locationManager.addNmeaListener(mOnNmeaMessageListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }

    }


    public void addGnssNavigationMsg(){
        mGnssNavMessageListener = new GnssNavigationMessage.Callback() {
            @Override
            public void onGnssNavigationMessageReceived(GnssNavigationMessage obj) {
                super.onGnssNavigationMessageReceived(obj);
                displayNavMessage(obj);
            }
            public void onStatusChanged(int status){
                final String statusMessage;
                switch (status) {
                    case STATUS_LOCATION_DISABLED:
                        statusMessage = "STATUS_LOCATION_DISABLED";
                        break;
                    case STATUS_NOT_SUPPORTED:
                        statusMessage = "STATUS_NOT_SUPPORTED";
                        break;
                    case STATUS_READY:
                        statusMessage = "STATUS_READY";
                        break;
                    default:
                        statusMessage = "gnss_status_unknown";
                }
                Log.d("GnssMeasureEvt", "on Status Changed: " + statusMessage);
            }
        };
        try{
            locationManager.registerGnssNavigationMessageCallback(mGnssNavMessageListener);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    public void displayNavMessage(GnssNavigationMessage obj){
        TextView tv = (TextView)findViewById(R.id.tv_gnssNavMsg);
        String gnssString =
                obj.getData() + " | " +
                        obj.getMessageId() + " | " +
                        obj.getStatus() + " | " +
                        obj.getSubmessageId() + " | " +
                        obj.getSvid() + " | " +
                        obj.getType();
        Log.d("Marina-GnssMeasureEvt", "onGnssNavigationMessageReceived: " + gnssString);
        tv.setText(gnssString);
    }

    public void displayGnssInfo(GnssMeasurementsEvent pGnssMeasurementsEvent){
        Collection<GnssMeasurement> gnssList = pGnssMeasurementsEvent.getMeasurements();
        String satData = "";
        for (GnssMeasurement sat : gnssList){
            Log.d("Marina-GnssMeasureEvt", "displayGnssInfo: ");
            satData = satData + "\n" +
                    sat.getAccumulatedDeltaRangeMeters() + " | " +
                    sat.getCn0DbHz() + " | " +
                    sat.getConstellationType() + " | " +
                    sat.getSnrInDb() + " | " +
                    sat.getState() + " | " +
                    sat.getSvid();
            Log.d("Marina-GnssMeasureEvt", "sat: " + sat);
        }
        TextView tv_satelite = (TextView)findViewById(R.id.tv_satelite);
        tv_satelite.setText(satData);
    }

    public void displayGnssStatus(GnssStatus status){
        TextView tv_gnssStatus = (TextView)findViewById(R.id.tv_gnssStatus);
        String data = "";
        Log.d("Marina-GnssStatus", "satellites count: " + status.getSatelliteCount());
        for(int i=0; i< status.getSatelliteCount(); i++){
            data = data + "\n" +
                    status.getSvid(i) + " | " +
                    status.getAzimuthDegrees(i) + " | " +
                    status.getCn0DbHz(i)+ " | " +
                    status.getConstellationType(i);
            //SV id | Azimuth Degrees | Signal | Constellation type
            Log.d("Marina-GnssStatus", "data: " + data);
        }
        tv_gnssStatus.setText(data);
    }

    public void displayNmea(String nmea){
        Log.d("Marina-NavigationMsg", "displayNmea");
        TextView tv = (TextView)findViewById(R.id.tv_nmea);
        tv.setText(nmea);
        Log.d("Marina-NavigationMsg", "nmea: " + nmea);
    }


}
