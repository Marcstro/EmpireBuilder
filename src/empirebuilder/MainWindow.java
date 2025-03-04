package empirebuilder;

import javax.swing.*;
import java.awt.*;

// Main Window Class
public class MainWindow {
    
    GameManager gameManager;

    /*
    public static void main(String[] args) {
        // Create the main frame
        JFrame frame = new JFrame("Grid Map Application");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create the grid map and button panel
        GridPanel gridPanel = new GridPanel();
        ButtonPanel buttonPanel = new ButtonPanel();

        // Add panels to the frame
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST);

        frame.setVisible(true);
    }*/
    
    MainWindow(GameManager gameManager, GridPanel gridPanel, ButtonPanel buttonPanel, int width, int height) {
        
        this.gameManager = gameManager;
        JFrame frame = new JFrame("Grid Map Application");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
     
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST);

        frame.setVisible(true);
    }
}
