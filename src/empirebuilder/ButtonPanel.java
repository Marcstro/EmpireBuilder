package empirebuilder;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ButtonPanel extends JPanel {
    
    GameManager gameManager;
    private TextField tickRateLabel;
    final int WIDTH = 200;
    final int HEIGHT = 800;
    final int BUTTONHEIGHT = 30;
    
    ButtonPanel(GameManager gameManager) {
        
        
        this.gameManager = gameManager;
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(new BorderLayout());

        JButton button1 = new JButton("Start");
        JButton button2 = new JButton("Stop");
        JButton button3 = new JButton("Create random farm");
        JButton button4 = new JButton("create farm next to farm 0");
        JButton button5 = new JButton("experiment 2");
        JButton button6 = new JButton("experiment 3");
        JButton button7 = new JButton("test1");
        JButton button8 = new JButton("create 6 farms");
        
        ArrayList<JButton> buttons = new ArrayList<>(Arrays.asList(button1, button2, button3, button4, button5, button6, button7, button8));
        buttons.forEach(button -> {
            button.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
        });
        
        JPanel buttonArea = new JPanel();
        buttonArea.setLayout(new BoxLayout(buttonArea, BoxLayout.Y_AXIS));
        buttonArea.add(Box.createRigidArea(new Dimension(0, 5)));
        JScrollPane scrollPane = new JScrollPane(buttonArea);
        add(scrollPane, BorderLayout.CENTER);

        buttonArea.add(button1);
        buttonArea.add(button2);
        buttonArea.add(button3);
        buttonArea.add(button4);
        buttonArea.add(button5);
        buttonArea.add(button6);
        buttonArea.add(button7);
        buttonArea.add(button8);
        
        JPanel tickRatePanel = new JPanel();
        tickRatePanel.setLayout(new GridLayout(3,1));
        
        JLabel tickRateTitle = new JLabel("Tick rate:", SwingConstants.CENTER);
        tickRateLabel = new TextField(String.format("%.3f ", gameManager.getEngine().getTickRate(), SwingConstants.CENTER));
        tickRateLabel.setFocusable(false);
        
        
        JPanel speedControlPanel = new JPanel(new FlowLayout());
        JButton increaseSpeedButton = new JButton("+");
        JButton decreaseSpeedButton = new JButton("−");

        speedControlPanel.add(decreaseSpeedButton);
        speedControlPanel.add(increaseSpeedButton);

        tickRatePanel.add(tickRateTitle);
        tickRatePanel.add(tickRateLabel);
        tickRatePanel.add(speedControlPanel);

        add(buttonArea, BorderLayout.CENTER);
        add(tickRatePanel, BorderLayout.SOUTH);
        
        button1.addActionListener(e -> gameManager.getEngine().start());
        button2.addActionListener(e -> gameManager.getEngine().stop());
        button3.addActionListener(e -> gameManager.getGame().createFarmAtRandomPoint());
        button4.addActionListener(e -> gameManager.getGame().experiment());
        button5.addActionListener(e -> gameManager.getGame().experiment2());
        button6.addActionListener(e -> gameManager.getGame().experiment3());
        button7.addActionListener(e -> gameManager.getGame().experiment4());
        button8.addActionListener(e -> gameManager.getGame().experiment5());
        
        increaseSpeedButton.addActionListener(e -> {
            gameManager.getEngine().increaseSpeed();
            updateTickRateDisplay();
        });

        decreaseSpeedButton.addActionListener(e -> {
            gameManager.getEngine().decreaseSpeed();
            updateTickRateDisplay();
        });
    }
    
    private void updateTickRateDisplay() {
        tickRateLabel.setText(String.format("%.3f s", gameManager.getEngine().getTickRate()));
    }
}
