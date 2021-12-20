import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageFile {
    public static int[][] getImageMatrix(String path, int vectorWidth, int vectorHeight) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(path));
            int width = image.getWidth();
            int height = image.getHeight();
            int widthRemainder = width % vectorWidth;
            int heightRemainder = height % vectorWidth;
            int modifiedWidth = widthRemainder == 0 ? width : width + (vectorWidth - widthRemainder);
            int modifiedHeight = heightRemainder == 0 ? height : height + (vectorHeight - heightRemainder);
            int[][] imageArray = new int[modifiedHeight][modifiedWidth];
            System.out.println("orig width: " + width);
            System.out.println("orig height: " + height);
            System.out.println("mod width: " + modifiedWidth);
            System.out.println("mod height: " + modifiedHeight);
            for (int i=0; i<modifiedWidth; i++) {
                for (int j=0; j<modifiedHeight; j++) {
                    if(i<width && j<height) {
                        float r = new Color(image.getRGB(i, j)).getRed();
                        float g = new Color(image.getRGB(i, j)).getGreen();
                        float b = new Color(image.getRGB(i, j)).getBlue();
                        int grayScaled = (int)(r+g+b)/3;
                        imageArray[j][i] = grayScaled;
                    }
                    else {
                        //Add black padding
                        imageArray[j][i] = 0;
                    }
                }
            }
            return imageArray;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
