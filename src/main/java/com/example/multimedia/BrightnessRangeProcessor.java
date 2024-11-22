package com.example.multimedia;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class BrightnessRangeProcessor implements ImageProcessor {

    private final int minBrightness;
    private final int maxBrightness;
    private final int fixedBrightness;

    public BrightnessRangeProcessor(int minBrightness, int maxBrightness, int fixedBrightness) {
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;
        this.fixedBrightness = fixedBrightness;
    }

    @Override
    public Image process(Image inputImage) {
        WritableImage processedImage = new WritableImage((int) inputImage.getWidth(), (int) inputImage.getHeight());
        PixelReader pixelReader = inputImage.getPixelReader();

        for (int y = 0; y < inputImage.getHeight(); y++) {
            for (int x = 0; x < inputImage.getWidth(); x++) {
                int rgb = pixelReader.getArgb(x, y);
                int red = (rgb >> 16) & 0xff;
                int green = (rgb >> 8) & 0xff;
                int blue = rgb & 0xff;

                int brightness = (int) (0.299 * red + 0.587 * green + 0.114 * blue);

                if (brightness >= minBrightness && brightness <= maxBrightness) {
                    red = green = blue = fixedBrightness;
                }

                int newRgb = (255 << 24) | (red << 16) | (green << 8) | blue;
                processedImage.getPixelWriter().setArgb(x, y, newRgb);
            }
        }
        return processedImage;
    }
}
