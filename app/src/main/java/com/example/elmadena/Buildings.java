package com.example.elmadena;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Buildings extends AppCompatActivity {

    @BindView(R.id.A)
    Button mA;
    @BindView(R.id.B)
    Button mB;
    @BindView(R.id.C)
    Button mC;
    @BindView(R.id.D)
    Button mD;
    @BindView(R.id.E)
    Button mE;
    @BindView(R.id.F)
    Button mF;
    @BindView(R.id.G)
    Button mG;
    @BindView(R.id.setupToolbar)
    Toolbar mSetupToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buildings);
        ButterKnife.bind(this);
        setSupportActionBar(mSetupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("المبانى");
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
        Intent i = new Intent(Buildings.this, MainActivity.class);
        startActivity(i);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserTOMainActivity();
    }
}
