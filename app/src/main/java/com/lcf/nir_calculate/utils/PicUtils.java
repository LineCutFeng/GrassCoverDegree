package com.lcf.nir_calculate.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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

public class PicUtils {
    public static List<Bitmap> calibrateDualCamera(Context context, Bitmap result1, Bitmap result2) {
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
                                {586.0564078625439, 0.0, 295.3086849227241},
                                {0.0, 585.4118048897083, 206.3912604805205},
                                {0.0, 0.0, 1.0},
                        },
                        {
                                {590.8272115948905, 0.0, 328.63509506971565},
                                {0.0, 590.1454285679533, 195.60953766537318},
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
                                {-0.002591520208037512, 0.3815664120916118, -0.001992809428458405, 0.0021681190102660836, -1.2682380299294251}
                        },
                        {
                                {0.09278449522151463, -0.910400548216305, 7.240862105747525E-4, -2.6696772582669617E-4, 3.6944628764557774}
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
                        {0.9998455917030901, -0.017222784287587903, 0.0034883310165313274},
                        {0.017238701025919574, 0.9998408881248751, -0.004585370279479617},
                        {-0.003408803138440045, 0.00464479655573748, 0.9999834028253265},
                };
                for (int i1 = 0; i1 < 3; i1++) {
                    for (int i2 = 0; i2 < 3; i2++) {
                        int put = R.put(i1, i2, new double[]{RArray[i1][i2]});
                        System.out.println(put);
                    }
                }

                double[][] TArray = new double[][]{
                        {17.409579295631502},
                        {-0.16005063237580344},
                        {1.726390043161054}
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
            result1 = Bitmap.createBitmap(result1, result1.getWidth() / 2 - RMin, result1.getHeight() / 2 - RMin, 2 * RMin + 1, 2 * RMin + 1, null, false);
            result2 = Bitmap.createBitmap(result2, result2.getWidth() / 2 - RMin - 5, result2.getHeight() / 2 - RMin, 2 * RMin + 1, 2 * RMin + 1, null, false);
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
