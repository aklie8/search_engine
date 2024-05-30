package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A Multi-threaded version of SearchProcessor
 */
public class ThreadedSearchProcessor implements SearchProcessorInterface {

	/**
	 * the work queue containing the worker thread
	 */
	private final WorkQueue queue;

	/**
	 * A sorted map that stores a set of strings as keys and a list of search
	 * results as values. The map is sorted based on the concatenated string
	 * representation of the keys.
	 */
	private final Map<String, List<InvertedIndex.SearchResult>> allSearchResults;

	/**
	 * index - the inverted index used for searching.
	 */
	private final ThreadedInvertedIndex index;
	/**
	 * partial - Indicates whether partial or exact search should be performed.
	 */
	private final boolean partial;

	/**
	 * @param index   - the inverted index used for searching.
	 * @param partial - Indicates whether partial or exact search should be
	 *                performed.
	 * @param queue   - the work queue containing the worker thread
	 */
	public ThreadedSearchProcessor(ThreadedInvertedIndex index, boolean partial, WorkQueue queue) {
		this.index = index;
		this.partial = partial;

		this.allSearchResults = new TreeMap<>();

		this.queue = queue;

	}

	@Override
	public void processQueryFile(Path path) throws IOException {
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				queue.execute(new Task(line));
			}
		} finally {
			queue.finish();
		}
	}

	@Override
	public void parseQueryLine(String queryLine) {
		Set<String> stems = FileStemmer.uniqueStems(queryLine);

		if (!stems.isEmpty()) {
			String joinedString = String.join(" ", stems);

			synchronized (this) {
				boolean shouldSearch = !allSearchResults.containsKey(joinedString);
				if (!shouldSearch) {
					return;
				}
			}

			var results = index.search(stems, partial);
			synchronized (this) {
				allSearchResults.put(joinedString, results);
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
		synchronized (this) {
			JsonWriter.writeQueryResults(allSearchResults, resultsPath);
		}
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
		Set<String> stems = FileStemmer.uniqueStems(queryLine);
		String joinedStems = String.join(" ", stems);
		synchronized (this) {
			return allSearchResults.get(joinedStems);
		}
	}

	@Override
	public synchronized String toString() {

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
	public synchronized Set<String> getQueryLines() {
		return Collections.unmodifiableSet(allSearchResults.keySet());
	}

	/**
	 * Represents a task to be executed by the worker thread
	 */
	private class Task implements Runnable {

		/**
		 * The query line associated with this task
		 */
		private final String queryLine;

		/**
		 * Initializes a new instance of the Task.
		 * 
		 * @param queryLine - the line in the query file associated with this task
		 */
		public Task(String queryLine) {
			this.queryLine = queryLine;
		}

		@Override
		public void run() {
			parseQueryLine(queryLine);
		}

	}

}
