package com.chowdhuryelab.addressbook;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity{

    //view
    private ImageView profileIv;
    private Button registerUserBtn;
    private EditText nameEt, mobleNoEt, mobleNo2Et, addressEt, emailEt, passwordEt, cpasswordEt;

    private static final int CAMERA_REQUEST_CODE = 200;
    private static final int STORAGE_REQUEST_CODE = 300;
    private static final int IMAGE_PICK_GALLERY_CODE = 400;
    private static final int IMAGE_PICK_CAMERA_CODE = 500;

    private String[] locationPermission;
    private String[] cameraPermission;
    private String[] storagePermission;

    private String name, phoneNumber,phoneNumber2, address, email, password, confermPassword;

    FirebaseAuth mAuth;
    ProgressDialog mProgressDialog;

    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //init views

        profileIv = findViewById(R.id.profileIV);
        registerUserBtn = findViewById(R.id.registerUserBtn);
        nameEt = findViewById(R.id.nameET);
        mobleNoEt = findViewById(R.id.phoneET);
        mobleNo2Et = findViewById(R.id.phone2ET);
        addressEt= findViewById(R.id.addressET);
        emailEt = findViewById(R.id.emailET);
        passwordEt = findViewById(R.id.passwordET);
        cpasswordEt = findViewById(R.id.cpasswordET);

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);

        //init permission
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};


        profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImagePickDialog();
            }
        });
        registerUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open main activity
                inputData();
            }
        });

    }

    private void inputData() {
        name = nameEt.getText().toString().trim();
        phoneNumber = mobleNoEt.getText().toString().trim();
        phoneNumber2 = mobleNoEt.getText().toString().trim();
        address = addressEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        confermPassword = cpasswordEt.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter Name...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter correct email...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter Phone Number...", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!phoneNumber2.isEmpty()){

            if (TextUtils.isEmpty(phoneNumber)) {
                Toast.makeText(this, "Enter Phone Number...", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Enter Street Name...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Pasword must be atleast 6 charecter long...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confermPassword)) {
            Toast.makeText(this, "Pasword doesn't match...", Toast.LENGTH_SHORT).show();
            return;
        }
        createAccount();
    }

    private void createAccount() {
        mProgressDialog.setMessage("Creating Account...");
        mProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        saveFirebaseData();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mProgressDialog.dismiss();
                        Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveFirebaseData() {
        mProgressDialog.setMessage("Saving Account Details..");

        final String timestamp = "" + System.currentTimeMillis();
        if (image_uri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("uid", "" + mAuth.getUid());
            hashMap.put("email", "" + email);
            hashMap.put("name", "" + name);
            hashMap.put("phn1", "" + phoneNumber);
            hashMap.put("phn2", "" + phoneNumber2);
            hashMap.put("address", "" + address);
            hashMap.put("timestamp", "" + timestamp);
            hashMap.put("profileImage", "");

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
            ref.child(mAuth.getUid()).setValue(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgressDialog.dismiss();
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
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
            String filePathName = "profile_image/" + "" + mAuth.getUid();

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
                                hashMap.put("uid", "" + mAuth.getUid());
                                hashMap.put("email", "" + email);
                                hashMap.put("name", "" + name);
                                hashMap.put("phn1", "" + phoneNumber);
                                hashMap.put("phn2", "" + phoneNumber2);
                                hashMap.put("address", "" + address);
                                hashMap.put("timestamp", "" + timestamp);
                                hashMap.put("profileImage", "" + downlodImageUri);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
                                ref.child(mAuth.getUid()).setValue(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressDialog.dismiss();
                                                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
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
                            Toast.makeText(RegisterActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void showImagePickDialog() {
        String[] options = {"Camera", "Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("pick Image")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            if (checkCameraPermission()) {
                                pickFromCamera();
                            } else {
                                requestCameraPermission();
                            }
                        } else {
                            if (checkStoragePermission()) {
                                pickFromGalery();
                            } else {
                                requestStoragePermission();
                            }
                        }
                    }
                }).show();
    }

    private void pickFromGalery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Temp_Image_Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Image_Description");

        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
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

            case STORAGE_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted) {
                        pickFromGalery();
                    } else {
                        Toast.makeText(this, "Not working", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                (PackageManager.PERMISSION_GRANTED);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                image_uri = data.getData();
                profileIv.setImageURI(image_uri);
            }
            else if(requestCode == IMAGE_PICK_CAMERA_CODE){
                profileIv.setImageURI(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
