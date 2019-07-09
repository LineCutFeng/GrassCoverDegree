package com.lcf.nir_calculate.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PicUtils {

    /**
     * 矫正双目摄像头
     *
     * @param context
     * @param result1
     * @param result2
     * @param offset
     * @return
     */
    public static List<Bitmap> calibrateDualCamera(Context context, Bitmap result1, Bitmap result2, int offset) {
        System.out.println("开始提取");
        List<Mat> objectPoints = new ArrayList<>();//角点的实际物理坐标
        List<Mat> objectPoints1 = new ArrayList<>();//角点的实际物理坐标
        List<Mat> objectPoints2 = new ArrayList<>();//角点的实际物理坐标
        List<Mat> imagePoints1 = new ArrayList<>();//角点的图像坐标
        List<Mat> imagePoints2 = new ArrayList<>();//角点的图像坐标
        Size imageSize = new Size(640, 480);
        Size patSize = new Size(9, 6);
        double patLen = 25.0;
        double imgScale = 1.0;

        try {
            Mat src1 = new Mat();
            Utils.bitmapToMat(result1, src1);
            Mat src2 = new Mat();
            Utils.bitmapToMat(result2, src2);
            {
                for (int i = 0; i < 1; i++) {
                    Mat srcL = new Mat();
                    Mat srcR = new Mat();
                    Mat gray1 = new Mat();
                    Mat gray2 = new Mat();
                    MatOfPoint2f corners1 = new MatOfPoint2f();
                    MatOfPoint2f corners2 = new MatOfPoint2f();
                    Utils.bitmapToMat(BitmapFactory.decodeStream(context.getAssets().open("chess" + i + "_left.jpg")), srcL);
                    Utils.bitmapToMat(BitmapFactory.decodeStream(context.getAssets().open("chess" + i + "_right.jpg")), srcR);
                    Imgproc.cvtColor(srcL, gray1, Imgproc.COLOR_RGB2GRAY);
                    Imgproc.cvtColor(srcR, gray2, Imgproc.COLOR_RGB2GRAY);
                    boolean chessboardCorners1 = Calib3d.findChessboardCorners(gray1, patSize, corners1, Calib3d.CALIB_CB_ADAPTIVE_THRESH | Calib3d.CALIB_CB_FILTER_QUADS);
                    boolean chessboardCorners2 = Calib3d.findChessboardCorners(gray2, patSize, corners2, Calib3d.CALIB_CB_ADAPTIVE_THRESH | Calib3d.CALIB_CB_FILTER_QUADS);
                    if (chessboardCorners1 && chessboardCorners2) {
                        System.out.println("左右提取完成:index=" + i + " " + corners1.toList().size());
                        Imgproc.cornerSubPix(gray1, corners1, new Size(5, 5), new Size(-1, -1), new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 300, 0.01));
                        Imgproc.cornerSubPix(gray2, corners2, new Size(5, 5), new Size(-1, -1), new TermCriteria(TermCriteria.MAX_ITER + TermCriteria.EPS, 300, 0.01));
                        imagePoints1.add(corners1);
                        imagePoints2.add(corners2);
                        objectPoints.add(calRealPoint(patSize, patLen));
                        objectPoints1.add(calRealPoint(patSize, patLen));
                        objectPoints2.add(calRealPoint(patSize, patLen));
                    }
                }
            }
            Mat[] cameraMatrix = new Mat[]{Mat.eye(new Size(3, 3), CvType.CV_64F), Mat.eye(new Size(3, 3), CvType.CV_64F)};//相机的内参矩阵
            Mat[] distCoeffs = new Mat[]{Mat.eye(new Size(5, 1), CvType.CV_64F), Mat.eye(new Size(5, 1), CvType.CV_64F)};//相机的畸变参数
            List<Mat>[] rvecs = new List[]{new ArrayList<>(), new ArrayList<>()};//旋转矢量(外参数)
            List<Mat>[] tvecs = new List[]{new ArrayList<>(), new ArrayList<>()};//平移矢量(外参数）
            Calib3d.calibrateCamera(objectPoints1, imagePoints1, imageSize, cameraMatrix[0], distCoeffs[0], rvecs[0], tvecs[0], Calib3d.CALIB_FIX_K3);
            Calib3d.calibrateCamera(objectPoints2, imagePoints2, imageSize, cameraMatrix[1], distCoeffs[1], rvecs[1], tvecs[1], Calib3d.CALIB_FIX_K3);


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


//            Mat[] cameraMatrix = new Mat[]{Mat.eye(new Size(3, 3), CvType.CV_64F), Mat.eye(new Size(3, 3), CvType.CV_64F)};//相机的内参矩阵
//            Mat[] distCoeffs = new Mat[]{Mat.eye(new Size(5, 1), CvType.CV_64F), Mat.eye(new Size(5, 1), CvType.CV_64F)};//相机的畸变参数
//            Mat R = new Mat(3, 3, CvType.CV_64FC1);
//            Mat T = new Mat(1, 3, CvType.CV_64FC1);

            {
                double[][][] cameraArray = new double[][][]{
                        {
                                {591.7003546879771, 0.0, 298.69574852625595},
                                {0.0, 591.5899685070495, 206.1120051364847},
                                {0.0, 0.0, 1.0},
                        },
                        {
                                {598.4206627473541, 0.0, 328.5788779504157},
                                {0.0, 598.2210879757324, 195.90708769694876},
                                {0.0, 0.0, 1.0},
                        }
                };
                for (int i = 0; i < cameraMatrix.length; i++) {
                    for (int i1 = 0; i1 < 3; i1++) {
                        for (int i2 = 0; i2 < 3; i2++) {
                            int put = cameraMatrix[i].put(i1, i2, new double[]{cameraArray[i][i1][i2]});
                            System.out.println(put);
                        }
                    }
                }

                double[][][] dist = new double[][][]{
                        {
                                {0.004045538804547804, 0.14406226762879215, -0.0037431600102399297, 0.002829414069931479, 0.20637962419970457}
                        },
                        {
                                {0.02134807761291104, -0.04010339987136857, -0.0010387141810351802, 0.00877366849309704, 0.5034542711715101}
                        }
                };
                for (int i = 0; i < distCoeffs.length; i++) {
                    for (int i1 = 0; i1 < 5; i1++) {
                        int put = distCoeffs[i].put(0, i1, new double[]{dist[i][0][i1]});
                        System.out.println(put);
                    }
                }

            }

            {

                double[][] RArray = new double[][]{
                        {0.9995738051575416, -0.01841833481500854, -0.022648906938642452},
                        {0.018301540474601523, 0.9998181815634215, -0.005353263627881041},
                        {0.022743387151643972, 0.004936472207489476, 0.9997291481111347}
                };
                for (int i1 = 0; i1 < 3; i1++) {
                    for (int i2 = 0; i2 < 3; i2++) {
                        int put = R.put(i1, i2, new double[]{RArray[i1][i2]});
                        System.out.println(put);
                    }
                }

                double[][] TArray = new double[][]{
                        {17.88820001445013},
                        {-1.3872845041127477},
                        {1.281559974162247}
                };
                for (int i1 = 0; i1 < 3; i1++) {
                    for (int i2 = 0; i2 < 1; i2++) {
                        int put = T.put(i1, i2, new double[]{TArray[i1][i2]});
                        System.out.println(put);
                    }
                }
            }


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
                    Calib3d.CALIB_ZERO_DISPARITY,/*0,*/
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


            result1 = Bitmap.createBitmap(dst1.width(), dst1.height(), Bitmap.Config.ARGB_8888);
            result2 = Bitmap.createBitmap(dst1.width(), dst2.height(), Bitmap.Config.ARGB_8888);
//            StereoSGBM stereoSGBM = new StereoSGBM(-16, 64, 5);
//            Mat disp = new Mat();
//            Mat disp8 = new Mat();
//            stereoSGBM.set_preFilterCap(31);
//            stereoSGBM.compute(dst1, dst2, disp);
//            disp.convertTo(disp8, CvType.CV_8U, 255 / (64 * 16.0));
            Utils.matToBitmap(dst1, result1);
            int R1 = 1;
            while (hasNoNullPixel(result1, R1)) {
                R1++;
            }
            R1--;
            System.out.println(R1);
            Utils.matToBitmap(dst2, result2);
            int R2 = 1;
            while (hasNoNullPixel(result2, R2)) {
                R2++;
            }
            R2--;
            System.out.println(R2);
            int RMin = Math.min(R1, R2);
            result1 = Bitmap.createBitmap(result1, result1.getWidth() / 2 - 19 + offset - RMin, result1.getHeight() / 2 - RMin, 2 * RMin + 1, 2 * RMin + 1, null, false);
            result2 = Bitmap.createBitmap(result2, result2.getWidth() / 2 - RMin, result2.getHeight() / 2 + 1 - RMin, 2 * RMin + 1, 2 * RMin + 1, null, false);
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            bitmaps.add(result1);
            bitmaps.add(result2);
            System.out.println("图像显示完毕");
            return bitmaps;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 添加实际空间坐标点
     *
     * @param patSize
     * @param patLen
     * @return
     */
    private static MatOfPoint3f calRealPoint(Size patSize, double patLen) {
        MatOfPoint3f matOfPoint3f = new MatOfPoint3f();
        List<Point3> point3List = new ArrayList<>();
        for (int y = 0; y < ((int) patSize.height); y++) {
            for (int x = 0; x < ((int) patSize.width); x++) {
                point3List.add(new Point3(x * patLen, y * patLen, 0));
            }
        }
        matOfPoint3f.fromList(point3List);
        return matOfPoint3f;
    }

    /**
     * 计算空间匹配像素
     *
     * @param sr1
     * @param src
     * @return
     */
    private static int translateDistance(Bitmap sr1, Bitmap src) {
        try {
            Mat src1 = new Mat();
            Utils.bitmapToMat(sr1, src1);
            Mat src2 = new Mat();
            Utils.bitmapToMat(src, src2);
            FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
            DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);

            MatOfKeyPoint keypoints1, keypoints2;
            Mat descriptors1, descriptors2;
            DescriptorMatcher descriptorMatcher;
            MatOfDMatch matches = new MatOfDMatch();
            keypoints1 = new MatOfKeyPoint();
            keypoints2 = new MatOfKeyPoint();
            descriptors1 = new Mat();
            descriptors2 = new Mat();

            //特征匹配算法
            detector = FeatureDetector.create(FeatureDetector.ORB);
            descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);


            //检测关键点
            detector.detect(src1, keypoints1);
            detector.detect(src2, keypoints2);
//            //添加变量，用于显示关键点数量
//            int keypointsObject1 = keypoints1.toArray().length;
//            int keypointsObject2 = keypoints2.toArray().length;
            //计算描述子
            descriptor.compute(src1, keypoints1, descriptors1);
            descriptor.compute(src2, keypoints2, descriptors2);


            descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);//基于FLANN匹配器
            descriptorMatcher.match(descriptors1, descriptors2, matches);
            List<DMatch> dMatches = new ArrayList<>(matches.toList());
            Collections.sort(dMatches, new Comparator<DMatch>() {
                @Override
                public int compare(DMatch o1, DMatch o2) {
                    return (int) (o1.distance - o2.distance);
                }
            });
//            double maxDistance = dMatches.get(dMatches.size() - 1).distance;

            List<DMatch> tenMatches = new ArrayList<>(dMatches.subList(0, 10));
            System.out.println("取到了");
            MatOfDMatch goodMatches = new MatOfDMatch();
            goodMatches.fromList(tenMatches);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean hasNoNullPixel(Bitmap result, int L) {
        if (L > result.getWidth() / 2 ||
                L > result.getHeight() / 2) {
            return false;
        }
        int centerX = result.getWidth() / 2;
        int centerY = result.getHeight() / 2;
        int minX = centerX - L;
        int maxX = centerX + L;
        int minY = centerY - L;
        int maxY = centerY + L;
        for (int x = minX; x <= maxX; x++) {
            if (result.getPixel(x, minY) == 0)
                return false;
            if (result.getPixel(x, maxY) == 0)
                return false;
        }
        for (int y = minY; y <= maxY; y++) {
            if (result.getPixel(minX, y) == 0)
                return false;
            if (result.getPixel(maxY, y) == 0)
                return false;
        }
        return true;
    }
}
