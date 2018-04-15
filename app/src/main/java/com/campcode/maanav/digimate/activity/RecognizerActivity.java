package com.campcode.maanav.digimate.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.campcode.maanav.digimate.R;
import com.campcode.maanav.digimate.helper.ContactWriter;
import com.campcode.maanav.digimate.helper.PdfGenerator;
import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecognizerActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textExtracted;
    private EditText search;
    private ProgressDialog progressCopy;
    private ProgressDialog progressOcr;
    private String textScanned;     // textScanned has extracted text output
    private String DATA_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() +
            MainActivity.DIRECTORY_PATH + File.separator;
    private AsyncTask<Void, Void, Void> copy = new copyTask();
    private AsyncTask<Void, Void, Void> ocr = new ocrTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognizer);

        initActivity();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String ett = search.getText().toString().replaceAll("\n", " ");
                String tvt = textExtracted.getText().toString().replaceAll("\n", " ");
                textExtracted.setText(textExtracted.getText().toString());
                if (!ett.isEmpty()) {
                    int ofe = tvt.toLowerCase().indexOf(ett.toLowerCase(), 0);
                    Spannable WordtoSpan = new SpannableString(textExtracted.getText());
                    for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
                        ofe = tvt.toLowerCase().indexOf(ett.toLowerCase(), ofs);
                        if (ofe == -1)
                            break;
                        else {
                            WordtoSpan.setSpan(new BackgroundColorSpan(ContextCompat.getColor
                                    (RecognizerActivity.this, R.color.colorAccent)), ofe, ofe +
                                    ett.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            textExtracted.setText(WordtoSpan, TextView.BufferType.SPANNABLE);
                        }
                    }
                }
            }
        });

        copy.execute();
        ocr.execute();
    }

    private void initActivity() {
        textExtracted = (TextView) findViewById(R.id.textExtracted);
        textExtracted.setMovementMethod(new ScrollingMovementMethod());
        search = (EditText) findViewById(R.id.search_text);
        // Setting progress dialog for copy job.
        progressCopy = new ProgressDialog(RecognizerActivity.this);
        progressCopy.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressCopy.setIndeterminate(true);
        progressCopy.setCancelable(false);
        progressCopy.setTitle("Dictionaries");
        progressCopy.setMessage("Copying dictionary files");
        // Setting progress dialog for ocr job.
        progressOcr = new ProgressDialog(this);
        progressOcr.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressOcr.setIndeterminate(true);
        progressOcr.setCancelable(false);
        progressOcr.setTitle("OCR");
        progressOcr.setMessage("Extracting text, please wait");
        textScanned = "";
        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.nextStep);
        mFab.setOnClickListener(this);
    }

    // Copy assets trainneddata to tessdata in External Storage
    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("trainneddata");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) {
            for (String filename : files) {
                Log.i("files", filename);
                InputStream in;
                OutputStream out;
                String dirout = DATA_PATH + "tessdata/";
                File outFile = new File(dirout, filename);
                if (!outFile.exists()) {
                    try {
                        in = assetManager.open("trainneddata/" + filename);
                        (new File(dirout)).mkdirs();
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                        in.close();
                        out.flush();
                        out.close();
                    } catch (IOException e) {
                        Log.e("tag", "Error creating files", e);
                    }
                }
            }
        }
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    private void recognizeText() {
        String language = "eng";
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.init(DATA_PATH, language, TessBaseAPI.OEM_TESSERACT_ONLY);
        baseApi.setImage(BinarizationActivity.umbralization);
        textScanned = baseApi.getUTF8Text();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.nextStep) {
            if (MainActivity.CURRENT_PAGE < MainActivity.TOTAL_PAGES) {
                // if pages scanned are less than total pages
                startActivity(new Intent(RecognizerActivity.this, CaptureActivity.class));
            } else {
                // if all pages scanned, then genereate pdf's and save contact
                PdfGenerator.generateImagePdf();
                PdfGenerator.generateText(MainActivity.TITLE,
                        MainActivity.CONTENT);
                PdfGenerator.deleteImageFiles();
                Toast.makeText(this, "PDF's Generated", Toast.LENGTH_SHORT).show();
                ContactWriter contactWriter = new ContactWriter(this);
                contactWriter.addContact(MainActivity.TITLE, MainActivity.CONTENT);
                Toast.makeText(this, "Contact Saved as " + MainActivity.TITLE, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RecognizerActivity.this, MainActivity.class));
            }
        }
    }

    private class copyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressCopy.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressCopy.cancel();
            progressOcr.show();

        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i("CopyTask", "copying..");
            copyAssets();
            return null;
        }
    }

    private class ocrTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressOcr.cancel();
            textExtracted.setText(textScanned);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.i("OCRTask", "extracting..");
            recognizeText();
            MainActivity.CONTENT += textScanned;
            MainActivity.CURRENT_PAGE++;
            return null;
        }
    }
}
