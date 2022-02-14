package com.example.tomatodisease;

import static android.os.Environment.getExternalStoragePublicDirectory;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    Button button_camera, button_gallery;
    ImageView imgview;
    File f;
    Bitmap mybitmap;

    ActivityResultLauncher<Intent> startforResult_camera = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
//            Toast.makeText(MainActivity.this, "Onactivity result", Toast.LENGTH_SHORT).show();
            if(result.getResultCode() == RESULT_OK){
                f = new File(currentPhotoPath);
                try {
                    resizeImage(Uri.fromFile(f), imgview);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    ActivityResultLauncher<Intent> startforResult_gallery = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == RESULT_OK){
                assert result.getData() != null;
                Uri contentUri = result.getData().getData();

                try {
                    resizeImage(contentUri, imgview);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                button_camera.setEnabled(true);
                button_gallery.setEnabled(true);
            }
        }
    }

    private void resizeImage(Uri imageuri,  ImageView imgview) throws FileNotFoundException {

        try {
            mybitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), imageuri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // "RECREATE" THE NEW BITMAP
        Bitmap newBitmap = Bitmap.createScaledBitmap(mybitmap, 256, 256, true);
        imgview.setImageURI(imageuri);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_camera = findViewById(R.id.camera);
        button_gallery = findViewById(R.id.gallery);
        imgview = findViewById(R.id.imageView);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            button_gallery.setEnabled(false);
            button_camera.setEnabled(false);
            //request permission for camera and external storage
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        button_camera.setOnClickListener(view -> {
            dispatchTakePictureIntent();
            galleryAddPic();
        });
        button_gallery.setOnClickListener(view -> {
            Intent gallery_intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startforResult_gallery.launch(gallery_intent);
        });

        }
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Toast.makeText(this, ""+currentPhotoPath, Toast.LENGTH_LONG).show();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
            Toast.makeText(this, ""+photoFile, Toast.LENGTH_LONG).show();
        } catch (IOException ex) {
            // Error occurred while creating the File

        }
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startforResult_camera.launch(takePictureIntent);
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}