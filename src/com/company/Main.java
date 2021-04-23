package com.company;

import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main {
    static final int CHARBLOCK_WIDTH = 6;   //单位字符占用宽度
    static final int CHARBLOCK_HEIGHT = 8;  //单位字符占用高度

    //Pair<Integer, Integer>代表一个像素，即一个点；
    //Set<Pair<Integer, Integer>>代表一个字符包含的所有像素的坐标
    //List<Set<Pair<Integer, Integer>>>代表字符库内所有可用的字符
    private static List<Set<Pair<Integer, Integer>>> list;

    public static void main(String[] args) {
        //设置字符像素表
        readConfigPicture();

        //计算字符密度表
        //Map的key值Integer为字符密度，即字符包含的黑色像素的数量
        //Map的value值List<Set<Pair<Integer, Integer>>>为所有字符密度为key值的字符
        Map<Integer, List<Set<Pair<Integer, Integer>>>> densityMap = new HashMap<>();
        for (Set<Pair<Integer, Integer>> chard : list) {
            if (!densityMap.containsKey(chard.size())) {
                List<Set<Pair<Integer, Integer>>> tmp = new ArrayList<>();
                tmp.add(chard);
                densityMap.put(chard.size(), tmp);
            } else {
                densityMap.get(chard.size()).add(chard);
            }
        }
        System.out.println(densityMap.toString());

        //读图片文件
        BufferedImage srcOrigin = null;
        try {
            srcOrigin = ImageIO.read(new File("./pic01.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //压缩图片，仅在原图长度或宽度大于规定值时才压缩
        final int limitWidth = 2560;
        final int limitHeight = 1600;
        BufferedImage src;
        if (srcOrigin.getWidth() > limitWidth || srcOrigin.getHeight() > limitHeight) {
            //在等比压缩的前提下，使压缩后的图片越大越好（长或宽之中的一个达到极限值）
            int targetWidth, targetHeight;
            if (srcOrigin.getWidth() * limitHeight > srcOrigin.getHeight() * limitWidth) {
                targetWidth = limitWidth;
                targetHeight = Math.round(srcOrigin.getHeight() * limitWidth / srcOrigin.getWidth());
            } else {
                targetWidth = Math.round(srcOrigin.getWidth() * limitHeight / srcOrigin.getHeight());
                targetHeight = limitHeight;
            }
            src = new BufferedImage(targetWidth, targetHeight, srcOrigin.getType());
            src.getGraphics().drawImage(srcOrigin.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH), 0, 0, null);
        } else {
            src = srcOrigin;
        }

        //灰度滤镜
        src = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(src, null);

        //初始化像素矩阵,并计算灰度处理后的图片最深值和最浅值
        int[][] pixels = new int[src.getWidth()][src.getHeight()];
        int maxValue = 0;
        int minValue = 255;
        for (int x = 0; x < src.getWidth(); x++) {
            for (int y = 0; y < src.getHeight(); y++) {
                int pixelValue = src.getRGB(x, y) & 0xff;
                pixels[x][y] = pixelValue;
                maxValue = Math.max(pixelValue, maxValue);
                minValue = Math.min(pixelValue, minValue);
            }
        }
        System.out.println(maxValue);
        System.out.println(minValue);

        //计算字符密度表的最大和最小密度
        int charDensityMax = Collections.max(densityMap.keySet());
        int charDensityMin = Collections.min(densityMap.keySet());

        //初始化输出图片，全白
        BufferedImage output = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics graphics = output.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, output.getWidth(), output.getHeight());

        //像素=>字符转换
        for (int x = 0; x < src.getWidth() - CHARBLOCK_WIDTH; x += CHARBLOCK_WIDTH) {
            for (int y = 0; y < src.getHeight() - CHARBLOCK_HEIGHT; y += CHARBLOCK_HEIGHT) {
                //计算block区域平均像素值，并转换为字符密度
                int sum = 0;
                for (int i = x; i < x + CHARBLOCK_WIDTH; i++) {
                    for (int j = y; j < y + CHARBLOCK_HEIGHT; j++) {
                        sum += pixels[i][j];
                    }
                }
                float average = sum / (CHARBLOCK_HEIGHT * CHARBLOCK_WIDTH);
                float charDensity = charDensityMax - (charDensityMax - charDensityMin) * average / 255;

                //选取合适的字符
                //TODO:在所有可用字符中，选取字符像素分布和图片最相近的字符
                Set<Pair<Integer, Integer>> charDrawUsed;
                if (densityMap.containsKey(Math.round(charDensity))) {
                    charDrawUsed = densityMap.get(Math.round(charDensity)).get(0);
                } else {
                    int candidate = 0;
                    float currentBest = charDensityMax;
                    for (int n : densityMap.keySet()) {
                        if (Math.abs(n - charDensity) < currentBest) {
                            candidate = n;
                            currentBest = Math.abs(n - charDensity);
                        }
                    }
                    charDrawUsed = densityMap.get(candidate).get(0);
                }

                //将字符写入输出图片
                for (Pair<Integer, Integer> p : charDrawUsed) {
                    output.setRGB(x + p.getKey(), y + p.getValue(), 0);
                }

            }
        }

        try {
            ImageIO.write(output, "JPEG", new File("./result.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //把图片当作配置文件读取，获取字符库
    private static void readConfigPicture() {
        //初始化字符像素表
        if (list == null) {
            list = new ArrayList<Set<Pair<Integer, Integer>>>();
        } else {
            list.clear();
        }
        //读配置文件（它是个图片）
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("./EnjoyDrawingIt.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int x = 0; x < img.getWidth() - CHARBLOCK_WIDTH - 1; x += (CHARBLOCK_WIDTH + 1)) {
            for (int y = 0; y < img.getHeight() - CHARBLOCK_HEIGHT - 1; y += (CHARBLOCK_HEIGHT + 1)) {
                //读取block里画的字符
                Set<Pair<Integer, Integer>> charDraw = new HashSet<>();
                for (int i = x; i < x + CHARBLOCK_WIDTH; i++) {
                    for (int j = y; j < y + CHARBLOCK_HEIGHT; j++) {
                        //如果该像素是偏黑的话，纳入统计（因为是用画图软件画的，好像没法设置成纯黑也就是像素值0）
                        if ((img.getRGB(i, j) & 0xff) < 100) {
                            charDraw.add(new Pair<Integer, Integer>(i - x, j - y));
                        }
                    }
                }
                if (!charDraw.isEmpty()) {
                    list.add(charDraw);
                }
            }
        }
        //最后再加上空白的情况
        list.add(new HashSet<>());
    }
}
