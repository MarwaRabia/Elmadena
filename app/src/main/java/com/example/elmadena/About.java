package com.example.elmadena;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

public class About extends AppCompatActivity {
    Button inform, publish, assess, send;
    @BindView(R.id.setupToolbar)
    Toolbar mSetupToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);


        inform = findViewById(R.id.bt_inform);
        publish = findViewById(R.id.bt_publish);
        assess = findViewById(R.id.bt_assess);
        send = findViewById(R.id.bt_send_sugg);
        setSupportActionBar(mSetupToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("عن البرنامج");

        inform.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(About.this, Info.class);
                startActivity(intent);
            }
        });
        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String shareBody = "تطبيق لسهوله التواصل مع اداره" +
                        " المدينه الجامعيه باساليب التكنولوجيا الحديثه";
                intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
                startActivity(Intent.createChooser(intent, "Share using"));
            }
        });
        assess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + ""));
                startActivity(intent);
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
        Intent i = new Intent(About.this, MainActivity.class);
        startActivity(i);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        sendUserTOMainActivity();
    }
}
