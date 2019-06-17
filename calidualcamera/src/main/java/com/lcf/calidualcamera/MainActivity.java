package com.lcf.calidualcamera;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        staticLoadCVLibraries();
    }

    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.i("CV", "Open CV Libraries loaded...");
            distinctConerPoint();
        }
    }

    private void distinctConerPoint() {
        Size imgSize = new Size(640, 480);
        Size patSize = new Size(9, 6);
        double patLen = 25;
        double imgScale = 1.0;
        try {
            Mat src = new Mat();
            Mat temp = new Mat();
            MatOfPoint2f matOfPoint = new MatOfPoint2f();
            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("chess_right.jpg")), src);
            Imgproc.cvtColor(src, temp, Imgproc.COLOR_RGB2GRAY);
            Calib3d.findChessboardCorners(temp, patSize, matOfPoint);
            System.out.println("提取完成");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
