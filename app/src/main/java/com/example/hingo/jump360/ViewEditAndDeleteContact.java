package com.example.hingo.jump360;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewEditAndDeleteContact extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private int PLACE_PICKER_REQUEST = 1;
    Marker marker;

    FirebaseDatabase fbDatabase;
    FirebaseUser fbUser;
    FirebaseAuth fbAuth;
    DatabaseReference dbReference;

    String name;
    Long number;
    double latitude, longitude, newLat, newLong;
    private GoogleMap mMap;
    boolean isTextViewON = false;

    TextView tvName, tvNumber;
    Button btnEdit, btnOk, btnLocation, btnDelete;
    EditText etName, etNumber;
    ProgressBar pbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_edit_and_delete_contact);

        //Referencing xml objects in java
        tvName = findViewById(R.id.tv_ContactName);
        tvNumber = findViewById(R.id.tv_ContactNumber);
        etName = findViewById(R.id.et_add_contactName);
        etNumber = findViewById(R.id.et_add_contactNumber);
        btnEdit = findViewById(R.id.btn_editContact);
        btnOk = findViewById(R.id.btn_Done);
        btnLocation = findViewById(R.id.btn_changeLocation);
        btnDelete = findViewById(R.id.btn_deleteContact);

        pbar = findViewById(R.id.progressEditDelete);

        //Adding listeners to the button
        btnEdit.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnLocation.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        //Disabling the change location button. Will only be enables when user clicks on edt contact button
        btnLocation.setEnabled(false);
        btnLocation.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY); //Changing color to show btn os disabled

        //Getting the data passed from the ContactsActivity
        if( getIntent().getExtras() != null)
        {
            Intent i = getIntent();
            name = i.getStringExtra("name");
            number = i.getLongExtra("number",0);
            latitude = i.getDoubleExtra("latitude",0);
            longitude = i.getDoubleExtra("longitude",0);

            tvName.setText(name);
            tvNumber.setText(number+"");
            etName.setText(name);
            etNumber.setText(number+"");
        }

        //Setting up the static map which shows contact's address location
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

        //Setting up the google maps api for change location feature. This is for taking new position from user
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        //Setting up and initializing firebase Database
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();
        dbReference = FirebaseDatabase.getInstance().getReference();
    }

    //When the static map which shows contacts location is ready
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng initialPos = new LatLng(latitude, longitude);

        //Removing previous marker to add new
        if(marker!=null)
            marker.remove();

        //Adding new marker at contacts specified location
        marker = mMap.addMarker(new MarkerOptions().position(initialPos).title("Address"));
        CameraPosition cameraPosition = new CameraPosition.Builder().target(initialPos).zoom(14.0f).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        mMap.moveCamera(cameraUpdate);
        // mMap.moveCamera(CameraUpdateFactory.newLatLng(initialPos));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_editContact:
                toggleTextView();   //Toggling the edittexts and change location button so that user can edit
                break;


            case R.id.btn_changeLocation:
                //Start new Activity provided by google when user chooses to change location
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ViewEditAndDeleteContact.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;


            case R.id.btn_deleteContact:
                //When user chooses to delete contact
                pbar.setVisibility(View.VISIBLE);

                //Show confirm dialog box
                AlertDialog.Builder build = new AlertDialog.Builder(this);

                build.setTitle("Confirm");
                build.setMessage("Are you sure?");

                //If user selects yes in dialog box
                build.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        pbar.setVisibility(View.INVISIBLE);

                        //Delete contact from firebase database
                        dbReference.child("contacts").child(fbUser.getUid()).child(number+"").removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                try{
                                    //When contact is deleted hide loading bar and change activity
                                    pbar.setVisibility(View.INVISIBLE);
                                    startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                                }
                                finally {
                                    finish();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                build.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //If user chooses not to delete the contact in the dialog, then simply hide loading bar and dismiss the dialogbox
                        pbar.setVisibility(View.INVISIBLE);
                        dialog.dismiss();
                    }
                });

                AlertDialog alert = build.create();
                alert.show();
                break;


            case R.id.btn_Done:
                //When user clicks on "Done" i.e save changes and edit contact

                pbar.setVisibility(View.VISIBLE); //Show loading bar
                //Get data from textboxes
                final String newName = etName.getText().toString().trim();
                final String newNumber = etNumber.getText().toString().trim();
                final Contact contact = new Contact(newName, Long.parseLong(newNumber), latitude, longitude);

                //Validations
                if(newName.isEmpty()){
                    etName.setError("Name is required!");
                    etName.requestFocus();
                    return;
                }
                if(newNumber.isEmpty()){
                    etNumber.setError("Contact Number is Required");
                    etNumber.requestFocus();
                    return;
                }
                if(newNumber.length() > 10)
                {
                    etNumber.setError("Contact Number should be maximum 10 digits long!");
                    etNumber.requestFocus();
                    return;
                }

                pbar.setVisibility(View.VISIBLE);

                //If user has actually made any new changes to the contact then only fire the database query
                if(!name.equals(newName) || !number.equals(newNumber) || latitude!=newLat || longitude!=newLong){

                    //Editing the contact and setting new values
                    dbReference.child("contacts").child(fbUser.getUid()).child(number+"").removeValue(new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            dbReference.child("contacts").child(fbUser.getUid()).child(newNumber).setValue(contact);
                            pbar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
                        }
                    });
                }
                else //If user has made no  new changes and directly clicked on "Done". simply change the activity
                    startActivity(new Intent(getApplicationContext(), ContactsActivity.class));

                finish();

                break;
        }
    }

    //To turn EditText and location button on and hide textview so that user can make changes(edit) contact
    void textViewOn(){
        tvName.setVisibility(View.INVISIBLE);
        tvNumber.setVisibility(View.INVISIBLE);
        etName.setVisibility(View.VISIBLE);
        etNumber.setVisibility(View.VISIBLE);
        btnLocation.setEnabled(true);
        btnLocation.getBackground().setColorFilter(null);
    }

    //To turn EditText and location btn off and turn of TextViews
    void textViewOff(){
        tvName.setText(etName.getText().toString());
        tvNumber.setText(etNumber.getText().toString());

        tvName.setVisibility(View.VISIBLE);
        tvNumber.setVisibility(View.VISIBLE);
        etName.setVisibility(View.INVISIBLE);
        etNumber.setVisibility(View.INVISIBLE);
        btnLocation.setEnabled(false);
        btnLocation.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
    }

    //Toggling between the functions
    void toggleTextView(){
        if(isTextViewON)
            textViewOff();
        else
            textViewOn();

        isTextViewON = !isTextViewON;
    }

    //When user has successfully selected a new position in the maps. This func is called
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this,data);//getPlace(data, this);

                //Getting new latitude and longitude
                newLat = place.getLatLng().latitude;
                newLong = place.getLatLng().longitude;
                String address = String.format("%s", place.getAddress());

                //Removing previous marker from the static map to add new
                if(marker!=null)
                    marker.remove();

                //Add new marker to the static map at specified latitide and longitude
                LatLng newPos = new LatLng(newLat, newLong);
                mMap.addMarker(new MarkerOptions().position(newPos).title(address));
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
