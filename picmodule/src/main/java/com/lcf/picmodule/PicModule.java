package com.lcf.picmodule;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class PicModule extends JFrame {

    public static void main(String[] args) {
        renameDualPic("C:\\Users\\PC\\Desktop\\chess\\new");
//        renameChessPic("C:\\Users\\PC\\Desktop\\chess\\new");
//        cutChessPic("C:\\Users\\PC\\Desktop\\chess\\new", 1, 2);
    }

    public static void renameChessPic(String path) {
        File filePath = new File(path);
        if (filePath.exists() && filePath.isDirectory()) {
            File[] files = filePath.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                file.renameTo(new File(path + "\\chess" + i + ".jpg"));
            }
        }
    }

    public static void renameDualPic(String path) {
        File filePath = new File(path);
        if (filePath.exists() && filePath.isDirectory()) {
            File[] files = filePath.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                File outPutDir = new File(path + "\\sample" + (i + 21));
                if (!outPutDir.exists()) {
                    outPutDir.mkdirs();
                }
                file.renameTo(new File(outPutDir + "\\sample_couple.jpg"));
            }
        }
    }

    public static void cutChessPic(String path, int rows, int cols) {
        int chunks = rows * cols;
        File filePath = new File(path);
        if (filePath.exists() && filePath.isDirectory()) {
            for (File file : filePath.listFiles()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    int count = 0;
                    BufferedImage[] imgs = new BufferedImage[chunks];
                    BufferedImage image = ImageIO.read(fis);
                    int chunkWidth = image.getWidth() / cols;
                    int chunkHeight = image.getHeight() / rows;
                    for (int x = 0; x < rows; x++) {
                        for (int y = 0; y < cols; y++) {
                            imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());

                            Graphics2D gr = imgs[count++].createGraphics();
                            gr.drawImage(image, 0, 0, chunkWidth, chunkHeight,
                                    chunkWidth * y, chunkHeight * x,
                                    chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);
                            gr.dispose();
                        }
                    }
                    File outPutPath = new File(path + File.separator + "complish");
                    if (!outPutPath.exists()) {
                        outPutPath.mkdirs();
                    }
                    for (int i = 0; i < imgs.length; i++) {
//                        ImageIO.write(imgs[i], "jpg", new File("C:\\Users\\liwj\\Desktop\\tidb\\image\\" + i + ".jpg"));
                        String regexString = file.getName().substring(0, file.getName().indexOf('.'));
                        if (i == 0) {
                            ImageIO.write(imgs[i], "jpg", new File(path + File.separator + "complish" + File.separator + regexString + "_left.jpg"));
                        } else if (i == 1) {
                            ImageIO.write(imgs[i], "jpg", new File(path + File.separator + "complish" + File.separator + regexString + "_right.jpg"));
                        }
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
