package com.chowdhuryelab.addressbook;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class update_data extends AppCompatActivity{
    DatabaseHelper myDb;
    EditText editName, editPhn1, editPhn2, editemail, editaddree ;
    Button pickImage, btnUpdate;
    Bitmap bitmap;
    ProgressDialog progressBar;
    int progressBarStatus = 0;
    Handler progressBarbHandler = new Handler();
    boolean hasImageChanged = false;

   ImageView profileImageView;

    byte[] data;
    Uri image_uri;
    static final int CAMERA_CODE = 1;
    static final int GALLERY_CODE = 0;

    final int bmpHeight = 160;
    final int bmpWidth = 160;

    private static final int CAMERA_REQUEST_CODE =200;
    private static final int STORAGE_REQUEST_CODE =300;
    private static final int IMAGE_PICK_GALLERY_CODE =400;
    private static final int IMAGE_PICK_CAMERA_CODE =500;


    private String[] cameraPermission;
    private String[] storagePermission;

    private ProgressDialog mProgressDialog;
    private FirebaseAuth mAuth;
    String ID;

    String timestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_data);

        myDb = new DatabaseHelper(this);
        editName = findViewById(R.id.editText_name);
        editPhn1 = findViewById(R.id.editText_phn1);
        editPhn2 = findViewById(R.id.editText_phn2);
        editemail = findViewById(R.id.editText_eMail);
        editaddree = findViewById(R.id.editText_Address);
        btnUpdate = findViewById(R.id.btn_update_update);


        Intent i = getIntent();
        ID = i.getStringExtra("GetID");

        mAuth = FirebaseAuth.getInstance();
        try {
            FirebaseUser user = mAuth.getCurrentUser();
            if (!user.getUid().equals(ID) ){
                editemail.setEnabled(true);
            }
            else {
                editemail.setEnabled(false);
            }
        }catch(NullPointerException e)
        {
            editemail.setEnabled(false);
            System.out.print("NullPointerException Caught");
        }


        profileImageView = findViewById(R.id.profileImageView);

        //Read permission from manifest
        cameraPermission = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};




//        Cursor res = myDb.readData(ID);
//        while (res.moveToNext()) {
//            editName.setText(res.getString(1));
//            editPhn1.setText("0"+res.getString(2));
//
//            if(!(res.getString(3).toString().length() >0))
//                editPhn2.setText(res.getString(3));
//            else editPhn2.setText(String.format("0"+res.getString(3)));
//
//            editemail.setText(res.getString(4));
//            editaddree.setText(res.getString(5));
//            byte[] img = res.getBlob(6);
//
//            bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);
//            profileImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), false));
//
//        }

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);



        loadAddressInfo();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                updateProfile();

            }
        });

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    private void loadAddressInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
        ref.orderByChild("uid").equalTo(ID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            String address = "" + ds.child("address").getValue();
                            String email = "" + ds.child("email").getValue();
                            String name = "" + ds.child("name").getValue();
                            String phn1 = "" + ds.child("phn1").getValue();
                            String phn2 = "" + ds.child("phn2").getValue();
                            String profileImage = "" + ds.child("profileImage").getValue();
                            timestamp = "" + ds.child("timestamptimestamp").getValue();
                            String uid = "" + ds.child("uid").getValue();

                            editName.setText(name);
                            editemail.setText(email);
                            editPhn1.setText(phn1);
                            editPhn2.setText(phn2);
                            editaddree.setText(address);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.cover1).into(profileImageView);
                            } catch (Exception e) {
                                profileImageView.setImageResource(R.drawable.cover2);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
    }

    private void updateProfile() {
        String name = editName.getText().toString();
        String phn1 = editPhn1.getText().toString();
        String phn2 = editPhn2.getText().toString();
        String email = editemail.getText().toString();
        String address = editaddree.getText().toString();

        //Check input data validation
        String error = "";

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
        if(address.length()<5){
            editName.setError("Error");
            error = error +"\naddress";
        }

        if(!error.isEmpty()){
            Toast.makeText(this, "Enter data properly", Toast.LENGTH_SHORT).show();
            return;
        }


        mProgressDialog.setMessage("Updating Profile...");
        mProgressDialog.show();
        if (image_uri == null) {
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("name", "" + name);
            hashMap.put("phn1", "" + phn1);
            hashMap.put("phn2", "" + phn2);
            hashMap.put("address", "" + address);

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
            ref.child(ID).updateChildren(hashMap)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Intent i = new Intent(update_data.this,MainActivity.class);
                            startActivity(i);
                            mProgressDialog.dismiss();
                            Toast.makeText(update_data.this, "Updating Profile...", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(update_data.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            String filePathName = "profile_image/" + "" + email+timestamp;
            String filtPathAnsName = ID;
            StorageReference storageReference = FirebaseStorage.getInstance().getReference(filtPathAnsName);
            storageReference.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful()) ;
                            Uri downlodImageUri = uriTask.getResult();

                            if (uriTask.isSuccessful()) {
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("name", "" + name);
                                hashMap.put("phn1", "" + phn1);
                                hashMap.put("phn2", "" + phn2);
                                hashMap.put("address", "" + address);
                                hashMap.put("profileImage", "" + downlodImageUri);

                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
                                ref.child(mAuth.getUid()).updateChildren(hashMap)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent i = new Intent(update_data.this,MainActivity.class);
                                                startActivity(i);
                                                mProgressDialog.dismiss();
                                                Toast.makeText(update_data.this, "Updating Profile...", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                mProgressDialog.dismiss();
                                                Toast.makeText(update_data.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(update_data.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    public void ImageOnClick(View v) {
        String[] options = {"Camera", "Gallery", "remove"};

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

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("@update_data.onResume");

    }

    @Override
    public void onPause() {
        super.onPause();
        System.out.println("@update_data.onPause");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        System.out.println("@update_data.onRestart");

    }


}

