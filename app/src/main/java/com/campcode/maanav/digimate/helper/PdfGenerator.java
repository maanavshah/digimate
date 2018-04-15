package com.campcode.maanav.digimate.helper;

import android.os.Environment;

import com.campcode.maanav.digimate.activity.MainActivity;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by maanav on 25/3/18.
 */

public class PdfGenerator {
    private static String dirpath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public static void generateText(String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName + ".txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void generateImagePdf() {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(
                    dirpath + "/" +
                            MainActivity.TITLE + "_image.pdf")); //  Change pdf's name.
        } catch (DocumentException | FileNotFoundException e) {
            e.printStackTrace();
        }
        document.open();

        for (int i = 0; i < MainActivity.TOTAL_PAGES; i++) {
            Image img = null;  // Change image's name and extension.
            try {
                img = Image.getInstance(dirpath + MainActivity.DIRECTORY_PATH + File.separator +
                        MainActivity.FILE_NAMES.get(i));
            } catch (BadElementException | IOException e) {
                e.printStackTrace();
            }

            float width = document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin();
            float height = document.getPageSize().getHeight() - document.topMargin() - document.bottomMargin();
            if (img != null) {
                img.scaleToFit(width, height);
            }

            // OR USE SCALER
            // float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0) / img.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
            // img.scalePercent(scaler);
            // img.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
            // img.setAlignment(Image.LEFT| Image.TEXTWRAP);

            try {
                document.add(img);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        document.close();
    }

    public static void deleteImageFiles() {
        for (int i = 0; i < MainActivity.TOTAL_PAGES; i++) {
            File file = new File(dirpath + MainActivity.DIRECTORY_PATH + File.separator,
                    MainActivity.FILE_NAMES.get(i));
            if (file.exists()) {
                if (file.delete()) {
                    System.out.println("File deleted :" + file.getAbsolutePath());
                } else {
                    System.out.println("File not deleted :" + file.getAbsolutePath());
                }
            }
        }
    }
}
