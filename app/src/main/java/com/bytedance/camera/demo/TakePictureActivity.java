package com.bytedance.camera.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bytedance.camera.demo.utils.Utils;

import java.io.File;

public class TakePictureActivity extends AppCompatActivity {

    private ImageView imageView;
    private File imageFile;

    private static final String TAG = TakePictureActivity.class.getName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;


    private static final int REQUEST_PERMISSION_STORAGE_CAMERA = 1000;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);
        imageView = findViewById(R.id.img);
        findViewById(R.id.btn_picture).setOnClickListener(v -> {

            if (ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(TakePictureActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //todo 在这里申请相机、存储的权限
                ActivityCompat.requestPermissions(TakePictureActivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_STORAGE_CAMERA);
            } else {
                takePicture();
            }
        });

    }

    private void takePicture() {
        //todo 打开相机
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageFile = Utils.getOutputMediaFile(Utils.MEDIA_TYPE_IMAGE);
        if (imageFile != null) {
            Log.d(TAG, imageFile.getAbsolutePath());
            Uri fileUri = FileProvider.getUriForFile(this,
                    "com.bytedance.camera.demo", imageFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            /*
            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(bitmap);
            */
            setPic();
            // 发送广播通知相册更新数据,显示所拍摄的照片
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)));
        }
    }


    private void setPic() {
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        //Get dimensions of the bitmap
        BitmapFactory.Options btmOptions = new BitmapFactory.Options();
        btmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), btmOptions);
        int photoW = btmOptions.outWidth;
        int photoH = btmOptions.outHeight;

        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        btmOptions.inJustDecodeBounds = false;
        btmOptions.inSampleSize = scaleFactor;
        btmOptions.inPurgeable = true;

        Bitmap bmp = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), btmOptions);
        imageView.setImageBitmap(Utils.rotateImage(bmp, imageFile.getAbsolutePath()));

        //todo 根据imageView裁剪

        //todo 根据缩放比例读取文件，生成Bitmap

        //todo 如果存在预览方向改变，进行图片旋转

        //todo 如果存在预览方向改变，进行图片旋转
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_STORAGE_CAMERA: {
                //todo 判断权限是否已经授予
                for (int i = 0; i < grantResults.length; i++) {
                    int state = grantResults[i];
                    if (state == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(TakePictureActivity.this, permissions[i] + " permission granted",
                                Toast.LENGTH_SHORT).show();
                    } else if (state == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(TakePictureActivity.this, permissions[i] + " permission denied",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                takePicture();
                break;
            }

        }
    }
}
