package com.example.multimedia;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedianFilterProcessor implements ImageProcessor {

    @Override
    public Image process(Image inputImage) {
        int width = (int) inputImage.getWidth();
        int height = (int) inputImage.getHeight();

        WritableImage outputImage = new WritableImage(width, height);
        PixelReader pixelReader = inputImage.getPixelReader();
        PixelWriter pixelWriter = outputImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                List<Double> reds = new ArrayList<>();
                List<Double> greens = new ArrayList<>();
                List<Double> blues = new ArrayList<>();

                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int neighborX = Math.min(Math.max(x + dx, 0), width - 1);
                        int neighborY = Math.min(Math.max(y + dy, 0), height - 1);
                        Color color = pixelReader.getColor(neighborX, neighborY);
                        reds.add(color.getRed());
                        greens.add(color.getGreen());
                        blues.add(color.getBlue());
                    }
                }

                double medianRed = findMedian(reds);
                double medianGreen = findMedian(greens);
                double medianBlue = findMedian(blues);

                pixelWriter.setColor(x, y, new Color(medianRed, medianGreen, medianBlue, 1.0));
            }
        }

        return outputImage;
    }

    private double findMedian(List<Double> values) {
        Collections.sort(values);
        int size = values.size();
        if (size % 2 == 0) {
            return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
        } else {
            return values.get(size / 2);
        }
    }
}
