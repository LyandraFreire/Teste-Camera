package com.example.aula_01.digitalfotos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.aula_01.digitalfotos.adapters.RecyclerViewImageAdapter;
import com.example.aula_01.digitalfotos.model.Image;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static  final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_CAPTURE_IMAGE = 150;
    private FloatingActionButton fab;
    private RecyclerView recyclerImages;
    private RecyclerViewImageAdapter adapter;
    private FirebaseStorage storage;
    private String imageFilePath;
    private String imageFileName;
    private List<Image> images = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();


        fab.setOnClickListener(view -> {
            openCameraIntent();

        });

    }

    private void initViews() {
        fab = findViewById(R.id.fab);
        recyclerImages = findViewById(R.id.recycler_images);
        adapter = new RecyclerViewImageAdapter(new ArrayList<>());
        storage = FirebaseStorage.getInstance();

        recyclerImages.setLayoutManager(new LinearLayoutManager(this));
        recyclerImages.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void openCameraIntent() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CAMERA_PERMISSION);
            return;
        }
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.i("LOG", "Erro ao criar caminho para a imagem");
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.aula_01.digitalfotos.provider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, REQUEST_CAPTURE_IMAGE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        imageFileName = imageFileName + ".jpg";
        imageFilePath = image.getAbsolutePath();
        return image;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CAPTURE_IMAGE) {
            Log.i("LOG", "imageFilePath: " + imageFilePath);
            saveImageOnFirebaseStorage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_CAMERA_PERMISSION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("LOG", "Camera permission granted - initialize the camera source");
        }
    }
    private void saveImageOnFirebaseStorage() {

        try {
            StorageReference imagesRef = storage.getReference().child("images");
            StorageReference childRef = imagesRef.child(imageFileName);

            InputStream stream;
            stream = new FileInputStream(new File(imageFilePath));
            UploadTask uploadTask = childRef.putStream(stream);


            uploadTask.addOnFailureListener(exception ->
                    Log.i("LOG", "Deu ruim: " + exception.getMessage())
            ).addOnSuccessListener(taskSnapshot -> {


                Toast.makeText(MainActivity.this, "Deu bom o upload", Toast.LENGTH_SHORT).show();

                childRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String name = taskSnapshot.getMetadata().getName();
                            images.add(new Image(uri.toString(), name));
                            adapter.update(images);
                        }
                );
            });

        } catch (FileNotFoundException e) {
            Log.e("LOG", "saveImageOnFirebaseStorage: ", e);
        }
    }
}
