package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Interface for the SearchProcessor and for the ThreadedSearchProcessor
 */
public interface SearchProcessorInterface {

	/**
	 * Processes a query file and populates the search results based on the provided
	 * index.
	 *
	 * @param path - The path to the query file.
	 * @throws IOException If an I/O error occurs while reading the query file.
	 */

	public default void processQueryFile(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				parseQueryLine(line);
			}
		}
	}

	/**
	 * method that handles the per-line operations (stemming, joining, searching)
	 * 
	 * @param queryLine - a single line representing a query
	 */
	public void parseQueryLine(String queryLine);

	/**
	 * Writes the search results to a JSON file at the specified path.
	 *
	 * @param resultsPath The path where the JSON file will be written.
	 * @throws IOException If an error occurs while writing the JSON file.
	 */
	public void writeJson(Path resultsPath) throws IOException;

	/**
	 * @return - the stored inverted index
	 */
	public InvertedIndex getIndex();

	/**
	 * @return - return true if set to do partial searches
	 */
	public boolean isPartial();

	/**
	 * @param queryLine - a single line representing a query
	 * @return - the stored search results associated with that query
	 */
	public List<InvertedIndex.SearchResult> getStoredSearchResult(String queryLine);

	@Override
	public String toString();

	/**
	 * Retrieves a set of query lines for which search results are available.
	 * 
	 * @return - An unmodifiable set of query lines.
	 */
	public Set<String> getQueryLines();
}
