package com.example.elmadena;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.grpc.Compressor;

public class SetUpActivity extends AppCompatActivity {

    private String user_id;
    private boolean isChanged = false;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Bitmap compressedImageFile;
    private static final int Gallery_Pick = 1;
    private Uri mainImageURI = null;


    @BindView(R.id.setupToolbar)
    Toolbar mSetupToolbar;
    @BindView(R.id.setup_image)
    CircleImageView mSetupImage;
    @BindView(R.id.setup_name)
    EditText mSetupName;
    @BindView(R.id.setup_btn)
    Button mSetupBtn;
    @BindView(R.id.setup_progress)
    ProgressBar mSetupProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);
        ButterKnife.bind(this);


        Toolbar setupToolbar = findViewById(R.id.setupToolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setTitle("الاعدادات");


        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();



        mSetupProgress.setVisibility(View.VISIBLE);
        mSetupBtn.setEnabled(false);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {


                if(task.isSuccessful()){


                    if(task.getResult().exists()){

                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("image");

                        mainImageURI = Uri.parse(image);
                        mSetupName.setText(name);

                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_image);

                        Glide.with(SetUpActivity.this).setDefaultRequestOptions(placeholderRequest).load(image).into(mSetupImage);


                    }




                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetUpActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();

                }


                mSetupProgress.setVisibility(View.INVISIBLE);
                mSetupBtn.setEnabled(true);

            }
        });

        mSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String user_name = mSetupName.getText().toString();

                if (!TextUtils.isEmpty(user_name) && mainImageURI != null) {

                    mSetupProgress.setVisibility(View.VISIBLE);

                    if (isChanged) {

                        user_id = firebaseAuth.getCurrentUser().getUid();

                        File newImageFile = new File(mainImageURI.getPath());


//                        try {
//
//                            compressedImageFile = new Compressor(SetUpActivity.this)
//                                    .setMaxHeight(125)
//                                    .setMaxWidth(125)
//                                    .setQuality(50)
//                                    .compressToBitmap(newImageFile);
//
//
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }



                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();

                        UploadTask image_path = storageReference.child("profile_images").child(user_id + ".jpg").putBytes(thumbData);

                        image_path.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    storeFirestore(task, user_name);

                                } else {

                                    String error = task.getException().getMessage();
                                    Toast.makeText(SetUpActivity.this, "(IMAGE Error) : " + error, Toast.LENGTH_LONG).show();

                                    mSetupProgress.setVisibility(View.INVISIBLE);

                                }
                            }
                        });

                    } else {

                        storeFirestore(null, user_name);

                    }

                }

            }

        });





        mSetupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });


    }

    private void storeFirestore(@NonNull Task<UploadTask.TaskSnapshot> task, String user_name) {

        Uri download_uri;

        if(task != null) {

            download_uri = task.getResult().getUploadSessionUri();

        } else {

            download_uri = mainImageURI;

        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", user_name);
        userMap.put("image", download_uri.toString());

        firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){

                    Toast.makeText(SetUpActivity.this, "تم اضافة التعديلات", Toast.LENGTH_LONG).show();
                  sendUserTOMainActivity();
                    finish();

                } else {

                    String error = task.getException().getMessage();
                    Toast.makeText(SetUpActivity.this, "(FIRESTORE Error) : " + error, Toast.LENGTH_LONG).show();

                }

                mSetupProgress.setVisibility(View.INVISIBLE);

            }
        });


    }



    private void BringImagePicker() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(SetUpActivity.this);

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
            Uri ImageUri = data.getData();

            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(SetUpActivity.this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                mSetupImage.setImageURI(mainImageURI);
            }
        }


    }

    public  static  class NewsPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, DatePickerDialog.OnDateSetListener {



        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);



            // Find the "color theme" Preference object according to its key
            Preference colorTheme = findPreference(getString(R.string.settings_color_key));
            // Update the summary so that it displays the current value stored in SharedPreferences
            bindPreferenceSummaryToValue(colorTheme);

            // Find the "text size" Preference object according to its key
            Preference textSize = findPreference(getString(R.string.settings_text_size_key));
            // Update the summary so that it displays the current value stored in SharedPreferences
            bindPreferenceSummaryToValue(textSize);


        }
        private void bindPreferenceSummaryToValue(Preference preference) {
            // Set the current NewsPreferenceFragment instance to listen for changes to the preference
            // we pass in using
            preference.setOnPreferenceChangeListener(this);

            // Read the current value of the preference stored in the SharedPreferences on the device,
            // and display that in the preference summary
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            onPreferenceChange(preference, preferenceString);
        }





        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

//            month = month + 1;
//            // Date the user selected
//            String selectedDate = year + "-" + month + "-" + dayOfMonth;
//            // Convert selected date string(i.e. "2017-2-1" into formatted date string(i.e. "2017-02-01")
//            String formattedDate = formatDate(selectedDate);
//
//            // Storing selected date
//            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putString(getString(R.string.settings_from_date_key), formattedDate).apply();
//
//            // Update the displayed preference summary after it has been changed
//            Preference fromDatePreference = findPreference(getString(R.string.settings_from_date_key));
//            bindPreferenceSummaryToValue(fromDatePreference);

        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();
            // Update the summary of a ListPreference using the label
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            sendUserTOMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendUserTOMainActivity() {
        Intent i = new Intent(SetUpActivity.this, MainActivity.class);
        startActivity(i);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserTOMainActivity();
    }
}







