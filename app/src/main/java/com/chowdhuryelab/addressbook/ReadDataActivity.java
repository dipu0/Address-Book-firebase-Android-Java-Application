package com.chowdhuryelab.addressbook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class ReadDataActivity extends AppCompatActivity {
    Button btn_update, btn_delete;
    DatabaseHelper myDb;
    TextView textView_Name, textView_Address, textView_Email, textView_Phn1,textView_Phn2;
    Bitmap bitmap;
    ImageView profileImageView;
    LinearLayout llphn2;
    String ID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_data);
        myDb = new DatabaseHelper(this);
        btn_update = findViewById(R.id.button_Update);
        btn_delete = findViewById(R.id.button_delete);

        textView_Name = findViewById(R.id.textView_Name);
        textView_Phn1 = findViewById(R.id.textView_phn1);
        textView_Phn2 = findViewById(R.id.textView_phn2);
        textView_Address = findViewById(R.id.textView_Address);
        textView_Email = findViewById(R.id.textView_Email);
        profileImageView = findViewById(R.id.profileImageView);

        llphn2 = findViewById(R.id.llphn2);

        Intent i = getIntent();
        ID = i.getStringExtra("GetID");

        btn_update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ReadDataActivity.this, UpdateDataActivity.class);
                intent.putExtra("GetID",ID);
                startActivity(intent);
            }
        });

        btn_delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!isFinishing()) {
                            new AlertDialog.Builder(ReadDataActivity.this)
                                    .setTitle("Delete")
                                    .setMessage("Do you want to delete the record")
                                    .setCancelable(true)
                                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
                                            ref.child(ID).removeValue()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            finish();
                                                            Toast.makeText(getApplicationContext(), "Deleted...", Toast.LENGTH_SHORT).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(getApplicationContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.cancel();
                                        }
                                    })
                                    .show();

                        }
                    }
                });
            }
        });

        loadAddressInfo();

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
                            String timestamp = "" + ds.child("timestamptimestamp").getValue();
                            String uid = "" + ds.child("uid").getValue();

                            if(phn2.isEmpty())
                                llphn2.setVisibility(View.GONE);
                            else textView_Phn2.setText(phn2);

                            textView_Name.setText(name);
                            textView_Email.setText(email);
                            textView_Phn1.setText(phn1);

                            textView_Address.setText(address);
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

    public void call1(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+textView_Phn1.getText().toString()));
        startActivity(intent);
    }

    public void message1(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + textView_Phn1.getText().toString()));
        startActivity(intent);
    }

    public void call2(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+textView_Phn2.getText().toString()));
        startActivity(intent);
    }

    public void message2(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + textView_Phn2.getText().toString()));
        startActivity(intent);
    }

    public void SenMail(View view) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + textView_Email.getText().toString()));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAddressInfo();
        System.out.println("@ReadDataActivity.onResume");

    }

    @Override
    public void onPause() {
        super.onPause();
        loadAddressInfo();
        System.out.println("@ReadDataActivity.onPause");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        loadAddressInfo();
        System.out.println("@ReadDataActivity.onRestart");

    }

    @Override
    public void onBackPressed() {
        finish();
    }

}