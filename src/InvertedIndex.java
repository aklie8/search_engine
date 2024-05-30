package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class responsible representing the backwards index data structure
 */
public class InvertedIndex {
	/**
	 * Stores the locations of all words in the backwards index
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> index;
	/**
	 * Stores the word count of words in a file
	 */
	private final TreeMap<String, Integer> wordCounts;

	/**
	 * Method that will construct an empty backwards index
	 */
	public InvertedIndex() {
		index = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		wordCounts = new TreeMap<String, Integer>();
	}

	/**
	 * Method that performs a search operation based on a set of queries.
	 * 
	 * @param queries - a set of search queries to be used for searching.
	 * @param partial - a boolean flag indicating whether to perform a partial
	 *                search. If true, partial search is performed; if false, exact
	 *                search is performed.
	 * @return - a list of search results based on the provided queries.
	 */
	public List<SearchResult> search(Set<String> queries, boolean partial) {
		return partial ? partialSearch(queries) : exactSearch(queries);
	}

	/**
	 * Method that finds matches from the inverted index data structure and generate
	 * a search result for each match
	 * 
	 * @param queries - the set of search terms to search
	 * @return - a sorted list of search results
	 */
	public List<SearchResult> exactSearch(Set<String> queries) {

		Map<String, SearchResult> matches = new HashMap<>();
		List<SearchResult> results = new ArrayList<>();

		// Iterate through each file and calculate the match count and score
		for (String word : queries) {
			exactSearchHelper(matches, results, word);
		}

		// Sort the results based on score, match count, and location
		Collections.sort(results);

		return results;
	}

	/**
	 * Helper method to perform the search for a given word and update matches and
	 * results.
	 * 
	 * @param matches - map to store matches
	 * @param results - list to store search results
	 * @param word    - word to search for
	 */
	private void exactSearchHelper(Map<String, SearchResult> matches, List<SearchResult> results, String word) {
		var locations = index.get(word);
		if (locations != null) {
			for (var entry : locations.entrySet()) {
				SearchResult result = matches.get(entry.getKey());
				if (result == null) {
					result = new SearchResult(entry.getKey());
					matches.put(entry.getKey(), result);
					results.add(result);
				}
				result.update(entry.getValue().size());
			}
		}
	}

	/**
	 * Performs a partial search on the inverted index using word stems.
	 * 
	 * @param partialQuery The query containing partial search terms (word stems).
	 * @return A list of SearchResult objects, sorted by relevance.
	 */
	public List<SearchResult> partialSearch(Set<String> partialQuery) {

		Map<String, SearchResult> matches = new HashMap<>();
		List<SearchResult> results = new ArrayList<>();

		// Iterate through each file and calculate the match count and score
		for (String queryWord : partialQuery) {
			// Iterate over the tailMap of the index, starting from the given queryWord
			for (var entry : index.tailMap(queryWord).entrySet()) {
				String word = entry.getKey();
				if (!word.startsWith(queryWord)) {
					break; // Exit the loop if the current word no longer matches the query
				}
				exactSearchHelper(matches, results, word);
			}
		}

		// Sort the results based on score, match count, and location
		Collections.sort(results);

		return results;
	}

	/**
	 * Updates the backward index so word has a location at fileName, position
	 * 
	 * @note does not update the word count of the file
	 * 
	 * @param word     - word to be added to the index
	 * @param fileName - the file the word was found in
	 * @param position - location of word in the file
	 */
	public void insertWord(String word, String fileName, int position) {

		var wordLocations = index.get(word);

		if (wordLocations == null) {
			wordLocations = new TreeMap<>();
			index.put(word, wordLocations);
		}

		var positions = wordLocations.get(fileName);

		if (positions == null) {
			positions = new TreeSet<>();
			wordLocations.put(fileName, positions);
		}

		positions.add(position);

		// Update the word count here keeping the maximum position found for a location
		// as the word count.
		wordCounts.merge(fileName, position, Integer::max);

	}

	@Override
	public String toString() {
		return JsonWriter.writeIndex(index);
	}

	/**
	 * Method that checks if the word count for a file is known
	 * 
	 * @param location - location the path to a file
	 * @return if word count is known
	 */
	public boolean containsCount(String location) {
		return wordCounts.containsKey(location);
	}

	/**
	 * Method that checks if a word exists in the index
	 * 
	 * @param word - to query
	 * @return if the word exists in the index
	 */
	public boolean containsWord(String word) {
		return index.containsKey(word);
	}

	/**
	 * Method that checks if a word exists in the index and the location of the word
	 * 
	 * @param word     - to query
	 * @param location - of the path in the query
	 * @return if the word exists in the index
	 */
	public boolean containsLocation(String word, String location) {
		var locations = index.get(word);
		return locations != null && locations.containsKey(location);
	}

	/**
	 * Method that checks if a word exists in the index and the location of the word
	 * 
	 * @param word     - to query
	 * @param location - of the path in the query
	 * @param position - position within the file
	 * @return if the word exists in the index
	 */
	public boolean containsPosition(String word, String location, int position) {
		var locations = index.get(word);
		if (locations != null) {
			var positions = locations.get(location);
			return positions != null && positions.contains(position);
		}
		return false;
	}

	/**
	 * Method that returns the number of unique words in the index
	 * 
	 * @return the number of unique words in the index
	 */
	public int numUniqueWords() {
		return index.size();
	}

	/**
	 * Method the number of files in the index
	 * 
	 * @return the number of files in the index
	 */
	public int numCounts() {
		return wordCounts.size();
	}

	/**
	 * Method that returns the number of locations where a word occurs in the index.
	 * 
	 * @param word - word in the query
	 * @return the number of locations where the word occurs, or 0 if the word is
	 *         not found in the index
	 */
	public int numLocations(String word) {
		var locations = index.get(word);
		if (locations != null) {
			return locations.size();
		}
		return 0;
	}

	/**
	 * Returns the number of positions where a word occurs in a specific location.
	 * 
	 * @param word     - word in the query
	 * @param location - location in the index
	 * @return the number of positions where the word occurs in the location, or 0
	 *         if the word or location is not found in the index
	 */
	public int numPositions(String word, String location) {
		var locations = index.get(word);

		if (locations != null) {
			var positions = locations.get(location);

			if (positions != null) {
				return positions.size();
			}
		}
		return 0;

	}

	/**
	 * Method that returns the number of words in a specific file
	 * 
	 * 
	 * @param file - in the index
	 * @return number of words in the file or null if file not found
	 */
	public Integer getWordCount(String file) {
		return wordCounts.getOrDefault(file, 0);
	}

	/**
	 * Retrieves the positions where a word occurs in a specific file in the index.
	 * 
	 * @param word - word in the query
	 * @param file - file in the index
	 * @return a set of positions where the word occurs in the file
	 */
	public Set<Integer> getPositions(String word, String file) {
		var locations = index.get(word);
		if (locations == null) {
			return Collections.emptySet();
		}
		var positions = locations.get(file);
		if (positions == null) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(positions);
	}

	/**
	 * Retrieves all the files that contain a specific word in the index.
	 * 
	 * @param word - word in the query
	 * @return all the files that contain the word
	 */
	public Set<String> getLocations(String word) {
		var locations = index.get(word);
		if (locations != null) {
			return Collections.unmodifiableSet(locations.keySet());
		}
		return Collections.emptySet();

	}

	/**
	 * Method that returns the set of all words in the index files
	 * 
	 * @return the set of all words in the index files
	 */
	public NavigableSet<String> getWords() {
		return Collections.unmodifiableNavigableSet(index.navigableKeySet());
	}

	/**
	 * intended for use by JsonWriter.java *
	 * 
	 * @return the word counts as an unmodifiable map
	 */
	public Map<String, Integer> getWordCounts() {
		return Collections.unmodifiableMap(wordCounts);
	}

	/**
	 * Method to add data from one InvertedIndex to this InvertedIndex It is assumed
	 * the inverted indices have not indexed the same files
	 * 
	 * @param other - the InvertedIndex to add
	 */
	public void addAll(InvertedIndex other) {
		for (var entry : other.wordCounts.entrySet()) {
			this.wordCounts.merge(entry.getKey(), entry.getValue(), Integer::max);
		}

		for (var entry : other.index.entrySet()) {
			String otherWord = entry.getKey();
			var otherMap = entry.getValue();
			var thisMap = this.index.get(otherWord);

			if (thisMap == null) {
				this.index.put(otherWord, otherMap);
			} else {
				for (var treeEntry : otherMap.entrySet()) {
					String otherFile = treeEntry.getKey();
					var otherList = treeEntry.getValue();
					var thisList = thisMap.get(otherFile);
					if (thisList == null) {
						thisMap.put(otherFile, otherList);
					} else {
						thisList.addAll(otherList);
					}
				}
			}
		}

	}

	/**
	 * @param path - path to write
	 * @throws IOException - throw IOexcetion if IOexcetion occurs while writing the
	 *                     index
	 */
	public void writeJson(Path path) throws IOException {
		JsonWriter.writeIndex(index, path);
	}

	/**
	 * Represents a search result containing information about matches, score, and
	 * file location.
	 */
	public class SearchResult implements Comparable<SearchResult> {

		/**
		 * Represents the total number of matches found for the current result,
		 * 
		 */
		private int matchCount;
		/**
		 * Represents the percent of words in the file that match the query
		 * 
		 */
		private double score;
		/**
		 * Represents file location of the search result.
		 */
		private final String location;

		/**
		 * 
		 * @param location - file location of the search result.
		 */
		public SearchResult(String location) {
			this.matchCount = 0;
			this.score = 0;
			this.location = location;
		}

		/**
		 * @param matches - The number of matches to add to the current match count.
		 */
		private void update(int matches) {
			matchCount += matches;
			score = (double) matchCount / getWordCount(location);
		}

		/**
		 * Retrieves the total number of matches found for the current result.
		 * 
		 * @return the match count
		 */
		public int getMatchCount() {
			return matchCount;
		}

		/**
		 * Retrieves the percent of words in the file that match the query.
		 * 
		 * @return the matching score
		 */
		public double getScore() {
			return score;
		}

		/**
		 * Retrieves the file location of the search result.
		 * 
		 * @return the file location
		 */
		public String getLocation() {
			return location;
		}

		@Override
		public int compareTo(SearchResult other) {
			// sorting in descending order
			int scoreComp = Double.compare(other.getScore(), this.getScore());
			if (scoreComp != 0) {
				return scoreComp;
			}
			// sorting in descending order
			int matchComp = Integer.compare(other.getMatchCount(), this.getMatchCount());
			if (matchComp != 0) {
				return matchComp;
			}

			int locationComp = this.getLocation().compareToIgnoreCase(other.getLocation());
			return locationComp;
		}

	}

}
