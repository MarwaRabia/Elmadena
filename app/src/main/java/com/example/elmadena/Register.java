package com.example.elmadena;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Register extends AppCompatActivity {

    @BindView(R.id.emailregister)
    EditText mEmailregister;
    @BindView(R.id.passwordregister)
    EditText mPasswordregister;
    @BindView(R.id.confirmregisterpass)
    EditText mConfirmregisterpass;
    @BindView(R.id.creatbutton)
    Button mCreatbutton;
    FirebaseAuth mFirebaseAuth;
    ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mFirebaseAuth=FirebaseAuth.getInstance();
        mProgressDialog=new ProgressDialog(this);


        mCreatbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCreatAcount();

            }
        });



    }


    private  void setCreatAcount(){
        String em=mEmailregister.getText().toString();
        String pa=mPasswordregister.getText().toString();
        String cpa=mConfirmregisterpass.getText().toString();
        if (TextUtils.isEmpty(em)){
            Toast.makeText(this, "Email is Empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(pa)){
            Toast.makeText(this, "password is Empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(cpa)){
            Toast.makeText(this, "Confirm Password is Empty", Toast.LENGTH_SHORT).show();
        }
        else if (!pa.equals(cpa)){
            Toast.makeText(this, "password  not match with confirm", Toast.LENGTH_SHORT).show();
        }
        else{
            mProgressDialog.setTitle("creat Account");
            mProgressDialog.setMessage("plesae wait");
            mProgressDialog.show();
            mProgressDialog.setCanceledOnTouchOutside(true);
            mFirebaseAuth.createUserWithEmailAndPassword(em,pa).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
sendUserTOMainActivity();

                        Toast.makeText(Register.this, "تم انشاء الحساب", Toast.LENGTH_SHORT).show();

                        mProgressDialog.dismiss();
                    }
                    else{
                        Toast.makeText(Register.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();

                    }
                }
            });

        }



    }
       @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mCUser=mFirebaseAuth.getCurrentUser();
        if (mCUser!=null){
            sendUserTOMainActivity();

        }


    }
    private void  sendUserTOMainActivity(){
        Intent i=new Intent(Register.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();

    }


}
