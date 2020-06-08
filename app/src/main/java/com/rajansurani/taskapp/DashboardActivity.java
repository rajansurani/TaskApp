package com.rajansurani.taskapp;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rajansurani.taskapp.Model.Task;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private final  String TAG = "DashboardActivity";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    TextView mName, mEmail;
    FloatingActionButton mAddTask;
    RecyclerView mRecyclerView;

    private FirebaseUser currentUser;

    FirebaseDatabase mDatabase;
    DatabaseReference mReference;
    FirebaseAuth mAuth;

    ArrayList<String> taskID;
    ArrayList<Task> mTasksList;
    TaskViewAdapter mTaskViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_dashboard);

        currentUser = FirebaseAuth.getInstance ().getCurrentUser ();

        mDrawerLayout = findViewById(R.id.activity_dashboard);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,R.string.Open, R.string.Close);

        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar ().setTitle ("My Tasks");

        mRecyclerView = findViewById (R.id.task_recyclerview);
        mRecyclerView.setLayoutManager (new LinearLayoutManager (this));
        mTasksList = new ArrayList<> ();

        mAddTask = findViewById (R.id.new_task);
        mAddTask.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (DashboardActivity.this, NewTaskActivity.class);
                startActivity (intent);
            }
        });

        mNavigationView = findViewById(R.id.nv);
        mName = mNavigationView.getHeaderView (0).findViewById (R.id.nav_name);
        mEmail = mNavigationView.getHeaderView (0).findViewById (R.id.nav_email);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id)
                {
                    case R.id.my_tasks:
                        Toast.makeText(DashboardActivity.this, "My Tasks",Toast.LENGTH_SHORT).show();break;
                    case R.id.notification_menu:
                        {
                            Intent intent = new Intent (DashboardActivity.this, NotificationActivity.class);
                            startActivity (intent);
                        }break;
                    case R.id.signout:
                    {
                        FirebaseAuth.getInstance ().signOut ();
                        Intent intent = new Intent (DashboardActivity.this, LoginActivity.class);
                        startActivity (intent);
                        finish ();
                    }break;
                    default:
                        return true;
                }
                return true;
            }
        });

        mName.setText (currentUser.getDisplayName ());
        mEmail.setText (currentUser.getEmail ());

        mDatabase = FirebaseDatabase.getInstance ();
        mAuth = FirebaseAuth.getInstance ();
        mReference = mDatabase.getReference ("AllocatedTask/"+mAuth.getCurrentUser ().getDisplayName ());

        taskID = new ArrayList<> ();
        mReference.addValueEventListener (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists ()){
                    taskID.clear ();
                    for(DataSnapshot data : dataSnapshot.getChildren ())
                    {
                        taskID.add (data.getValue ().toString ());
                        Log.d (TAG, "onDataChange: "+ taskID.toString ());
                    }
                    updateTaskview();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void updateTaskview()
    {
        mTasksList.clear ();
        for(String task : taskID)
        {
            mReference = mDatabase.getReference ("Tasks").child (task);
            mReference.addValueEventListener (new ValueEventListener () {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Task t = dataSnapshot.getValue (Task.class);
                    mTasksList.add (t);
                    mTaskViewAdapter.notifyDataSetChanged ();
                    Log.d (TAG, "onDataChange: Task : "+ mTasksList.toString ());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        mTaskViewAdapter = new TaskViewAdapter (mTasksList, DashboardActivity.this, taskID);
        mRecyclerView.setAdapter (mTaskViewAdapter);
        Log.d (TAG, "updateTaskview: Done");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(mActionBarDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

}
