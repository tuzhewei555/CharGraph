package com.company;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        //读图片文件
        BufferedImage src = ImageIO.read(new File("E:/MyPrograms/CharGraph/pic01.jpg"));

        //灰度滤镜
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
        ColorConvertOp op = new ColorConvertOp(cs, null);
        src = op.filter(src, null);

        int a = src.getRGB(1,1);
        Color color = new Color(a);

        System.out.println(color.getRed());
        System.out.println(color.getBlue());
        System.out.println(color.getGreen());
        System.out.println(color.getAlpha());

        ImageIO.write(src, "JPEG", new File("E:/MyPrograms/CharGraph/result.jpg"));

    }
}
