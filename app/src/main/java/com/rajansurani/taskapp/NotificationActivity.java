package com.rajansurani.taskapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    ArrayList<String> mNotificationList;

    ListView mListView;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mReference;

    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_notification);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar ().setTitle ("Notifications");

        mListView = findViewById (R.id.notification_list);

        mAuth = FirebaseAuth.getInstance ();
        mDatabase = FirebaseDatabase.getInstance ();
        mReference = mDatabase.getReference ("Notification/"+mAuth.getCurrentUser ().getDisplayName ());

        mNotificationList = new ArrayList<> ();
        adapter = new ArrayAdapter<String> (NotificationActivity.this, android.R.layout.simple_list_item_1, mNotificationList);
        mReference.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists ()){
                    mNotificationList.clear ();
                    for(DataSnapshot data : dataSnapshot.getChildren ())
                    {
                        mNotificationList.add (data.getValue ().toString ());
                    }
                    mListView.setAdapter (adapter);
                    adapter.notifyDataSetChanged ();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
