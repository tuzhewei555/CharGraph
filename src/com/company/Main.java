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
    static final int CHARBLOCK_WIDTH = 6;
    static final int CHARBLOCK_HEIGHT = 8;

    public static void main(String[] args) throws IOException {
        //设置字符像素表
        //Pair<Integer, Integer>代表一个像素，即一个点；
        //Set<Pair<Integer, Integer>>代表一个字符包含的所有像素的坐标
        //List<Set<Pair<Integer, Integer>>>代表字符库内所有可用的字符
        List<Set<Pair<Integer, Integer>>> list = new ArrayList<Set<Pair<Integer, Integer>>>();
        Set<Pair<Integer, Integer>> charDraw = new HashSet<Pair<Integer, Integer>>();
        charDraw.add(new Pair<Integer, Integer>(1, 1));
        list.add(charDraw);
        Set<Pair<Integer, Integer>> charDraw2 = new HashSet<Pair<Integer, Integer>>();
        charDraw2.add(new Pair<Integer, Integer>(1, 1));
        charDraw2.add(new Pair<Integer, Integer>(3, 3));
        charDraw2.add(new Pair<Integer, Integer>(5, 5));
        list.add(charDraw2);
        Set<Pair<Integer, Integer>> charDraw3 = new HashSet<Pair<Integer, Integer>>();
        charDraw3.add(new Pair<Integer, Integer>(1, 1));
        charDraw3.add(new Pair<Integer, Integer>(2, 1));
        charDraw3.add(new Pair<Integer, Integer>(3, 1));
        charDraw3.add(new Pair<Integer, Integer>(4, 1));
        charDraw3.add(new Pair<Integer, Integer>(5, 1));
        charDraw3.add(new Pair<Integer, Integer>(1, 2));
        charDraw3.add(new Pair<Integer, Integer>(1, 3));
        charDraw3.add(new Pair<Integer, Integer>(1, 4));
        charDraw3.add(new Pair<Integer, Integer>(2, 4));
        charDraw3.add(new Pair<Integer, Integer>(3, 4));
        charDraw3.add(new Pair<Integer, Integer>(4, 4));
        charDraw3.add(new Pair<Integer, Integer>(5, 4));
        charDraw3.add(new Pair<Integer, Integer>(5, 5));
        charDraw3.add(new Pair<Integer, Integer>(5, 6));
        charDraw3.add(new Pair<Integer, Integer>(5, 7));
        charDraw3.add(new Pair<Integer, Integer>(4, 7));
        charDraw3.add(new Pair<Integer, Integer>(3, 7));
        charDraw3.add(new Pair<Integer, Integer>(2, 7));
        charDraw3.add(new Pair<Integer, Integer>(1, 7));
        list.add(charDraw3);

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
        BufferedImage src = ImageIO.read(new File("./pic01.jpg"));

        //压缩图片

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
                float charDensity = (charDensityMax - charDensityMin) * average / 255 + charDensityMin;

                //选取合适的字符
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
                    output.setRGB(x + p.getKey(), y + p.getValue(), 255);
                }

            }
        }

        ImageIO.write(output, "JPEG", new File("./result.jpg"));

    }
}
