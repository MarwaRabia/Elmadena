package com.example.elmadena;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class Submitting extends AppCompatActivity {

    @BindView(R.id.tool)
    Toolbar mMainAppToolbar;
    @BindView(R.id.setup_image)
    CircleImageView mSetupImage;
    @BindView(R.id.names)
    TextInputEditText mNames;
    @BindView(R.id.emails)
    TextInputEditText mEmails;
    @BindView(R.id.phones)
    TextInputEditText mPhones;
    @BindView(R.id.address)
    TextInputEditText mAddress;
    @BindView(R.id.ssn)
    TextInputEditText mSsn;
    @BindView(R.id.collages)
    TextInputEditText mCollages;
    @BindView(R.id.sections)
    TextInputEditText mSections;
    @BindView(R.id.fer)
    TextInputEditText mFer;
    @BindView(R.id.grades)
    TextInputEditText mGrades;
    @BindView(R.id.parentphone)
    TextInputEditText mParentphone;

    private static final int Gallery_Pick = 1;
    @BindView(R.id.uploadform)
    Button mUploadform;
    private Uri ImageUri = null;
    private ProgressDialog loadingBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    Bitmap compressedImageFile;
    private StorageReference PostsImagesRefrence;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;
    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;

    String randomName;
    String downloadUri;
    String name ,email,phone,address,ssn,fuc,dept,sem,grade,parnum;
    String downloadthumbUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submitting);
        ButterKnife.bind(this);

        setSupportActionBar(mMainAppToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("استمارة التقديم");

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        PostsImagesRefrence = FirebaseStorage.getInstance().getReference();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Application form").child(current_user_id);


        mSetupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();

            }
        });
        mUploadform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               upload();

            }
        });



    }




    private void upload() {

definition();

        if(!TextUtils.isEmpty(name) && ImageUri != null&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(phone)&&!TextUtils.isEmpty(address)
                &&!TextUtils.isEmpty(ssn)&&!TextUtils.isEmpty(fuc)&&!TextUtils.isEmpty(dept)&&!TextUtils.isEmpty(sem)&&!TextUtils.isEmpty(grade)&&!TextUtils.isEmpty(parnum)){
            bar();

            randomName = UUID.randomUUID().toString();

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

                                savedata();
                            }
                        }
                    });





                }
            });


        }

    }

    private void savedata() {

        Map<String, Object> postMap = new HashMap<>();
        postMap.put("image_url", downloadUri);
        postMap.put("image_thumb", downloadthumbUri);
        postMap.put("اسم الطالب",name);
        postMap.put("رقم الهاتف",phone);
        postMap.put("البريد الالكتروني",email);
        postMap.put(" العنوان ",address);
        postMap.put(" رقم البطاقه",ssn);
        postMap.put(" الكليه",fuc);
        postMap.put("قسم ",dept);
        postMap.put("الفرقه",sem);
        postMap.put("التقدير",grade);
        postMap.put("رقم ولى الأمر",parnum);


        postMap.put("user_id", current_user_id);
        postMap.put("timestamp", FieldValue.serverTimestamp());

        firebaseFirestore.collection("Application form").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {

                if(task.isSuccessful()){

                    Toast.makeText(Submitting.this, "تم ارسال البيانات بنجاح", Toast.LENGTH_LONG).show();
                    sendUserTOMainActivity();

                } else {


                }

                loadingBar.dismiss();

            }
        });
    }

    private void definition() {
        name=mNames.getText().toString();
        email=mEmails.getText().toString();
        phone=mPhones.getText().toString();
        address=mAddress.getText().toString();
        ssn=mSsn.getText().toString();
        fuc=mCollages.getText().toString();
        dept=mSections.getText().toString();
        sem=mFer.getText().toString();
        grade=mGrades.getText().toString();
        parnum=mParentphone.getText().toString();
    }


    private void bar() {


        loadingBar.setTitle("ارسال البيانات");
        loadingBar.setMessage("من فضلك انتظر حتى يتم ارسال بياناتك");
        loadingBar.show();
        loadingBar.setCanceledOnTouchOutside(true);

    }


    private void openGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null) {
            ImageUri = data.getData();
            mSetupImage.setImageURI(ImageUri);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            sendUserTOMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }


    private void sendUserTOMainActivity() {
        Intent i = new Intent(Submitting.this, MainActivity.class);
        startActivity(i);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserTOMainActivity();
    }
}
