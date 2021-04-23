package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.company.Main.*;

//这个class不用于生成字符画，而是生成Class Main需要的配置文件，因此不纳入总代码
public class CharInfoSupport {
    public static void main(String[] args) {
        //它的作用很简单，就是生成一个有一个个小方格的图片，方便人自己在上面画字符，有点像小学时候的练字簿
        BufferedImage output = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = output.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, output.getWidth(), output.getHeight());

        graphics.setColor(Color.ORANGE);
        for (int x = CHARBLOCK_WIDTH; x < 400; x += (CHARBLOCK_WIDTH + 1)) {
            graphics.drawLine(x, 0, x, 400);
        }
        for (int y = CHARBLOCK_HEIGHT; y < 400; y += (CHARBLOCK_HEIGHT + 1)) {
            graphics.drawLine(0, y, 400, y);
        }

        graphics.dispose();
        try {
            ImageIO.write(output, "JPEG", new File("./config.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
