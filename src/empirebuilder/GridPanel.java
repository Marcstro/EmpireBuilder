package empirebuilder;

import buildings.Town;
import buildings.Village;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class GridPanel extends JPanel {
    
    GameManager gameManager;
    Map map;
    int pixelSize;
    int height;
    int width;
    private Point selectedPoint = null;
    private boolean showLines = true;

    GridPanel(GameManager gameManager, Map map, int width, int height, int pixelSize, int buttonPanelWidth) {
        this.map = map;
        this.gameManager = gameManager;
        this.pixelSize = pixelSize;
        this.height = height;
        this.width = width;
        setPreferredSize(new Dimension((width * pixelSize), (height * pixelSize)));
        
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

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                g.setColor(map.getPoint(x, y).getColor());
                g.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
            }
        }


        if (showLines){
            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(2.0F));
            g.setColor(new Color( 237, 180, 102 ));
            for(Village village: new ArrayList<>(gameManager.getGame().villages)){
                if (village.hasTown()){
                    g2d.drawLine(village.getPoint().getX()* pixelSize, village.getPoint().getY()* pixelSize,
                            village.getTown().getPoint().getX()* pixelSize, village.getTown().getPoint().getY()* pixelSize);
                }
            }

            g.setColor(Color.black);
            for (Town town: new ArrayList<>(gameManager.getGame().towns)){
                if (town.hasCity()){
                    g2d.drawLine(town.getPoint().getX()* pixelSize, town.getPoint().getY()* pixelSize,
                            town.getCity().getPoint().getX()* pixelSize, town.getCity().getPoint().getY()* pixelSize);
                }
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

        int gridX = mouseX / pixelSize;
        int gridY = mouseY / pixelSize;

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
