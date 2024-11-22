package com.example.multimedia;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class Histogram {

    private final MainController controller;

    public Histogram(MainController controller) {
        this.controller = controller;
    }

    public void showHistogram(Image image) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        xAxis.setLabel("Яркость");
        yAxis.setLabel("Количество пикселей");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Гистограмма яркости изображения");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Яркость");

        int[] brightnessCounts = new int[256];
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int brightness = (int) (pixelReader.getColor(x, y).getBrightness() * 255);
                brightnessCounts[brightness]++;
            }
        }

        for (int i = 0; i < brightnessCounts.length; i++) {
            series.getData().add(new XYChart.Data<>(Integer.toString(i), brightnessCounts[i]));
        }

        barChart.getData().add(series);

        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        Button equalizeButton = new Button("Эквализация гистограммы");
        equalizeButton.setOnAction(e -> {
            Image equalizedImage = equalizeHistogram(image);
            controller.updateImage(equalizedImage);
            stage.close();
        });

        Button otsuButton = new Button("Порог Оцу");
        otsuButton.setOnAction(e -> {
            Image otsuImage = applyOtsuThreshold(image);
            controller.updateImage(otsuImage);
            stage.close();
        });

        Slider thresholdSlider = new Slider(0, 255, 128);
        thresholdSlider.setShowTickLabels(true);
        thresholdSlider.setShowTickMarks(true);
        thresholdSlider.setMajorTickUnit(50);
        thresholdSlider.setMinorTickCount(5);
        thresholdSlider.setBlockIncrement(10);
        thresholdSlider.getStyleClass().add("custom-slider"); // Добавляем кастомный стиль

        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("slider.css")).toExternalForm());

        thresholdSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            Image binaryImage = applyThreshold(image, newVal.intValue());
            controller.updateImage(binaryImage);
        });

        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);

        vbox.getChildren().addAll(barChart, thresholdSlider, equalizeButton, otsuButton);

        root.setCenter(vbox);
        stage.setScene(scene);
        stage.setTitle("Гистограмма и бинаризация");
        stage.show();
    }

    private Image equalizeHistogram(Image image) {
        int width;
        width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter pixelWriter = newImage.getPixelWriter();

        int[] brightnessCounts = new int[256];
        int totalPixels = width * height;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int brightness = (int) (pixelReader.getColor(x, y).getBrightness() * 255);
                brightnessCounts[brightness]++;
            }
        }

        int[] cdf = new int[256];
        cdf[0] = brightnessCounts[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + brightnessCounts[i];
        }

        for (int i = 0; i < 256; i++) {
            cdf[i] = (cdf[i] * 255) / totalPixels;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int brightness = (int) (pixelReader.getColor(x, y).getBrightness() * 255);
                int newBrightness = cdf[brightness];
                pixelWriter.setColor(x, y, pixelReader.getColor(x, y).deriveColor(0, 1, newBrightness / 255.0, 1));
            }
        }

        return newImage;
    }

    private Image applyThreshold(Image image, int threshold) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage binaryImage = new WritableImage(width, height);
        PixelWriter pixelWriter = binaryImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; !(x >= width); x++) {
                int brightness;
                brightness = (int) (pixelReader.getColor(x, y).getBrightness() * 255);
                if (brightness >= threshold) {
                    pixelWriter.setColor(x, y, javafx.scene.paint.Color.WHITE);
                } else {
                    pixelWriter.setColor(x, y, javafx.scene.paint.Color.BLACK);
                }
            }
        }

        return binaryImage;
    }

    private Image applyOtsuThreshold(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        PixelReader pixelReader = image.getPixelReader();
        WritableImage binaryImage = new WritableImage(width, height);
        PixelWriter pixelWriter = binaryImage.getPixelWriter();

        int[] histogram = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int brightness = (int) (pixelReader.getColor(x, y).getBrightness() * 255);
                histogram[brightness]++;
            }
        }

        double total = width * height;
        double sumB = 0;
        double wB = 0;
        double maximum = 0.0;
        double threshold = 0.0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;

            double wF = total - wB;
            if (wF == 0) break;

            sumB += i * histogram[i];
            double mB = sumB / wB;
            double mF = (0.0) / wF;

            double betweenClassVariance = wB * wF * Math.pow(mB - mF, 2);

            if (betweenClassVariance > maximum) {
                maximum = betweenClassVariance;
                threshold = i;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int brightness = (int) (pixelReader.getColor(x, y).getBrightness() * 255);
                if (brightness >= threshold) {
                    pixelWriter.setColor(x, y, javafx.scene.paint.Color.WHITE);
                } else {
                    pixelWriter.setColor(x, y, javafx.scene.paint.Color.BLACK);
                }
            }
        }

        return binaryImage;
    }
}
