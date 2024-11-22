package com.example.multimedia;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class LaplacianProcessor {

    private static final double[][] LAPLACIAN_90 = {{0, -1, 0}, {-1, 4, -1}, {0, -1, 0}};
    private static final double[][] LAPLACIAN_45 = {{-1, -1, -1}, {-1, 8, -1}, {-1, -1, -1}};

    public Image applyLaplacian90(Image inputImage) {
        return applyLaplacian(inputImage, LAPLACIAN_90);
    }

    public Image applyLaplacian45(Image inputImage) {
        return applyLaplacian(inputImage, LAPLACIAN_45);
    }

    private Image applyLaplacian(Image inputImage, double[][] kernel) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(width, height);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double red = 0, green = 0, blue = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        Color color = inputImage.getPixelReader().getColor(x + kx, y + ky);
                        red += color.getRed() * kernel[ky + 1][kx + 1];
                        green += color.getGreen() * kernel[ky + 1][kx + 1];
                        blue += color.getBlue() * kernel[ky + 1][kx + 1];
                    }
                }

                red = Math.min(Math.max(red, 0), 1);
                green = Math.min(Math.max(green, 0), 1);
                blue = Math.min(Math.max(blue, 0), 1);

                outputImage.getPixelWriter().setColor(x, y, Color.color(red, green, blue));
            }
        }

        return outputImage;
    }
}
