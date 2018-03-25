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

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    EditText etEmailId, etPassword;
    ProgressBar loadingBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance(); //Setting instance of firebase authentication

        //Referencing xml objects in JAVA
        etEmailId = (EditText) findViewById(R.id.loginEmail);
        etPassword = (EditText) findViewById(R.id.loginPassword);
        loadingBar = (ProgressBar) findViewById(R.id.loginLoadingBar);
        findViewById(R.id.btnLogin).setOnClickListener(this);
    }

    private void loginUser()
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

        loadingBar.setVisibility(View.VISIBLE); //Making the loading bar visible

        //Make user signin using email and password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                loadingBar.setVisibility(View.GONE); //Making loading bar invisible when done loading

                //If login was successful, open new activity
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, ContactsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                else{ //else display error message
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.btnLogin : loginUser();
                                 break;
        }
    }
}
