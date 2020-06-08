package com.rajansurani.taskapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rajansurani.taskapp.Model.Files;
import com.rajansurani.taskapp.Model.Task;
import com.rajansurani.taskapp.Model.User;

import java.util.ArrayList;

public class ViewTask extends AppCompatActivity {

    private static final int PICK_PDF_CODE = 2342;
    private final String TAG = "ViewTask";
    EditText mTitle, mInfo;
    AutoCompleteTextView mMembers;
    Button mAddMember, mAddFile, mUpdate;

    String taskId;

    ListView mMemberListView;
    RecyclerView mFilesView;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mReference;
    StorageReference mStorageReference;

    ArrayList<String> userName, memberList;
    ArrayAdapter<String> adapterUserName, adapterMember;

    ArrayList<Files> mFilesList;
    FileViewAdapter fileViewAdapter;

    ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_view_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar ().setTitle ("Your Task");

        Intent intent = getIntent ();
        Task t = (Task) intent.getSerializableExtra ("task");
        taskId = intent.getStringExtra ("taskId");
        Log.d (TAG, "onCreate: "+t);

        mTitle = findViewById (R.id.et_title_view);
        mInfo = findViewById (R.id.et_info_view);
        mMembers = findViewById (R.id.et_member_view);
        mMemberListView = findViewById (R.id.member_list_view);
        mFilesView = findViewById (R.id.file_list_view);
        mAddFile = findViewById (R.id.btn_add_file_view);
        mAddMember = findViewById (R.id.btn_add_member_view);
        mUpdate = findViewById (R.id.btn_update_task);

        mTitle.setText (t.getTitle ());
        mInfo.setText (t.getContent ());

        mFilesList = t.getFiles ();
        memberList = t.getMembers ();
        Log.d (TAG, "onCreate: "+mFilesList);
        Log.d (TAG, "onCreate: "+memberList);

        mDatabase = FirebaseDatabase.getInstance ();
        mReference = mDatabase.getReference ("Users");
        mStorageReference = FirebaseStorage.getInstance ().getReference ();
        mAuth = FirebaseAuth.getInstance ();
        userName = new ArrayList<> ();

        mReference.addListenerForSingleValueEvent (new ValueEventListener () {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d (TAG,dataSnapshot.toString ());
                for (DataSnapshot data: dataSnapshot.getChildren()) {
                    User user = data.getValue (User.class);
                    userName.add (user.getName ());
                    Log.d (TAG,user.toString ());
                }
                adapterUserName = new ArrayAdapter<String> (ViewTask.this, android.R.layout.select_dialog_item, userName);
                mMembers.setAdapter (adapterUserName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString ());
            }
        });
        Log.d (TAG, userName.toString ());
        mMembers.setThreshold (1);

        adapterMember = new ArrayAdapter<String> (ViewTask.this, android.R.layout.simple_list_item_1, memberList);
        mMemberListView.setAdapter (adapterMember);
        adapterMember.notifyDataSetChanged ();
        mAddMember.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                memberList.add (mMembers.getText ().toString ());
                adapterMember.notifyDataSetChanged ();
            }
        });

        mAddFile.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (ContextCompat.checkSelfPermission (ViewTask.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED)) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    return;
                }
                //creating an intent for file chooser
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_CODE);
            }
        });

        fileViewAdapter = new FileViewAdapter (mFilesList);
        mFilesView.setLayoutManager (new LinearLayoutManager (this));
        mFilesView.setAdapter (fileViewAdapter);
        fileViewAdapter.notifyDataSetChanged ();

        mUpdate.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                String title = mTitle.getText ().toString ();
                String info = mInfo.getText ().toString ();

                mReference = mDatabase.getReference ("Tasks/"+taskId+"/title");
                mReference.setValue (title);

                mReference = mDatabase.getReference ("Tasks/"+taskId+"/content");
                mReference.setValue (info);

                for(String member : memberList)
                {
                    mDatabase.getReference ("Notification").child (member).push ().setValue ("Task Update : "+title);
                }
                finish ();

            }
        });

    }

    private String uploadFile(Uri data,String filename) {
        mProgressBar.setVisibility (View.VISIBLE);
        final String[] downloadURL = {""};
        final StorageReference sRef = mStorageReference.child("uploads").child (filename);
        sRef.putFile(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot> () {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mProgressBar.setVisibility (View.GONE);
                        Log.d (TAG, "Upload done.");
                        sRef.getDownloadUrl ().addOnSuccessListener (new OnSuccessListener<Uri> () {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.d(TAG,uri.toString ());
                                downloadURL[0]=uri.toString ();
                            }
                        });
                        Log.d (TAG,downloadURL[0]);
                    }
                })
                .addOnFailureListener(new OnFailureListener () {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot> () {
                    @SuppressWarnings("VisibleForTests")
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mProgressBar.setProgress ((int) progress);
                    }
                });
        Log.d (TAG,downloadURL[0]);

        return downloadURL[0];

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //when the user choses the file
        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            //if a file is selected
            if (data.getData() != null) {
                //uploading the file
                Files file = new Files (getFileName (data.getData ()),data.getData ().toString ());
                mFilesList.add (file);
                fileViewAdapter.notifyDataSetChanged ();
            }else{
                Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
