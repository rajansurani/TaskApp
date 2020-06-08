package com.rajansurani.taskapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextView mSignup;
    EditText mEmail, mPassword;
    Button mLogin;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mSignup = findViewById (R.id.tv_signup);
        mEmail = findViewById (R.id.et_LOGIN_email);
        mPassword = findViewById (R.id.et_LOGIN_pass);
        mLogin = findViewById (R.id.btn_login);

        mSignup.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (LoginActivity.this, SignUpActivity.class);
                startActivity (intent);
                finish ();
            }
        });

        mLogin.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    public void loginUser()
    {
        String email_id = mEmail.getText ().toString ();
        String password = mPassword.getText ().toString ();

        if(email_id.isEmpty ())
        {
            mEmail.setError ("Email is Required");
            mEmail.requestFocus ();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher (email_id).matches ())
        {
            mEmail.setError ("Invalid Email Address");
            mEmail.requestFocus ();
            return;
        }

        if(password.isEmpty ())
        {
            mPassword.setError ("Password is Required");
            mPassword.requestFocus ();
            return;
        }

        if(password.length () <8)
        {
            mPassword.setError ("Password length should be more than 8");
            mPassword.requestFocus ();
            return;
        }

        final ProgressDialog mDialog = new ProgressDialog ((LoginActivity.this));
        mDialog.setMessage ("Login in progess...");
        mDialog.show ();

        mAuth.signInWithEmailAndPassword(email_id, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult> () {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mDialog.dismiss ();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignIn", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Intent i = new Intent (LoginActivity.this, DashboardActivity.class);
                            finish ();
                            startActivity (i);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignIn", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }
}
