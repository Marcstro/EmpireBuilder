package graphics;

import buildings.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ImageManager {

    private static final Map<Class<? extends Building>, BufferedImage> baseImageCache = new HashMap<>();

    private static final List<Class<? extends Building>> allBuildings = Arrays.asList(
            City.class,
            CityArea.class,
            Farm.class,
            Town.class,
            TownArea.class,
            Village.class
            // TODO add buildings here as they are created
            // TODO possibly create some feature to automatically fill in this list?
    );

    private static Map<Class<? extends Building>, Image> smallerImages = new HashMap<>();
    private static Map<Class<? extends Building>, Image> currentImages = new HashMap<>();
    private static Map<Class<? extends Building>, Image> largerImages = new HashMap<>();

    private static double zoomFactor = 1.25;

    private static int currentPixelSize = -1;

    private static final ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

    public static void setZoomFactor(double factor) {
        zoomFactor = factor;
    }

    public static void preloadAllBaseImages() {
        for (Class<? extends Building> cls : allBuildings) {
            try {
                Building instance = cls.getDeclaredConstructor().newInstance();
                String path = instance.getImagePath();
                BufferedImage img = ImageIO.read(ImageManager.class.getResourceAsStream(path));
                if (img == null) {
                    throw new IOException("Image not found at: " + path);
                }
                baseImageCache.put(cls, img);
            } catch (Exception e) {
                System.err.println("Failed to load image for " + cls.getSimpleName());
                e.printStackTrace();
            }
        }
    }

    public static void initialize(int startPixelSize) {
        if (currentPixelSize != -1) {
            return;
        }

        System.out.println("Initializing Image manager");
        currentPixelSize = startPixelSize;

        currentImages = scaleAndCache(currentPixelSize);
        preloadLargerImages();
        preloadSmallerImages();
    }

    public static void onZoomChange(int newPixelSize) {
        if (newPixelSize == currentPixelSize) {
            return;
        }

        if (newPixelSize > currentPixelSize) {
            smallerImages = currentImages;
            currentImages = largerImages;
            preloadLargerImages();
        } else {
            largerImages = currentImages;
            currentImages = smallerImages;
            preloadSmallerImages();
        }
        currentPixelSize = newPixelSize;
    }

    public static Image getBuildingImage(Class<? extends Building> buildingClass) {
        return currentImages.get(buildingClass);
    }

    private static Map<Class<? extends Building>, Image> scaleAndCache(int size) {
        Map<Class<? extends Building>, Image> scaledSet = new HashMap<>();
        for (Class<? extends Building> cls : allBuildings) {
            BufferedImage baseImage = baseImageCache.get(cls);
            if (baseImage != null) {
                Image scaledImage = baseImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                scaledSet.put(cls, scaledImage);
            }
        }
        return scaledSet;
    }

    private static void preloadLargerImages() {
        int nextSize = (int) Math.round(currentPixelSize * zoomFactor);

        backgroundExecutor.schedule(() -> {
            largerImages = scaleAndCache(nextSize);
        }, 0, TimeUnit.MILLISECONDS);
    }

    private static void preloadSmallerImages() {
        int nextSize = (int) Math.round(currentPixelSize / zoomFactor);
        if (nextSize > 0) {
            backgroundExecutor.schedule(() -> {
                smallerImages = scaleAndCache(nextSize);
            }, 0, TimeUnit.MILLISECONDS);
        } else {
            smallerImages = new HashMap<>();
        }
    }
}