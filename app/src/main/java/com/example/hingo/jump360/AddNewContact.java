package com.example.hingo.jump360;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddNewContact extends FragmentActivity  implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 1;
    Marker marker;

    FirebaseDatabase fbDatabase;
    FirebaseUser fbUser;
    FirebaseAuth fbAuth;
    DatabaseReference dbReference;
    //GoogleApiClient mGoogleApiClient;

    private GoogleMap mMap;
    Double latitude=0.0, longitude=0.0;

    EditText etName, etContactNo;
    ProgressBar pbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contact);

        //Getting the refrences of xml elements
        etName = (EditText) findViewById(R.id.add_contactName);
        etContactNo = (EditText) findViewById(R.id.add_contactNumber);
        pbar = findViewById(R.id.progress_addBtn);

        findViewById(R.id.add_contact_btn).setOnClickListener(this);
        findViewById(R.id.add_location_btn).setOnClickListener(this);

        //initializing firebase elements
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();

        //Setting up Api reference for google map location picker
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        //Initializing the map to display contact's static position
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    //When the static map is successfully initalized call this, and add marker to specified latitude and longitude
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        LatLng initialPos = new LatLng(0, 0);
        mMap.addMarker(new
                MarkerOptions().position(initialPos).title("Initial Position"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPos));
    }

    //Connect to google maps api on start
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    //Disconnect from google maps api on end
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //If user clicks on Add Contact Button
            case R.id.add_contact_btn:

                String number = etContactNo.getText().toString().trim();
                String name = etName.getText().toString().trim();

                //Validations
                if(name.isEmpty()){
                    etName.setError("Name is required!");
                    etName.requestFocus();
                    return;
                }
                if(number.isEmpty()){
                    etContactNo.setError("Contact Number is Required");
                    etContactNo.requestFocus();
                    return;
                }
                if(number.length() > 10)
                {
                    etContactNo.setError("Contact Number should be maximum 10 digits long!");
                    etContactNo.requestFocus();
                    return;
                }

                pbar.setVisibility(View.VISIBLE);  //Making the loading bar Visible
                Contact contact = new Contact();   //Initializing a new contact object to be added to firebase
                contact.setName(name);
                contact.setContactNo(Long.parseLong(number));
                contact.setLatitude(latitude);
                contact.setLongitude(longitude);
                //Adding new object to firebase
                dbReference.child("contacts").child(fbUser.getUid()).child(number).setValue(contact).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        pbar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Contact Added", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                        finish();
                    }
                });
                break;

            //When Pick Location button is clicked
            case R.id.add_location_btn:
                //Calling new activity provided by google to select location
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(AddNewContact.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    //Called after user has selected a location
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);//getPlace(data, this);
                StringBuilder stBuilder = new StringBuilder();
                //String placename = String.format("%s", place.getName());
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                String address = String.format("%s", place.getAddress());

                LatLng newPos = new LatLng(latitude, longitude);

                //Removing previous markers from the static map to assign new ones
                if(marker!=null)
                    marker.remove();

                //Adding new marker to new position
                marker = mMap.addMarker(new MarkerOptions().position(newPos).title(address));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(newPos).zoom(14.0f).build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
                mMap.moveCamera(cameraUpdate);

                //mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos));
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
