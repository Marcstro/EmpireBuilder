package empirebuilder;

import javax.swing.*;
import java.awt.*;

// Main Window Class
public class MainWindow {
    
    GameManager gameManager;
    
    MainWindow(GameManager gameManager, JScrollPane gridPanel, ButtonPanel buttonPanel, int width, int height) {
        
        this.gameManager = gameManager;
        JFrame frame = new JFrame("Empire Builder");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
     
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST);

        frame.setVisible(true);
        frame.pack();
    }
}
