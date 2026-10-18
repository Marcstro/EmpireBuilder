package graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

public final class GarrisonIndicator {
    private static final int STAR_SIZE = 12;
    private static final int STAR_OVERLAP = STAR_SIZE / 4;
    private static final int STAR_STEP = STAR_SIZE - STAR_OVERLAP;
    public static final int MAX_WIDTH = STAR_SIZE + STAR_STEP;
    public static final int MAX_HEIGHT = STAR_SIZE + STAR_STEP;
    private static final BufferedImage STAR = createStar();

    private GarrisonIndicator() {
    }

    public static int getStarCount(int garrisonCount) {
        if (garrisonCount < 0) {
            throw new IllegalArgumentException("Garrison count cannot be negative");
        }
        if (garrisonCount == 0) {
            return 0;
        }
        if (garrisonCount <= 6) {
            return 1;
        }
        return garrisonCount <= 16 ? 2 : 3;
    }

    public static void draw(Graphics2D g, int buildingX, int buildingY, int pixelSize, int garrisonCount) {
        int stars = getStarCount(garrisonCount);
        int startX = buildingX + pixelSize - STAR_SIZE / 2;
        int startY = buildingY - STAR_SIZE / 2;
        if (stars == 3) {
            g.drawImage(STAR, startX + STAR_STEP / 2, startY - STAR_STEP, null);
        }
        for (int i = 0; i < Math.min(stars, 2); i++) {
            g.drawImage(STAR, startX + i * STAR_STEP, startY, null);
        }
    }

    private static BufferedImage createStar() {
        BufferedImage image = new BufferedImage(STAR_SIZE, STAR_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Path2D star = new Path2D.Double();
            double center = STAR_SIZE / 2.0;
            double outerRadius = (STAR_SIZE - 1) / 2.0;
            for (int i = 0; i < 10; i++) {
                double angle = -Math.PI / 2 + i * Math.PI / 5;
                double radius = i % 2 == 0 ? outerRadius : outerRadius * 0.45;
                double x = center + Math.cos(angle) * radius;
                double y = center + Math.sin(angle) * radius;
                if (i == 0) {
                    star.moveTo(x, y);
                } else {
                    star.lineTo(x, y);
                }
            }
            star.closePath();
            g.setColor(Color.YELLOW);
            g.fill(star);
            g.setColor(new Color(96, 72, 0));
            g.draw(star);
        } finally {
            g.dispose();
        }
        return image;
    }
}
