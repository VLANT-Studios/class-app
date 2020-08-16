package de.vlant.klassenapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

public class LoadActivity extends AppCompatActivity {

    TextView progressView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressView = findViewById(R.id.progress_text);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMessages();
            }
        }, 1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadBulletinBoard();
            }
        }, 1500);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadPolls();
            }
        }, 2000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        }, 2000);
    }

    private void loadMessages() {
        progressView.setText(getText(R.string.load_text_2));
    }

    private void loadBulletinBoard() {
        progressView.setText(getText(R.string.load_text_3));
    }

    private void loadPolls() {
        Toast.makeText(this, "Loaded!", Toast.LENGTH_SHORT).show();
    }
}