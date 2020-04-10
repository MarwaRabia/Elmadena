package com.example.elmadena;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.grpc.Compressor;

public class AddPost extends AppCompatActivity {
    private static final int Gallery_Pick = 1;
    private Uri ImageUri= null;
    private ProgressDialog loadingBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    Bitmap compressedImageFile;
    private StorageReference PostsImagesRefrence;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;


    @BindView(R.id.update_post)
    Toolbar mMainAppToolbar;
//    @BindView(R.id.postTitle)
//    EditText mPostTitle;
    @BindView(R.id.postImage)
    ImageView mPostImage;
    @BindView(R.id.postDescrip)
    EditText mPostDescrip;
    @BindView(R.id.upload)
    Button mUpload;
    String Description;


    String randomName;
    String downloadUri;
    String desc;
    String downloadthumbUri;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        PostsImagesRefrence = FirebaseStorage.getInstance().getReference();
//        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(current_user_id);

        setSupportActionBar(mMainAppToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("إضافة منشور جديد");



        mPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });


        mUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

uplpad();


            }
        });



    }

    private void uplpad() {




        desc = mPostDescrip.getText().toString();

        if(!TextUtils.isEmpty(desc) && ImageUri != null){
            bar();

            randomName = UUID.randomUUID().toString();

//                    imageFile();

//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
////                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                    byte[] imageData = baos.toByteArray();


            final StorageReference filePath = storageReference.child("post_images")
                    .child(randomName + ".jpg");

            final UploadTask uploadTask = filePath.putFile(ImageUri);



            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {



                    Task<Uri> urlTask =uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                        {
                            if (!task.isSuccessful())
                            {
                                throw task.getException();
                            }

                            downloadUri = filePath.getDownloadUrl().toString();
                            downloadthumbUri=filePath.getDownloadUrl().toString();

                            return filePath.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task)
                        {
                            if (task.isSuccessful())
                            {
                                downloadUri = task.getResult().toString();
                                downloadthumbUri=task.getResult().toString();


                                Toast.makeText(AddPost.this, "got the Product image Url Successfully...", Toast.LENGTH_SHORT).show();

                                savedata();
                            }
                        }
                    });
//
//                            if(task.isSuccessful()){
//
//
////                                thumbFile();
//
//
//                            } else {
//
//                               loadingBar.dismiss();
//
//                            }




                }
            });


        }

    }



    private void bar() {


        loadingBar.setTitle("اضافة منشور جديد");
        loadingBar.setMessage("من فضلك انتظر حتى يتم اضافة المنشور");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

    }


    private void savedata() {

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("image_url", downloadUri);
        postMap.put("image_thumb", downloadthumbUri);
        postMap.put("desc", desc);
        postMap.put("user_id", current_user_id);
        postMap.put("timestamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if(task.isSuccessful()){

                    Toast.makeText(AddPost.this, "تم اضافة المنشور بنجاح", Toast.LENGTH_LONG).show();
                 sendUserTOMainActivity();

                } else {


                }

               loadingBar.dismiss();

            }
        });
    }







    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            sendUserTOMainActivity();

        }
        return super.onOptionsItemSelected(item);


    }

    private void sendUserTOMainActivity() {
        Intent i = new Intent(AddPost.this, MainActivity.class);
        startActivity(i);

    }

    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            mPostImage.setImageURI(ImageUri);
        }
    }


}







































































//
//    public  void  downloadPost(){
//
//
//
//        final String desc = mPostDescrip.getText().toString();
//
//        if(!TextUtils.isEmpty(desc) &&  ImageUri!= null){
//
//            loadingBar.setTitle("Add New Post");
//            loadingBar.setMessage("Please wait, while we are updating your new post...");
//            loadingBar.show();
//            loadingBar.setCanceledOnTouchOutside(true);
//
//            final String randomName = UUID.randomUUID().toString();
//
////             PHOTO UPLOAD
//            File newImageFile = new File(ImageUri.getPath());
////            try {
////
////                compressedImageFile =new Compressor(AddPost.this)
////                        .setMaxHeight(720)
////                        .setMaxWidth(720)
////                        .setQuality(50)
////                        .compressToBitmap(newImageFile);
////
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
////          compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//            byte[] imageData = baos.toByteArray();
//
//            // PHOTO UPLOAD
//
//            UploadTask filePath = storageReference.child("post_images").child(randomName + ".jpg").putBytes(imageData);
//
//            filePath.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
//
//                    final String downloadUri = task.getResult().getUploadSessionUri().toString();
//
//                    if(task.isSuccessful()){
//
//                        File newThumbFile = new File(ImageUri.getPath());
////                        try {
////
////                            compressedImageFile = new Compressor(AddPost.this)
////                                    .setMaxHeight(100)
////                                    .setMaxWidth(100)
////                                    .setQuality(1)
////                                    .compressToBitmap(newThumbFile);
////
////                        } catch (IOException e) {
////                            e.printStackTrace();
////                        }
//
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
////                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//                        byte[] thumbData = baos.toByteArray();
//
//                        UploadTask uploadTask = storageReference.child("post_images/thumbs")
//                                .child(randomName + ".jpg").putBytes(thumbData);
//
//                        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                            @Override
//                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//
//                                String downloadthumbUri = taskSnapshot.getUploadSessionUri().toString();
//
//                                Map<String, Object> postMap = new HashMap<>();
//                                postMap.put("image_url", downloadUri);
//                                postMap.put("image_thumb", downloadthumbUri);
//                                postMap.put("desc", desc);
//                                postMap.put("user_id", current_user_id);
//                                postMap.put("timestamp", FieldValue.serverTimestamp());
//
//                                firebaseFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DocumentReference> task) {
//
//                                        if(task.isSuccessful()){
//
//                                            Toast.makeText(AddPost.this, "Post was added", Toast.LENGTH_LONG).show();
//                                            Intent mainIntent = new Intent(AddPost.this, MainActivity.class);
//                                            startActivity(mainIntent);
//                                            finish();
//                                            loadingBar.dismiss();
//
//
//                                        } else {
//
//
//                                        }
//
//                                        loadingBar.dismiss();
//
//                                    }
//                                });
//
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//
//                                //Error handling
//
//                            }
//                        });
//
//
//                    } else {
//
//                        loadingBar.dismiss();
//
//                    }
//
//                }
//            });
//
//
//        }
//    }









//
//public class AddPost extends AppCompatActivity {
//    public static final int CAMERA_REQUESR_CODE = 100;
//    public static final int STORAGE_REQUESR_CODE = 200;
//    String[] cameraPermition;
//    String[] stoarPermition;
//
//
//
//    FirebaseAuth mAuth;
//    @BindView(R.id.postTitle)
//    EditText mPostTitle;
//    @BindView(R.id.postImage)
//    ImageView mPostImage;
//    @BindView(R.id.postDescrip)
//    EditText mPostDescrip;
//    @BindView(R.id.upload)
//    Button mUpload;
//    @BindView(R.id.main_app_toolbar)
//    Toolbar  mActionBar;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_post);
//        ButterKnife.bind(this);
//
//
//        setSupportActionBar(mActionBar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle("add new post");
//
//        cameraPermition = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        stoarPermition = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//
//        mAuth = FirebaseAuth.getInstance();
//        checkuserstatus();
//        mUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showImage();
//            }
//        });
//        mUpload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String title = mPostTitle.getText().toString();
//                String des = mPostDescrip.getText().toString();
//
//
//            }
//        });
//    }
//
//    private boolean checkStoragepermition() {
//        boolean result = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
//        return result;
//    }
//
//    private void requestPermission() {
//
//        ActivityCompat.requestPermissions(this, stoarPermition, STORAGE_REQUESR_CODE);
//    }
//
//    private boolean checkCamerapermition() {
//        boolean result = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
//        boolean resultl = ContextCompat.checkSelfPermission(this,
//                Manifest.permission.CAMERA) == (PackageManager.PERMISSION_GRANTED);
//        return result && resultl;
//    }
//
//    private void requestCameraPermission() {
//
//        ActivityCompat.requestPermissions(this, cameraPermition, CAMERA_REQUESR_CODE);
//
//    }
//
//    private void showImage() {
//        String[] options = {"camera", "Gallery"};
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("choose");
//        builder.setItems(options, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                if (which == 0) {
//                    if (!checkCamerapermition()) {
//                        requestCameraPermission();
//
//                    } else {
//                        pickFromCamera();
//                    }
//
//                }
//                if (which == 1) {
//                    if (!checkStoragepermition()) {
//                        requestPermission();
//
//                    } else {
//                        pickFromGallery();
//                    }
//
//                }
//
//            }
//        });
//        builder.create().show();
//
//
//    }
//
//    private void pickFromGallery() {
//    }
//
//    private void pickFromCamera() {
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        checkuserstatus();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        checkuserstatus();
//    }
//
//    @Override
//    public boolean onSupportNavigateUp() {
//        onBackPressed();
//        return super.onSupportNavigateUp();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.navigation_menu, menu);
//        menu.findItem(R.id.nav_home).setVisible(false);
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        int id = item.getItemId();
//        if (id == R.id.logout) {
//            mAuth.signOut();
//            checkuserstatus();
//
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void checkuserstatus() {
//        FirebaseUser mUser = mAuth.getCurrentUser();
//        if (mUser != null) {
//
//        } else {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        }
//    }
//
//    @OnClick({R.id.postImage, R.id.upload})
//    public void onViewClicked(View view) {
//        switch (view.getId()) {
//            case R.id.postImage:
//                break;
//            case R.id.upload:
//                break;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case CAMERA_REQUESR_CODE: {
//            }
//            break;
//            case STORAGE_REQUESR_CODE: {
//            }
//            break;
//
//        }
//    }
//}

//    private void validatePostInfo()
//    {
//        Description = mPostDescrip.getText().toString();
//
//        if(ImageUri == null)
//        {
//            Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
//        }
//        else if(TextUtils.isEmpty(Description))
//        {
//            Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
//        }
//        else
//        {
//            loadingBar.setTitle("Add New Post");
//            loadingBar.setMessage("Please wait, while we are updating your new post...");
//            loadingBar.show();
//            loadingBar.setCanceledOnTouchOutside(true);
//
//            StoringImageToFirebaseStorage();
//        }
//    }
//
//    private void StoringImageToFirebaseStorage()
//    {
//        Calendar calFordDate = Calendar.getInstance();
//        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
//        saveCurrentDate = currentDate.format(calFordDate.getTime());
//
//        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
//        saveCurrentTime = currentTime.format(calFordDate.getTime());
//
//        postRandomName = saveCurrentDate + saveCurrentTime;
//
//
//        StorageReference filePath = PostsImagesRefrence.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
//
//        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
//            {
//                if(task.isSuccessful())
//                {
//                    downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//                    Toast.makeText(AddPost.this, "image uploaded successfully to Storage...", Toast.LENGTH_SHORT).show();
//                    SavingPostInformationToDatabase();
//
//                }
//                else
//                {
//                    String message = task.getException().getMessage();
//                    Toast.makeText(AddPost.this, "Error occured: " + message, Toast.LENGTH_SHORT).show();
//                }
//            }
//
//
//            private void SavingPostInformationToDatabase()
//            {
//
//                PostsRef.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot)
//                    {
//                        if(dataSnapshot.exists())
//                        {
//
//                            String userFullName = dataSnapshot.child("fullname").getValue().toString();
//                            String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
//
//                            HashMap postsMap = new HashMap();
//                            postsMap.put("uid", current_user_id);
//                            postsMap.put("date", saveCurrentDate);
//                            postsMap.put("time", saveCurrentTime);
//                            postsMap.put("description", Description);
//                            postsMap.put("postimage", downloadUrl);
//                            PostsRef.child(current_user_id ).updateChildren(postsMap)
//                                    .addOnCompleteListener(new OnCompleteListener() {
//                                        @Override
//                                        public void onComplete(@NonNull Task task)
//                                        {
//                                            if(task.isSuccessful())
//                                            {
//                                                sendUserTOMainActivity();
//                                                Toast.makeText(AddPost.this, "New Post is updated successfully.", Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                            else
//                                            {
//                                                Toast.makeText(AddPost.this, "Error Occured while updating your post.", Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                        }
//                                    });
//                        }
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//            }
//        });
//
//
//    }
