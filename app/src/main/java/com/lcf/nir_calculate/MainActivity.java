package com.lcf.nir_calculate;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    String[] premission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int REQUEST_PERMISSION = 101;
    //    String picPath1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/band3.png";
    String picPath1 = "file:///android_asset/" + "right.jpg";
    //    String picPath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/band4.png";
    String picPath2 = "file:///android_asset/" + "left.jpg";

    boolean hasPermission = false;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private TextView tvDegree;
    private ProgressBar progress;

    boolean showOrigin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);
        tvDegree = findViewById(R.id.tv_degree);
        progress = findViewById(R.id.pb_degree);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((ContextCompat.checkSelfPermission(this, premission[0]) != PackageManager.PERMISSION_GRANTED) ||
                    (ContextCompat.checkSelfPermission(this, premission[1]) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(premission, REQUEST_PERMISSION);
            } else {
                hasPermission = true;
                initView();
            }
        } else {
            hasPermission = true;
            initView();
        }
    }

    private void initView() {
        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrigin = !showOrigin;
                showPicByBoolean();
            }
        });
        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOrigin = !showOrigin;
                showPicByBoolean();
            }
        });
        showPicByBoolean();
        startCalculate();
    }

    private void startCalculate() {
        Bitmap bitmap1 = null;
        Bitmap bitmap2 = null;
        try {
            int greenCount = 0;
            bitmap1 = BitmapFactory.decodeStream(getAssets().open("right.jpg"));
            bitmap2 = BitmapFactory.decodeStream(getAssets().open("left.jpg"));
            int minW = Math.min(bitmap1.getWidth(), bitmap2.getWidth());
            int minH = Math.min(bitmap1.getHeight(), bitmap2.getHeight());
            int[] pixel1 = new int[minW * minH];
            int[] pixel2 = new int[minW * minH];
            float[] ndvi = new float[minW * minH];
            bitmap1.getPixels(pixel1, 0, minW, 0, 0, minW, minH);
            bitmap2.getPixels(pixel2, 0, minW, 0, 0, minW, minH);
            Toast.makeText(this, "pixel1.length:" + pixel1.length + "pixel2.length:" + pixel2.length, Toast.LENGTH_LONG).show();
            System.out.println("开始算");
            for (int i = 0; i < pixel1.length; i++) {
                final int r1 = (pixel1[i] & 0xff0000) >> 16, g1 = (pixel1[i] & 0xff00) >> 8, b1 = pixel1[i] & 0xff;
//                final float gray1 = 0.299f * r1 + 0.578f * g1 + 0.114f * b1;
                final float gray1 = (r1 + g1 + b1) / 3;
                final int r2 = (pixel2[i] & 0xff0000) >> 16, g2 = (pixel2[i] & 0xff00) >> 8, b2 = pixel2[i] & 0xff;
                final float gray2 = (r2 + g2 + b2) / 3;
                if (gray1 == 0 && gray2 == 0)
                    ndvi[i] = 0;
                else
                    ndvi[i] = (gray2 - gray1) / (gray2 + gray1);
                if (ndvi[i] > 0.1f) {
                    System.out.println("绿色像素值:" + ndvi[i] + " 绿色像素坐标：" + i);
                    greenCount++;
                }
            }

            int percent = (int) (greenCount * 100f / (minH * minW));
            progress.setProgress(percent);
            tvDegree.setText("覆盖度：" + percent + "%");

            merge2Color(pixel1, ndvi, Color.RED, Color.GREEN);
            System.out.println("结束算");
            Bitmap bitmap = Bitmap.createBitmap(minW, minH, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixel1, 0, minW, 0, 0, minW, minH);
            Glide.with(this)
                    .applyDefaultRequestOptions(new RequestOptions().centerCrop())
                    .load(bitmap)
                    .into(iv3);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void merge2Color(int[] pixel, float[] ndvi, int color1, int color2) {
        int rT, gT, bT;
        for (int i = 0; i < pixel.length; i++) {
            final int r1 = (color1 & 0xff0000) >> 16, g1 = (color1 & 0xff00) >> 8, b1 = color1 & 0xff;
            final int r2 = (color2 & 0xff0000) >> 16, g2 = (color2 & 0xff00) >> 8, b2 = color2 & 0xff;
            rT = r1 + ((int) ((r2 - r1) * ((ndvi[i] + 1) / 2f)));
            gT = g1 + ((int) ((g2 - g1) * ((ndvi[i] + 1) / 2f)));
            bT = b1 + ((int) ((b2 - b1) * ((ndvi[i] + 1) / 2f)));
            pixel[i] = Color.rgb(rT, bT, gT);
        }
    }

    private void showPicByBoolean() {
        Glide.with(this)
                .applyDefaultRequestOptions(new RequestOptions().centerCrop())
                .load(showOrigin ? picPath1 : picPath2)
                .into(iv1);
        Glide.with(this)
                .applyDefaultRequestOptions(new RequestOptions().centerCrop())
                .load(showOrigin ? picPath2 : picPath1)
                .into(iv2);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (permissions != null && permissions.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initView();
                } else {
                    Toast.makeText(this, "未获取相关权限，无法进行计算", Toast.LENGTH_LONG).show();
                    hasPermission = false;
                }
            }
        }
    }
}
