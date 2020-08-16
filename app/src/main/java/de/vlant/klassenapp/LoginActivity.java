package de.vlant.klassenapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class LoginActivity extends AppCompatActivity {

    Button signIn;
    EditText name;
    EditText password;
    TextView tvFail;

    static File credFile;

    Intent service;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        signIn = findViewById(R.id.buttonSignIn);
        name = findViewById(R.id.editTextEmailAddress);
        password = findViewById(R.id.editTextPassword);
        tvFail = findViewById(R.id.textViewFail);
        auth = FirebaseAuth.getInstance();
        service = new Intent(this, NotificationService.class);
        credFile = new File(getFilesDir() + "/credentials.vlant");
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signInWithEmailAndPassword(name.getText().toString().toLowerCase() + "@vlant.de", password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveCredentials();
                            startService(service);
                            startActivity(new Intent(getApplicationContext(), MsgActivity.class));
                            finish();
                        } else {
                            tvFail.setAlpha(1f);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    tvFail.setAlpha(0f);
                                }
                            }, 2000);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        tvFail.setAlpha(1f);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tvFail.setAlpha(0f);
                            }
                        }, 2000);
                    }
                });
            }
        });
        loadCredentials();
    }

    private void saveCredentials() {
        try {
            if (!credFile.createNewFile())
                throw new IOException();
        } catch (IOException e) {
            Toast.makeText(this, "Fehler beim Login! Fehlercode: #VL153", Toast.LENGTH_LONG).show();
            finish();
        }
        try {
            FileWriter myWriter = new FileWriter(credFile);
            myWriter.write(name.getText().toString() + ";;;" + password.getText().toString());
            myWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    private void loadCredentials() {
        if (credFile.exists()) {
            StringBuilder creds = new StringBuilder();
            try {
                Scanner myReader = new Scanner(credFile);
                while (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    creds.append(data);
                }
                myReader.close();
            } catch (IOException e) {
                return;
            }
            String[] credentials = creds.toString().split(";;;");
            Log.e("", credentials.toString());
            auth.signInWithEmailAndPassword(credentials[0].toLowerCase() + "@vlant.de", credentials[1]).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startService(service);
                        startActivity(new Intent(getApplicationContext(), MsgActivity.class));
                        finish();
                    } else {
                        tvFail.setAlpha(1f);
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tvFail.setAlpha(0f);
                            }
                        }, 2000);
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    tvFail.setAlpha(1f);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tvFail.setAlpha(0f);
                        }
                    }, 2000);
                }
            });
        }
    }
}