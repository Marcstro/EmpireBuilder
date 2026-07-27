package graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class ImageManager {

    // These maps store 3 levels of scaled 'Image' objects, optimized for quick drawing after zoom change.
    private static Map<String, Image> smallerBuildingImages = new HashMap<>();
    private static Map<String, Image> currentBuildingImages = new HashMap<>();
    private static Map<String, Image> largerBuildingImages = new HashMap<>();

    // pairs etc <"KnightImage", Object>
    private static final Map<String, BufferedImage> unitImages = new HashMap<>();
    private static final Map<String, BufferedImage> effectImages = new HashMap<>();

    private static final Map<String, BufferedImage> baseBuildingsImageCache = new HashMap<>();

    // List to hold the keys of all buildings, used to iterate during scaling
    private static final List<String> allBuildingKeys = new ArrayList<>();

    private static double zoomFactor = 1.25;
    private static int currentPixelSize = -1;
    private static final int DEFAULT_SIZE = 32;

    enum FileType {
        BUILDING,
        UNIT,
        EFFECT
    }

    private static final ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();

    private ImageManager() {
        // prevents instantiation
    }


    public static void setZoomFactor(double factor) {
        zoomFactor = factor;
    }

    /**
     * Public wrapper to load all three properties files at startup.
     * This loads the original image into baseImageCache and pre-scales Units/Effects.
     */
    public static void loadAllAssets() {
        baseBuildingsImageCache.clear();
        allBuildingKeys.clear();
        unitImages.clear();
        effectImages.clear();

        processPropertiesFile("/resources/BuildingImages.properties", FileType.BUILDING);
        processPropertiesFile("/resources/UnitImages.properties", FileType.UNIT);
        processPropertiesFile("/resources/EffectImages.properties", FileType.EFFECT);

        // TODO need to rewrite the entire "multiple frames-effects system". Currently there is only 1 such effect but this is... poorly handled.
        processSpriteSheet("bloodSpark", "/resources/images/BloodEffect3.png", 4, 2, 106, 106);
    }

    /**
     * Initializes the building zoom system by loading the current, smaller, and larger image sets.
     * Must be called AFTER loadAllAssets().
     * @param startPixelSize The initial drawing size (e.g., 32).
     */
    public static void initialize(int startPixelSize) {
        if (currentPixelSize != -1) {
            return;
        }

        System.out.println("Initializing Image manager for zoom system.");
        currentPixelSize = startPixelSize;

        // Load initial set for current, and preload the others in the background
        currentBuildingImages = scaleAndCache(currentPixelSize);
        preloadLargerImages();
        preloadSmallerImages();
    }

    /**
     * Loads a spritesheet, splits it into frames, and caches them as effects.
     * @param baseKey The base name for the animation frames (e.g., "blood").
     * @param path The classpath resource path to the spritesheet image.
     * @param cols The number of columns in the spritesheet.
     * @param rows The number of rows in the spritesheet.
     * @param frameWidth The desired width of each scaled frame.
     * @param frameHeight The desired height of each scaled frame.
     *
     * // TODO make use of this function but implement middle-layer like "processPropertySpriteSheets" that handles ALL animations
     */
    private static void processSpriteSheet(String baseKey, String path, int cols, int rows, int frameWidth, int frameHeight) {
        try {
            BufferedImage sheet = ImageIO.read(ImageManager.class.getResource(path));
            if (sheet == null) {
                throw new IOException("Spritesheet not found on classpath: " + path);
            }

            int subImageWidth = sheet.getWidth() / cols;
            int subImageHeight = sheet.getHeight() / rows;
            int frameCount = 1;

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    BufferedImage subImage = sheet.getSubimage(x * subImageWidth, y * subImageHeight, subImageWidth, subImageHeight);
                    BufferedImage scaledImage = getScaledImage(subImage, frameWidth, frameHeight, true);
                    String imageKey = baseKey + frameCount;
                    effectImages.put(imageKey, scaledImage);
                    frameCount++;
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error processing spritesheet key: " + baseKey + ". " + e.getMessage());
        }
    }

    private static void processPropertiesFile(String propertiesFileName, FileType fileType) {
        Properties props = new Properties();

        try (InputStream in = ImageManager.class.getResourceAsStream(propertiesFileName)) {

            if (in == null) {
                System.err.println("FATAL ERROR: Properties file not found on classpath: " + propertiesFileName);
                return;
            }

            props.load(in);
            System.out.println("Loaded properties file: " + propertiesFileName);

            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                loadAndScaleImage(key, value, fileType);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error reading or loading properties file: " + propertiesFileName);
        }
    }

    private static void loadAndScaleImage(String key, String metadataString, FileType fileType) {
        try {
            String[] parts = metadataString.split(",");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Metadata for key '" + key +
                        "' must be in format 'path,width,height'. Found: " + metadataString);
            }

            String path = parts[0].trim();
            int requiredWidth = Integer.parseInt(parts[1].trim());
            int requiredHeight = Integer.parseInt(parts[2].trim());

            BufferedImage originalImage = ImageIO.read(ImageManager.class.getResource(path));
            if (originalImage == null) {
                throw new IOException("Image not found on classpath: " + path);
            }

            if (fileType.equals(FileType.BUILDING)) {
                // For Buildings: Store the original image and the key for the zoom system
                baseBuildingsImageCache.put(key, originalImage);
                allBuildingKeys.add(key);
            }
            else {
                BufferedImage scaledImage = getScaledImage(originalImage, requiredWidth, requiredHeight, false);
                if (fileType.equals(FileType.UNIT)){
                    unitImages.put(key, scaledImage);
                }
                else {
                    effectImages.put(key, scaledImage);
                }
            }

        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error processing image key: " + key + ". " + e.getMessage());
            // Create placeholders for failed fixed-size assets
            if (!fileType.equals(FileType.BUILDING)) {
                unitImages.put(key, createPlaceholderImage(DEFAULT_SIZE, DEFAULT_SIZE));
            }
        }
    }

    public static void onZoomChange(int newPixelSize) {
        if (newPixelSize == currentPixelSize) {
            return;
        }

        System.out.println("Zoom changed. New size: " + newPixelSize);
        if (newPixelSize > currentPixelSize) {
            // Zooming IN
            smallerBuildingImages = currentBuildingImages;
            currentBuildingImages = largerBuildingImages;
            preloadLargerImages();
        } else {
            // Zooming OUT
            largerBuildingImages = currentBuildingImages;
            currentBuildingImages = smallerBuildingImages;
            preloadSmallerImages();
        }
        currentPixelSize = newPixelSize;
    }

    private static Map<String, Image> scaleAndCache(int size) {
        Map<String, Image> scaledSet = new HashMap<>();
        for (String key : allBuildingKeys) {
            BufferedImage baseImage = baseBuildingsImageCache.get(key);
            if (baseImage != null) {
                Image scaledImage = baseImage.getScaledInstance(size, size, Image.SCALE_SMOOTH);
                scaledSet.put(key, scaledImage);
            }
        }
        return scaledSet;
    }

    private static void preloadLargerImages() {
        int nextSize = (int) Math.round(currentPixelSize * zoomFactor);

        backgroundExecutor.schedule(() -> {
            System.out.println("Preloading larger building images to size: " + nextSize);
            largerBuildingImages = scaleAndCache(nextSize);
        }, 0, TimeUnit.MILLISECONDS);
    }

    private static void preloadSmallerImages() {
        int nextSize = (int) Math.round(currentPixelSize / zoomFactor);
        if (nextSize > 0) {
            backgroundExecutor.schedule(() -> {
                System.out.println("Preloading smaller building images to size: " + nextSize);
                smallerBuildingImages = scaleAndCache(nextSize);
            }, 0, TimeUnit.MILLISECONDS);
        } else {
            // If size is too small, use an empty map
            smallerBuildingImages = new HashMap<>();
        }
    }

    /**
     * Gets the currently scaled Image for a building, adjusted by the active zoom level.
     */
    public static Image getBuildingImage(String buildingName) {
        Image img = currentBuildingImages.get(buildingName);
        if (img == null) {
            return baseBuildingsImageCache.getOrDefault(buildingName,
                    createPlaceholderImage(currentPixelSize, currentPixelSize));
        }
        return img;
    }

    public static BufferedImage getUnitImage(String unitName) {
        BufferedImage img = unitImages.get(unitName);
        if (img == null) {
            System.err.println("Warning: Unit image key '" + unitName + "' not found.");
            return createPlaceholderImage(DEFAULT_SIZE, DEFAULT_SIZE);
        }
        return img;
    }

    public static BufferedImage getEffectImage(String effectName) {
        BufferedImage img = effectImages.get(effectName);
        if (img == null) {
            System.err.println("Warning: Effect image key '" + effectName + "' not found.");
            return createPlaceholderImage(DEFAULT_SIZE, DEFAULT_SIZE);
        }
        return img;
    }

    /**
     * Resizes an image to the specified width and height
     */
    private static BufferedImage getScaledImage(BufferedImage originalImage, int width, int height, boolean opaque) {
        Image tmp = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = scaledImage.createGraphics();
        if (opaque){
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));

        }
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return scaledImage;
    }

    /**
     * Creates a placeholder image for missing assets. Replace with something better later
     */
    private static BufferedImage createPlaceholderImage(int width, int height) {
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = placeholder.createGraphics();
        g.setColor(Color.MAGENTA);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return placeholder;
    }
}