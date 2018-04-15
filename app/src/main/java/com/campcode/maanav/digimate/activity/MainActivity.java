package com.campcode.maanav.digimate.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.campcode.maanav.digimate.R;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String TITLE;
    public static int TOTAL_PAGES;
    public static String CONTENT;
    public static int CURRENT_PAGE;
    public static String DIRECTORY_PATH = "/Android/data/com.campcode.maanav.digimate";
    public static ArrayList<String> FILE_NAMES = new ArrayList<>();
    private EditText editTitle, editPages;
    private Button buttonGenerate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initActivity();

        buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TITLE = editTitle.getText().toString();
                String pages = editPages.getText().toString();
                if (TITLE.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter the title of PDF",
                            Toast.LENGTH_SHORT).show();
                } else if (pages.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter the total pages in PDF",
                            Toast.LENGTH_SHORT).show();
                } else {
                    TOTAL_PAGES = Integer.parseInt(pages);
                    CURRENT_PAGE = 0;
                    CONTENT = "";
                    if (CURRENT_PAGE < TOTAL_PAGES) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Request Camera Permissions
                            requestCameraPermission();
                        } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Request External Storage Permissions
                            requestExternalStoragePermission();
                        } else if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_CONTACTS)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Request Contact Permissions
                            requestContactPermission();
                        } else {
                            startCamera();
                        }
                    }
                }
            }
        });
    }

    private void requestContactPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_CONTACTS}, 3);
    }

    private void requestExternalStoragePermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA}, 1);
    }

    private void initActivity() {
        // Initialize the view elements
        editTitle = (EditText) findViewById(R.id.editTitle);
        editPages = (EditText) findViewById(R.id.editPages);
        buttonGenerate = (Button) findViewById(R.id.buttonGenerate);
    }

    private void startCamera() {
        long TIME_OUT = 100;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, CaptureActivity.class));
            }
        }, TIME_OUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestExternalStoragePermission();
                } else {
                    Toast.makeText(getApplicationContext(), "Camera permission not granted.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            case 2:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestContactPermission();
                } else {
                    Toast.makeText(getApplicationContext(), "External Storage permission not granted.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            case 3:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                } else {
                    Toast.makeText(getApplicationContext(), "Contact permission not granted.",
                            Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }
}
