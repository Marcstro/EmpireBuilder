package empirebuilder;

import buildings.*;
import graphics.ImageManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GridPanel extends JPanel {

    private final int width;
    private final int height;
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
    private boolean displayBuildingImages = false;

    GameManager gameManager;

    GridPanel(GameManager gameManager, Map map, int width, int height, int pixelSize, int buttonPanelWidth) {
        this.map = map;
        this.gameManager = gameManager;
        this.height = height;
        this.width = width;
        setPreferredSize(new Dimension((width * pixelSize), (height * pixelSize)));
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
    }

    private void renderPixelView(Graphics2D g, int pixelSize) {
        // Calculate visible points
        int tilesAcross = getWidth() / pixelSize + 2;
        int tilesDown = getHeight() / pixelSize + 2;

        for (int x = 0; x < tilesAcross; x++) {
            for (int y = 0; y < tilesDown; y++) {
                int worldX = cameraX + x;
                int worldY = cameraY + y;

                if (worldX < 0 || worldY < 0 || worldX >= width || worldY >= height) {
                    continue;
                }

                Point point = map.getPoint(worldX, worldY);
                Building building = point.getBuilding();

                int screenX = x * pixelSize;
                int screenY = y * pixelSize;

                // TODO pixelpainted farms should only cover 75% of the area, should show land beneath
                /*if (building instanceof Farm) {
                    g.setColor(point.getColor());
                    g.fillRect(screenX, screenY, pixelSize, pixelSize);

                    int innerSize = pixelSize / 2;
                    int offset = (pixelSize - innerSize) / 2;
                    g.setColor(building.getColor());
                    g.fillRect(screenX + offset, screenY + offset, innerSize, innerSize);
                }  */
                if (building != null) {
                    g.setColor(building.getColor());
                    g.fillRect(screenX, screenY, pixelSize, pixelSize);
                } else {
                    g.setColor(point.getColor());
                    g.fillRect(screenX, screenY, pixelSize, pixelSize);
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

                if (worldX < 0 || worldY < 0 || worldX >= width || worldY >= height) {
                    continue;
                }

                Point point = map.getPoint(worldX, worldY);
                Building building = point.getBuilding();

                int screenX = x * pixelSize;
                int screenY = y * pixelSize;

                g.setColor(point.getColor());
                g.fillRect(screenX, screenY, pixelSize, pixelSize);

                if (building != null) {
                    Image img = ImageManager.getBuildingImage(building.getClass());
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
        if (cameraY < 0) cameraY = 0;
        repaint();
    }

    public void moveCameraDown() {
        cameraY += getTilesDown() / 3;
        if (cameraY > height - getTilesDown()) cameraY = height - getTilesDown();
        repaint();
    }

    public void moveCameraLeft() {
        cameraX -= getTilesAcross() / 3;
        if (cameraX < 0) cameraX = 0;
        repaint();
    }

    public void moveCameraRight() {
        cameraX += getTilesAcross() / 3;
        if (cameraX > width - getTilesAcross()) cameraX = width - getTilesAcross();
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

        double centerWorldX = cameraX + (getWidth()  / 2.0) / oldPx;
        double centerWorldY = cameraY + (getHeight() / 2.0) / oldPx;

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

        cameraX = Math.max(0, Math.min(cameraX, width  - tilesAcross));
        cameraY = Math.max(0, Math.min(cameraY, height - tilesDown));
    }

    public int getPixelSize() {
        return (int) Math.max(1, Math.round(basePixelSize * zoom));
    }

    private void handleMouseClick(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        int gridX = cameraX + (mouseX / getPixelSize());
        int gridY = cameraY + (mouseY / getPixelSize());

        if (gridX >= 0 && gridX < width && gridY >= 0 && gridY < height) {
            Point clickedPoint = map.getPoint(gridX, gridY);
            if (clickedPoint == selectedPoint){
                selectedPoint = null;
            }
            else {
                selectedPoint = clickedPoint;
                System.out.println("Clicked on: (" + gridX + ", " + gridY + ")");
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
