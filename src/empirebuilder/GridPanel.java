package empirebuilder;

import buildings.*;
import entities.effects.Effect;
import entities.effects.Missile;
import entities.units.Unit;
import graphics.ImageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;

public class GridPanel extends JPanel {

    private final int mapHeight;
    private final int mapWidth;


    private final int basePixelSize;
    private double zoom = 1.0;
    private int cameraX = 0;
    private int cameraY = 0;
    private double zoomLimit = 0.25;
    private double zoomFactor = 1.25;
    private double pixelPixelSize = 14;
    private double basicImagePixelSize = 45;
    private boolean imageManagerInitialized = false;

    private Map map;
    private Point selectedPoint;
    private boolean showLines = false;
    private boolean showPointLines = true;
    private boolean displayBuildingImages = false;

    GameManager gameManager;

    GridPanel(GameManager gameManager, Map map, int windowWidth, int windowHeight, int mapWidth, int mapHeight, int pixelSize, int buttonPanelWidth) {
        this.map = map;
        this.gameManager = gameManager;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        setPreferredSize(new Dimension((windowWidth * pixelSize), (windowHeight * pixelSize)));
        basePixelSize = pixelSize;

        ImageManager.setZoomFactor(getZoomFactor());
        if (pixelSize >= getPixelPixelSize()) {
            ImageManager.initialize(pixelSize);
        }
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                handleMouseClick(e);
            }
        });
    }

    public double getPixelPixelSize() {
        return pixelPixelSize;
    }

    public double getBasicImagePixelSize() {
        return basicImagePixelSize;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        int pixelSize = getPixelSize();

        double minimumImagePixelSize = getPixelPixelSize();

        if (displayBuildingImages && pixelSize >= minimumImagePixelSize){
            renderImageView(g2d, pixelSize);
        }
        else {
            renderPixelView(g2d, pixelSize);
        }
        drawOverlays(g2d, pixelSize);

        renderUnitsView2(g2d, pixelSize);
        renderEffectsView(g2d, pixelSize);
    }

    private void renderUnitsView2(Graphics2D g, int pixelSize) {

        // --- CULLING PRE-CALCULATIONS ---
        // 1. Determine the extent of the visible area in world-coordinate units (points).
        final double viewWidthInPoints = (double)getWidth() / pixelSize;
        final double viewHeightInPoints = (double)getHeight() / pixelSize;

        // 2. Define the camera's top-left corner in world coordinates.
        final double cameraWorldX = (double)cameraX;
        final double cameraWorldY = (double)cameraY;

        List<Unit> units = gameManager.getGame().getUnits();

        // Take a snapshot under the lock so the game thread is not blocked
        // for the entire render duration, eliminating tick-rate contention.
        List<Unit> snapshot;
        synchronized (units) {
            snapshot = new java.util.ArrayList<>(units);
        }

        for (Unit unit : snapshot) {

                // 3. Get unit's precise, smooth position
                final double unitWorldX = unit.getX();
                final double unitWorldY = unit.getY();

                // 4. Rigorous Visibility Culling
                // Check the unit's world coordinate against the visible bounds, plus a 1-tile buffer
                // to account for any part of the unit image spilling over the edge.
                if (unitWorldX < cameraWorldX - 1 || unitWorldY < cameraWorldY - 1 ||
                        unitWorldX > cameraWorldX + viewWidthInPoints + 1 ||
                        unitWorldY > cameraWorldY + viewHeightInPoints + 1) {

                    // offscreen = ignored
                    continue;
                }

                // 5. Calculate relative world position: how far is the unit from the camera's top-left corner?
                final double relativeWorldX = unitWorldX - cameraWorldX;
                final double relativeWorldY = unitWorldY - cameraWorldY;

                // 6. Convert world difference into screen pixels
                final double screenPixelX_double = relativeWorldX * pixelSize;
                final double screenPixelY_double = relativeWorldY * pixelSize;

                // 7. Convert to integer pixels, using rounding for smooth visual alignment.
                final int screenX = (int) Math.round(screenPixelX_double);
                final int screenY = (int) Math.round(screenPixelY_double);

                // --- DRAWING (Fixed Size) ---

                // 7.5 circle surrounding units
                // remove in future
                double unitSize = unit.getSize();
                int circleDiameter = (int) Math.round(unitSize * pixelSize); // Scale size with zoom
                int circleRadius = circleDiameter / 2;

                int circleX = screenX - circleRadius;
                int circleY = screenY - circleRadius;

                g.setColor(Color.pink);
                g.setStroke(new BasicStroke(1));

                g.drawOval(circleX, circleY, circleDiameter, circleDiameter);

                BufferedImage img = ImageManager.getUnitImage(unit.getImageName());

                final int drawX = screenX - (img.getWidth() / 2);
                final int drawY = screenY - (img.getHeight() / 2);

                g.drawImage(img, drawX, drawY, null);
            }
    }

    private void renderEffectsView(Graphics2D g, int pixelSize) {

        // same algorithm as renderUnitsView
        final double viewWidthInTiles = (double)getWidth() / pixelSize;
        final double viewHeightInTiles = (double)getHeight() / pixelSize;
        final double cameraWorldX = (double)cameraX;
        final double cameraWorldY = (double)cameraY;

        List<Effect> effectsSnapshot;
        synchronized (gameManager.getGame().getEffects()) {
            effectsSnapshot = new java.util.ArrayList<>(gameManager.getGame().getEffects());
        }
        for (Effect effect : effectsSnapshot) {

            final double effectWorldX = effect.getX();
            final double effectWorldY = effect.getY();

            if (effectWorldX < cameraWorldX - 1 || effectWorldY < cameraWorldY - 1 ||
                    effectWorldX > cameraWorldX + viewWidthInTiles + 1 ||
                    effectWorldY > cameraWorldY + viewHeightInTiles + 1) {
                continue;
            }

            final double relativeWorldX = effectWorldX - cameraWorldX;
            final double relativeWorldY = effectWorldY - cameraWorldY;

            final double screenPixelX_double = relativeWorldX * pixelSize;
            final double screenPixelY_double = relativeWorldY * pixelSize;

            final int screenX = (int) Math.round(screenPixelX_double);
            final int screenY = (int) Math.round(screenPixelY_double);

            BufferedImage img = ImageManager.getEffectImage(effect.getImageName());
            final int imgWidth = img.getWidth();
            final int imgHeight = img.getHeight();

            if (effect instanceof Missile) {
                // --- ROTATION FOR MISSILES ---
                final double rotationAngle = ((Missile)effect).getRotation();

                final int centerX = screenX + imgWidth / 2;
                final int centerY = screenY + imgHeight / 2;

                AffineTransform oldTransform = g.getTransform();
                g.translate(centerX, centerY);
                g.rotate(rotationAngle);
                g.drawImage(img, -imgWidth / 2, -imgHeight / 2, null);
                g.setTransform(oldTransform);
            } else {
                // --- STANDARD DRAWING FOR OTHER EFFECTS (e.g., Blood Spatter) ---
                final int drawX = screenX - (imgWidth / 2);
                final int drawY = screenY - (imgHeight / 2);
                g.drawImage(img, drawX, drawY, null);
            }
        }
    }

    private void renderPixelView(Graphics2D g, int pixelSize) {
        int tilesAcross = getWidth() / pixelSize + 2;
        int tilesDown = getHeight() / pixelSize + 2;

        for (int x = 0; x < tilesAcross; x++) {
            for (int y = 0; y < tilesDown; y++) {
                int worldX = cameraX + x;
                int worldY = cameraY + y;

                if (worldX < 0 || worldY < 0 || worldX >= mapWidth || worldY >= mapHeight) {
                    continue;
                }

                Point point = map.getPoint(worldX, worldY);
                int screenX = x * pixelSize;
                int screenY = y * pixelSize;


                // TODO come up with a pretty way to display fertilityLevel, and/or terrain beneath farms
                // displaying farm images works at any zoom level but at zoom beneath 10, farms are hard to spot on the map
                g.setColor(point.getColor());
                g.fillRect(screenX, screenY, pixelSize, pixelSize);

                // Draw thin border around each point
                // not drawing if pixel size is too small to avoid clutter
                if (showPointLines && pixelSize >= 6) {
                    g.setColor(new Color(200, 200, 200, 30));
                    // Only draw bottom and right edges to reduce line density
                    g.drawLine(screenX, screenY + pixelSize - 1, screenX + pixelSize - 1, screenY + pixelSize - 1); // bottom
                    g.drawLine(screenX + pixelSize - 1, screenY, screenX + pixelSize - 1, screenY + pixelSize - 1); // right
                }
            }
        }

    }

    private void renderImageView(Graphics2D g, int pixelSize) {
        // Calculate visible tiles.
        int tilesAcross = getWidth() / pixelSize + 2;
        int tilesDown = getHeight() / pixelSize + 2;

        for (int x = 0; x < tilesAcross; x++) {
            for (int y = 0; y < tilesDown; y++) {
                int worldX = cameraX + x;
                int worldY = cameraY + y;

                if (worldX < 0 || worldY < 0 || worldX >= mapWidth || worldY >= mapHeight) {
                    continue;
                }

                Point point = map.getPoint(worldX, worldY);
                Building building = point.getBuilding();

                int screenX = x * pixelSize;
                int screenY = y * pixelSize;

                g.setColor(point.getColor());
                g.fillRect(screenX, screenY, pixelSize, pixelSize);

                if (building != null) {
                    Image img = ImageManager.getBuildingImage(building.getImageName());
                    if (img != null) {
                        g.drawImage(img, screenX, screenY, pixelSize, pixelSize, null);
                    } else {
                        g.setColor(building.getColor());
                        g.fillRect(screenX, screenY, pixelSize, pixelSize);
                    }
                }
            }
        }
    }

    private void drawOverlays(Graphics2D g2d, int pixelSize) {
        if (showLines) {
            g2d.setStroke(new BasicStroke(2.0F));
            g2d.setColor(new Color(237, 180, 102));
            for (Village village : new java.util.ArrayList<>(gameManager.getGame().villages)) {
                if (village.hasOwner()) {
                    g2d.drawLine(
                            (village.getPoint().getX() - cameraX) * pixelSize,
                            (village.getPoint().getY() - cameraY) * pixelSize,
                            (village.getOwner().getPoint().getX() - cameraX) * pixelSize,
                            (village.getOwner().getPoint().getY() - cameraY) * pixelSize
                    );
                }
            }

            g2d.setColor(Color.black);
            for (Town town : new java.util.ArrayList<>(gameManager.getGame().towns)) {
                if (town.hasCity()) {
                    g2d.drawLine(
                            (town.getPoint().getX() - cameraX) * pixelSize,
                            (town.getPoint().getY() - cameraY) * pixelSize,
                            (town.getCity().getPoint().getX() - cameraX) * pixelSize,
                            (town.getCity().getPoint().getY() - cameraY) * pixelSize
                    );
                }
            }
        }

        if (selectedPoint != null) {
            g2d.setColor(Color.RED);
            int sx = (selectedPoint.getX() - cameraX) * pixelSize;
            int sy = (selectedPoint.getY() - cameraY) * pixelSize;
            g2d.drawRect(sx, sy, pixelSize - 1, pixelSize - 1);
        }
    }

    public int getBasePixelSize() {
        return basePixelSize;
    }

    private int getTilesAcross() {
        return getWidth() / getPixelSize();
    }
    private int getTilesDown() {
        return getHeight() / getPixelSize();
    }

    public void moveCameraUp() {
        cameraY -= getTilesDown() / 3;
        clampCameraToWorld();
        repaint();
    }

    public void moveCameraDown() {
        cameraY += getTilesDown() / 3;
        clampCameraToWorld();
        repaint();
    }

    public void moveCameraLeft() {
        cameraX -= getTilesAcross() / 3;
        clampCameraToWorld();
        repaint();
    }

    public void moveCameraRight() {
        cameraX += getTilesAcross() / 3;
        clampCameraToWorld();
        repaint();
    }

    public double getZoomLimit() {
        return zoomLimit;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public void zoomIn()  { zoomBy(getZoomFactor()); }
    public void zoomOut() { zoomBy(1.0 / getZoomFactor()); }

    private void zoomBy(double factor) {

        int oldPx = getPixelSize();

        double centerWorldX;
        double centerWorldY;

        if (selectedPoint != null) {
            centerWorldX = selectedPoint.getX();
            centerWorldY = selectedPoint.getY();
        } else {
            centerWorldX = cameraX + (getWidth() / 2.0) / oldPx;
            centerWorldY = cameraY + (getHeight() / 2.0) / oldPx;
        }

        zoom *= factor;
        if (zoom < getZoomLimit()) zoom = getZoomLimit();

        int newPx = getPixelSize();
        if (!imageManagerInitialized && newPx >= getPixelPixelSize()) {
            ImageManager.initialize(newPx);
            imageManagerInitialized = true;
        }

        if (imageManagerInitialized) {
            ImageManager.onZoomChange(newPx);
        }

        // set camera so the same world point remains at the screen center
        cameraX = (int) Math.floor(centerWorldX - (getWidth()  / 2.0) / newPx);
        cameraY = (int) Math.floor(centerWorldY - (getHeight() / 2.0) / newPx);

        clampCameraToWorld();
        repaint();
    }

    private void clampCameraToWorld() {
        int tilesAcross = Math.max(1, getWidth()  / getPixelSize());
        int tilesDown   = Math.max(1, getHeight() / getPixelSize());

        cameraX = Math.max(0, Math.min(cameraX, mapWidth  - tilesAcross));
        cameraY = Math.max(0, Math.min(cameraY, mapHeight - tilesDown));
    }

    public int getPixelSize() {
        return (int) Math.max(1, Math.round(basePixelSize * zoom));
    }

    private void handleMouseClick(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        int gridX = cameraX + (mouseX / getPixelSize());
        int gridY = cameraY + (mouseY / getPixelSize());

        if (gridX >= 0 && gridX < mapWidth && gridY >= 0 && gridY < mapHeight) {
            Point clickedPoint = map.getPoint(gridX, gridY);
            if (clickedPoint == selectedPoint){
                selectedPoint = null;
            }
            else {
                selectedPoint = clickedPoint;
                System.out.println("Point Info: " + clickedPoint.getInfo());
            }
        }
        updateUI();
    }

    public void updateMap(Map map){
        this.map = map;
        selectedPoint = null;
        showLines = false;
    }

    public void changeShowLines() {
        showLines = !showLines;
    }

    public boolean isShowLines(){
        return showLines;
    }

    public empirebuilder.Point getSelectedPoint() {
        return selectedPoint;
    }

    public boolean isDisplayBuildingImages() {
        return displayBuildingImages;
    }

    public void setDisplayBuildingImages(boolean displayBuildingImages) {
        this.displayBuildingImages = displayBuildingImages;
        repaint();
    }
}
