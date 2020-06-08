package com.rajansurani.taskapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.widget.Adapter;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rajansurani.taskapp.Model.Files;
import com.rajansurani.taskapp.Model.Task;
import com.rajansurani.taskapp.Model.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NewTaskActivity extends AppCompatActivity {

    final static int PICK_PDF_CODE = 2342;
    private final String TAG = "NewTaskActivity";
    EditText mTitle, mInfo;
    AutoCompleteTextView mMembers;
    Button mAddMember, mAddFile, mCreateTask;

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
        setContentView (R.layout.activity_new_task);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar ().setTitle ("New Task");

        mTitle = findViewById (R.id.et_title);
        mInfo = findViewById (R.id.et_info);
        mMembers = findViewById (R.id.et_member);
        mMemberListView = findViewById (R.id.member_list);
        mFilesView = findViewById (R.id.file_list);
        mAddFile = findViewById (R.id.btn_add_file);
        mAddMember = findViewById (R.id.btn_add_member);
        mCreateTask = findViewById (R.id.btn_create_task);
        mProgressBar = findViewById (R.id.progressbar);

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
                adapterUserName = new ArrayAdapter<String> (NewTaskActivity.this, android.R.layout.select_dialog_item, userName);
                mMembers.setAdapter (adapterUserName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, databaseError.toString ());
            }
        });
        Log.d (TAG, userName.toString ());
        mMembers.setThreshold (1);

        memberList = new ArrayList<> ();
        adapterMember = new ArrayAdapter<String> (NewTaskActivity.this, android.R.layout.simple_list_item_1, memberList);
        mMemberListView.setAdapter (adapterMember);
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
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && (ContextCompat.checkSelfPermission (NewTaskActivity.this,
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

        mFilesList = new ArrayList<Files> ();
        fileViewAdapter = new FileViewAdapter (mFilesList);
        mFilesView.setLayoutManager (new LinearLayoutManager (this));
        mFilesView.setAdapter (fileViewAdapter);

        mCreateTask.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {

                for(Files file: mFilesList)
                {
                    String url = uploadFile (Uri.parse (file.getUrl ()), file.getName ());
                    Log.d (TAG,url);
                    file.setUrl (url);
                }
                String title = mTitle.getText ().toString ();
                String info = mInfo.getText ().toString ();
                String createdBy = mAuth.getCurrentUser ().getDisplayName ();
                memberList.add (createdBy);
                Task newTask = new Task (title,info,createdBy,memberList,mFilesList);
                DatabaseReference  myRef = mDatabase.getReference ("Tasks").push ();
                myRef.setValue (newTask);

                for(String member : memberList)
                {
                    mDatabase.getReference ("AllocatedTask").child (member).push ().setValue (myRef.getKey ());
                    mDatabase.getReference ("Notification").child (member).push ().setValue ("New Task assigned to you : "+title);
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
