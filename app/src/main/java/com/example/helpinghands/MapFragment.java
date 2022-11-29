package com.example.helpinghands;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class Volunteer {
    boolean flag;
    String userId;
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Marker getMarker() {
        return marker;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    Marker marker;
}
public class MapFragment extends Fragment {

    static List<Volunteer> volunteerList = new ArrayList<Volunteer>();
    LocationManager locationManager;
    static LatLng curPosition;
    static User user;
    static FirebaseFirestore db;
    private ImageButton recenter;
    static CameraPosition googlePlex;
    static Address address;
    static Marker marker;
    static boolean currFlag = true;
    static int locFlag = 1;
    static Circle circle;
    private static final String TAG = "MapLOG";
    static int focus = 1;

    public MapFragment() {
    }

    public static MapFragment newInstance(String param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Log.v(TAG,"Location permission granted");
////                    locationfetch(); TODO
//                    NavController nc = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
//                    PendingIntent Pin = nc.createDeepLink().setDestination(R.id.navigation_map).createPendingIntent();
//                    try {
//                        Pin.send();
//                        getActivity().overridePendingTransition(0,0);
//                    } catch (PendingIntent.CanceledException e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    Log.v(TAG,"Location permission rejected");
//                }
                return;
            }
        }
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    static double toRadians(double angleIn10thofaDegree) {
        return (angleIn10thofaDegree * Math.PI) / 180;
    }

    static double distance(LatLng point1, LatLng point2){
        double lon1 = toRadians(point1.longitude);
        double lon2 = toRadians(point2.longitude);
        double lat1 = toRadians(point1.latitude);
        double lat2 = toRadians(point2.latitude);
        double disLon = lon2 - lon1;
        double disLat = lat2 - lat1;
        double a = Math.pow(Math.sin(disLat / 2), 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.pow(Math.sin(disLon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371;
        Log.d(TAG,"Distance Difference: "+c*r);
        return (c * r);
    }

    public Marker setMarker(GoogleMap map, LatLng my_position){
        Marker location_marker = map.addMarker(new MarkerOptions()
                .position(my_position)
                .title("You are here")
                .icon(bitmapDescriptorFromVector(getActivity(), R.drawable.baseline_location_on_24)));
        return location_marker;
    }

    public Circle drawCircle(LatLng my_position,GoogleMap map){
        CircleOptions circleOptions = new CircleOptions()
                .center(my_position)
                .radius(2500)
                .fillColor(Color.argb(50,255,0,0)).strokeWidth(0);
        Circle circle = map.addCircle(circleOptions);
        return circle;
    }

    public void setUserLocation(User myuser,LatLng my_position){
        Log.v(TAG,"Setting User location: "+my_position);
        myuser.setLatitude(Double.toString(my_position.latitude));
        myuser.setLongitude(Double.toString(my_position.longitude));
        db.collection("user_details").document(myuser.getUserid()).update("latitude",my_position.latitude, "longitude",my_position.longitude).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.v(TAG,"ERROR UPDATING LOCATION IN DATABASE");
                Intent in = new Intent(getActivity(), MainActivity.class);
                startActivity(in);
            }
        });
    }

    public LatLng fetchCurrLocation(){
        locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        final Location currentLoc;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
        currentLoc = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
        Log.v(TAG,"Last known location(NETWORK): " + currentLoc);
        if(currentLoc == null){ return  new LatLng(0,0); }
        else{return new LatLng(currentLoc.getLatitude(),currentLoc.getLongitude());}
    }

    public void gpsDisabledAlert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("GPS service is disabled. You need to enable GPS location to access the map.")
                .setCancelable(false)
                .setPositiveButton("Turn on GPS", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void internetDisableAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Internet Connection is required to perform the following task.");
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        builder.show();
    }
}