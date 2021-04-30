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
        System.out.println("字符密度表:");
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

        //初始化输出图片，全白
        BufferedImage output = new BufferedImage(src.getWidth(), src.getHeight(), src.getType());
        Graphics graphics = output.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, output.getWidth(), output.getHeight());

        //灰度滤镜
        BufferedImage src_gray = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(src, null);

        //初始化像素矩阵,并计算灰度处理后的图片最深值和最浅值
        int[][] pixels = new int[src_gray.getWidth()][src_gray.getHeight()];
        int maxValue = 0;
        int minValue = 255;
        for (int x = 0; x < src_gray.getWidth(); x++) {
            for (int y = 0; y < src_gray.getHeight(); y++) {
                int pixelValue = src_gray.getRGB(x, y) & 0xff;
                pixels[x][y] = pixelValue;
                maxValue = Math.max(pixelValue, maxValue);
                minValue = Math.min(pixelValue, minValue);
            }
        }

        //计算字符密度表的最大和最小密度
        int charDensityMax = Collections.max(densityMap.keySet());
        int charDensityMin = Collections.min(densityMap.keySet());

        for (int x = CHARBLOCK_WIDTH-1; x < src.getWidth(); x += CHARBLOCK_WIDTH) {
            for(int y=0;y<src.getHeight();y++){
                if (pixels[x][y] >150) {
                    output.setRGB(x, y, src.getRGB(x, y));
                }
            }
        }
        for (int y = CHARBLOCK_HEIGHT-1; y < src.getHeight(); y += CHARBLOCK_HEIGHT) {
            for(int x=0;x<src.getWidth();x++){
                if (pixels[x][y] >150) {
                    output.setRGB(x, y, src.getRGB(x, y));
                }
            }
        }

        //像素=>字符转换
        for (int x = 0; x < src_gray.getWidth() - CHARBLOCK_WIDTH; x += CHARBLOCK_WIDTH) {
            for (int y = 0; y < src_gray.getHeight() - CHARBLOCK_HEIGHT; y += CHARBLOCK_HEIGHT) {
                //计算block区域平均像素值，并转换为字符密度
                int sum = 0;
                for (int i = x; i < x + CHARBLOCK_WIDTH; i++) {
                    for (int j = y; j < y + CHARBLOCK_HEIGHT; j++) {
                        sum += pixels[i][j];
                    }
                }
                float average = sum / (CHARBLOCK_HEIGHT * CHARBLOCK_WIDTH);
                float charDensity = charDensityMax - (charDensityMax - charDensityMin) * average / 255;

                //选取合适的字符密度
                List<Set<Pair<Integer, Integer>>> monoDensityChars;
                if (densityMap.containsKey(Math.round(charDensity))) {
                    monoDensityChars = densityMap.get(Math.round(charDensity));
                } else {
                    int candidate = 0;
                    float currentBestDensity = charDensityMax;
                    for (int n : densityMap.keySet()) {
                        if (Math.abs(n - charDensity) < currentBestDensity) {
                            candidate = n;
                            currentBestDensity = Math.abs(n - charDensity);
                        }
                    }
                    monoDensityChars = densityMap.get(candidate);
                }

                //在所有字符密度为currentBestDensity的字符中，选取字符像素分布和原图最接近的
                Set<Pair<Integer, Integer>> charDrawUsed;
                if (monoDensityChars.size() == 1) {
                    //当备选字符只有一个时，直接使用
                    charDrawUsed = monoDensityChars.get(0);
                } else {
                    //当有多个备选字符时，选取字符像素分布和原图最接近的
                    //计算每个候选字符的每个像素分别对应的图片像素值之和，当和最小时就是我们期望的字符
                    int currentMinValue = 255 * CHARBLOCK_HEIGHT * CHARBLOCK_WIDTH;
                    int currentBestCharIndex = 0;
                    for (int i = 0; i < monoDensityChars.size(); i++) {
                        int pixelSum = 0;
                        for (Pair<Integer, Integer> p : monoDensityChars.get(i)) {
                            pixelSum += pixels[x + p.getKey()][y + p.getValue()];
                        }
                        if (pixelSum < currentMinValue) {
                            currentBestCharIndex = i;
                            currentMinValue = pixelSum;
                        }
                    }
                    charDrawUsed = monoDensityChars.get(currentBestCharIndex);
                }

                //将字符写入输出图片
                for (Pair<Integer, Integer> p : charDrawUsed) {
                    Color c = new Color(src.getRGB(x + p.getKey(), y + p.getValue()));
                    output.setRGB(x + p.getKey(), y + p.getValue(), c.darker().getRGB());
                }

            }
        }

        try {
            ImageIO.write(output, "JPG", new File("./result.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("图片生成成功。");

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
            img = ImageIO.read(new File("./EnjoyDrawingIt.cfg"));
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
