package com.example.multimedia;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class GradientProcessor implements ImageProcessor {

    @Override
    public Image process(Image inputImage) {
        return null;
    }

    public Image applySobel(Image image) {
        WritableImage processedImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelReader pixelReader = image.getPixelReader();

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};

        int[][] sobelY = {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}};

        for (int y = 1; y < image.getHeight() - 1; y++) {
            for (int x = 1; x < image.getWidth() - 1; x++) {
                double pixelX = 0;
                double pixelY = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int rgb = pixelReader.getArgb(x + j, y + i);
                        int gray = (rgb >> 16) & 0xff;
                        pixelX += gray * sobelX[i + 1][j + 1];
                        pixelY += gray * sobelY[i + 1][j + 1];
                    }
                }

                int magnitude = (int) Math.min(255, Math.sqrt(pixelX * pixelX + pixelY * pixelY));
                processedImage.getPixelWriter().setArgb(x, y, (magnitude << 16) | (magnitude << 8) | magnitude | (255 << 24));
            }
        }
        return processedImage;
    }

    public Image applyRoberts(Image image) {
        WritableImage processedImage = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        PixelReader pixelReader = image.getPixelReader();

        int[][] robertsX = {{1, 0}, {0, -1}};

        int[][] robertsY = {{0, 1}, {-1, 0}};

        for (int y = 0; y < image.getHeight() - 1; y++) {
            for (int x = 0; x < image.getWidth() - 1; x++) {
                double pixelX = 0;
                double pixelY = 0;

                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        int rgb = pixelReader.getArgb(x + j, y + i);
                        int gray = (rgb >> 16) & 0xff;
                        pixelX += gray * robertsX[i][j];
                        pixelY += gray * robertsY[i][j];
                    }
                }

                int magnitude = (int) Math.min(255, Math.sqrt(pixelX * pixelX + pixelY * pixelY));
                processedImage.getPixelWriter().setArgb(x, y, (magnitude << 16) | (magnitude << 8) | magnitude | (255 << 24));
            }
        }
        return processedImage;
    }
}
