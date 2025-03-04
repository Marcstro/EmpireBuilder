package empirebuilder;

import javax.swing.*;
import java.awt.*;

public class ButtonPanel extends JPanel {
    
    GameManager gameManager;
    
    ButtonPanel(GameManager gameManager) {
        
        this.gameManager = gameManager;
        
        setPreferredSize(new Dimension(200, 800));
        setLayout(new GridLayout(5, 1, 10, 10));

        JButton button1 = new JButton("Start");
        JButton button2 = new JButton("Stop");
        JButton button3 = new JButton("Create random farm");
        JButton button4 = new JButton("create farm next to farm 0");
        JButton button5 = new JButton("experiment 2");
        JButton button6 = new JButton("experiment 3");
        JButton button7 = new JButton("test1");
        JButton button8 = new JButton("create 6 farms");

        add(button1);
        add(button2);
        add(button3);
        add(button4);
        add(button5);
        add(button6);
        add(button7);
        add(button8);
        
        button1.addActionListener(e -> gameManager.getEngine().start());
        button2.addActionListener(e -> gameManager.getEngine().stop());
        button3.addActionListener(e -> gameManager.getGame().createFarmAtRandomPoint());
        button4.addActionListener(e -> gameManager.getGame().experiment());
        button5.addActionListener(e -> gameManager.getGame().experiment2());
        button6.addActionListener(e -> gameManager.getGame().experiment3());
        button7.addActionListener(e -> gameManager.getGame().experiment4());
        button8.addActionListener(e -> gameManager.getGame().experiment5());
    }
}
