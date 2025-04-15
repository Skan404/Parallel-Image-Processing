package org.example;

import org.apache.commons.lang3.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class ImageProcessor {

    public static void main(String[] args) {
        String inputDirectoryPath = "C:/Users/Mytov/OneDrive/Pulpit/input";
        String outputDirectoryPath = "C:/Users/Mytov/OneDrive/Pulpit/output";

        for (int poolSize = 6; poolSize <= 8; poolSize++) {
            long startTime = System.currentTimeMillis();

            processImages(Path.of(inputDirectoryPath), Path.of(outputDirectoryPath), poolSize);

            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            System.out.println("Execution time with pool size " + poolSize + ": " + executionTime + " ms");
        }
    }

    private static void processImages(Path inputDirectory, Path outputDirectory, int threadPoolSize) {
        List<Path> files;
        try {
            files = Files.list(inputDirectory)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading input directory: " + e.getMessage());
            return;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(threadPoolSize);

        forkJoinPool.submit(() ->
                files.parallelStream().map(file -> {
                    try {
                        BufferedImage image = ImageIO.read(file.toFile());
                        Pair<String, BufferedImage> pair = Pair.of(file.getFileName().toString(), image);
                        return pair;
                    } catch (IOException e) {
                        System.err.println("Error processing image: " + e.getMessage());
                        return null;
                    }
                }).forEach(pair -> {
                    if (pair != null) {
                        BufferedImage processedImage = processImage(pair.getRight());
                        Path outputFile = outputDirectory.resolve(pair.getLeft());
                        try {
                            ImageIO.write(processedImage, "jpg", outputFile.toFile());
                        } catch (IOException e) {
                            System.err.println("Error writing image: " + e.getMessage());
                        }
                    }
                })
        ).join();

        forkJoinPool.shutdown();
    }

    private static BufferedImage processImage(BufferedImage originalImage) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        BufferedImage processedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Obliczanie punktów po obrocie o 180 stopni
                int rotatedX = width - x - 1;
                int rotatedY = height - y - 1;

                int rgb = originalImage.getRGB(x, y);
                Color color = new Color(rgb);

                int red = color.getRed();
                int green = color.getBlue();
                int blue = color.getGreen();

                // Tworzenie nowego koloru
                Color processedColor = new Color(red, green, blue);
                processedImage.setRGB(rotatedX, rotatedY, processedColor.getRGB());
            }
        }

        return processedImage;
    }

}
