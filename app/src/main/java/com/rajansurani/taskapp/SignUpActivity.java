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
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rajansurani.taskapp.Model.User;

public class SignUpActivity extends AppCompatActivity {

    TextView mLogin;
    EditText mEmail, mName, mPassword;
    Button mSignUp;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance ();
        mDatabase = database.getReference ("Users");

        mLogin = findViewById (R.id.tv_login);
        mEmail = findViewById (R.id.et_SIGNUP_email);
        mPassword = findViewById (R.id.et_SIGNUP_pass);
        mName = findViewById (R.id.et_SIGNUP_name);
        mSignUp = findViewById (R.id.btn_signup);

        mLogin.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (SignUpActivity.this, LoginActivity.class);
                startActivity (intent);
                finish ();
            }
        });

        mSignUp.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                registerNewUser ();
            }
        });
    }

    public void registerNewUser()
    {
        final String email_id = mEmail.getText ().toString ();
        String password = mPassword.getText ().toString ();
        final String fullname  = mName.getText ().toString ();
        if(fullname.isEmpty ())
        {
            mName.setError ("Name is Required");
            mName.requestFocus ();
            return;
        }
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

        final ProgressDialog mDialog = new ProgressDialog ((SignUpActivity.this));
        mDialog.setMessage ("Signing  Up...");
        mDialog.show ();

        mAuth.createUserWithEmailAndPassword(email_id, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult> () {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mDialog.dismiss ();
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SignUp", "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();

                            if(user!=null)
                            {
                                UserProfileChangeRequest profile = new  UserProfileChangeRequest.Builder()
                                        .setDisplayName (fullname)
                                        .build ();

                                user.updateProfile (profile)
                                        .addOnCompleteListener (new OnCompleteListener<Void> () {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful ()){
                                                    Log.d ("SignUp","Display name updated");
                                                    User u = new User(mAuth.getCurrentUser ().getDisplayName (), user.getEmail ());
                                                    mDatabase.child (user.getUid ()).setValue (u);
                                                }

                                                else
                                                    Log.d ("SignUp","Display name not updated");
                                            }
                                        });
                            }
                            Intent i = new Intent (SignUpActivity.this, DashboardActivity.class);
                            finish ();
                            startActivity (i);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("SignUp", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                            if(task.getException () instanceof FirebaseAuthUserCollisionException)
                            {
                                mEmail.setError ("Account Already Exsits");
                                mEmail.requestFocus ();
                            }
                        }

                    }
                });

    }
}
