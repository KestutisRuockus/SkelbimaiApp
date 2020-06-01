package skelbimas.lt;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import skelbimas.lt.category.PhonesCategory;
import skelbimas.lt.model.ImageUploadInfo;

import static com.google.firebase.storage.FirebaseStorage.getInstance;

public class AdminAddNewProductActivity extends AppCompatActivity {

    EditText mTitleEt, mDescrEt;
    ImageView mPostIv;
    Button mUpload;

    // Folder path for Firebase storage
    String mStoragePath = "All_Image_Uploads/";
    // root database name for firebase database
    String mDatabasePath = "Data";
    // Creating Uri
    Uri mFilePathUri;

    // Creating Storage reference and Database reference
    StorageReference mStorageReference;
    DatabaseReference mDatabaseReference;

    // Progress dialog
    ProgressDialog mProgressDialog;

    // Image request code for choosing image
    int IMAGE_REQUEST_CODE = 5;

    // intent data will be stored in these variables
    String cTitle, cDescr, cImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);

        // Actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Ada new post");

        mTitleEt = findViewById(R.id.pTitleEt);
        mDescrEt = findViewById(R.id.pDescrEt);
        mPostIv = findViewById(R.id.pImageIv);
        mUpload = findViewById(R.id.pUploadBtn);

        // try to get data from intent if not null
        Bundle intent = getIntent().getExtras();
        if (intent != null){
            // get and store data
            cTitle = intent.getString("cTitle");
            cDescr = intent.getString("cDescr");
            cImage = intent.getString("cImage");
            // set this data to views
            mTitleEt.setText(cTitle);
            mDescrEt.setText(cDescr);
            Picasso.get().load(cImage).into(mPostIv);
            // change title of action bar and button
            actionBar.setTitle("Update Post");
            mUpload.setText("Update");
        }

        // image click to choose image
        mPostIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // creating intent
                Intent intent = new Intent();
                // setting intent type as image to select image from phone storage
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), IMAGE_REQUEST_CODE);
            }
        });

        // button click to upload data to firebase
        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // if we came here from "Add" title of button will be "Upload"
                // if we came from 'update' title of button will be 'update'
                if(mUpload.getText().equals("Upload")) {
                    // call method to upload data to firebase
                    uploadDataToFirebase();
                } else {
                    // begin update
                    beginUpdate();
                }
            }
        });

        // assign Firebase storage instance to storage reference object
        mStorageReference = getInstance().getReference();
        // assign Firebase Database Instance while root database name
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(mDatabasePath);

        // progress dialog
        mProgressDialog = new ProgressDialog(AdminAddNewProductActivity.this);


    }

    private void beginUpdate() {
        // first we will delete previous image
        // we can delete image using it's url which is stored in cImage variable

        mProgressDialog.setMessage("Updating...");

        deletePreviousImage();
    }

    private void deletePreviousImage() {
        StorageReference mPictureRef = getInstance().getReferenceFromUrl(cImage);
        mPictureRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // deleted
                Toast.makeText(AdminAddNewProductActivity.this, "Previous image deleted...", Toast.LENGTH_SHORT).show();
                // now upload new image and get it's url
                uploadNewImage();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failure
                        // get and show error message
                        Toast.makeText(AdminAddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void uploadNewImage() {
        // Image name
        String imageName = System.currentTimeMillis()+ ".PNG";
        // storage reference
        StorageReference storageReference2 = mStorageReference.child(mStoragePath + imageName);
        // get bitmap from image view
        Bitmap bitmap = ((BitmapDrawable)mPostIv.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // compress image
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageReference2.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // uploaded
                Toast.makeText(AdminAddNewProductActivity.this, "Image uploaded...", Toast.LENGTH_SHORT).show();

                // get url of newly uploaded image
                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                Uri downloadUri = uriTask.getResult();
                // now update dataBase with new data
                updateDatabase(downloadUri.toString());
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // error, get and show error message
                        Toast.makeText(AdminAddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void updateDatabase(final String s) {
        // new values to update to previous
        final String title = mTitleEt.getText().toString();
        final String descr = mDescrEt.getText().toString();
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mRef = mFirebaseDatabase.getReference("Data");

        Query query = mRef.orderByChild("title").equalTo(cTitle);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    // update data
                    ds.getRef().child("title").setValue(title);
                    ds.getRef().child("search").setValue(title.toLowerCase());
                    ds.getRef().child("description").setValue(descr);
                    ds.getRef().child("image").setValue(s);
                    // these keys in .child() must be spelled same as in your firebaseDatabase
                }
                mProgressDialog.dismiss();
                Toast.makeText(AdminAddNewProductActivity.this, "Database updated...", Toast.LENGTH_SHORT).show();
                // start PhoneCategory activity after updating data
                startActivity(new Intent(AdminAddNewProductActivity.this, PhonesCategory.class));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void uploadDataToFirebase() {
        // check whether filePathUri is empty or not
        if (mFilePathUri != null){
            // setting progress bar title
            mProgressDialog.setTitle("Image is Uploading....");
            // show progress dialog
            mProgressDialog.show();
            // create second storage reference
            StorageReference storageReference2dn = mStorageReference.child(mStoragePath + System.currentTimeMillis() + "." + getFileExtension(mFilePathUri));

            // adding addOnSuccessListener to storageReference2dn
            storageReference2dn.putFile(mFilePathUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri>uriTask = taskSnapshot.getStorage().getDownloadUrl();
                            while (!uriTask.isSuccessful());
                            Uri downloadUri = uriTask.getResult();

                            // get title
                            String mPostTitle = mTitleEt.getText().toString().trim();
                            // get description
                            String mPostDescr = mDescrEt.getText().toString().trim();
                            // hide progress dialog
                            mProgressDialog.dismiss();
                            // show Toast that image is uploaded
                            Toast.makeText(AdminAddNewProductActivity.this, "Uploaded successfully", Toast.LENGTH_SHORT).show();
                            ImageUploadInfo imageUploadInfo = new ImageUploadInfo(mPostTitle, mPostDescr, downloadUri.toString(), mPostTitle.toLowerCase());
                            // getting image upload id
                            String imageUploadId = mDatabaseReference.push().getKey();
                            // adding image upload id's child element into databaseReference
                            mDatabaseReference.child(imageUploadId).setValue(imageUploadInfo);
                        }
                    })
                    // if something goes wrong such as network failure etc
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // hide progress dialog
                    mProgressDialog.dismiss();
                    // show error toast
                    Toast.makeText(AdminAddNewProductActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.setTitle("Image is uploading...");
                        }
                    });
        } else {
            Toast.makeText(this, "Please select image or add image name", Toast.LENGTH_SHORT).show();
        }
    }

    // method to get selected image file extension from file Path Uri
    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // returning the file extension
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_CODE
                    && resultCode == RESULT_OK
                    && data != null
                    && data.getData() != null){
            mFilePathUri = data.getData();
        }
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mFilePathUri);
            mPostIv.setImageBitmap(bitmap);
        } catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
