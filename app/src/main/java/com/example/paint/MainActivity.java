package com.example.paint;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import java.io.FileNotFoundException;

import yuku.ambilwarna.AmbilWarnaDialog;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "123";
    private static final int PICK_IMAGE = 1000;
    private static final int REQUEST_FOR_GET_IMAGE = 1002;
    private boolean mTiny = false;
    private boolean mNormal = false;
    private boolean mWide = false;
    private boolean mLine = false;
    private boolean mCircle = false;
    private boolean mSquare = false;
    private Toolbar mToolbar;
    private PaintView paintView;
    private int defaultColor;
    static final int GALLERY_REQUEST = 1;
    private int STORAGE_PERMISSION_CODE = 1;
    private int GET_FROM_GALLERY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Button button;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paintView = findViewById(R.id.paintView);
        paintView.setDrawingCacheEnabled(true);
        button = findViewById(R.id.change_color_button);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        paintView.initialise(displayMetrics);

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                openColourPicker();

            }

        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("Needed to save image")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Access granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Access denied", Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == REQUEST_FOR_GET_IMAGE){
            getImage();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.eraser:
                paintView.eraser();
                return true;
            case R.id.clear:
                paintView.clear();
                return true;
            case R.id.tiny:
                mTiny = true;
                mNormal = false;
                mWide = false;
                item.setChecked(true);
                updateView();
                return true;
            case R.id.normal:
                mNormal = true;
                mTiny = false;
                mWide = false;
                item.setChecked(true);
                updateView();
                return true;
            case R.id.wide:
                mWide = true;
                mNormal = false;
                mTiny = false;
                item.setChecked(true);
                updateView();
                return true;
            case R.id.line:
                mLine = true;
                mCircle = false;
                mSquare = false;
                item.setChecked(true);
                updateView();
                return true;
            case R.id.circle:
                mCircle = true;
                mLine = false;
                mSquare = false;
                item.setChecked(true);
                updateView();
                return true;
            case R.id.square:
                mSquare = true;
                mLine = false;
                mCircle = false;
                item.setChecked(true);
                updateView();
                return true;
            case R.id.save:
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission();
                }
                paintView.save();
                return true;
            case R.id.image:
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_FOR_GET_IMAGE);
                }else
                getImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    protected void updateView() {
        if (mLine && !mCircle && !mSquare) {
            if (mTiny && !mNormal && !mWide) {
                paintView.tiny();
            } else if (!mTiny && mNormal && !mWide) {
                paintView.normal();
            } else if (!mTiny && !mNormal && mWide) {
                paintView.wide();
            }
            paintView.line();
        } else if (!mLine && mCircle && !mSquare) {
            if (mTiny && !mNormal && !mWide) {
                paintView.tiny();
            } else if (!mTiny && mNormal && !mWide) {
                paintView.normal();
            } else if (!mTiny && !mNormal && mWide) {
                paintView.wide();
            }
            paintView.circle();
        } else if (!mLine && !mCircle && mSquare) {
            if (mTiny && !mNormal && !mWide) {
                paintView.tiny();
            } else if (!mTiny && mNormal && !mWide) {
                paintView.normal();
            } else if (!mTiny && !mNormal && mWide) {
                paintView.wide();
            }
            paintView.square();
        }
    }

    private void openColourPicker() {

        AmbilWarnaDialog ambilWarnaDialog = new AmbilWarnaDialog(this, defaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {

            @Override
            public void onCancel(AmbilWarnaDialog dialog) {

                Toast.makeText(MainActivity.this, "Cancel", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {

                defaultColor = color;

                paintView.setColor(color);

            }

        });

        ambilWarnaDialog.show(); // add

    }

    public void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE);
    }

        @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_IMAGE && data != null && resultCode == RESULT_OK){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                try {
                    ParcelFileDescriptor fileDescriptor = getContentResolver().openFileDescriptor(data.getData(), "r");
                    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor());
                    paintView.setImage(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else {
                Uri pickedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
                cursor.close();

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

                paintView.setImage(bitmap);
            }
        }
    }

}