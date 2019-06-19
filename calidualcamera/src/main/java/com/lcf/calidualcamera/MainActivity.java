package com.lcf.calidualcamera;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.BOWImgDescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FastFeatureDetector;

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
//            distinctConerPoint();
            distinctMatchPoint();
        }
    }

    private void distinctMatchPoint() {
        try {
            Mat src1 = new Mat();
            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("14_left.jpg")), src1);
            Mat src2 = new Mat();
            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("14_right.jpg")), src2);
            FastFeatureDetector detector;
            MatOfKeyPoint keypoints1, keypoints2;
            BOWImgDescriptorExtractor descriptorExtractor;
            Mat descriptors1, descriptors2;
            DescriptorMatcher descriptorMatcher;
            MatOfDMatch matches = new MatOfDMatch();
            keypoints1 = new MatOfKeyPoint();
            keypoints2 = new MatOfKeyPoint();
            descriptors1 = new Mat();
            descriptors2 = new Mat();

            //特征匹配算法
            detector = FastFeatureDetector.create(FastFeatureDetector.THRESHOLD);
            descriptorExtractor = BOWImgDescriptorExtractor.__fromPtr__(detector.getType());


            //检测关键点
            detector.detect(src2, keypoints2);
            detector.detect(src1, keypoints1);
            //添加变量，用于显示关键点数量
//            int keypointsObject1 = keypoints1.toArray().length;
//            int keypointsObject2 = keypoints2.toArray().length;
            //计算描述子
            descriptorExtractor.compute(src1, keypoints1, descriptors1);
            descriptorExtractor.compute(src2, keypoints2, descriptors2);


            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);//基于FLANN匹配器
//            descriptorMatcher.match();
            System.out.println("特征提取完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void distinctConerPoint() {
//        Size imgSize = new Size(640, 480);
//        Size patSize = new Size(9, 6);
//        double patLen = 25;
//        double imgScale = 1.0;
//        try {
//            Mat src = new Mat();
//            Mat temp = new Mat();
//            MatOfPoint2f matOfPoint = new MatOfPoint2f();
//            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("chess_right.jpg")), src);
//            Imgproc.cvtColor(src, temp, Imgproc.COLOR_RGB2GRAY);
//            Calib3d.findChessboardCorners(temp, patSize, matOfPoint);
//            System.out.println("提取完成");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
