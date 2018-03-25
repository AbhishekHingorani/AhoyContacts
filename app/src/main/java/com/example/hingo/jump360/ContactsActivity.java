package com.example.hingo.jump360;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class ContactsActivity extends AppCompatActivity implements View.OnClickListener {

    //Initial variable Declarations
    TextView tv_currentUserName, tvNoContactsYet;
    ProgressBar pb_loadingBar;
    RelativeLayout rightRL;
    DrawerLayout drawerLayout;
    FirebaseUser fbUser;
    FirebaseAuth fbAuth;
    private RecyclerView recyclerView;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<Contact, ContactsHolder> firebaseContactsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        //Refrencing variables from xml file
        pb_loadingBar = (ProgressBar) findViewById(R.id.pb_contactsLoading);
        findViewById(R.id.logoutBtn).setOnClickListener(this);
        findViewById(R.id.btn_addNewContact).setOnClickListener(this);
        tvNoContactsYet = (TextView) findViewById(R.id.tv_noContactsYet);

        //Initializing the draweer that opens from the side
        rightRL = (RelativeLayout) findViewById(R.id.whatYouWantInRightDrawer);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        findViewById(R.id.contactSideMenuButton).setOnClickListener(this);

        //Initializing firebase objects
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();

        //Setting email of user in the Navigation drawer
        tv_currentUserName = (TextView) findViewById(R.id.tv_nameTxt);
        Toast.makeText(this, fbUser.getEmail(), Toast.LENGTH_LONG);
        tv_currentUserName.setText(fbUser.getEmail());

        //Handling firebase database and setting up connection
        mDatabase = FirebaseDatabase.getInstance().getReference().child("contacts").child(fbUser.getUid());
        mDatabase.keepSynced(true);

        recyclerView = (RecyclerView) findViewById(R.id.contactsRecycler);

        //Getting database Reference
        DatabaseReference personsRef = FirebaseDatabase.getInstance().getReference().child("contacts").child(fbUser.getUid());
        Query personsQuery = personsRef.orderByKey();

        recyclerView.hasFixedSize();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseRecyclerOptions personsOptions = new FirebaseRecyclerOptions.Builder<Contact>().setQuery(personsQuery, Contact.class).build();

        //Populating the Recycler View
        firebaseContactsAdapter = new FirebaseRecyclerAdapter<Contact, ContactsActivity.ContactsHolder>(personsOptions) {
            @Override
            protected void onBindViewHolder(ContactsActivity.ContactsHolder holder, final int position, final Contact model) {
                holder.setName(model.getName());
                holder.setContactNumber(model.getContactNo());

                //Adding new onclicklistener to every item of the recycler view. i.e evey contact
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String cno = model.getName();
                        Toast.makeText(getApplicationContext(), cno, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ViewEditAndDeleteContact.class);
                        intent.putExtra("number", model.getContactNo());
                        intent.putExtra("name", model.getName());
                        intent.putExtra("latitude", model.getLatitude());
                        intent.putExtra("longitude", model.getLongitude());
                        startActivity(intent);
                        finish();
                    }
                });
            }

            //Detecting if Recycler view has done loading
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                //If done loading disable loading bar
                if (pb_loadingBar != null && pb_loadingBar.getVisibility()==View.VISIBLE) {
                    pb_loadingBar.setVisibility(View.GONE);

                    //If recycler is empty, show "no contacts" message in this textview
                    if(firebaseContactsAdapter.getItemCount() == 0)
                        tvNoContactsYet.setVisibility(View.VISIBLE);

                }
            }

            //Inflating every single contact's view for the recycler
            @Override
            public ContactsActivity.ContactsHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_column_row, parent, false);

                view.getBackground().setAlpha(200);
                return new ContactsActivity.ContactsHolder(view);
            }
        };

        recyclerView.setAdapter(firebaseContactsAdapter);
    }

    //Start listening to realtime database
    @Override
    public void onStart() {
        super.onStart();
        firebaseContactsAdapter.startListening();
    }
    //Stop listening to realtime database
    @Override
    public void onStop() {
        super.onStop();
        firebaseContactsAdapter.stopListening();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //Open the right navigation drawer if clicked on top right button
            case R.id.contactSideMenuButton:
                drawerLayout.openDrawer(Gravity.RIGHT);
                break;
            case R.id.logoutBtn:
                fbAuth.signOut(); //Logout if clicked on logout button in side navigation drawer
                finish();
                startActivity(new Intent(this, HomeActivity.class));
                break;
            case R.id.btn_addNewContact:
                startActivity(new Intent(this, AddNewContact.class)); //start new activity if user chooses to add new contact
                break;
        }
    }

    //Class for the refernce of Recycler view. Object of this will be generated for each contact in the recylcer to populate it
    public static class ContactsHolder extends RecyclerView.ViewHolder {
        View mView;

        public ContactsHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView contact_name = (TextView) mView.findViewById(R.id.contactName);
            contact_name.setText(name);
        }

        public void setContactNumber(Long number) {
            TextView contact_number = (TextView) mView.findViewById(R.id.contactNumber);
            contact_number.setText(number+"");
        }
    }
}
