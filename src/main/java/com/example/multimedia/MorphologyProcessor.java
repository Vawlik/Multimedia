package com.example.multimedia;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class MorphologyProcessor {

    private final boolean isMaskColorBlack;

    public MorphologyProcessor(boolean isMaskColorBlack) {
        this.isMaskColorBlack = isMaskColorBlack;
    }

    // Применение метода дилатации
    public Image dilate(Image inputImage, int[][] structuringElement) {
        return applyMorphology(inputImage, structuringElement, true);
    }

    // Применение метода эрозии
    public Image erode(Image inputImage, int[][] structuringElement) {
        return applyMorphology(inputImage, structuringElement, false);
    }

    // Применение метода замыкания (дилатация, затем эрозия)
    public Image close(Image inputImage, int[][] structuringElement) {
        Image dilated = dilate(inputImage, structuringElement);
        return erode(dilated, structuringElement);
    }

    // Применение метода размыкания (эрозия, затем дилатация)
    public Image open(Image inputImage, int[][] structuringElement) {
        Image eroded = erode(inputImage, structuringElement);
        return dilate(eroded, structuringElement);
    }

    // Применение метода выделения границ (дилатация - оригинал)
    public Image boundaryExtraction(Image inputImage, int[][] structuringElement) {
        Image dilated = dilate(inputImage, structuringElement);
        return subtractImages(dilated, inputImage);
    }

    // Применение метода остова (алгоритм Zhang-Suen)
    public Image skeletonize(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        boolean[][] image = new boolean[height][width];
        boolean[][] marker = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image[y][x] = reader.getArgb(x, y) == (isMaskColorBlack ? 0xFF000000 : 0xFFFFFFFF);
                marker[y][x] = false;
            }
        }

        boolean hasChanged;
        do {
            hasChanged = false;

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (image[y][x] && canBeRemoved(image, x, y, true)) {
                        marker[y][x] = true;
                        hasChanged = true;
                    }
                }
            }

            for (int y = 1; !(height - 1 <= y); y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (marker[y][x]) {
                        image[y][x] = false;
                        marker[y][x] = false;
                    }
                }
            }
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (image[y][x] && canBeRemoved(image, x, y, false)) {
                        marker[y][x] = true;
                        hasChanged = true;
                    }
                }
            }

            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (marker[y][x]) {
                        image[y][x] = false;
                        marker[y][x] = false;
                    }
                }
            }
        } while (hasChanged);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                writer.setArgb(x, y, image[y][x] ? (isMaskColorBlack ? 0xFF000000 : 0xFFFFFFFF) : (isMaskColorBlack ? 0xFFFFFFFF : 0xFF000000));
            }
        }

        return outputImage;
    }

    private boolean canBeRemoved(boolean[][] image, int x, int y, boolean firstStep) {
        int p2 = image[y - 1][x] ? 1 : 0;
        int p3 = image[y - 1][x + 1] ? 1 : 0;
        int p4 = image[y][x + 1] ? 1 : 0;
        int p5 = image[y + 1][x + 1] ? 1 : 0;
        int p6 = image[y + 1][x] ? 1 : 0;
        int p7 = image[y + 1][x - 1] ? 1 : 0;
        int p8 = image[y][x - 1] ? 1 : 0;
        int p9 = image[y - 1][x - 1] ? 1 : 0;

        int B = p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9;
        int A = (p2 == 0 && p3 == 1) ? 1 : 0;
        A += !(p3 != 0 || p4 != 1) ? 1 : 0;
        A += !(p4 != 0 || p5 != 1) ? 1 : 0;
        A += (p5 == 0 && p6 == 1) ? 1 : 0;
        A += (p6 == 0 && p7 == 1) ? 1 : 0;
        A += (p7 == 0 && p8 == 1) ? 1 : 0;
        A += (p8 == 0 && p9 == 1) ? 1 : 0;
        A += (p9 == 0 && p2 == 1) ? 1 : 0;

        if (B >= 2 && B <= 6 && A == 1) {
            if (firstStep) {
                return p2 * p4 * p6 == 0 && p4 * p6 * p8 == 0;
            } else {
                return p2 * p4 * p8 == 0 && p2 * p6 * p8 == 0;
            }
        }
        return false;
    }

    private Image applyMorphology(Image inputImage, int[][] structuringElement, boolean isDilation) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader = inputImage.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean result = !isDilation;

                for (int i = 0; i < structuringElement.length; i++) {
                    for (int j = 0; j < structuringElement[0].length; j++) {
                        int offsetX = x + i - structuringElement.length / 2;
                        int offsetY = y + j - structuringElement[0].length / 2;
                        if (offsetX >= 0 && offsetY >= 0 && offsetX < width && offsetY < height) {
                            int pixelValue = reader.getArgb(offsetX, offsetY) == (isMaskColorBlack ? 0xFF000000 : 0xFFFFFFFF) ? 1 : 0;
                            if (isDilation) {
                                result = result || (pixelValue == structuringElement[i][j]);
                            } else {
                                result = result && (pixelValue == structuringElement[i][j]);
                            }
                        }
                    }
                }
                writer.setArgb(x, y, result ? (isMaskColorBlack ? 0xFF000000 : 0xFFFFFFFF) : (isMaskColorBlack ? 0xFFFFFFFF : 0xFF000000));
            }
        }
        return outputImage;
    }

    private Image subtractImages(Image img1, Image img2) {
        int width = (int) img1.getWidth();
        int height = (int) img1.getHeight();
        WritableImage outputImage = new WritableImage(width, height);
        PixelReader reader1 = img1.getPixelReader();
        PixelReader reader2 = img2.getPixelReader();
        PixelWriter writer = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel1 = reader1.getArgb(x, y);
                int pixel2 = reader2.getArgb(x, y);
                writer.setArgb(x, y, pixel1 != pixel2 ? (isMaskColorBlack ? 0xFF000000 : 0xFFFFFFFF) : (isMaskColorBlack ? 0xFFFFFFFF : 0xFF000000));
            }
        }
        return outputImage;
    }
}
