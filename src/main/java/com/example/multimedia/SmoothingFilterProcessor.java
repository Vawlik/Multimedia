package com.example.multimedia;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class SmoothingFilterProcessor implements ImageProcessor {

    @Override
    public Image process(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();

        WritableImage outputImage = new WritableImage(width, height);
        PixelReader pixelReader = inputImage.getPixelReader();
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double red = 0, green = 0, blue = 0;

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        Color color = pixelReader.getColor(x + dx, y + dy);
                        red += color.getRed();
                        green += color.getGreen();
                        blue += color.getBlue();
                    }
                }

                red /= 9;
                green /= 9;
                blue /= 9;

                pixelWriter.setColor(x, y, new Color(red, green, blue, 1.0));
            }
        }

        return outputImage;
    }
}
