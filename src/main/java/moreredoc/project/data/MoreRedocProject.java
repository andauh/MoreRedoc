package moreredoc.project.data;

import moreredoc.application.exceptions.InvalidRequirementInputException;
import moreredoc.datainput.InputDataHandler;
import moreredoc.linguistics.processing.WordRegularizerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class MoreRedocProject {
	private static Logger logger = Logger.getLogger(MoreRedocProject.class);

	private InputDataHandler inputDataHandler;
	private List<List<String>> keywordsRaw;

	// Set of all concepts
	private Set<String> projectDomainConcepts = new HashSet<>();
	private List<Requirement> projectRequirements;
	private List<ProcessedRequirement> processedProjectRequirements = new ArrayList<>();
	private Map<String, Integer> conceptCount = new HashMap<>();

	private String wholeText;
	private String wholeCorefResolvedRegularizedText;
	private String wholeProcessedText;

	/*
	 * Hide default constructor because a project has to be initialized with
	 * requirements
	 */
	@SuppressWarnings("unused")
	private MoreRedocProject() {
		throw new UnsupportedOperationException();
	}

	public MoreRedocProject(List<List<String>> keywordsRaw, List<List<String>> sentencesRaw,
			InputDataHandler dataHandler) throws InvalidRequirementInputException {
		Objects.requireNonNull(keywordsRaw);
		Objects.requireNonNull(sentencesRaw);
		Objects.requireNonNull(dataHandler);

		this.keywordsRaw = keywordsRaw;
		this.inputDataHandler = dataHandler;
		
		this.projectRequirements = dataHandler.getRequirementsFromCsvInputs(keywordsRaw, sentencesRaw);

		// process requirements via the ProcessedRequirement constructor
		for (Requirement r : projectRequirements) {
			processedProjectRequirements.add(new ProcessedRequirement(r));
		}

		// after initializing the processed requirements, all texts of the project
		// requirements can be generated
		this.wholeText = generateText();
		this.wholeCorefResolvedRegularizedText = generateCorefResolvedRegularizedText();
		this.wholeProcessedText = generateWholeProcessedText();

		// calculate occurences of keywords
		initializeDomainConcepts();
	}

	/**
	 * Generates a string containing all processed (normalization, coref,
	 * decomposition) requirement texts
	 * 
	 * @return Processed Text for the whole Project
	 */
	private String generateWholeProcessedText() {
		StringBuilder textBuilder = new StringBuilder();

		for (ProcessedRequirement r : this.getProcessedProjectRequirements()) {
			textBuilder.append(r.getProcessedText());
		}

		return textBuilder.toString();
	}

	private String generateCorefResolvedRegularizedText() {
		StringBuilder textBuilder = new StringBuilder();

		for (ProcessedRequirement r : this.getProcessedProjectRequirements()) {
			textBuilder.append(r.getCorefResolvedRegularizedText());
		}

		return textBuilder.toString();
	}

	private String generateText() {
		StringBuilder textBuilder = new StringBuilder();

		for (Requirement r : this.getProjectRequirements()) {
			textBuilder.append(r.getUnprocessedText());
		}

		return textBuilder.toString();
	}

	private void initializeDomainConcepts() {
		// add additional concepts
		this.projectDomainConcepts.addAll(inputDataHandler.getAdditionalDomainConcepts(keywordsRaw));
		for(String concept : projectDomainConcepts){
			conceptCount.put(concept, 1);
		}

		StringBuilder wholeProcessedTextBuilder = new StringBuilder();

		for (ProcessedRequirement r : processedProjectRequirements) {
			// concatenate processed text
			wholeProcessedTextBuilder.append(r.getCorefResolvedRegularizedText());
			// regularize strings, put in set
			for (String s : r.getKeywords()) {
				String regularizedConcept = WordRegularizerService.regularize(s);
				projectDomainConcepts.add(regularizedConcept);

				int occurrences;

				// count occurences
				// initialize value for count
				if (conceptCount.get(regularizedConcept) == null) {
					occurrences = 1;
				} else {
					occurrences = conceptCount.get(regularizedConcept) + 1;
				}

				conceptCount.put(regularizedConcept, occurrences);
			}

		}
		// normalize each word of whole processed text
		// split by whitespaces via regex
		String[] splitProcessedText = StringUtils.split(wholeProcessedTextBuilder.toString());
		for (int i = 0; i < splitProcessedText.length; i++) {
			splitProcessedText[i] = WordRegularizerService.regularize(splitProcessedText[i]);
		}

		// count domain concept occurences in regularized text
		// has to be done this way, otherwise it will count infixes..
		for (String s : projectDomainConcepts) {
			int count = conceptCount.get(s);

			for (int i = 0; i < splitProcessedText.length; i++) {
				if (splitProcessedText[i].equals(s))
					count++;
			}

			conceptCount.put(s, count);
		}

	}

	public String getWholeText() {
		return wholeText;
	}

	public String getWholeCorefResolvedRegularizedText() {
		return wholeCorefResolvedRegularizedText;
	}

	public String getWholeProcessedText() {
		return wholeProcessedText;
	}

	public Set<String> getProjectDomainConcepts() {
		return projectDomainConcepts;
	}

	public List<Requirement> getProjectRequirements() {
		return projectRequirements;
	}

	public List<ProcessedRequirement> getProcessedProjectRequirements() {
		return processedProjectRequirements;
	}

	public Map<String, Integer> getEntityCount() {
		return conceptCount;
	}

	public void printEntityCount() {
		Set<String> keySet = conceptCount.keySet();
		for (String s : keySet) {
			logger.info("Keyword: " + s);
			logger.info("\tabsolute: " + conceptCount.get(s));
			logger.info("\trelative: " + getRelativeFrequencyOfKeyword(s));
		}
	}

	public double getRelativeFrequencyOfKeyword(String keyword) {
		int totalCount = conceptCount.keySet().size();
		int occurrences = conceptCount.get(keyword);

		return (double) occurrences / totalCount;
	}
}
