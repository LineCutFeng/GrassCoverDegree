package com.lcf.calidualcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.iv);
        staticLoadCVLibraries();
    }

    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if (load) {
            Log.i("CV", "Open CV Libraries loaded...");
            distinctConerPoint();
//            distinctMatchPoint();
        }
    }

//    private void distinctMatchPoint() {
//        try {
//            Mat src1 = new Mat();
//            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("1_left.jpg")), src1);
//            Mat src2 = new Mat();
//            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("1 _right.jpg")), src2);
//            FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
//            DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
//
//            MatOfKeyPoint keypoints1, keypoints2;
//            Mat descriptors1, descriptors2;
//            DescriptorMatcher descriptorMatcher;
//            MatOfDMatch matches = new MatOfDMatch();
//            keypoints1 = new MatOfKeyPoint();
//            keypoints2 = new MatOfKeyPoint();
//            descriptors1 = new Mat();
//            descriptors2 = new Mat();
//
//            //特征匹配算法
//            detector = FeatureDetector.create(FeatureDetector.ORB);
//            descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
//
//
//            //检测关键点
//            detector.detect(src1, keypoints1);
//            detector.detect(src2, keypoints2);
//            //添加变量，用于显示关键点数量
//            int keypointsObject1 = keypoints1.toArray().length;
//            int keypointsObject2 = keypoints2.toArray().length;
//            //计算描述子
//            descriptor.compute(src1, keypoints1, descriptors1);
//            descriptor.compute(src2, keypoints2, descriptors2);
//
//
//            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);//基于FLANN匹配器
//            descriptorMatcher.match(descriptors1, descriptors2, matches);
//            List<DMatch> dMatches = new ArrayList<>(matches.toList());
//            Collections.sort(dMatches, new Comparator<DMatch>() {
//                @Override
//                public int compare(DMatch o1, DMatch o2) {
//                    return (int) (o1.distance - o2.distance);
//                }
//            });
//            double maxDistance = dMatches.get(dMatches.size() - 1).distance;
//            Iterator<DMatch> iterator = dMatches.iterator();
//            while (iterator.hasNext()) {
//                DMatch next = iterator.next();
//                if (next.distance > 10) {
//                    iterator.remove();
//                }
//            }
//            matches.fromList(dMatches);
//            Mat outImg = new Mat();
//            Features2d.drawMatches(src1, keypoints1, src2, keypoints2, matches, outImg, Scalar.all(-1), Scalar.all(-1), new MatOfByte(), 4);
//
//            Bitmap bitmap = Bitmap.createBitmap(outImg.width(), outImg.height(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(outImg, bitmap);
//            System.out.println("特征提取完成");
//            Glide.with(this)
//                    .load(bitmap)
//                    .into(imageView);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void distinctConerPoint() {
        Size imgSize = new Size(640, 480);
        Size patSize = new Size(9, 6);
        double patLen = 25;
        double imgScale = 1.0;
        try {
            Mat src = new Mat();
            Mat temp = new Mat();
            MatOfPoint2f matOfPoint = new MatOfPoint2f();
            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("chess4.jpg")), src);
            Imgproc.cvtColor(src, temp, Imgproc.COLOR_RGB2GRAY);
            Calib3d.findChessboardCorners(temp, patSize, matOfPoint);
            System.out.println("提取完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
