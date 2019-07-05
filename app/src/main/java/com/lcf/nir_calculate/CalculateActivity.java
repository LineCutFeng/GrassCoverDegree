package com.lcf.nir_calculate;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.lcf.nir_calculate.utils.PicUtils;

import org.opencv.android.OpenCVLoader;

import java.lang.ref.WeakReference;
import java.util.List;

public class CalculateActivity extends AppCompatActivity {

    String[] premission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final int REQUEST_PERMISSION = 101;
    //    String picPath1 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/band3.png";
    String dirTempPath = "sample10";
//    String picPath1 = "file:///android_asset/" + dirTempPath + "/sample_in.jpg";
//    //    String picPath2 = Environment.getExternalStorageDirectory().getAbsolutePath() + "/band4.png";
//    String picPath2 = "file:///android_asset/" + dirTempPath + "/sample_out.jpg";

    Bitmap result1 = null;
    Bitmap result2 = null;

    boolean hasPermission = false;
    private ImageView iv0;
    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private TextView tvDegree;
    private ProgressBar progress;

    boolean showNotReverse = true;

    MyHandler myHandler = new MyHandler(this);

    class MyHandler extends Handler {
        WeakReference<Activity> activityWeakReference;

        public MyHandler(Activity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cal);
        String path = getIntent().getStringExtra("path");
        if (!TextUtils.isEmpty(path)) {
            dirTempPath = path;
        }
        iv0 = findViewById(R.id.iv0);
        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);
        tvDegree = findViewById(R.id.tv_degree);
        progress = findViewById(R.id.pb_degree);
        myHandler.post(new Runnable() {
            @Override
            public void run() {
                if (myHandler.activityWeakReference.get() != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if ((ContextCompat.checkSelfPermission(CalculateActivity.this, premission[0]) != PackageManager.PERMISSION_GRANTED) ||
                                (ContextCompat.checkSelfPermission(CalculateActivity.this, premission[1]) != PackageManager.PERMISSION_GRANTED)) {
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
            }
        });
    }


    private void initView() {
        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotReverse = !showNotReverse;
                showPicByBoolean();
            }
        });
        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotReverse = !showNotReverse;
                showPicByBoolean();
            }
        });
        staticLoadCVLibraries();
    }

    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.i("CV", "Open CV Libraries loaded...");
            loadImg();
        }
    }

    private void loadImg() {
        try {
            result1 = BitmapFactory.decodeStream(getAssets().open(dirTempPath + "/sample_in.jpg"));
            result2 = BitmapFactory.decodeStream(getAssets().open(dirTempPath + "/sample_out.jpg"));
            showPicByBoolean();
            List<Bitmap> bitmaps = PicUtils.calibrateDualCamera(this, result1, result2);
            if (bitmaps != null && bitmaps.size() == 2) {
                result1 = bitmaps.get(0);
                result2 = bitmaps.get(1);
                showPicByBoolean();
                startCalculate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCalculate() {
        try {
            double greenCount = 0;
            int minW = Math.min(result1.getWidth(), result2.getWidth());
            int minH = Math.min(result1.getHeight(), result2.getHeight());
            int[] pixel1 = new int[minW * minH];
            int[] pixel2 = new int[minW * minH];
            float[] ndvi = new float[minW * minH];
            result1.getPixels(pixel1, 0, minW, 0, 0, minW, minH);
            result2.getPixels(pixel2, 0, minW, 0, 0, minW, minH);
            System.out.println("开始算");
            for (int i = 0; i < pixel1.length; i++) {
                final int r1 = (pixel1[i] & 0xff0000) >> 16, g1 = (pixel1[i] & 0xff00) >> 8, b1 = pixel1[i] & 0xff;
//                final float gray1 = 0.299f * r1 + 0.578f * g1 + 0.114f * b1;
                final float gray1 = (r1 + g1 + b1) / 3;
                final int r2 = (pixel2[i] & 0xff0000) >> 16, g2 = (pixel2[i] & 0xff00) >> 8, b2 = pixel2[i] & 0xff;
                final float gray2 = (r2 + g2 + b2) / 3;
                if (gray1 == 0 && gray2 == 0) {
                    ndvi[i] = 0;
                } else {
                    ndvi[i] = (gray2 - gray1) / (gray2 + gray1);
                }
//                float[] clone = ndvi.clone();
//
//                if (ndvi[i] >= 0.10) {
////                    System.out.println("绿色像素值:" + ndvi[i] + " 绿色像素坐标：" + i);
//                    greenCount++;
//                } else if (ndvi[i] >= 0.05) {
//                    System.out.println("绿色像素值:" + ndvi[i] + " 绿色像素坐标：" + i);
//                    greenCount += ((ndvi[i] - 0.05) / (0.10 - 0.05));
//                } else {
//
//                }
            }

//            opencv膨胀处理(步骤繁琐，不会膨胀的同时，)
//            {
//                float[] ndviClone = new float[minW * minH];
//                for (int i = 0; i < ndviClone.length; i++) {
//                    if (ndviClone[i] < 0.05) {
//                        ndviClone[i] = 0;
//                    }
//                }
//
//                Mat src = new Mat(minW, minH, CvType.CV_64F);
//                Mat dst = new Mat(minW, minH, CvType.CV_64F);
//                for (int i = 0; i < ndviClone.length; i++) {
//                    src.put(i / minW, i % minW, new double[]{ndviClone[i]});
//                }
//                Mat kernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(50, 50));
//                Imgproc.dilate(src, dst, kernal, new Point(-1, -1), 10);
//                ndviClone = null;
//                System.gc();
//                for (int i = 0; i < ndvi.length; i++) {
//                    if (ndvi[i] < 0.05) {
//                        double valueDilate = dst.get(i / minW, i % minW)[0];
//                        if (valueDilate != ndvi[i]) {
//                            ndvi[i] = (float) valueDilate;
//                        }
//                    }
//                }
//            }

            /**
             * 膨胀卷积算子（九宫格八邻域形态）(或者更多的邻域)
             */
            int[][] core = {
                    {1, 1, 1},
                    {1, 1, 1},
                    {1, 1, 1},
            };
            MyDilate(ndvi, minW, minH, core);


            merge2Color(pixel1, ndvi, Color.rgb(239, 200, 143), Color.parseColor("#2abb46"));
//            merge2Color(pixel1, ndvi, Color.BLACK, Color.parseColor("#2abb46"));
            System.out.println("结束膨胀处理");
            Bitmap bitmap = Bitmap.createBitmap(minW, minH, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixel1, 0, minW, 0, 0, minW, minH);

            {
//                Mat src = new Mat();
//                Utils.bitmapToMat(bitmap, src);
//                Mat dst = new Mat();
//                Mat kernal = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
//                Imgproc.dilate(src, dst, kernal, new Point(-1, -1), 1);
//                Utils.matToBitmap(dst, bitmap);
            }

            Glide.with(this)
                    .applyDefaultRequestOptions(new RequestOptions().centerCrop())
                    .load(bitmap)
                    .into(iv3);

            bitmap.getPixels(pixel1, 0, minW, 0, 0, minW, minH);
            for (int i = 0; i < pixel1.length; i++) {
                if (ndvi[i] >= 0.10) {
//                    System.out.println("绿色像素值:" + ndvi[i] + " 绿色像素坐标：" + i);
                    greenCount++;
                } else if (ndvi[i] >= 0.05) {
//                    System.out.println("绿色像素值:" + ndvi[i] + " 绿色像素坐标：" + i);
                    greenCount += ((ndvi[i] - 0.05) / (0.10 - 0.05));
                } else {

                }
            }

            int percent = (int) (greenCount * 100.0 / (minH * minW));
            progress.setProgress(percent);
            tvDegree.setText("覆盖度：" + percent + "%");
            showPicByBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * |●|◯|◯|    |◯|●|◯|   |◯|◯|●|   |◯|◯|◯|  |◯|◯|◯|
     * |◯|⦿|◯|    |◯|⦿|◯|   |◯|⦿|◯|   |●|⦿|◯|   |◯|⦿|◯|
     * |◯|◯|◯|   |◯|◯|◯|  |◯|◯|◯|  |◯|◯|◯|  |◯|●|◯|.
     * 不仅仅以上几种的，中心像素，如果周围八邻域空间有符合要求的像素，就把中心像素取值为符合像素的最大值。
     * （为什么不用opencv膨胀，因为opencv需要先生成符合像素的初图，然后进行膨胀，然后根据膨胀以后的图，来算覆盖度，而且不会自定义膨胀的需求）
     *
     * @param ndvi ndvn数组
     * @param minW 图片长度
     * @param minH 图片宽度
     * @param core 膨胀卷积算子
     */
    private void MyDilate(float[] ndvi, int minW, int minH, int[][] core) {
        int pompSizo = 0;
        float[] ndviCopy = ndvi.clone();
        //以膨胀算子中心和图片每一个像素来重合，依次往右，往下平移，算中心附近的8个像素点的值
        for (int i = core.length / 2; i < minH - 1; i++) {
            for (int j = core.length / 2; j < minW - 1; j++) {
                float max = Float.MIN_VALUE;
                int startY = i - core.length / 2;
                int startX = j - core.length / 2;
                //筛选出超出图片边界的像素
                if ((startY >= 0 && (startY + core.length) < minH) &&
                        (startX >= 0 && (startX + core[0].length) < minW)) {
                    //遍历邻域
                    for (int i1 = 0; i1 < core.length; i1++) {
                        for (int i2 = 0; i2 < core[0].length; i2++) {
                            float value = 0;
                            try {
                                value = ndvi[(startY + i1) * minW + (startX + i2)];
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                            //邻域NDVI大于0.05才算有效邻域值
                            if (value > 0.12) {
                                if (max == Float.MIN_VALUE || value < max) {
                                    max = value;
                                }
                            }
                        }
                    }
                    //只膨胀NDVN小于0.1的部分
                    if (max != Float.MIN_VALUE) {
                        if (ndvi[i * minW + j] < max ||
                                ndvi[i * minW + j] < 0.12) {
                            pompSizo++;
                            ndviCopy[i * minW + j] = max;
                        }
                    }
                }
            }
        }
        System.out.println("膨胀的像素个数为：" + pompSizo);
        System.arraycopy(ndviCopy, 0, ndvi, 0, ndviCopy.length);
    }


    private void merge2Color(int[] pixel, float[] ndvi, int color1, int color2) {
        int rT, gT, bT;
        for (int i = 0; i < pixel.length; i++) {
            if (ndvi[i] >= 0.15f) {
                pixel[i] = color2;
            } else if (ndvi[i] >= 0.1) {
                final int r1 = (color1 & 0xff0000) >> 16, g1 = (color1 & 0xff00) >> 8, b1 = (color1 & 0xff);
                final int r2 = (color2 & 0xff0000) >> 16, g2 = (color2 & 0xff00) >> 8, b2 = (color2 & 0xff);
                rT = r1 + ((int) ((r2 - r1) * ((ndvi[i] + 1.33) / 2f)));
                gT = g1 + ((int) ((g2 - g1) * ((ndvi[i] + 1.33) / 2f)));
                bT = b1 + ((int) ((b2 - b1) * ((ndvi[i] + 1.33) / 2f)));
                pixel[i] = Color.rgb(rT, gT, bT);
            } else if (ndvi[i] > 0.05) {
                final int r1 = (color1 & 0xff0000) >> 16, g1 = (color1 & 0xff00) >> 8, b1 = (color1 & 0xff);
                final int r2 = (color2 & 0xff0000) >> 16, g2 = (color2 & 0xff00) >> 8, b2 = (color2 & 0xff);
                rT = r1 + ((int) ((r2 - r1) * ((ndvi[i] + 0.67) / 2f)));
                gT = g1 + ((int) ((g2 - g1) * ((ndvi[i] + 0.67) / 2f)));
                bT = b1 + ((int) ((b2 - b1) * ((ndvi[i] + 0.67) / 2f)));
                pixel[i] = Color.rgb(rT, gT, bT);
            } else {
                pixel[i] = color1;
            }
        }
    }

    private void showPicByBoolean() {
        Glide.with(this)
                .applyDefaultRequestOptions(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE))
                .load("file:///android_asset/" + dirTempPath + "/sample_origin.jpg")
                .into(iv0);
        Glide.with(this)
                .applyDefaultRequestOptions(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE))
                .load(showNotReverse ? result1 : result2)
                .into(iv1);
        Glide.with(this)
                .applyDefaultRequestOptions(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE))
                .load(showNotReverse ? result2 : result1)
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

    @Override
    protected void onDestroy() {
        myHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
