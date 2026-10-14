package empirebuilder;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import buildingsTools.TerrainGeneratorType;

public class ButtonPanel extends JPanel {
    
    GameManager gameManager;
    private TextField tickRateLabel;
    private TextField tickCounterLabel;
    private JLabel zoomValueLabel;
    private Timer tickCounterTimer;
    final int WIDTH = 200;
    final int HEIGHT = 800;
    final int BUTTONHEIGHT = 30;
    
    ButtonPanel(GameManager gameManager) {
        
        
        this.gameManager = gameManager;
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLayout(new BorderLayout());

        //different components

        JCheckBox displayBuildingImagesCheck = new JCheckBox("Building images",
                gameManager.getGridPanel().isDisplayBuildingImages());
        JCheckBox generateTerrainCheck = new JCheckBox("Generate terrain",
                gameManager.getWorldSettings().isGenerateTerrain());
        JComboBox<TerrainGeneratorType> terrainTypeDropdown =
                new JComboBox<>(TerrainGeneratorType.values());
        terrainTypeDropdown.setSelectedItem(gameManager.getWorldSettings().getGeneratorType());
        terrainTypeDropdown.setEnabled(generateTerrainCheck.isSelected());

        JCheckBox displayOwnershipLines = new JCheckBox("Display lines",
                gameManager.getGridPanel().isShowLines());
        JCheckBox enableDarksideCheck = new JCheckBox("Enable darkside",
                gameManager.getGame().getDarkside().isEvilSideSpawningEnabled());

        // Camera movements
        JPanel cameraControlPanel = new JPanel();
        cameraControlPanel.setBorder(BorderFactory.createTitledBorder("Camera Controls")); // gives it a label
        cameraControlPanel.setLayout(new GridLayout(2, 3, 5, 5)); // 2 rows, 3 cols, with gaps

        JButton zoomInBtn = new JButton("+");
        JButton upBtn = new JButton("↑");
        JButton zoomOutBtn = new JButton("−");

        JButton leftBtn = new JButton("←");
        JButton downBtn = new JButton("↓");
        JButton rightBtn = new JButton("→");

        cameraControlPanel.add(zoomInBtn);
        cameraControlPanel.add(upBtn);
        cameraControlPanel.add(zoomOutBtn);

        cameraControlPanel.add(leftBtn);
        cameraControlPanel.add(downBtn);
        cameraControlPanel.add(rightBtn);

        zoomValueLabel = new JLabel(String.valueOf(gameManager.gridPanel.getPixelSize()), SwingConstants.CENTER);
        zoomValueLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JPanel zoomDisplayPanel = new JPanel();
        zoomDisplayPanel.setLayout(new BorderLayout());
        zoomDisplayPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));
        zoomDisplayPanel.add(zoomValueLabel, BorderLayout.CENTER);
        Dimension zoomPanelSize = new Dimension(180, 45);
        zoomDisplayPanel.setPreferredSize(zoomPanelSize);
        zoomDisplayPanel.setMaximumSize(zoomPanelSize);
        zoomDisplayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cameraWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        cameraWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        cameraWrapper.add(cameraControlPanel);
        cameraWrapper.setMaximumSize(new Dimension(180, cameraWrapper.getPreferredSize().height));

        JButton startButton = new JButton("Start");
        JButton stopButton = new JButton("Stop");
        JButton quickstartButton = new JButton("Quickstart");
        JButton printMapInfoButton = new JButton("Print Map info");
        JButton recreateMapButton = new JButton("Recreate the map");

        Map<String, Runnable> testActions = new LinkedHashMap<>();
        testActions.put("NONE", () -> {});
        testActions.put("Create random farm", () -> gameManager.getGame().getTestFunctions().createFarmAtRandomPoint());
        testActions.put("Create 20 random farms", () -> gameManager.getGame().getTestFunctions().create20FarmAtRandomPoint());
        testActions.put("Experiment 2", () -> gameManager.getGame().getTestFunctions().experiment2());
        testActions.put("Calculate amount of farms", () -> gameManager.getGame().getTestFunctions().experiment3());
        testActions.put("Test function time", () -> gameManager.getGame().getTestFunctions().experiment3());
        testActions.put("Create 6 farms", () -> gameManager.getGame().getTestFunctions().experiment5());
        testActions.put("Create 13 farms in a circle", () ->
                gameManager.getGame().getTestFunctions().create13FarmsInCircleAtRandomPoint());
        testActions.put("Create farm at point", () -> {
            Point point = getSelectedPointOrNotify();
            if (point != null) {
                gameManager.getGame().getTestFunctions().createFarmAtPoint(point.getX(), point.getY());
            }
        });
        testActions.put("Create complete village at point", () -> {
            Point point = getSelectedPointOrNotify();
            if (point != null) {
                gameManager.getGame().getTestFunctions().createCompleteVillageAt(point.getX(), point.getY());
            }
        });
        testActions.put("Add ALL farms", () -> gameManager.getGame().getTestFunctions().experiment15());
        testActions.put("Check village domains", () -> gameManager.getGame().getTestFunctions().checkVillageDomain());
        testActions.put("Create path of water", () -> gameManager.getGame().getTestFunctions().createWaterPath());
        testActions.put("Current test", () -> gameManager.getGame().getTestFunctions().currentExperiment());
        testActions.put("Timing test", () -> gameManager.getGame().getTestFunctions().experimentTimeMethod());
        testActions.put("Spawn Orc", () -> gameManager.getGame().getTestFunctions().spawnOrcRandomlyTest());
        testActions.put("Current test 2", () -> gameManager.getGame().getTestFunctions().currentExperiment2());

        JComboBox<String> testDropdown =
                new JComboBox<>(testActions.keySet().toArray(new String[0]));
        JButton runTestButton = new JButton("Run test");

        // Set sizes and dimensions.
        JButton[] controlButtons = {
                startButton, stopButton, quickstartButton, printMapInfoButton, recreateMapButton, runTestButton
        };
        for (JButton button : controlButtons) {
            button.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        displayBuildingImagesCheck.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
        generateTerrainCheck.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
        displayOwnershipLines.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
        enableDarksideCheck.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
        terrainTypeDropdown.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
        terrainTypeDropdown.setMaximumSize(new Dimension(180, BUTTONHEIGHT));
        terrainTypeDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);
        testDropdown.setPreferredSize(new Dimension(180, BUTTONHEIGHT));
        testDropdown.setMaximumSize(new Dimension(180, BUTTONHEIGHT));
        testDropdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel testPanel = new JPanel();
        testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.Y_AXIS));
        testPanel.setBorder(BorderFactory.createTitledBorder("Tests"));
        testPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        testPanel.add(testDropdown);
        testPanel.add(runTestButton);

        //add things
        
        JPanel buttonArea = new JPanel();
        buttonArea.setLayout(new BoxLayout(buttonArea, BoxLayout.Y_AXIS));
        buttonArea.add(Box.createRigidArea(new Dimension(0, 5)));
        JScrollPane scrollPane = new JScrollPane(buttonArea);
        add(scrollPane, BorderLayout.CENTER);

        buttonArea.add(startButton);
        buttonArea.add(stopButton);
        buttonArea.add(quickstartButton);
        buttonArea.add(printMapInfoButton);
        buttonArea.add(recreateMapButton);
        buttonArea.add(testPanel);
        buttonArea.add(displayOwnershipLines);
        buttonArea.add(enableDarksideCheck);
        buttonArea.add(displayBuildingImagesCheck);
        buttonArea.add(generateTerrainCheck);
        buttonArea.add(terrainTypeDropdown);
        buttonArea.add(zoomDisplayPanel);
        buttonArea.add(cameraWrapper);

        // TODO move these to appropriete parts

        JPanel tickRatePanel = new JPanel();
        tickRatePanel.setLayout(new GridLayout(3,1));
        
        JLabel tickRateTitle = new JLabel("Tick rate:", SwingConstants.CENTER);
        tickRateLabel = new TextField(String.format("%.3f ", gameManager.getEngine().getTickRate(), SwingConstants.CENTER));
        tickRateLabel.setFocusable(false);

        JLabel tickCounterTitle = new JLabel("Tick counter:", SwingConstants.CENTER);
        tickCounterLabel = new TextField(String.valueOf(gameManager.getEngine().getTickCounter()));
        tickCounterLabel.setFocusable(false);

        tickCounterTimer = new Timer(250, e -> {
            updateTickCounterDisplay();
            updateTickRateDisplay();
        });
        tickCounterTimer.start();

        JPanel speedControlPanel = new JPanel(new FlowLayout());
        JButton increaseSpeedButton = new JButton("+");
        JButton decreaseSpeedButton = new JButton("−");

        speedControlPanel.add(decreaseSpeedButton);
        speedControlPanel.add(increaseSpeedButton);

        tickRatePanel.add(tickRateTitle);
        tickRatePanel.add(tickRateLabel);
        tickRatePanel.add(tickCounterTitle);
        tickRatePanel.add(tickCounterLabel);
        tickRatePanel.add(speedControlPanel);

        add(tickRatePanel, BorderLayout.SOUTH);

        // listeners

        startButton.addActionListener(e -> gameManager.getEngine().start());
        stopButton.addActionListener(e -> gameManager.getEngine().stop());
        quickstartButton.addActionListener(e -> gameManager.getGame().getTestFunctions().quickstart());
        printMapInfoButton.addActionListener(e -> gameManager.getGame().getTestFunctions().printMapInfo());
        recreateMapButton.addActionListener(e -> gameManager.recreateWorld());
        runTestButton.addActionListener(e -> {
            String selectedTest = (String) testDropdown.getSelectedItem();
            Runnable testAction = testActions.get(selectedTest);
            if (testAction != null) {
            testAction.run();
            }
        });

        zoomInBtn.addActionListener(e -> {
            gameManager.getGridPanel().zoomIn();
            updateZoomDisplay();
        });

        zoomOutBtn.addActionListener(e -> {
            gameManager.getGridPanel().zoomOut();
            updateZoomDisplay();
        });

        upBtn.addActionListener(e -> gameManager.getGridPanel().moveCameraUp());
        downBtn.addActionListener(e -> gameManager.getGridPanel().moveCameraDown());
        leftBtn.addActionListener(e -> gameManager.getGridPanel().moveCameraLeft());
        rightBtn.addActionListener(e -> gameManager.getGridPanel().moveCameraRight());


        displayOwnershipLines.addActionListener(e -> {
            gameManager.getGridPanel().changeShowLines();
            gameManager.getGridPanel().updateUI();
        });

        enableDarksideCheck.addActionListener(e ->
                gameManager.getGame().getDarkside()
                        .setEvilSideSpawningEnabled(enableDarksideCheck.isSelected()));

        displayBuildingImagesCheck.addActionListener(e -> {
            boolean enabled = displayBuildingImagesCheck.isSelected();
            gameManager.getGridPanel().setDisplayBuildingImages(enabled);
        });

        generateTerrainCheck.addActionListener(e -> {
            boolean enabled = generateTerrainCheck.isSelected();
            gameManager.getWorldSettings().setGenerateTerrain(enabled);
            terrainTypeDropdown.setEnabled(enabled);
        });

        terrainTypeDropdown.addActionListener(e -> {
            TerrainGeneratorType selected = (TerrainGeneratorType) terrainTypeDropdown.getSelectedItem();
            gameManager.getWorldSettings().setGeneratorType(selected);
        });

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

    public void updateTickCounterDisplay() {
        tickCounterLabel.setText(String.valueOf(gameManager.getEngine().getTickCounter()));
    }

    public void updateZoomDisplay() {
        zoomValueLabel.setText(String.format(String.valueOf(gameManager.getGridPanel().getPixelSize())));    }

    private Point getSelectedPointOrNotify() {
        Point selectedPoint = gameManager.getGridPanel().getSelectedPoint();
        if (selectedPoint == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a point on the map first.",
                    "No map point selected",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return selectedPoint;
    }
}
