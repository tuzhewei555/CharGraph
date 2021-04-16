package com.company;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) throws IOException {
        //设置字符像素表
        List<Set<Pair<Integer,Integer>>> list = new ArrayList<Set<Pair<Integer,Integer>>>();
        Set<Pair<Integer,Integer>> charDraw = new HashSet<Pair<Integer,Integer>>();
        charDraw.add(new Pair<Integer, Integer>(1,1));
        list.add(charDraw);
        Set<Pair<Integer,Integer>> charDraw2 = new HashSet<Pair<Integer,Integer>>();
        charDraw.add(new Pair<Integer, Integer>(1,1));
        charDraw.add(new Pair<Integer, Integer>(3,3));
        charDraw.add(new Pair<Integer, Integer>(5,5));
        list.add(charDraw2);

        //读图片文件
        BufferedImage src = ImageIO.read(new File("E:/MyPrograms/CharGraph/pic01.jpg"));

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

        for(int x=0;x<src.getWidth();x++){
            for(int y=0;y<src.getHeight();y++){
                pixels[x][y]=src.getRGB(x,y) & 0xff;

            }
        }

        //计算灰度处理后的图片最深值和最浅值


        ImageIO.write(src, "JPEG", new File("E:/MyPrograms/CharGraph/result.jpg"));

    }
}
