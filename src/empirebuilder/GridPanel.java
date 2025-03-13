package empirebuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GridPanel extends JPanel {
    
    GameManager gameManager;
    Map map;
    int pixelSize;
    int height;
    int width;
    private Point selectedPoint = null;

    GridPanel(GameManager gameManager, Map map, int width, int height, int pixelSize, int buttonPanelWidth) {
        this.map = map;
        this.gameManager = gameManager;
        this.pixelSize = pixelSize;
        this.height = height;
        this.width = width;
        setPreferredSize(new Dimension((width-buttonPanelWidth), height));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e){
                handleMouseClick(e);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the grid
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Point point = map.getPoint(x, y);
                g.setColor(point.getColor());
                g.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
            }
        }
        if (selectedPoint != null){
                    g.setColor(Color.RED);
                    g.drawRect(selectedPoint.getX() * pixelSize, selectedPoint.getY() * pixelSize, pixelSize-1, pixelSize-1);
        }
    }
    
    private void handleMouseClick(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        // Convert pixel coordinates to grid coordinates
        int gridX = mouseX / pixelSize;
        int gridY = mouseY / pixelSize;

        // Ensure we don't go out of bounds
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

    public empirebuilder.Point getSelectedPoint() {
        return selectedPoint;
    }
    
    
    
}
