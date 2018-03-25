package com.example.hingo.jump360;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener {

    EditText etEmailId, etPassword;
    ProgressBar loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance(); //Setting instance of firebase authentication

        //Referencing xml objects in JAVA
        etEmailId = (EditText) findViewById(R.id.signupEmail);
        etPassword = (EditText) findViewById(R.id.signupPassword);
        loadingBar = (ProgressBar) findViewById(R.id.loadingBar);
        findViewById(R.id.btnSignup).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.btnSignup : registerUser();
                break;
        }
    }

    private void registerUser()
    {
        String email = etEmailId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        //Validations
        if(email.isEmpty())
        {
            etEmailId.setError("Email is required!");
            etEmailId.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            etEmailId.setError("Please enter correct email!");
            etEmailId.requestFocus();
            return;
        }
        if(password.isEmpty())
        {
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
            return;
        }
        if(password.length() < 6)
        {
            etPassword.setError("Minimum length of password should be 6 characters!");
            etPassword.requestFocus();
            return;
        }

        loadingBar.setVisibility(View.VISIBLE); //Showing loading bar

        //Make user signup using email and password
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.setVisibility(View.GONE);
                if(task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "User Registered Successfully!", Toast.LENGTH_SHORT).show();

                    //starting new activity when done loading and user is signed up properly
                    Intent intent = new Intent(SignupActivity.this, ContactsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else {
                    //Validations and Error handling if user already exists
                    if(task.getException() instanceof FirebaseAuthUserCollisionException)
                        Toast.makeText(getApplicationContext(), "You are already registered!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(getApplicationContext(), "Some Error Occurred!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
