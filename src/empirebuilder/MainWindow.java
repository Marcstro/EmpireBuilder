package empirebuilder;

import javax.swing.*;
import java.awt.*;

public class MainWindow {
    
    GameManager gameManager;
    JFrame frame;
    
    MainWindow(GameManager gameManager, GridPanel gridPanel, ButtonPanel buttonPanel, int width, int height) {
        
        this.gameManager = gameManager;
        frame = new JFrame("Empire Builder");
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
     
        frame.add(gridPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.EAST);

        frame.setVisible(true);
        frame.pack();
    }
}
