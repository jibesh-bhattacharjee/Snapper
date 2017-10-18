package shillonghack.snapper;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class second extends AppCompatActivity implements View.OnClickListener {
    Button button;
    FirebaseUser user;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    DatabaseReference databaseComplaints;
    private Button mSelectImage;
    private StorageReference mStorage;
    private DatabaseReference mref;
    private static final int Gallery_Intent = 2;
    private ProgressDialog mProgressDialog;
    //Widgets
    EditText editTextCaption;
    Spinner spinnerPriorityLevel;
    EditText location;

    private FirebaseDatabase mFirebaseDatabase;
    private Button buttonChoose, buttonUpload;
    private ImageView imageView;
    private static final int PICK_IMAGE_REQUEST = 234;
    int imageCount;
    private Context mContext;

    //For location finding part
    String fullAddress;
    TextView textViewLocation;
    LocationManager locationManager;
    LocationListener locationListener;
    //Variables
    Double latitude, longitude;
    Geocoder geocoder;
    List<Address> addresses;


    //End


    private Uri filePath;
    String imgname;
    private StorageReference storageReference;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        databaseComplaints = FirebaseDatabase.getInstance().getReference("Complaints");
        storageReference = FirebaseStorage.getInstance().getReference();
        editTextCaption = (EditText) findViewById(R.id.description);
        imageView = (ImageView) findViewById(R.id.imageView);
        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mref = mFirebaseDatabase.getReference();
        spinnerPriorityLevel = (Spinner) findViewById(R.id.spinnerPriorityLevel);


        textViewLocation = (TextView) findViewById((R.id.textViewLocation));
        geocoder = new Geocoder(this, Locale.getDefault());


        location = (EditText) findViewById(R.id.Location);
        /**
         * the following part is for LOGOUT
         */
        button = (Button) findViewById(R.id.button4);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(second.this, MainActivity.class));
                }
            }
        };
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
            }
        });


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                try{
                    addresses = geocoder.getFromLocation(latitude,longitude,1);
                    String address = addresses.get(0).getAddressLine(0);
                    String area = addresses.get(0).getLocality();
                    String city = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    fullAddress = address+", "+area+", "+city+", "+country+", "+postalCode;
                    textViewLocation.setText(fullAddress);
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }; //End of initializing new location listener
        /**
         * Permission Check started
         */


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission( Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[] {
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },10);

                return;
            }
        } else {
            locationManager.requestLocationUpdates("gps",5000,0,locationListener);
        }
        locationManager.requestLocationUpdates("gps", 5000, 0, locationListener);
    } ///End of onCreate()

    //Handlling the results of the permissions. Results are stored in the following method


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates("gps",5000,0,locationListener);
                }
                locationManager.requestLocationUpdates("gps",5000,0,locationListener);


        }
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select an Image"), PICK_IMAGE_REQUEST);
    }

    private void uploadFile(){

        if(filePath != null) {

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            user = FirebaseAuth.getInstance().getCurrentUser();
            String uid = user.getUid();

            mref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    imageCount=imageCount(dataSnapshot);
                    //Toast.makeText(mContext, , Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(mContext, "Error!!", Toast.LENGTH_SHORT).show();
                }
            });
            imageCount = (int) (Math.random() * 999999999);
            StorageReference riversRef = storageReference.child("images/"+uid+"/photo"+imageCount+".jpg");
            imgname = "photo"+imageCount+".jpg";
            riversRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Complaint Added Successfully", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress =  (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                            progressDialog.setMessage("Uploading " + (int)progress + "%");
                        }
                    });
            ;
            addComplaint();
        }
        else{
            //display an error toast
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try {
                Bitmap bitMap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view == buttonChoose){
            //open file chooser
            showFileChooser();
        }
        else if(view == buttonUpload){
            //upload to firebase storage
            uploadFile();
        }
    }

    public int imageCount(DataSnapshot dataSnapshot) {
//        int count = 0;
//        for(DataSnapshot ds : dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getChildren()){
//            count++;
//        }
       //Toast.makeText(mContext, count, Toast.LENGTH_SHORT).show();
        int count = (int) (Math.random() * 214700000);
        return  count;
    }
    private void addComplaint() {
        String caption = editTextCaption.getText().toString().trim();
        String prioritylevel = spinnerPriorityLevel.getSelectedItem().toString();
        String loc = location.getText().toString().trim();
        String imgPath = storageReference.child("images/"+FirebaseAuth.getInstance().getCurrentUser().getUid()).child(imgname).toString();
        if(!TextUtils.isEmpty(caption)) {
            String id = databaseComplaints.push().getKey();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Complaint complaint = new Complaint(caption,loc,uid,prioritylevel,imgPath,fullAddress);
            databaseComplaints.child(id).setValue(complaint);
        }
        else {
            Toast.makeText(this, "Enter a caption", Toast.LENGTH_SHORT).show();
        }
    }

}
