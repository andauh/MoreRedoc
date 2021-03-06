package moreredoc.application;

import moreredoc.analysis.MoreRedocAnalysisConfiguration;
import moreredoc.application.exceptions.InvalidRequirementInputException;
import moreredoc.datainput.tools.SupportedRedocumentationTools;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Hastily developed UI to make application usable without using the API. Code
 * is mostly generated with the help of the WindowBuilder Eclipse Plugin. This
 * UI is just for quickstarting the modeling process, therefore no attention was
 * paid to refactorability.
 */
public class MoreRedocGui extends JFrame {
    /**
     * automatically generated
     */
    private static final long serialVersionUID = 618505241078949953L;
    private static final String APPLICATION_TITLE = "MoreRedoc";

    private static final Logger logger = Logger.getLogger(MoreRedocGui.class);

    // ui elements
    private final JComboBox<SupportedRedocumentationTools> comboBoxToolSelection;
    private final JTextField textfieldCsvKeywords;
    private final JTextField textfieldCsvText;
    private final JTextField textfieldOutputFolder;

    private final JCheckBox cbVerbsMethods;
    private final JCheckBox cbVerbsRelationships;
    private final JCheckBox cbCropEmptyClasses;
    private final JCheckBox cbRawXmi;
    private final JCheckBox cbArgoUml;
    private final JCheckBox cbStarUml;
    private final JCheckBox cbPng;
    private final JCheckBox cbSvg;
    private final JCheckBox cbPlantText;

    private final JButton buttonGenerateModels;

    // preload file chooser, on demand would take noticeably long.
    private final transient FutureTask<JFileChooser> futureFileChooser = new FutureTask<>(JFileChooser::new);
    private final JButton buttonChooseKeywordsCsv;
    private final JButton buttonChooseTextCsv;
    private final JButton buttonChooseOutputFolder;
    private final JButton buttonConfigure;

    private static final String ERROR_MESSAGE_INVALID_INPUT = "An error occurred while the input files were parsed. Please make sure that both requirement inputs are valid.";
    private static final String ERROR_HEADER_INVALID_INPUT = "Error parsing input files";

    public static final String ERROR_MESSAGE_UNSPECIFIED = "Error.";
    public static final String ERROR_HEADER_UNSPECIFIED = "An error occurred, see logging.";

    public MoreRedocGui() {
        super(APPLICATION_TITLE);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(futureFileChooser);

        Border border = BorderFactory.createDashedBorder(Color.BLACK);
        Border margin = new EmptyBorder(10, 10, 10, 10);
        mainPanel.setBorder(new CompoundBorder(border, margin));

        this.getContentPane().add(mainPanel, BorderLayout.CENTER);
        GridBagLayout gblPanel = new GridBagLayout();
        gblPanel.columnWidths = new int[] { 0, 0, 0, 0 };
        gblPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
        gblPanel.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
        gblPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                Double.MIN_VALUE };
        mainPanel.setLayout(gblPanel);

        int currentRow = 0;

        JLabel labelToolSelection = new JLabel("Tool:");
        GridBagConstraints gbcLabelToolSelection = new GridBagConstraints();
        gbcLabelToolSelection.anchor = GridBagConstraints.WEST;
        Insets defaultInsets = new Insets(0, 0, 5, 5);
        gbcLabelToolSelection.insets = defaultInsets;
        gbcLabelToolSelection.gridx = 0;
        gbcLabelToolSelection.gridy = currentRow;
        mainPanel.add(labelToolSelection, gbcLabelToolSelection);

        comboBoxToolSelection = new JComboBox<>(SupportedRedocumentationTools.values());
        GridBagConstraints gbcComboBoxToolSelection = new GridBagConstraints();
        gbcComboBoxToolSelection.insets = defaultInsets;
        gbcComboBoxToolSelection.fill = GridBagConstraints.HORIZONTAL;
        gbcComboBoxToolSelection.gridx = 1;
        gbcComboBoxToolSelection.gridy = currentRow;
        mainPanel.add(comboBoxToolSelection, gbcComboBoxToolSelection);

        buttonConfigure = new JButton("Configure");
        buttonConfigure.addActionListener(e -> comboBoxToolSelection.getItemAt(comboBoxToolSelection.getSelectedIndex()).getDataHandler().configure());
        GridBagConstraints gbcButtonConfigure = new GridBagConstraints();
        gbcButtonConfigure.anchor = GridBagConstraints.WEST;
        gbcButtonConfigure.insets = new Insets(0, 0, 5, 0);
        gbcButtonConfigure.gridx = 2;
        gbcButtonConfigure.gridy = currentRow;
        gbcButtonConfigure.fill = GridBagConstraints.HORIZONTAL;
        gbcButtonConfigure.weightx = gbcButtonConfigure.weighty = 1;
        mainPanel.add(buttonConfigure, gbcButtonConfigure);

        currentRow++;
        JLabel labelKeywords = new JLabel("Keywords:");
        GridBagConstraints gbcLabelKeywords = new GridBagConstraints();
        gbcLabelKeywords.anchor = GridBagConstraints.WEST;
        gbcLabelKeywords.insets = defaultInsets;
        gbcLabelKeywords.gridx = 0;
        gbcLabelKeywords.gridy = currentRow;
        mainPanel.add(labelKeywords, gbcLabelKeywords);

        textfieldCsvKeywords = new JTextField();
        textfieldCsvKeywords.setEnabled(false);
        GridBagConstraints gbcTextfieldCsvKeywords = new GridBagConstraints();
        gbcTextfieldCsvKeywords.insets = defaultInsets;
        gbcTextfieldCsvKeywords.fill = GridBagConstraints.HORIZONTAL;
        gbcTextfieldCsvKeywords.gridx = 1;
        gbcTextfieldCsvKeywords.gridy = currentRow;
        mainPanel.add(textfieldCsvKeywords, gbcTextfieldCsvKeywords);
        textfieldCsvKeywords.setColumns(10);

        String textChooseButton = "Select";
        buttonChooseKeywordsCsv = new JButton(textChooseButton);
        buttonChooseKeywordsCsv.addActionListener(e -> {
            try {
                chooseFileAndUpdateTextfield(textfieldCsvKeywords, mainPanel, JFileChooser.FILES_ONLY);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                logger.info(e1);
            } catch (ExecutionException e1) {
                logger.info(e1);
            }
        });
        GridBagConstraints gbcButtonChooseKeywords = new GridBagConstraints();
        gbcButtonChooseKeywords.anchor = GridBagConstraints.WEST;
        gbcButtonChooseKeywords.insets = new Insets(0, 0, 5, 0);
        gbcButtonChooseKeywords.gridx = 2;
        gbcButtonChooseKeywords.gridy = currentRow;
        gbcButtonChooseKeywords.fill = GridBagConstraints.HORIZONTAL;
        gbcButtonChooseKeywords.weightx = gbcButtonConfigure.weighty = 1;
        mainPanel.add(buttonChooseKeywordsCsv, gbcButtonChooseKeywords);

        currentRow++;
        JLabel labelCsvText = new JLabel("Text:");
        labelCsvText.setHorizontalAlignment(SwingConstants.CENTER);
        GridBagConstraints gbcLabelCsvText = new GridBagConstraints();
        gbcLabelCsvText.anchor = GridBagConstraints.WEST;
        gbcLabelCsvText.insets = defaultInsets;
        gbcLabelCsvText.gridx = 0;
        gbcLabelCsvText.gridy = currentRow;
        mainPanel.add(labelCsvText, gbcLabelCsvText);

        textfieldCsvText = new JTextField();
        textfieldCsvText.setEnabled(false);
        GridBagConstraints gbcTextfieldCsvText = new GridBagConstraints();
        gbcTextfieldCsvText.insets = defaultInsets;
        gbcTextfieldCsvText.fill = GridBagConstraints.HORIZONTAL;
        gbcTextfieldCsvText.gridx = 1;
        gbcTextfieldCsvText.gridy = currentRow;
        mainPanel.add(textfieldCsvText, gbcTextfieldCsvText);
        textfieldCsvText.setColumns(10);

        buttonChooseTextCsv = new JButton(textChooseButton);
        buttonChooseTextCsv.addActionListener(e -> {
            try {
                chooseFileAndUpdateTextfield(textfieldCsvText, mainPanel, JFileChooser.FILES_ONLY);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                logger.info(e1);
            } catch (ExecutionException e1) {
                logger.info(e1);
            }
        });
        GridBagConstraints gbcButtonChooseTextCsv = new GridBagConstraints();
        gbcButtonChooseTextCsv.anchor = GridBagConstraints.WEST;
        gbcButtonChooseTextCsv.insets = new Insets(0, 0, 5, 0);
        gbcButtonChooseTextCsv.gridx = 2;
        gbcButtonChooseTextCsv.gridy = currentRow;
        gbcButtonChooseTextCsv.fill = GridBagConstraints.HORIZONTAL;
        gbcButtonChooseTextCsv.weightx = gbcButtonConfigure.weighty = 1;
        mainPanel.add(buttonChooseTextCsv, gbcButtonChooseTextCsv);

        currentRow++;
        JLabel labelOutputFolder = new JLabel("Output folder:");
        GridBagConstraints gbcLabelOutputFolder = new GridBagConstraints();
        gbcLabelOutputFolder.anchor = GridBagConstraints.WEST;
        gbcLabelOutputFolder.insets = defaultInsets;
        gbcLabelOutputFolder.gridx = 0;
        gbcLabelOutputFolder.gridy = currentRow;
        mainPanel.add(labelOutputFolder, gbcLabelOutputFolder);

        textfieldOutputFolder = new JTextField();
        textfieldOutputFolder.setEnabled(false);
        GridBagConstraints gbcTextfieldOutputFolder = new GridBagConstraints();
        gbcTextfieldOutputFolder.insets = defaultInsets;
        gbcTextfieldOutputFolder.fill = GridBagConstraints.HORIZONTAL;
        gbcTextfieldOutputFolder.gridx = 1;
        gbcTextfieldOutputFolder.gridy = currentRow;
        mainPanel.add(textfieldOutputFolder, gbcTextfieldOutputFolder);
        textfieldOutputFolder.setColumns(10);

        buttonChooseOutputFolder = new JButton(textChooseButton);
        buttonChooseOutputFolder.addActionListener(e -> {
            try {
                chooseFileAndUpdateTextfield(textfieldOutputFolder, mainPanel, JFileChooser.DIRECTORIES_ONLY);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                logger.info(e1);
            } catch (ExecutionException e1) {
                logger.info(e1);
            }
        });
        GridBagConstraints gbcButtonChooseOutputFolder = new GridBagConstraints();
        gbcButtonChooseOutputFolder.anchor = GridBagConstraints.WEST;
        gbcButtonChooseOutputFolder.insets = new Insets(0, 0, 5, 0);
        gbcButtonChooseOutputFolder.gridx = 2;
        gbcButtonChooseOutputFolder.gridy = currentRow;
        gbcButtonChooseOutputFolder.fill = GridBagConstraints.HORIZONTAL;
        gbcButtonChooseOutputFolder.weightx = gbcButtonConfigure.weighty = 1;
        mainPanel.add(buttonChooseOutputFolder, gbcButtonChooseOutputFolder);

        currentRow++;
        cbVerbsMethods = new JCheckBox("Model connecting verbs as methods");
        cbVerbsMethods.setSelected(true);
        cbVerbsMethods.setHorizontalTextPosition(SwingConstants.LEFT);
        GridBagConstraints gbcCbVerbsMethods = new GridBagConstraints();
        gbcCbVerbsMethods.anchor = GridBagConstraints.EAST;
        gbcCbVerbsMethods.insets = defaultInsets;
        gbcCbVerbsMethods.gridx = 1;
        gbcCbVerbsMethods.gridy = currentRow;
        mainPanel.add(cbVerbsMethods, gbcCbVerbsMethods);

        currentRow++;
        cbVerbsRelationships = new JCheckBox("Model connecting verbs as associations");
        cbVerbsRelationships.setSelected(true);
        cbVerbsRelationships.setHorizontalTextPosition(SwingConstants.LEFT);
        GridBagConstraints gbcCbVerbsRelationships = new GridBagConstraints();
        gbcCbVerbsRelationships.anchor = GridBagConstraints.EAST;
        gbcCbVerbsRelationships.insets = defaultInsets;
        gbcCbVerbsRelationships.gridx = 1;
        gbcCbVerbsRelationships.gridy = currentRow;
        mainPanel.add(cbVerbsRelationships, gbcCbVerbsRelationships);

        currentRow++;
        cbCropEmptyClasses = new JCheckBox("Crop classes with neither attributes, methods nor relationships");
        cbCropEmptyClasses.setSelected(true);
        cbCropEmptyClasses.setHorizontalTextPosition(SwingConstants.LEFT);
        GridBagConstraints gbcCbCropEmptyClasses = new GridBagConstraints();
        gbcCbCropEmptyClasses.anchor = GridBagConstraints.EAST;
        gbcCbCropEmptyClasses.insets = defaultInsets;
        gbcCbCropEmptyClasses.gridx = 1;
        gbcCbCropEmptyClasses.gridy = currentRow;
        mainPanel.add(cbCropEmptyClasses, gbcCbCropEmptyClasses);

        currentRow++;
        JLabel labelOutput = new JLabel("Output:");
        GridBagConstraints gbcLabelOutput = new GridBagConstraints();
        gbcLabelOutput.anchor = GridBagConstraints.WEST;
        gbcLabelOutput.insets = defaultInsets;
        gbcLabelOutput.gridx = 0;
        gbcLabelOutput.gridy = currentRow;
        mainPanel.add(labelOutput, gbcLabelOutput);

        cbRawXmi = new JCheckBox("XMI");
        cbRawXmi.setSelected(true);
        GridBagConstraints gbcCbRawXmi = new GridBagConstraints();
        gbcCbRawXmi.anchor = GridBagConstraints.WEST;
        gbcCbRawXmi.insets = defaultInsets;
        gbcCbRawXmi.gridx = 1;
        gbcCbRawXmi.gridy = currentRow;
        mainPanel.add(cbRawXmi, gbcCbRawXmi);

        currentRow++;
        cbArgoUml = new JCheckBox("XMI (ArgoUML)");
        cbArgoUml.setSelected(true);
        GridBagConstraints gbcCbArgoUml = new GridBagConstraints();
        gbcCbArgoUml.anchor = GridBagConstraints.WEST;
        gbcCbArgoUml.insets = defaultInsets;
        gbcCbArgoUml.gridx = 1;
        gbcCbArgoUml.gridy = currentRow;
        mainPanel.add(cbArgoUml, gbcCbArgoUml);

        currentRow++;
        cbStarUml = new JCheckBox("XMI (StarUML)");
        cbStarUml.setSelected(true);
        GridBagConstraints gbcCbStarUml = new GridBagConstraints();
        gbcCbStarUml.anchor = GridBagConstraints.WEST;
        gbcCbStarUml.insets = defaultInsets;
        gbcCbStarUml.gridx = 1;
        gbcCbStarUml.gridy = currentRow;
        mainPanel.add(cbStarUml, gbcCbStarUml);

        currentRow++;
        cbPng = new JCheckBox("PNG");
        cbPng.setSelected(true);
        GridBagConstraints gbcCbPng = new GridBagConstraints();
        gbcCbPng.anchor = GridBagConstraints.WEST;
        gbcCbPng.insets = defaultInsets;
        gbcCbPng.gridx = 1;
        gbcCbPng.gridy = currentRow;
        mainPanel.add(cbPng, gbcCbPng);

        currentRow++;
        cbSvg = new JCheckBox("SVG");
        cbSvg.setSelected(true);
        GridBagConstraints gbcCbSvg = new GridBagConstraints();
        gbcCbSvg.anchor = GridBagConstraints.WEST;
        gbcCbSvg.insets = defaultInsets;
        gbcCbSvg.gridx = 1;
        gbcCbSvg.gridy = currentRow;
        mainPanel.add(cbSvg, gbcCbSvg);

        currentRow++;
        cbPlantText = new JCheckBox("Plant UML");
        cbPlantText.setSelected(true);
        GridBagConstraints gbcPlantText = new GridBagConstraints();
        gbcPlantText.anchor = GridBagConstraints.WEST;
        gbcPlantText.insets = defaultInsets;
        gbcPlantText.gridx = 1;
        gbcPlantText.gridy = currentRow;
        mainPanel.add(cbPlantText, gbcPlantText);

        currentRow++;
        buttonGenerateModels = new JButton("Run");
        buttonGenerateModels.setVerticalAlignment(SwingConstants.BOTTOM);
        buttonGenerateModels.addActionListener(e -> runGenerateModel());
        GridBagConstraints gbcButtonGenerateModels = new GridBagConstraints();
        gbcButtonGenerateModels.insets = new Insets(0, 0, 5, 0);
        gbcButtonGenerateModels.gridx = 2;
        gbcButtonGenerateModels.gridy = currentRow;
        mainPanel.add(buttonGenerateModels, gbcButtonGenerateModels);

        pack();
    }

    private void chooseFileAndUpdateTextfield(JTextField textfield, Component parent, int selectionMode)
            throws InterruptedException, ExecutionException {
        if (selectionMode != JFileChooser.FILES_ONLY && selectionMode != JFileChooser.DIRECTORIES_ONLY) {
            throw new IllegalArgumentException();
        }

        JFileChooser fileChooser;
        fileChooser = futureFileChooser.get();
        fileChooser.setFileSelectionMode(selectionMode);
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        int result = fileChooser.showOpenDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textfield.setText(selectedFile.getAbsolutePath());
        }
    }

    private void runGenerateModel() {
        Component parentComponentForDialog = this;

        SwingWorker<Void, Void> backgroundWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                logger.info("Start model generation");
                setUiActive(false);

                MoreRedocAnalysisConfiguration analysisConfiguration = new MoreRedocAnalysisConfiguration(cbVerbsMethods.isSelected(), cbVerbsRelationships.isSelected(), cbCropEmptyClasses.isSelected());
                MoreRedocOutputConfiguration outputConfiguration = new MoreRedocOutputConfiguration(textfieldOutputFolder.getText(), cbRawXmi.isSelected(), cbArgoUml.isSelected(), cbStarUml.isSelected(), cbPng.isSelected(), cbSvg.isSelected(), cbPlantText.isSelected());

                SupportedRedocumentationTools tool = comboBoxToolSelection.getItemAt(comboBoxToolSelection.getSelectedIndex());
                Objects.requireNonNull(tool);

                MoreRedocStarter.generateModel(textfieldCsvText.getText(), textfieldCsvKeywords.getText(), outputConfiguration, analysisConfiguration, tool.getDataHandler());
                int openFolderResult = JOptionPane.showConfirmDialog(parentComponentForDialog, "Models were successfully generated. Open target folder?");
                if (openFolderResult == JOptionPane.YES_OPTION) {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        desktop.open(new File(textfieldOutputFolder.getText()));
                    } catch (IllegalArgumentException iae) {
                        logger.error("Output folder not found");
                    }
                }
                setUiActive(true);
                logger.info("Model generation done");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (ExecutionException e) {
                    // working around getting into swing too deeply..
                    if (e.getCause() instanceof InvalidRequirementInputException) {
                        logger.error("InvalidRequirementInput");
                        JOptionPane.showMessageDialog(parentComponentForDialog, ERROR_MESSAGE_INVALID_INPUT, ERROR_HEADER_INVALID_INPUT, JOptionPane.ERROR_MESSAGE);
                    } else {
                        displayGenericErrorDialog(parentComponentForDialog);
                        logger.error("Exception in runGenerateModels", e);
                    }
                } catch (InterruptedException e) {
                    logger.error("InterruptedException in runGenerateModels", e);
                    displayGenericErrorDialog(parentComponentForDialog);
                    Thread.currentThread().interrupt();
                } finally {
                    setUiActive(true);
                }
            }
        };

        if (necessaryFieldsHaveLegalArguments()) {
            backgroundWorker.execute();
        }
    }

    private void displayGenericErrorDialog(Component parentComponentForDialog) {
        JOptionPane.showMessageDialog(parentComponentForDialog, ERROR_MESSAGE_UNSPECIFIED, ERROR_HEADER_UNSPECIFIED, JOptionPane.ERROR_MESSAGE);
    }

    private void setUiActive(boolean activeState) {
        this.setCursor(activeState ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        this.comboBoxToolSelection.setEnabled(activeState);

        this.cbVerbsMethods.setEnabled(activeState);
        this.cbVerbsRelationships.setEnabled(activeState);
        this.cbCropEmptyClasses.setEnabled(activeState);
        this.cbArgoUml.setEnabled(activeState);
        this.cbRawXmi.setEnabled(activeState);
        this.cbStarUml.setEnabled(activeState);
        this.cbPng.setEnabled(activeState);
        this.cbSvg.setEnabled(activeState);
        this.cbPlantText.setEnabled(activeState);

        this.buttonConfigure.setEnabled(activeState);
        this.buttonChooseKeywordsCsv.setEnabled(activeState);
        this.buttonChooseOutputFolder.setEnabled(activeState);
        this.buttonChooseTextCsv.setEnabled(activeState);
        this.buttonGenerateModels.setEnabled(activeState);
    }

    private boolean necessaryFieldsHaveLegalArguments() {
        if (textfieldCsvKeywords.getText() == null || !new File(textfieldCsvKeywords.getText()).isFile()) {
            JOptionPane.showMessageDialog(this, "Specify a valid csv input file containing the keywords");
            return false;
        }

        if (textfieldCsvText.getText() == null || !new File(textfieldCsvText.getText()).isFile()) {
            JOptionPane.showMessageDialog(this, "Specify a valid csv input file containing the text");
            return false;
        }

        if (textfieldOutputFolder.getText() == null || !new File(textfieldOutputFolder.getText()).isDirectory()) {
            JOptionPane.showMessageDialog(this, "Specify a valid folder for the generated models");
            return false;
        }

        if (!cbArgoUml.isSelected() && !cbRawXmi.isSelected() && !cbStarUml.isSelected() && !cbPng.isSelected() && !cbSvg.isSelected()) {
            JOptionPane.showMessageDialog(this, "Please select at least one output file type.");
            return false;
        }
        return true;
    }

}
