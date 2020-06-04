package moreredoc.datainput;

import moreredoc.application.exceptions.InvalidRequirementInputException;
import moreredoc.project.data.Requirement;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.*;

public final class SoftRedocDataHandler implements InputDataHandler {

    private static final Logger logger = Logger.getLogger(SoftRedocDataHandler.class);

    @SuppressWarnings("unsused")
    private static final String EXPRESSION_TEST = "TEST";
    private static final String EXPRESSION_DATA = "DATA";
    @SuppressWarnings("unsused")
    private static final String EXPRESSION_REQU = "REQU";
    private static final String EXPRESSION_SECT = "SECT";
    private static final String EXPRESSION_CASE = "CASE";

    private static final int INDEX_TYPE = 0;
    private static final int INDEX_BASE_ENTITY = 1;
    private static final int INDEX_TARGET_TYPE = 3;
    private static final int INDEX_TARGET_ENTITY = 4;

    private static final int INDEX_SENTENCE_REQU_ID = 0;
    private static final int INDEX_LN = 5;
    private static final int INDEX_SENTENCE = 6;
    private static final String LINE_NUMBER_METADATA_1 = "1";

    private String prefixFunctionalRequirement = "FUNC-REQ";
    private String expressionBusinessObjects = "Business-Objects";
    private String expressionBusinessRules = "Business-Rules";
    private String expressionSystemActors = "System-Actors";
    private String expressionSystemUseCases = "system  Use Cases";

    private String csvDelimiter = ";";

    public SoftRedocDataHandler() {
        // try to load initial config from properties file
        Properties prop = new Properties();
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("tools/softredoc_config.properties");
        try {
            prop.load(stream);
            this.prefixFunctionalRequirement = prop.getProperty("prefixFunctionalRequirement");
            this.expressionBusinessObjects = prop.getProperty("expressionBusinessObjects");
            this.expressionBusinessRules = prop.getProperty("expressionBusinessRules");
            this.expressionSystemActors = prop.getProperty("expressionSystemActors");
            this.expressionSystemUseCases = prop.getProperty("expressionSystemUseCases");
        } catch (IOException | NullPointerException e) {
            // do nothing
        }
    }

    @Override
    public void configure() {
        JTextField fieldPrefix = new JTextField(prefixFunctionalRequirement);
        JTextField fieldCsvDelimiter = new JTextField(csvDelimiter);
        JTextField fieldBusinessObjects = new JTextField(expressionBusinessObjects);
        JTextField fieldBusinessRules = new JTextField(expressionBusinessRules);
        JTextField fieldSystemActors = new JTextField(expressionSystemActors);
        JTextField fieldSystemUseCases = new JTextField(expressionSystemUseCases);

        JPanel panel = new JPanel(new GridLayout(0, 1));

        panel.add(new JLabel("CSV delimiter:"));
        panel.add(fieldCsvDelimiter);

        panel.add(new JLabel("Prefix for functional requirement IDs:"));
        panel.add(fieldPrefix);

        panel.add(new JLabel("Expression for business objects:"));
        panel.add(fieldBusinessObjects);

        panel.add(new JLabel("Expression for business rules:"));
        panel.add(fieldBusinessRules);

        panel.add(new JLabel("Expression for system actors:"));
        panel.add(fieldSystemActors);

        panel.add(new JLabel("Expression for system use cases:"));
        panel.add(fieldSystemUseCases);

        int result = JOptionPane.showConfirmDialog(null, panel, "SoftRedoc configuration",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            this.prefixFunctionalRequirement = fieldPrefix.getText();
            this.csvDelimiter = fieldCsvDelimiter.getText();
            this.expressionBusinessObjects = fieldBusinessObjects.getText();
            this.expressionBusinessRules = fieldBusinessRules.getText();
            this.expressionSystemActors = fieldSystemActors.getText();
            this.expressionSystemUseCases = fieldSystemUseCases.getText();
        }
    }

    @Override
    public List<Requirement> getRequirementsFromCsvInputs(List<List<String>> keywordInput,
            List<List<String>> sentencesInput) throws InvalidRequirementInputException {
        try {
            List<Requirement> requirementsWithoutSentences = getRequirementsWithoutText(keywordInput);
            return addTextToRequirements(requirementsWithoutSentences, sentencesInput);
        } catch (ArrayIndexOutOfBoundsException e) {
            logger.error("Exception which causes InvalidRequirementInputException", e);
            throw new InvalidRequirementInputException();
        }
    }

    @Override
    public String getCsvDelimiter() {
        return csvDelimiter;
    }

    private List<Requirement> getRequirementsWithoutText(List<List<String>> keywordInput) {
        List<Requirement> result = new ArrayList<>();

        // iterate over raw input, check every line in csv data
        for (int i = 0; i < keywordInput.size(); i++) {
            // current line in csv, represented as list of strings
            List<String> currentCsvLine = keywordInput.get(i);

            if (currentCsvLine.get(INDEX_BASE_ENTITY).startsWith(prefixFunctionalRequirement) && currentCsvLine.get(INDEX_TARGET_TYPE).equals(EXPRESSION_DATA)) {
                String requirementId = currentCsvLine.get(INDEX_BASE_ENTITY);
                // assuming IDs are unique
                Optional<Requirement> currentRequirement = result.stream().filter(r -> r.getId().equals(requirementId)).findFirst();
                if (currentRequirement.isPresent()) {
                    currentRequirement.get().getKeywords().add(currentCsvLine.get(INDEX_TARGET_ENTITY));
                } else {
                    Requirement newRequirement = new Requirement(requirementId);
                    result.add(newRequirement);
                }
            }
        }
        return result;
    }

    private List<Requirement> addTextToRequirements(List<Requirement> requirements, List<List<String>> sentencesInput) {
        for (int i = 0; i < sentencesInput.size(); i++) {
            List<String> currentCsvLine = sentencesInput.get(i);

            // deal with empty sentences
            if (INDEX_SENTENCE >= currentCsvLine.size()) {
                continue;
            }

            String currentRequId = currentCsvLine.get(INDEX_SENTENCE_REQU_ID);
            String currentSentence = currentCsvLine.get(INDEX_SENTENCE);

            //iterate over requirements
            for (Requirement r : requirements) {
                //if requirements id in sentence csv matches one in the given requirements
                if (r.getId().trim().contains(currentRequId.trim())
                        && !currentCsvLine.get(INDEX_LN).contains(LINE_NUMBER_METADATA_1)
                        && !currentCsvLine.get(INDEX_SENTENCE).contains(r.getId())) {
                    r.concatenateUnprocessedText(currentSentence);
                }
            }
        }

        return requirements;
    }

    @Override
    public Set<String> getAdditionalDomainConcepts(List<List<String>> csvInput) {
        Set<String> result = new HashSet<>();

        for (int i = 0; i < csvInput.size(); i++) {
            List<String> currentCsvLine = csvInput.get(i);
            String typeI = currentCsvLine.get(INDEX_TYPE).trim();
            String baseEntity = currentCsvLine.get(INDEX_BASE_ENTITY).trim();

            // add DATA from SECT, where base entity is either business object, business rule, system use case or system actor
            if ((typeI.equals(EXPRESSION_SECT))
                    && currentCsvLine.get(INDEX_TARGET_TYPE).equals(EXPRESSION_DATA)
                    && (baseEntity.equals(expressionBusinessObjects) || baseEntity.equals(expressionBusinessRules) || baseEntity.equals(expressionSystemUseCases) || baseEntity.equals(expressionSystemActors))) {
                result.add(currentCsvLine.get(INDEX_TARGET_ENTITY));
            }

            // add DATA from CASES
            if ((typeI.equals(EXPRESSION_CASE) && currentCsvLine.get(INDEX_TARGET_TYPE).equals(EXPRESSION_DATA))) {
                result.add(currentCsvLine.get(INDEX_TARGET_ENTITY));
            }
        }

        return result;
    }

}
