package com.example.gallerybridge;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;
import java.io.InputStream;
import java.io.OutputStream;

public class GalleryCameraActivity extends Activity {
    private static final int PICK_IMAGE = 701;
    private Uri requestedOutput;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        Uri out = getIntent().getParcelableExtra("output");
        if (out == null) out = getIntent().getParcelableExtra(android.provider.MediaStore.EXTRA_OUTPUT);
        requestedOutput = out;

        Intent picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        picker.addCategory(Intent.CATEGORY_OPENABLE);
        picker.setType("image/*");
        picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        picker.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(picker, PICK_IMAGE);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGE) return;
        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            setResult(RESULT_CANCELED); finish(); return;
        }
        Uri selected = data.getData();
        try {
            String mime = getContentResolver().getType(selected);
            if (mime == null || !(mime.equals("image/jpeg") || mime.equals("image/png") ||
                    mime.equals("image/webp") || mime.equals("image/jpg"))) {
                Toast.makeText(this, "Choose JPG, PNG, or WebP image", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED); finish(); return;
            }

            Intent result = new Intent();
            if (requestedOutput != null) {
                try (InputStream in = getContentResolver().openInputStream(selected);
                     OutputStream out = getContentResolver().openOutputStream(requestedOutput, "w")) {
                    if (in == null || out == null) throw new Exception("Cannot open image stream");
                    byte[] buffer = new byte[8192]; int n;
                    while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                }
                result.setData(requestedOutput);
                result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                result.setData(selected);
                result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                result.setClipData(android.content.ClipData.newRawUri("selected-image", selected));
            }
            setResult(RESULT_OK, result);
        } catch (Exception e) {
            Toast.makeText(this, "Could not return selected image: " + e.getMessage(), Toast.LENGTH_LONG).show();
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
