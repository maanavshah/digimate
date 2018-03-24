package com.campcode.maanav.digimate.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
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

    public static String PDF_TITLE;
    public static int PDF_PAGES;
    public static String PDF_CONTENT;
    public static int CURRENT_PAGE;
    public static String DIRECTORY_PATH = "/Android/data/com.campcode.maanav.digimate";
    public static ArrayList<String> FILE_NAMES = new ArrayList<>();
    private EditText editTitle, editPages;
    private Button buttonGenerate;
    private long TIME_OUT = 1000;   // Interval to start camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initActivity();

        buttonGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request Camera Permissions
                    requestCameraPermission();
                } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Request External Storage Write Permissions
                    requestExternalStoragePermission();
                } else {
                    PDF_TITLE = editTitle.getText().toString();
                    String pages = editPages.getText().toString();
                    if (PDF_TITLE.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter the title of PDF",
                                Toast.LENGTH_SHORT).show();
                    } else if (pages.isEmpty()) {
                        Toast.makeText(MainActivity.this, "Please enter the total pages in PDF",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        PDF_PAGES = Integer.parseInt(pages);
                        CURRENT_PAGE = 0;
                        PDF_CONTENT = "";
                        if (CURRENT_PAGE < PDF_PAGES) {
                            startCamera();
                        }
                    }
                }
            }
        });
    }

    private void requestExternalStoragePermission() {
        Toast.makeText(MainActivity.this, "External storage permission is not granted.",
                Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    private void requestCameraPermission() {
        Toast.makeText(MainActivity.this, "Camera permission is not granted.",
                Toast.LENGTH_SHORT).show();
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.CAMERA}, 2);
    }

    private void initActivity() {
        // Initialize the view elements
        editTitle = (EditText) findViewById(R.id.editTitle);
        editPages = (EditText) findViewById(R.id.editPages);
        buttonGenerate = (Button) findViewById(R.id.buttonGenerate);
    }

    private void startCamera() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, CaptureActivity.class));
            }
        }, TIME_OUT);
    }
}
