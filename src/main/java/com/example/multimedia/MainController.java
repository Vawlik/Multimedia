package com.example.multimedia;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class MainController {

    @FXML
    private ImageView originalImageView;
    @FXML
    private ImageView processedImageView;
    @FXML
    private Label methodLabel;
    @FXML
    private Slider gammaSlider;
    @FXML
    private HBox gammaSliderContainer;
    @FXML
    private Label gammaValueLabel;
    @FXML
    private Button gradientButton;
    @FXML
    private Button laplacianButton;
    @FXML
    private TextField minBrightnessField;
    @FXML
    private TextField maxBrightnessField;
    @FXML
    private TextField fixedBrightnessField;
    @FXML
    private HBox brightnessRangeBox;

    private Image originalImage;
    private ImageProcessor imageProcessor;
    private boolean isSobelMethod = true;
    private String selectedMethod;
    private boolean isLaplacian90 = true;
    private boolean isMaskColorBlack = true;
    @FXML
    private Button toggleMaskColorButton;

    private static void showAlertError(String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    @FXML
    private void selectDilation() {
        selectedMethod = "Дилатация";
        applySelectedMorphology();
    }

    @FXML
    private void selectErosion() {
        selectedMethod = "Эрозия";
        applySelectedMorphology();
    }

    @FXML
    private void selectClosing() {
        selectedMethod = "Замыкание";
        applySelectedMorphology();
    }

    @FXML
    private void selectOpening() {
        selectedMethod = "Размыкание";
        applySelectedMorphology();
    }

    @FXML
    private void selectBoundaryExtraction() {
        selectedMethod = "Выделение границ";
        applySelectedMorphology();
    }

    @FXML
    private void selectSkeleton() {
        selectedMethod = "Остов";
        applySelectedMorphology();
    }

    @FXML
    private void openImage() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            try {
                originalImage = new Image(file.toURI().toString());
                originalImageView.setImage(originalImage);
            } catch (Exception e) {
                showAlertError(e.getMessage(), "Изображение отсутствует, попробуйте еще раз");
            }
        }
    }

    @FXML
    private void pasteImageFromClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasImage()) {
            Image clipboardImage = clipboard.getImage();
            originalImageView.setImage(clipboardImage);
            originalImage = clipboardImage;
        } else {
            showAlertError("Содержимое буфера обмена не является изображением",
                    "Пожалуйста, скопируйте изображение и попробуйте снова.");
        }
    }

    @FXML
    private void saveImage() {
        if (processedImageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Сохранить изображение");

            FileChooser.ExtensionFilter pngFilter = new FileChooser.ExtensionFilter("PNG файлы (*.png)", "*.png");
            FileChooser.ExtensionFilter jpgFilter = new FileChooser.ExtensionFilter("JPEG файлы (*.jpg)", "*.jpg");
            fileChooser.getExtensionFilters().addAll(pngFilter, jpgFilter);

            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                String fileName = file.getName();
                if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg")) {
                    if (fileChooser.getSelectedExtensionFilter() == pngFilter) {
                        file = new File(file.getAbsolutePath() + ".png");
                    } else {
                        file = new File(file.getAbsolutePath() + ".jpg");
                    }
                }

                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(processedImageView.getImage(), null), fileChooser.getSelectedExtensionFilter().getExtensions().getFirst().substring(2), file);
                } catch (IOException e) {
                    showAlertError(e.getMessage(), "Ошибка чтения изображения, попробуйте другой формат");
                }
            }
        }
    }

    @FXML
    private void applyNegative() {
        if (originalImage != null) {
            clearUIForMorphology();
            imageProcessor = new NegativeProcessor();
            Image processedImage = imageProcessor.process(originalImage);
            processedImageView.setImage(processedImage);
            methodLabel.setText("Выбран метод: Негатив");
        }
    }

    @FXML
    private void applyGammaCorrection() {
        if (originalImage != null) {
            clearUIForMorphology();
            gammaSliderContainer.setVisible(true);

            gammaSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
                gammaValueLabel.setText(String.format("%.2f", newValue.doubleValue()));

                imageProcessor = new GammaCorrectionProcessor(1.0, newValue.doubleValue());
                Image processedImage = imageProcessor.process(originalImage);
                processedImageView.setImage(processedImage);
            });

            imageProcessor = new GammaCorrectionProcessor(1.0, gammaSlider.getValue());
            Image processedImage = imageProcessor.process(originalImage);
            processedImageView.setImage(processedImage);
            methodLabel.setText("Выбран метод: Гамма-коррекция");
        }
    }


    @FXML
    private void applyGradient() {
        if (originalImage != null) {
            clearUIForMorphology();
            gradientButton.setVisible(true);

            Image processedImage;

            if (isSobelMethod) {
                processedImage = new GradientProcessor().applySobel(originalImage);
                methodLabel.setText("Применен градиент Собела.");
            } else {
                processedImage = new GradientProcessor().applyRoberts(originalImage);
                methodLabel.setText("Применен градиент Робертса.");
            }

            processedImageView.setImage(processedImage);
            isSobelMethod = !isSobelMethod;
        }
    }

    @FXML
    private void applyBrightnessRange() {
        if (originalImage != null) {
            try {
                int minBrightness = Integer.parseInt(minBrightnessField.getText());
                int maxBrightness = Integer.parseInt(maxBrightnessField.getText());
                int fixedBrightness = Integer.parseInt(fixedBrightnessField.getText());

                if (minBrightness < 0 || maxBrightness > 255 || fixedBrightness < 0 || fixedBrightness > 255 || minBrightness >= maxBrightness) {
                    methodLabel.setText("Неправильные значения! Убедитесь, что 0 <= Мин. яркость < Макс. яркость <= 255 и фиксированное значение в диапазоне 0-255.");
                    return;
                }

                imageProcessor = new BrightnessRangeProcessor(minBrightness, maxBrightness, fixedBrightness);
                Image processedImage = imageProcessor.process(originalImage);
                processedImageView.setImage(processedImage);
                methodLabel.setText("Выбран метод: Вырезание диапазона яркостей");

            } catch (NumberFormatException e) {
                methodLabel.setText("Пожалуйста, введите корректные числа.");
            }
        }
    }

    @FXML
    private void showBrightnessClipping() {
        clearUIForMorphology();
        brightnessRangeBox.setVisible(true);
        methodLabel.setText("Выбран метод: Вырезание диапазона яркостей");
    }

    @FXML
    private void applySmoothingFilter() {
        if (originalImage != null) {
            clearUIForMorphology();

            SmoothingFilterProcessor smoothingFilterProcessor = new SmoothingFilterProcessor();
            Image smoothedImage = smoothingFilterProcessor.process(originalImage);
            processedImageView.setImage(smoothedImage);
            methodLabel.setText("Выбран метод: Линейный Сглаживающий Фильтр");
        }
    }

    @FXML
    private void applyMedianFilter() {
        if (originalImage != null) {
            clearUIForMorphology();

            MedianFilterProcessor medianFilterProcessor = new MedianFilterProcessor();
            Image filteredImage = medianFilterProcessor.process(originalImage);
            processedImageView.setImage(filteredImage);
            methodLabel.setText("Выбран метод: Медианный Фильтр");
        }
    }

    @FXML
    private void applyLaplacian() {
        if (originalImage != null) {
            clearUIForMorphology();
            laplacianButton.setVisible(true);

            Image processedImage;
            LaplacianProcessor laplacianProcessor = new LaplacianProcessor();

            if (isLaplacian90) {
                processedImage = laplacianProcessor.applyLaplacian90(originalImage);
                methodLabel.setText("Применен Лаплассиан 90 градусов.");
            } else {
                processedImage = laplacianProcessor.applyLaplacian45(originalImage);
                methodLabel.setText("Применен Лаплассиан 45 градусов.");
            }

            processedImageView.setImage(processedImage);
            isLaplacian90 = !isLaplacian90;
        }
    }

    public void handleShowHistogram() {
        if (originalImage != null) {
            Histogram histogram = new Histogram(this);
            histogram.showHistogram(originalImage);
        } else {
            methodLabel.setText("Пожалуйста, выберите изображение.");
        }
    }

    public void updateImage(Image newImage) {
        processedImageView.setImage(newImage);
    }

    @FXML
    private void toggleMaskColor() {
        isMaskColorBlack = !isMaskColorBlack;
        methodLabel.setText("Цвет маски: " + (isMaskColorBlack ? "Черный" : "Белый"));
        applySelectedMorphology();
    }

    @FXML
    private void applySelectedMorphology() {
        if (originalImage != null) {
            clearUIForMorphology();
            toggleMaskColorButton.setVisible(true);

            MorphologyProcessor morphologyProcessor = new MorphologyProcessor(isMaskColorBlack);
            int[][] structuringElement = {{1, 1, 1}, {1, 1, 1}, {1, 1, 1}};

            Image processedImage = null;

            switch (selectedMethod) {
                case "Дилатация":
                    processedImage = morphologyProcessor.dilate(originalImage, structuringElement);
                    methodLabel.setText("Выбран метод: Дилатация");
                    break;
                case "Эрозия":
                    processedImage = morphologyProcessor.erode(originalImage, structuringElement);
                    methodLabel.setText("Выбран метод: Эрозия");
                    break;
                case "Замыкание":
                    processedImage = morphologyProcessor.close(originalImage, structuringElement);
                    methodLabel.setText("Выбран метод: Замыкание");
                    break;
                case "Размыкание":
                    processedImage = morphologyProcessor.open(originalImage, structuringElement);
                    methodLabel.setText("Выбран метод: Размыкание");
                    break;
                case "Выделение границ":
                    processedImage = morphologyProcessor.boundaryExtraction(originalImage, structuringElement);
                    methodLabel.setText("Выбран метод: Выделение границ");
                    break;
                case "Остов":
                    processedImage = morphologyProcessor.skeletonize(originalImage);
                    methodLabel.setText("Выбран метод: Остов");
                    break;
            }
            if (processedImage != null) {
                processedImageView.setImage(processedImage);
            }
        }
    }

    private void clearUIForMorphology() {
        gammaSliderContainer.setVisible(false);
        gradientButton.setVisible(false);
        brightnessRangeBox.setVisible(false);
        laplacianButton.setVisible(false);
        toggleMaskColorButton.setVisible(false);
    }
}
