package com.campcode.maanav.digimate.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.campcode.maanav.digimate.R;
import com.campcode.maanav.digimate.helper.CameraPreview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CaptureActivity extends AppCompatActivity {

    private static String TAG = "CaptureActivity";
    private Camera mCamera;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.v(TAG, "Error creating output file");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap capturedImage = rotateImage(BitmapFactory.decodeByteArray(data, 0,
                        data.length), 90);
                capturedImage.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                Log.v(TAG, e.getMessage());
            }

            // Start crop activity
            nextStep();
        }
    };

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // Attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(TAG, e.getMessage()); // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private static File getOutputMediaFile() {
        String state = Environment.getExternalStorageState();
        if (!state.equals(Environment.MEDIA_MOUNTED)) {
            return null;
        } else {
            File folder_gui = new File(Environment.getExternalStorageDirectory() +
                    MainActivity.DIRECTORY_PATH);
            if (!folder_gui.exists()) {
                folder_gui.mkdirs();
            }
            MainActivity.FILE_NAMES.add("img" + System.currentTimeMillis() + ".jpg");
            return new File(folder_gui, MainActivity.FILE_NAMES.get(MainActivity.CURRENT_PAGE));
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(),
                matrix, true);
    }

    private void nextStep() {
        startActivity(new Intent(CaptureActivity.this, CropActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_capture);

        mCamera = getCameraInstance();
        CameraPreview mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.frame_preview);
        preview.addView(mPreview);

        // Add a listener to the Capture button
        final Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                        Toast.makeText(CaptureActivity.this, "Image captured",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
