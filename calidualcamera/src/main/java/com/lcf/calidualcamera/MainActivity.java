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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView1;
    private ImageView imageView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView1 = findViewById(R.id.iv1);
        imageView2 = findViewById(R.id.iv2);
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
        System.out.println("开始提取");
        List<Mat> objectPoints = new ArrayList<>();//角点的实际物理坐标
        List<Mat> imagePoints1 = new ArrayList<>();//角点的图像坐标
        List<Mat> imagePoints2 = new ArrayList<>();//角点的图像坐标
        Size imageSize = new Size(640, 480);
        Size patSize = new Size(9, 6);
        double patLen = 25.0;
        double imgScale = 1.0;
        try {
            Mat src1 = new Mat();
            Mat src2 = new Mat();
            Mat gray1 = new Mat();
            Mat gray2 = new Mat();
            MatOfPoint2f corners1 = new MatOfPoint2f();
            MatOfPoint2f corners2 = new MatOfPoint2f();
            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("chess0_left.jpg")), src1);
            Utils.bitmapToMat(BitmapFactory.decodeStream(getAssets().open("chess0_right.jpg")), src2);
            Imgproc.cvtColor(src1, gray1, Imgproc.COLOR_RGB2GRAY);
            Imgproc.cvtColor(src2, gray2, Imgproc.COLOR_RGB2GRAY);
            boolean chessboardCorners1 = Calib3d.findChessboardCorners(gray1, patSize, corners1, Calib3d.CALIB_CB_ADAPTIVE_THRESH | Calib3d.CALIB_CB_FILTER_QUADS);
            boolean chessboardCorners2 = Calib3d.findChessboardCorners(gray2, patSize, corners2, Calib3d.CALIB_CB_ADAPTIVE_THRESH | Calib3d.CALIB_CB_FILTER_QUADS);
            if (chessboardCorners1 && chessboardCorners2) {
                System.out.println("左右提取完成" + corners1.toList().size());
                Imgproc.cornerSubPix(gray1, corners1, new Size(5, 5), new Size(-1, -1), new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 300, 0.01));
                Imgproc.cornerSubPix(gray2, corners2, new Size(5, 5), new Size(-1, -1), new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 300, 0.01));
                imagePoints1.add(corners1);
                imagePoints2.add(corners2);
                MatOfPoint3f matOfPoint3f = new MatOfPoint3f();
                List<Point3> point3List = new ArrayList<>();
                for (int y = 0; y < ((int) patSize.height); y++) {
                    for (int x = 0; x < ((int) patSize.width); x++) {
                        point3List.add(new Point3(x * patLen, y * patLen, 0));
                    }
                }
                matOfPoint3f.fromList(point3List);
                objectPoints.add(matOfPoint3f);
                Mat[] cameraMatrix = new Mat[]{Mat.eye(new Size(3, 3), CvType.CV_64F), Mat.eye(new Size(3, 3), CvType.CV_64F)};//相机的内参矩阵
                Mat[] distCoeffs = new Mat[]{Mat.eye(new Size(3, 3), CvType.CV_64F), Mat.eye(new Size(3, 3), CvType.CV_64F)};//相机的畸变参数
                List<Mat>[] rvecs = new List[]{new ArrayList<>(), new ArrayList<>()};//旋转矢量(外参数)
                List<Mat>[] tvecs = new List[]{new ArrayList<>(), new ArrayList<>()};//平移矢量(外参数）
                Calib3d.calibrateCamera(objectPoints, imagePoints1, imageSize, cameraMatrix[0], distCoeffs[0], rvecs[0], tvecs[0], Calib3d.CALIB_FIX_K3);
                Calib3d.calibrateCamera(objectPoints, imagePoints2, imageSize, cameraMatrix[1], distCoeffs[1], rvecs[1], tvecs[1], Calib3d.CALIB_FIX_K3);
                System.out.println("摄像头单矫正完成");
                Mat R = new Mat();
                Mat T = new Mat();
                Mat E = new Mat();
                Mat F = new Mat();
                Calib3d.stereoCalibrate(
                        objectPoints,
                        imagePoints1,
                        imagePoints2,
                        cameraMatrix[0],
                        distCoeffs[0],
                        cameraMatrix[1],
                        distCoeffs[1],
                        imageSize,
                        R,
                        T,
                        E,
                        F,
                        new TermCriteria(
                                TermCriteria.MAX_ITER + TermCriteria.EPS,
                                100,
                                1e-5),
                        Calib3d.CALIB_USE_INTRINSIC_GUESS
                );
                System.out.println("摄像头标定完成");
                Mat Rl = new Mat();
                Mat Rr = new Mat();
                Mat Pl = new Mat();
                Mat Pr = new Mat();
                Mat Q = new Mat();
                Rect validROIL = new Rect();
                Rect validROIR = new Rect();
                Calib3d.stereoRectify(
                        cameraMatrix[0],
                        distCoeffs[0],
                        cameraMatrix[1],
                        distCoeffs[1],
                        imageSize,
                        R,
                        T,
                        Rl,
                        Rr,
                        Pl,
                        Pr,
                        Q,
                        Calib3d.CALIB_ZERO_DISPARITY,
                        -1,
                        new Size(640, 480),
                        validROIL, validROIR);
                Mat map1x = new Mat();
                Mat map1y = new Mat();
                Mat map2x = new Mat();
                Mat map2y = new Mat();
                System.out.println("矩阵计算完成");
                Imgproc.initUndistortRectifyMap(cameraMatrix[0], distCoeffs[0], Rl, Pl, imageSize, CvType.CV_32FC1, map1x, map1y);
                Imgproc.initUndistortRectifyMap(cameraMatrix[1], distCoeffs[1], Rr, Pr, imageSize, CvType.CV_32FC1, map2x, map2y);
                System.out.println("求得映射函数");
                Mat dst1 = new Mat();
                Mat dst2 = new Mat();
                Imgproc.remap(src1, dst1, map1x, map1y, Imgproc.INTER_LINEAR);
                Imgproc.remap(src2, dst2, map2x, map2y, Imgproc.INTER_LINEAR);
                System.out.println("图像校正完成");
                Bitmap result1 = Bitmap.createBitmap(dst1.width(), dst1.height(), Bitmap.Config.ARGB_8888);
                Bitmap result2 = Bitmap.createBitmap(dst1.width(), dst2.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dst1, result1);
                Utils.matToBitmap(dst2, result2);
                Glide.with(this)
                        .load(result1)
                        .into(imageView1);
                Glide.with(this)
                        .load(result1)
                        .into(imageView2);
                System.out.println("图像显示完毕");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
