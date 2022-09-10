package com.chowdhuryelab.addressbook;

import static com.chowdhuryelab.addressbook.update_data.CAMERA_CODE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;


public class insert_data extends AppCompatActivity {
    DatabaseHelper myDb;
    EditText editName, editPhn1, editPhn2, editemail, editaddree;
    Button pickImage, btnAddData;
    private Uri image_uri;
    ImageView profileImageView;
    byte[] data;

    private static final int CAMERA_REQUEST_CODE =200;
    private static final int STORAGE_REQUEST_CODE =300;
    private static final int IMAGE_PICK_GALLERY_CODE =400;
    private static final int IMAGE_PICK_CAMERA_CODE =500;

    private FirebaseAuth mAuth;
    private String[] cameraPermission;
    private String[] storagePermission;
    //Check input data validation
    String error = "";
    final int bmpHeight = 160;
    final int bmpWidth = 160;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_data);

        myDb = new DatabaseHelper(this);
        editName = findViewById(R.id.editText_name);
        editPhn1 = findViewById(R.id.editText_phn1);
        editPhn2 = findViewById(R.id.editText_phn2);
        editemail = findViewById(R.id.editText_eMail);
        editaddree = findViewById(R.id.editText_Address);
        btnAddData = findViewById(R.id.button_add);
        profileImageView = findViewById(R.id.insert_profileImage);

        //Read permission from manifest
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        btnAddData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                saveFirebaseData();
            }
        });

    }



    private void saveFirebaseData() {

        String name = editName.getText().toString();
        String phn1 = editPhn1.getText().toString();
        String phn2 = editPhn2.getText().toString();
        String email = editemail.getText().toString();
        String address = editaddree.getText().toString();

        if(name.length()<2){
            editName.setError("Error");
            error = error +"\nName";
        }

        if(!isValidMobile(phn1) || phn1.length()<11){
            editPhn1.setError("Error");
            error = error +"\nPhone(Home)";
        }
        if(!phn2.isEmpty()){
            if(!isValidMobile(phn2) || phn2.length()<11) {
                editPhn2.setError("Error");
                error = error + "\nPhone(Office)";
            }
        }

        if (!isValidMail(email))
        {
            editemail.setError("email");
            error = error +"\nEmail";
        }

        if(address.length()<2){
            editaddree.setError("Error");
            error = error +"\nAddress";
        }

        mProgressDialog.setMessage("Saving Account Details..");
        mProgressDialog.show();

        final String timestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + timestamp);
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + name);
            hashMap.put("phn1", "" + phn1);
            hashMap.put("phn2", "" + phn2);
            hashMap.put("address", "" + address);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("profileImage", "");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
            ref.child(timestamp).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mProgressDialog.dismiss();
                            startActivity(new Intent(insert_data.this, MainActivity.class));
                            finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast toast = Toast.makeText(getApplicationContext(), "Failed to create account", Toast.LENGTH_SHORT);
                            toast.show();
                            finish();
                        }
                    });
        } else {
            String filePathName = "profile_image/" + "" + timestamp;

            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downlodImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("uid", "" + timestamp);
                                hashMap.put("email", "" + email);
                                hashMap.put("name", "" + name);
                                hashMap.put("phn1", "" + phn1);
                                hashMap.put("phn2", "" + phn2);
                                hashMap.put("address", "" + address);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("profileImage", "" + downlodImageUri);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
                                ref.child(timestamp).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressDialog.dismiss();
                                                startActivity(new Intent(insert_data.this, MainActivity.class));
                                                Toast toast = Toast.makeText(getApplicationContext(), "Data Created", Toast.LENGTH_SHORT);
                                                finish();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mProgressDialog.dismiss();
                                                Toast toast = Toast.makeText(getApplicationContext(), "Failed to create account", Toast.LENGTH_SHORT);
                                                toast.show();
                                                finish();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(insert_data.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    public void AddData() {

        String name = editName.getText().toString();
        String phn1 = editPhn1.getText().toString();
        String phn2 = editPhn2.getText().toString();
        String email = editemail.getText().toString();
        String address = editaddree.getText().toString();

        if(name.length()<2){
            editName.setError("Error");
            error = error +"\nName";
        }

        if(!isValidMobile(phn1) || phn1.length()<11){
            editPhn1.setError("Error");
            error = error +"\nPhone(Home)";
        }
        if(!phn2.isEmpty()){
            if(!isValidMobile(phn2) || phn2.length()<11) {
                editPhn2.setError("Error");
                error = error + "\nPhone(Office)";
            }
        }

        if (!isValidMail(email))
        {
            editemail.setError("email");
            error = error +"\nEmail";
        }

        if(address.length()<2){
            editaddree.setError("Error");
            error = error +"\nAddress";
        }

        profileImageView.setDrawingCacheEnabled(true);
        profileImageView.buildDrawingCache();

        Bitmap bitMap = profileImageView.getDrawingCache();
        ByteArrayOutputStream img = new ByteArrayOutputStream();
        bitMap.compress(Bitmap.CompressFormat.PNG, 100, img);
        data = img.toByteArray();

        if(error.isEmpty()){
            // Insert Data in sqlite using db helper
            //data =image
            boolean isInserted = myDb.insertData(name, phn1, phn2, email, address, data);
            if (isInserted == true) {
                Toast.makeText(insert_data.this, "Data Inserted", Toast.LENGTH_LONG).show();
                finish();
//                                Intent intent = new Intent(insert_data.this, MainActivity.class);
//                                startActivity(intent);
                editName.setText("");
                editaddree.setText("");
                editPhn1.setText("");
                editPhn2.setText("");
                editemail.setText("");

            } else
                Toast.makeText(insert_data.this, "Data not Inserted", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(insert_data.this, "Please check invalid fields", Toast.LENGTH_SHORT).show();
        }




    }
    public void InsertImageOnClick(View v) {
        String[] options = {"Camera", "Gallery"};

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which==0){
                            if (checkCameraPermission()){
                                pickFromCamera();
                            }
                            else {
                                requestCameraPermission();
                            }
                        }
                        else if(which==1){
                            if (checkStoragePermission()){
                                pickFromGalery();
                            }
                            else {
                                requestStoragePermission();
                            }
                        }
                        else {
                            profileImageView.setImageResource(R.drawable.cover1);
                        }
                    }
                }).show();
    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }


    private void pickFromGalery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                profileImageView.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                profileImageView.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
           case CAMERA_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted) {
                        pickFromCamera();
                    } else {
                        Toast.makeText(this, "Not Working", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;

            case STORAGE_REQUEST_CODE:{
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGalery();
                    } else {
                        Toast.makeText(this, "Not Working", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) ==
                (PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this, cameraPermission, CAMERA_REQUEST_CODE);
    }

}
