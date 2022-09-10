package com.chowdhuryelab.addressbook;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private ArrayList mAddressList;
    private FirebaseAuth mAuth;

//    public Adapter(Context context, Cursor cursor) {
//        mContext = context;
//        mCursor = cursor;
//    }
    public Adapter(Context context, ArrayList<ModelAddresses> addressList) {
        mContext = context;
        mAddressList = addressList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView name;
        public TextView phn1;
        public View btnCall, btnSMS;
        CircleImageView profileImageView;

        public ViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.textView_rName);
            phn1 = itemView.findViewById(R.id.textView_rPhn);
            profileImageView = itemView.findViewById(R.id.rprofileImageView);

            btnCall = itemView.findViewById(R.id.btnCall);
            btnSMS = itemView.findViewById(R.id.btnSMS);

            mAuth = FirebaseAuth.getInstance();

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            if(view == itemView)
            {
                Intent i = new Intent(mContext, ReadDataActivity.class);
                i.putExtra("GetID", String.valueOf(view.getTag()));
                mContext.startActivity(i);
                //((MainActivity)mContext).finish();
                System.out.println("Item clicked: "+view.getTag());
            }

        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.conatct_lstview, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        ModelAddresses modelAddresses = (ModelAddresses) mAddressList.get(position);



//        if (!mCursor.moveToPosition(position)) {
//            return;
//        }
//
//            String id    =mCursor.getString( 0);
//            String name  =mCursor.getString(1);
//            String phn1  =mCursor.getString(2);
//            String phn2   =mCursor.getString(3);
//            String email =mCursor.getString(4);
//            String address =mCursor.getString(5);
//            byte[] img = mCursor.getBlob(6);
//            Bitmap bitmap = BitmapFactory.decodeByteArray(img, 0, img.length);


            String name  =modelAddresses.getName();
            String phn1  =modelAddresses.getPhn1();
            String phn2   =modelAddresses.getPhn2();
            String email =modelAddresses.getEmail();
            String address =modelAddresses.getAddress();
            String img = modelAddresses.getProfileImage();
            String uid = modelAddresses.getUid();

        try {
            Picasso.get().load(img).placeholder(R.drawable.cover1).into(holder.profileImageView);
        }
        catch (Exception e) {
            holder.profileImageView.setImageResource(R.drawable.cover2);
        }

            holder.itemView.setTag(uid);
            holder.name.setText(String.format(name));
            holder.phn1.setText(String.format(""+phn1));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //on click item

                    Intent i = new Intent(mContext, ReadDataActivity.class);
                    i.putExtra("GetID",uid);
                    mContext.startActivity(i);

                }
            });

            holder.btnCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:"+ ""+phn1));
                    mContext.startActivity(intent);
                }
            });

        holder.btnSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("smsto:" +  ""+phn1));
                mContext.startActivity(intent);
            }
        });


    }
    @Override
    public int getItemCount() {
        return  mAddressList.size();
       // return mCursor.getCount();
    }

//    public void swapCursor(Cursor newCursor) {
//        if (mCursor != null) {
//            mCursor.close();
//        }
//        mCursor = newCursor;
//        if (newCursor != null) {
//            notifyDataSetChanged();
//        }
//    }
}