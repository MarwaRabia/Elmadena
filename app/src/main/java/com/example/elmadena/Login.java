package com.example.elmadena;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class Login extends AppCompatActivity {
    TextView mTextView;
    Button mButton;
    ProgressDialog mProgressDialog;
    FirebaseAuth mAuth;
    @BindView(R.id.emaillogin)
    EditText mEmaillogin;
    @BindView(R.id.passwordlogin)
    EditText mPasswordlogin;
    @BindView(R.id.loginbutton)
    Button mLoginbutton;
    @BindView(R.id.registerlink)
    TextView mRegisterlink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        mAuth=FirebaseAuth.getInstance();
        mProgressDialog=new ProgressDialog(this);

        mTextView=findViewById(R.id.registerlink);
        mButton=findViewById(R.id.loginbutton);

        mRegisterlink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserTORegisterActivity();

            }
        });

        mLoginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allowUserTOLogin();
            }
        });


    }
    private void allowUserTOLogin() {


        String em=mEmaillogin.getText().toString();
        String pa=mPasswordlogin.getText().toString();

        if (TextUtils.isEmpty(em)){
            Toast.makeText(this, "Email is Empty", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(pa)){
            Toast.makeText(this, "password is Empty", Toast.LENGTH_SHORT).show();
        }


        else{
            mProgressDialog.setTitle("Login Account");
            mProgressDialog.setMessage("plesae wait");
            mProgressDialog.show();
            mProgressDialog.setCanceledOnTouchOutside(true);
            mAuth.signInWithEmailAndPassword(em,pa).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        sendUserTOMainActivity();

                        Toast.makeText(Login.this, "Log in  is succefully", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                    }
                    else{
                        Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();

                    }

                }
            });



        }}
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser mCUser=mAuth.getCurrentUser();
        if (mCUser!=null){
            sendUserTOMainActivity();

        }


    }

    private void  sendUserTOMainActivity(){
        Intent i=new Intent(Login.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();

    }
    private  void sendUserTORegisterActivity(){
        Intent i=new Intent(Login.this,Register.class);
        startActivity(i);
    }
}
