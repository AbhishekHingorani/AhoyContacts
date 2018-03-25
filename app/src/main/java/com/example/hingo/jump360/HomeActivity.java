package com.example.hingo.jump360;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseUser fbUser;
    FirebaseAuth fbAuth;

    @Override
    protected void onStart() {
        super.onStart();
        fbAuth = FirebaseAuth.getInstance();
        fbUser = fbAuth.getCurrentUser();
        if(fbUser!=null)
        {
            try{
                startActivity(new Intent(this, ContactsActivity.class));
            }
            finally {
                this.finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Referencing xml objects in JAVA
        findViewById(R.id.tv_loginBtn).setOnClickListener(this);
        findViewById(R.id.tv_createAccountBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            //Start new login or signup activity based on the button clicked
            case R.id.tv_loginBtn : startActivity(new Intent(this, LoginActivity.class));
                                    break;
            case R.id.tv_createAccountBtn : startActivity(new Intent(this, SignupActivity.class));
                                            break;
        }
    }
}
