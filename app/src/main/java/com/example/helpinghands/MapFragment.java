package com.example.helpinghands;

import static android.content.Context.LOCATION_SERVICE;

import static com.example.helpinghands.Utils.checkInternetStatus;
import static com.example.helpinghands.Utils.findLocality;
import static com.example.helpinghands.Utils.sendFcmNotifications;
import static com.example.helpinghands.Utils.updateLocality;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class Volunteer {
    boolean flag;
    String userType;
    String userId;
    Marker marker;

    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Marker getMarker() {
        return this.marker;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
    public String getUsertype() {
        return this.userType;
    }
    public void setUsertype(String usertype) {
        this.userType = usertype;
    }
}

class Requester {
    boolean flag;
    String requestId;
    String userId;
    String latitude;
    String longitude;
    Marker marker;

    public String getLatitude() {
        return this.latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return this.longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getRequestId() {
        return this.requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public Marker getMarker() {
        return this.marker;
    }
    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}

public class MapFragment extends Fragment {

    static List<Volunteer> volunteerList = new ArrayList<Volunteer>();
    static List<Requester> requesterList = new ArrayList<Requester>();

    LocationManager locationManager;
    static LatLng currPosition;
    static User user;
    static FirebaseFirestore db;
    private ImageButton recenter;
    static CameraPosition googlePlex;
    static Address address;
    static Marker marker;
    static boolean currFlag = true;
    static boolean currFlag1 = true;
    static int locFlag = 1;
    static Circle circle;
    private static final String TAG = "MapLOG";
    static int focus = 1;
    static Context myContext;

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
        final View root = inflater.inflate(R.layout.fragment_map, container, false);
        volunteerList = new ArrayList<Volunteer>();
        recenter = (ImageButton)root.findViewById(R.id.recenterBtn);
        recenter.setVisibility(View.INVISIBLE);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFrag);
        myContext = getActivity();
        final ProgressDialog progressBar1;
        progressBar1 = new ProgressDialog(getContext());
        progressBar1.setMessage(getString(R.string.Finding_current_location));
        progressBar1.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar1.setCancelable(true);
        progressBar1.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });
        db = FirebaseFirestore.getInstance();
        user = new User(getActivity());

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap mMap) {

                if (!checkInternetStatus(requireContext())) { internetDisabledAlert(); }
                else{
                    locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
                    }
                    else {
                        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { gpsDisabledAlert(); }
                        else {
                            if(user.getLongitude() != "" && user.getLatitude() != ""){
                                currPosition = new LatLng(Double.parseDouble(user.getLatitude()),Double.parseDouble(user.getLongitude()));
                            }
                            else{currPosition = fetchCurrLocation();}
                            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                                @Override
                                public View getInfoWindow(Marker marker) {
                                    return null;
                                }

                                @Override
                                public View getInfoContents(Marker marker) {
                                    if(marker.getTitle().equals("Volunteer") || marker.getTitle().equals(getString(R.string.You_are_here)) || marker.getTitle().equals(getString(R.string.Assigned_Volunteer)))
                                    {
                                        return null;
                                    }
                                    else{
                                        // Marker is of emergency request
                                        final String title = marker.getTitle();
                                        String[] arrOfStr = title.split("@", 2);
                                        if (arrOfStr[1].equals("Active")){
                                            return prepareInfoView(getActivity());
                                        }
                                        else {
                                            return null;
                                        }
                                    }
                                }
                            });

                            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                                @Override
                                public void onInfoWindowClick(final Marker marker) {
                                    if(!marker.getTitle().equals(getString(R.string.Volunteer)) && !marker.getTitle().equals(getString(R.string.You_are_here)))
                                    {
                                        final String id = marker.getTitle().split("@", 2)[0];
                                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                                        final String[] token = new String[1];
                                        token[0] = null;
                                        db.collection("emergency_requests").document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                            @Override
                                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                String userId = documentSnapshot.get("userId").toString();
                                                db.collection("user_details").document(userId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                        token[0] = documentSnapshot.get("firebaseToken").toString();
                                                        JSONObject jsonObject = new JSONObject();
                                                        try {
                                                            jsonObject.put("vid", user.getUserid());
                                                            jsonObject.put("Type", "RequestAccept");
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (token[0] != null) {
                                                            db.collection("emergency_requests").document(id).update("status","Accepted","volunteerID",user.getUserid(),"volunteerNo",user.getContactNumber()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                sendFcmNotifications(requireActivity(), token[0], jsonObject);
                                                                Toast.makeText(getActivity(), "Request Accepted", Toast.LENGTH_SHORT).show();

                                                                marker.remove();
                                                                Marker newmarker = mMap.addMarker(new MarkerOptions()
                                                                        .position(marker.getPosition())
                                                                        .title(id + "@Accepted")
                                                                        .icon(bitmapDescriptorFromVector(myContext, R.drawable.accepted_request)));

                                                                }
                                                            });
                                                        }
                                                        else {
                                                            Toast.makeText(getActivity(), R.string.Error_connecting_to_database, Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                });
                                            }
                                        });


                                    }
                                }
                            });

                            mMap.setMapType(MAP_TYPE_NORMAL);
                            mMap.clear();
                            googlePlex = CameraPosition.builder().target(currPosition).zoom((float) 13.5).bearing(0).build();
                            circle = drawCircle(currPosition, mMap);//draw the circle of 2.5km radius
                            final Marker mark = setMarker(mMap, currPosition);//set the marker attribute
                            mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                                @Override
                                public void onCameraMoveStarted(int i) {
                                    if (i == 1) {
                                        focus = 0;
                                        recenter.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10, null);

                            if(currPosition.latitude == 0){
                                progressBar1.show();
                                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                                    @Override
                                    public void onLocationChanged(Location location) {
                                        locationManager.removeUpdates(this);
                                        Log.v(TAG,"current location: "+location);
                                        currPosition = new LatLng(location.getLatitude(),location.getLongitude());
                                        setUserLocation(user, currPosition);
                                        address = findLocality(getContext(), currPosition);
                                        updateLocality(db, user, address);
                                        mark.setPosition(currPosition);
                                        circle.setCenter(currPosition);
                                        googlePlex = CameraPosition.builder().target(currPosition).zoom((float) 13.5).bearing(0).build();
                                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
                                        progressBar1.dismiss();
                                        startVolunteerDiscoveryThread(mMap);
                                        if(user.getType() == 1){
                                            startRequesterDiscoveryThread(mMap);
                                        }
                                    }
                                    @Override
                                    public void onStatusChanged(String provider, int status, Bundle extras) {}
                                    @Override
                                    public void onProviderEnabled(String provider) {}
                                    @Override
                                    public void onProviderDisabled(String provider) {}
                                });
                            }
                            else{
                                setUserLocation(user, currPosition);
                                address = findLocality(getContext(), currPosition);
                                updateLocality(db, user, address);
                                startVolunteerDiscoveryThread(mMap);
                                if(user.getType() == 1){
                                    startRequesterDiscoveryThread(mMap);
                                }
                            }

                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    progressBar1.dismiss();
                                    if (currPosition.latitude == location.getLatitude() && currPosition.longitude == location.getLongitude()) {
                                        Log.v(TAG, "Location updated (SAME VALUE)");
                                    } else {
                                        currPosition = new LatLng(location.getLatitude(), location.getLongitude());
                                        setUserLocation(user, currPosition);
                                        Log.v(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());
                                        mark.setPosition(currPosition);
                                        circle.setCenter(currPosition);
                                        if(focus == 1){
                                            googlePlex = CameraPosition.builder().target(currPosition).zoom((float) 13.5).bearing(0).build();
                                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
                                        }
                                    }
                                }
                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {}
                                @Override
                                public void onProviderEnabled(String provider) {}
                                @Override
                                public void onProviderDisabled(String provider) {}
                            });
                            recenter.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    focus = 1;
                                    googlePlex = CameraPosition.builder().target(currPosition).zoom((float) 13.5).bearing(0).build();
                                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
                                    recenter.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    }
                }
            }
        });
        return root;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 123: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG,"Location permission granted");
                    fetchCurrLocation();
                    NavController nc = Navigation.findNavController(getActivity(), R.id.fragmentContainerView); /*TODO: Change inflator*/
                    PendingIntent Pin = nc.createDeepLink().setDestination(R.id.mapFragment).createPendingIntent();
                    try {
                        Pin.send();
                        getActivity().overridePendingTransition(0,0);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.v(TAG,"Location permission rejected");
                }
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
        return (c * r);
    }

    public Marker setMarker(GoogleMap map, LatLng my_position){
        Marker location_marker = map.addMarker(new MarkerOptions()
                .position(my_position)
                .title(getString(R.string.You_are_here))
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

    private View prepareInfoView(Activity myActivity){
        LayoutInflater inflater = myActivity.getLayoutInflater();
        View markerView = inflater.inflate(R.layout.marker_view, null,false);
        return markerView;
    }

    public void setUserLocation(User myUser,LatLng my_position){
        Log.v(TAG,"Setting User location: "+my_position);
        myUser.setLatitude(Double.toString(my_position.latitude));
        myUser.setLongitude(Double.toString(my_position.longitude));
        if (myUser.getUserid().equals("")){
            Log.v(TAG, "Found Null user ID");
            return;
        }
        db.collection("user_details").document(myUser.getUserid()).update("latitude",my_position.latitude, "longitude",my_position.longitude).addOnFailureListener(new OnFailureListener() {
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
        Log.v(TAG,"Last known location (NETWORK): " + currentLoc);
        if(currentLoc == null){ return  new LatLng(0,0); }
        else{return new LatLng(currentLoc.getLatitude(),currentLoc.getLongitude());}
    }

    public void gpsDisabledAlert(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(R.string.GPS_service_is_disabled)
                .setCancelable(false)
                .setPositiveButton(R.string.Turn_on_GPS, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void internetDisabledAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false);
        builder.setTitle(R.string.No_Internet_Connection);
        builder.setMessage(R.string.Internet_Connection_is_required);
        builder.setNegativeButton(R.string.Ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
        builder.show();
    }

    public int findVolunteer(String volunteerID){
        int i;
        for(i=0;i<volunteerList.size();i++){
            if(volunteerList.get(i).getUserId().equals(volunteerID)){
                return i;
            }
        }
        return -1;
    }

    public void discoverVolunteers(GoogleMap map, LatLng position, String myUserId, String locality){
        final LatLng currPosition = position;
        final GoogleMap mMap = map;
        final String userId = myUserId;
        List<Volunteer> clonedVolunteerList = new ArrayList<>(volunteerList);
        while(getActivity() != null) {
            if (clonedVolunteerList != null) {
                for (Volunteer volunteer : clonedVolunteerList ) {
                    if(currFlag == volunteer.flag) {
                        volunteerList.remove(volunteer);
                        marker = volunteer.marker;
                        Log.d(TAG,"Removing Marker of "+ volunteer.userId);
                        if(getActivity()!=null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    marker.remove();
                                }
                            });
                        }
                    }
                }
            }
            clonedVolunteerList = new ArrayList<>(volunteerList);
            db.collection("user_details").whereEqualTo("localeCity", locality).whereEqualTo("type","1").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.getId().equals(userId))
                                continue;
                            int i;
                            for(i = 0; i < volunteerList.size(); i++){
                                Volunteer volunteer = volunteerList.get(i);
                                if(volunteer.getUserId().equals(document.getId())){
                                    LatLng latLng = new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString()));
                                    if (distance(latLng, currPosition) < 2.5) {
                                        if(volunteer.getUsertype().equals("requester")){
                                            Log.d(TAG,"Skip Requester Volunteer: "+volunteer.getUserId());
                                        }
                                        else {
                                            Log.d(TAG, "Volunteer updated: " + document.getId());
                                            Marker marker = volunteer.getMarker();
                                            marker.setPosition(new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString())));
                                        }
                                        volunteer.flag = currFlag;
                                    }
                                    break;
                                }
                            }
                            if(i == volunteerList.size()){
                                LatLng latLng = new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString()));
                                if (distance(latLng, currPosition) < 2.5) {
                                    Volunteer volunteer = new Volunteer();
                                    volunteer.flag = currFlag;
                                    volunteer.setUserId(document.getId());
                                    volunteer.setUsertype("volunteer");
                                    if(user.getVolunteerId().equals(document.getId()) ){
                                        volunteer.marker = mMap.addMarker(new MarkerOptions()
                                                .position(latLng)
                                                .title(getString(R.string.Assigned_Volunteer))
                                                .icon(bitmapDescriptorFromVector(myContext, R.drawable.assigned_volunteer)));
                                    }
                                    else{
                                        volunteer.marker = mMap.addMarker(new MarkerOptions()
                                                .position(latLng)
                                                .title(getString(R.string.Volunteer))
                                                .icon(bitmapDescriptorFromVector(myContext, R.drawable.baseline_volunteer_location_on_24)));
                                    }
                                    volunteerList.add(volunteer);
                                    Log.d(TAG,"Volunteer Added: "+document.getId());
                                    Log.d(TAG,"Volunteer List"+volunteerList.toString());
                                }
                            }
                        }
                    } else {
                        Log.v(TAG, getString(R.string.Database_Error_in_fetching));
                    }
                }
            });
            try {
                sleep(5000);
                currFlag = !currFlag;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void discoverRequesters(GoogleMap mMap1, final LatLng my_cur_position, String myuserId, String Locality){
        final LatLng cur_position = my_cur_position;
        final GoogleMap mMap = mMap1;
        final String userId = myuserId;
        while(getActivity() != null) {
            if (requesterList != null) {
                List<Requester> clonedRequesterList = new ArrayList<>(requesterList);
                for (final Requester requester : clonedRequesterList ){
                    if(currFlag1 == requester.flag) {
                        requesterList.remove(requester);
                        final int position = findVolunteer(requester.getUserId());
                        final LatLng latlng = new LatLng(Double.parseDouble(requester.getLatitude()),Double.parseDouble(requester.getLongitude()));
                        if(position != -1){
                            if(getActivity()!= null){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(position<volunteerList.size()){
                                            Log.d(TAG,"Volunteer Requester Removed: "+requester.getUserId());
                                            marker = volunteerList.get(position).marker;
                                            marker.remove();
                                            volunteerList.get(position).setUsertype("volunteer");
                                            volunteerList.get(position).marker = mMap.addMarker(new MarkerOptions()
                                                    .position(latlng)
                                                    .title(getString(R.string.Volunteer))
                                                    .icon(bitmapDescriptorFromVector(myContext, R.drawable.baseline_volunteer_location_on_24)));
                                        }
                                    }
                                });
                            }
                        }
                        else{
                            marker = requester.marker;
                            if(getActivity()!=null && marker!= null){
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d(TAG,"Requester Removed: "+requester.getUserId());
                                        marker.remove();
                                    }
                                });
                            }
                        }
                    }
                }
            }

            db.collection("emergency_requests").whereEqualTo("localeCity", Locality).whereIn("status", Arrays.asList("Active", "Accepted")).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.get("userId").equals(userId))
                                continue;
                            int i;
                            int requestIcon;
                            String requestType;
                            if (document.get("status").toString().equals("Active")){
                                requestIcon = R.drawable.triggered_request;
                                requestType = "@Active";
                            }
                            else {
                                requestIcon = R.drawable.accepted_request;
                                requestType = "@Accepted";
                            }
                            for(i = 0; i < requesterList.size(); i++){
                                Requester requester = requesterList.get(i);
                                if(requester.getUserId().equals(document.get("userId"))){
                                    LatLng latLng = new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString()));
                                    if (distance(latLng, cur_position) < 2.5) {

                                        requester.setLatitude(document.get("latitude").toString());
                                        requester.setLongitude(document.get("longitude").toString());
                                        int position = findVolunteer(requester.getUserId());
                                        requester.flag = currFlag1;

                                        if(position != -1){
                                            Log.d(TAG,"Volunteer Requester Updated: "+volunteerList.get(position).getUserId());
                                            if(!volunteerList.get(position).getUsertype().equals("requester")){
                                                Log.d(TAG,"Volunteer --> Requester");
                                                volunteerList.get(position).setUsertype("requester");
                                                volunteerList.get(position).marker.remove();
                                                volunteerList.get(position).marker = mMap.addMarker(new MarkerOptions()
                                                        .position(latLng)
                                                        .title(requester.getRequestId() + requestType)
                                                        .icon(bitmapDescriptorFromVector(myContext, requestIcon)));
                                                //requester.marker = volunteerList.get(position).marker;
                                            }
                                        }
                                        else{
                                            Log.d(TAG, "Requester updated: "+document.get("userId").toString());
                                            Marker marker = requester.getMarker();
                                            marker.setPosition(new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString())));
                                        }
                                    }
                                    break;
                                }
                            }
                            if(i == requesterList.size()){
                                LatLng latLng = new LatLng(Double.parseDouble(document.get("latitude").toString()), Double.parseDouble(document.get("longitude").toString()));
                                if (distance(latLng, cur_position) < 2.5) {

                                    Requester requester = new Requester();
                                    requester.setRequestId(document.getId());
                                    requester.setUserId(document.get("userId").toString());
                                    requester.setLatitude(document.get("latitude").toString());
                                    requester.setLongitude(document.get("longitude").toString());
                                    requester.flag = currFlag1;

                                    int position = findVolunteer(requester.getUserId());
                                    if(position != -1){
                                        volunteerList.get(position).setUsertype("requester");
                                        volunteerList.get(position).marker.remove();
                                        volunteerList.get(position).marker = mMap.addMarker(new MarkerOptions()
                                                .position(latLng)
                                                .title(requester.getRequestId() + requestType)
                                                .icon(bitmapDescriptorFromVector(myContext, requestIcon)));
                                        //requester.marker = volunteerList.get(position).marker;
                                        Log.d(TAG,"Volunteer Requester Added: "+document.get("userId").toString());
                                    }
                                    else{
                                        requester.marker = mMap.addMarker(new MarkerOptions()
                                                .position(latLng)
                                                .title(requester.getRequestId() + requestType)
                                                .icon(bitmapDescriptorFromVector(myContext, requestIcon)));
                                        Log.d(TAG,"Requester Added: "+document.get("userId").toString());
                                    }
                                    requesterList.add(requester);
                                }
                            }
                        }
                    }
                    else {
                        Log.d(TAG, "Error fetching Requester");
                    }
                }
            });
            try {
                sleep(5000);
                currFlag1 = !currFlag1;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startVolunteerDiscoveryThread(GoogleMap map){
        final GoogleMap myMap = map;
        Log.v(TAG,"(VT)ThreadId1: "+currentThread().getName() + currentThread().getId());
        Thread thread = new Thread(){
            public void run(){
                Log.d(TAG,"Starting Volunteer Thread: "+currentThread().getName() + currentThread().getId());
                discoverVolunteers(myMap, currPosition, user.getUserid(), address.getLocality());
                Log.d(TAG,"Volunteer Thread Ended");
            }
        };
        thread.start();
    }

    public void startRequesterDiscoveryThread(GoogleMap map){
        final GoogleMap myMap = map;
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.v(TAG,"(VT)ThreadId1: "+currentThread().getName() + currentThread().getId());
        Thread thread = new Thread(){
            public void run(){
                Log.v(TAG,"Starting Requester Thread: "+currentThread().getName() + currentThread().getId());
                discoverRequesters(myMap, currPosition, user.getUserid(), address.getLocality());
                Log.d(TAG,"Requester Thread Ended");
            }
        };
        thread.start();
    }

}