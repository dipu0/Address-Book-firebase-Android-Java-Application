package com.chowdhuryelab.addressbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper myDb;
    SQLiteDatabase mDatabase;
    ExtendedFloatingActionButton extendedFAB;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    //Button extendedFAB;

    Adapter adapter;
    FirebaseAuth mAuth;
    private ProgressDialog mProgressDialog;
    private ArrayList<ModelAddresses> mAddressesList;

    private TextView nameTv, emailTv, phoneTv;
    private ImageButton logoutBtn, editProfileBtn;

    private ImageView profileIV;

    private RelativeLayout toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Please wait");
        mProgressDialog.setCanceledOnTouchOutside(false);


        toolbar = findViewById(R.id.toolBarRL);
        nameTv = findViewById(R.id.nameTV);
        emailTv = findViewById(R.id.emailTV);
        phoneTv = findViewById(R.id.phoneTV);
        profileIV = findViewById(R.id.profileIV);
        logoutBtn = findViewById(R.id.logoutBtn);
        editProfileBtn = findViewById(R.id.editProfileBtn);



       // myDb = new DatabaseHelper(this);
        extendedFAB = findViewById(R.id.extFab);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);

          recyclerView = findViewById(R.id.recyclerview);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new Adapter(this, myDb.getAllData());
//        adapter.swapCursor(myDb.getAllData());
//        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadMyInfo();
                loadAddress();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        loadMyInfo();

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, UpdateDataActivity.class);
                i.putExtra("GetID",mAuth.getUid());
                i.putExtra("action","profile");
                startActivity(i);
            }
        });


        extendedFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent myintent= new Intent(MainActivity.this, InsertDataActivity.class);
               // finish();
                startActivity(myintent);
            }
        });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
        ref.orderByChild("uid").equalTo(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds : dataSnapshot.getChildren()){
                            String name = ""+ds.child("name").getValue();
                            String email = ""+ds.child("email").getValue();
                            String phone = ""+ds.child("phn1").getValue();
                            String profileImage = ""+ds.child("profileImage").getValue();

                            nameTv.setText(name);
                            emailTv.setText(email);
                            phoneTv.setText(phone);
                            try {
                                Picasso.get().load(profileImage).placeholder(R.drawable.cover1).into(profileIV);
                            }
                            catch (Exception e) {
                                profileIV.setImageResource(R.drawable.cover1);
                            }
                            loadAddress();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
private void loadAddress(){

            mAddressesList = new ArrayList<>();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("addressbook");
            ref.child(mAuth.getUid()).child("book")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mAddressesList.clear();
                        for (DataSnapshot ds: dataSnapshot.getChildren()){

                            ModelAddresses modelAddresses = ds.getValue(ModelAddresses.class);
                            mAddressesList.add(modelAddresses);

                        }

                        adapter = new Adapter(MainActivity.this, mAddressesList);
                        recyclerView.setAdapter(adapter);
                    }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

}
    @Override
    public void onResume() {
        super.onResume();
//        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//        adapter = new Adapter(MainActivity.this, myDb.getAllData());
//        adapter.swapCursor(myDb.getAllData());
//        recyclerView.setAdapter(adapter);
        System.out.println("@MainActivity.onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
//        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//        adapter = new Adapter(MainActivity.this, myDb.getAllData());
//        adapter.swapCursor(myDb.getAllData());
//        recyclerView.setAdapter(adapter);
        System.out.println("@MainActivity.onPause");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        System.out.println("@MainActivity.onRestart");
        // re-load events from database after coming back from the next page
//        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//        adapter = new Adapter(MainActivity.this, myDb.getAllData());
//        adapter.swapCursor(myDb.getAllData());
//        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        System.out.println("@MainActivity.onStop");
        // clear the event data from memory as the page is completely hidden by now
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("@MainActivity.onDestroy");
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
