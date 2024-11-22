package com.example.multimedia;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class GammaCorrectionProcessor implements ImageProcessor {
    private final double c;
    private final double gamma;

    public GammaCorrectionProcessor(double c, double gamma) {
        this.c = c;
        this.gamma = gamma;
    }

    @Override
    public Image process(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader pixelReader = inputImage.getPixelReader();
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                double r = color.getRed();
                double g = color.getGreen();
                double b = color.getBlue();

                double correctedRed = c * Math.pow(r, gamma);
                double correctedGreen = c * Math.pow(g, gamma);
                double correctedBlue = c * Math.pow(b, gamma);

                Color correctedColor = new Color(correctedRed, correctedGreen, correctedBlue, color.getOpacity());
                pixelWriter.setColor(x, y, correctedColor);
            }
        }

        return outputImage;
    }

}
