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
        List<Set<Pair<Integer,Integer>>> list = new ArrayList<Set<Pair<Integer,Integer>>>();
        Set<Pair<Integer,Integer>> charDraw = new HashSet<Pair<Integer,Integer>>();
        charDraw.add(new Pair<Integer, Integer>(1,1));
        list.add(charDraw);
        Set<Pair<Integer,Integer>> charDraw2 = new HashSet<Pair<Integer,Integer>>();
        charDraw2.add(new Pair<Integer, Integer>(1,1));
        charDraw2.add(new Pair<Integer, Integer>(3,3));
        charDraw2.add(new Pair<Integer, Integer>(5,5));
        list.add(charDraw2);

        //计算字符密度表
        Map<Integer, List<Set<Pair<Integer,Integer>>>> densityMap = new HashMap<>();
        for (Set<Pair<Integer,Integer>> chard : list){
            if(!densityMap.containsKey(chard.size())){
                List<Set<Pair<Integer,Integer>>> tmp= new ArrayList<>();
                tmp.add(chard);
                densityMap.put(chard.size(),tmp);
            } else{
                densityMap.get(chard.size()).add(chard);
            }
        }
        System.out.println(densityMap.toString());

        //读图片文件
        BufferedImage src = ImageIO.read(new File("./pic01.jpg"));

        //灰度滤镜
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        src = op.filter(src, null);

        int a = src.getRGB(1,1);
        System.out.println( a & 0xff);
        Color color = new Color(a);
        System.out.println(color.getRed());
        System.out.println(color.getBlue());
        System.out.println(color.getGreen());
        System.out.println(color.getAlpha());

        //初始化像素矩阵,并计算灰度处理后的图片最深值和最浅值
        int [][] pixels = new int[src.getWidth()][src.getHeight()];
        int maxValue = 0;
        int minValue = 255;
        for(int x=0;x<src.getWidth();x++){
            for(int y=0;y<src.getHeight();y++){
                int pixelValue = src.getRGB(x,y) & 0xff;
                pixels[x][y]=pixelValue;
                maxValue = Math.max(pixelValue, maxValue);
                minValue = Math.min(pixelValue, minValue);
            }
        }
        System.out.println(maxValue);
        System.out.println(minValue);

        //计算字符密度表
        int charDensityMax = Collections.max(densityMap.keySet());
        int charDensityMin = Collections.min(densityMap.keySet());

        //像素=>字符转换
        for(int x=0;x<src.getWidth();x+=CHARBLOCK_WIDTH) {
            for (int y = 0; y < src.getHeight(); y += CHARBLOCK_HEIGHT) {
                
            }
        }

        ImageIO.write(src, "JPEG", new File("./result.jpg"));

    }
}
