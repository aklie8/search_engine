package edu.usfca.cs272;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * This class represents a Search Processor that performs exact search
 * operations on an Inverted Index using a given Query.
 */
public class SearchProcessor implements SearchProcessorInterface {
	/**
	 * A sorted map that stores a set of strings as keys and a list of search
	 * results as values. The map is sorted based on the concatenated string
	 * representation of the keys.
	 */
	private final Map<String, List<InvertedIndex.SearchResult>> allSearchResults;

	/**
	 * index - the inverted index used for searching.
	 */
	private final InvertedIndex index;
	/**
	 * partial - Indicates whether partial or exact search should be performed.
	 */
	private final boolean partial;

	/**
	 * stemmer - The stemmer used for processing query lines.
	 * 
	 */
	private final Stemmer stemmer;

	/**
	 * Constructor for search processor
	 * 
	 * @param index   - the inverted index used for searching.
	 * @param partial - Indicates whether partial or exact search should be
	 *                performed.
	 */
	public SearchProcessor(InvertedIndex index, boolean partial) {
		this.index = index;
		this.partial = partial;

		this.allSearchResults = new TreeMap<>();
		this.stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
	}

	/**
	 * method that handles the per-line operations (stemming, joining, searching)
	 * 
	 * @param queryLine - a single line representing a query
	 */
	@Override
	public void parseQueryLine(String queryLine) {
		Set<String> stems = FileStemmer.uniqueStems(queryLine, stemmer);
		if (!stems.isEmpty()) {
			String joinedString = String.join(" ", stems);
			if (!allSearchResults.containsKey(joinedString)) {
				allSearchResults.put(joinedString, index.search(stems, partial));
			}
		}
	}

	/**
	 * Writes the search results to a JSON file at the specified path.
	 *
	 * @param resultsPath The path where the JSON file will be written.
	 * @throws IOException If an error occurs while writing the JSON file.
	 */
	@Override
	public void writeJson(Path resultsPath) throws IOException {
		JsonWriter.writeQueryResults(allSearchResults, resultsPath);
	}

	/**
	 * @return - the stored inverted index
	 */
	@Override
	public InvertedIndex getIndex() {
		return index;
	}

	/**
	 * @return - return true if set to do partial searches
	 */
	@Override
	public boolean isPartial() {
		return partial;
	}

	/**
	 * @param queryLine - a single line representing a query
	 * @return - the stored search results associated with that query
	 */
	@Override
	public List<InvertedIndex.SearchResult> getStoredSearchResult(String queryLine) {
		Set<String> stems = FileStemmer.uniqueStems(queryLine, stemmer);
		return allSearchResults.get(String.join(" ", stems));
	}

	@Override
	public String toString() {

		StringWriter writer = new StringWriter();

		try {
			JsonWriter.writeQueryResults(allSearchResults, writer, 0);
		} catch (IOException e) {
			return allSearchResults.toString();
		}
		return writer.toString();
	}

	/**
	 * Retrieves a set of query lines for which search results are available.
	 * 
	 * @return - An unmodifiable set of query lines.
	 */
	@Override
	public Set<String> getQueryLines() {
		return Collections.unmodifiableSet(allSearchResults.keySet());
	}

}