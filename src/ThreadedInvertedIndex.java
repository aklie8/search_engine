package edu.usfca.cs272;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;

/**
 * Thread safe version of InvertedIndex
 */
public class ThreadedInvertedIndex extends InvertedIndex {
	/**
	 * lock to protect the InvertedIndex
	 */
	private final MultiReaderLock lock;

	/**
	 * Creates a thread safe inverted index
	 */
	public ThreadedInvertedIndex() {
		this.lock = new MultiReaderLock();
	}

	@Override
	public List<SearchResult> exactSearch(Set<String> queries) {
		lock.readLock().lock();
		try {
			var result = super.exactSearch(queries);
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Performs a partial search on the inverted index using word stems.
	 * 
	 * @param partialQuery The query containing partial search terms (word stems).
	 * @return A list of SearchResult objects, sorted by relevance.
	 */
	@Override
	public List<SearchResult> partialSearch(Set<String> partialQuery) {
		lock.readLock().lock();
		try {
			var result = super.partialSearch(partialQuery);
			return result;

		} finally {
			lock.readLock().unlock();
		}
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
	@Override
	public void insertWord(String word, String fileName, int position) {
		lock.writeLock().lock();
		try {
			super.insertWord(word, fileName, position);
		} finally {
			lock.writeLock().unlock();
		}
	}

	@Override
	public String toString() {
		lock.readLock().lock();
		try {
			var result = super.toString();
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Method that checks if the word count for a file is known
	 * 
	 * @param location - location the path to a file
	 * @return if word count is known
	 */
	@Override
	public boolean containsCount(String location) {
		lock.readLock().lock();
		try {
			var result = super.containsCount(location);
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Method that checks if a word exists in the index
	 * 
	 * @param word - to query
	 * @return if the word exists in the index
	 */
	@Override
	public boolean containsWord(String word) {
		lock.readLock().lock();
		try {
			var result = super.containsWord(word);
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Method that checks if a word exists in the index and the location of the word
	 * 
	 * @param word     - to query
	 * @param location - of the path in the query
	 * @return if the word exists in the index
	 */
	@Override
	public boolean containsLocation(String word, String location) {
		lock.readLock().lock();
		try {
			var result = super.containsLocation(word, location);
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Method that checks if a word exists in the index and the location of the word
	 * 
	 * @param word     - to query
	 * @param location - of the path in the query
	 * @param position - position within the file
	 * @return if the word exists in the index
	 */
	@Override
	public boolean containsPosition(String word, String location, int position) {
		lock.readLock().lock();
		try {
			var result = super.containsPosition(word, location, position);
			return result;
		} finally {
			lock.readLock().unlock();
		}

	}

	/**
	 * Method that returns the number of unique words in the index
	 * 
	 * @return the number of unique words in the index
	 */
	@Override
	public int numUniqueWords() {
		lock.readLock().lock();
		try {
			var result = super.numUniqueWords();
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Method the number of files in the index
	 * 
	 * @return the number of files in the index
	 */
	@Override
	public int numCounts() {
		lock.readLock().lock();
		try {
			var result = super.numCounts();
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Method that returns the number of locations where a word occurs in the index.
	 * 
	 * @param word - word in the query
	 * @return the number of locations where the word occurs, or 0 if the word is
	 *         not found in the index
	 */
	@Override
	public int numLocations(String word) {
		lock.readLock().lock();
		try {
			var result = super.numLocations(word);
			return result;

		} finally {
			lock.readLock().unlock();
		}

	}

	/**
	 * Returns the number of positions where a word occurs in a specific location.
	 * 
	 * @param word     - word in the query
	 * @param location - location in the index
	 * @return the number of positions where the word occurs in the location, or 0
	 *         if the word or location is not found in the index
	 */
	@Override
	public int numPositions(String word, String location) {
		lock.readLock().lock();
		try {
			var result = super.numPositions(word, location);
			return result;

		} finally {
			lock.readLock().unlock();
		}

	}

	/**
	 * Method that returns the number of words in a specific file
	 * 
	 * 
	 * @param file - in the index
	 * @return number of words in the file or null if file not found
	 */
	@Override
	public Integer getWordCount(String file) {
		lock.readLock().lock();
		try {
			var result = super.getWordCount(file);
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Retrieves the positions where a word occurs in a specific file in the index.
	 * 
	 * @param word - word in the query
	 * @param file - file in the index
	 * @return a set of positions where the word occurs in the file
	 */
	@Override
	public Set<Integer> getPositions(String word, String file) {
		lock.readLock().lock();
		try {
			var result = super.getPositions(word, file);
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Retrieves all the files that contain a specific word in the index.
	 * 
	 * @param word - word in the query
	 * @return all the files that contain the word
	 */
	@Override
	public Set<String> getLocations(String word) {
		lock.readLock().lock();
		try {
			var result = super.getLocations(word);
			return result;

		} finally {
			lock.readLock().unlock();

		}

	}

	/**
	 * Method that returns the set of all words in the index files
	 * 
	 * @return the set of all words in the index files
	 */
	@Override
	public NavigableSet<String> getWords() {
		lock.readLock().lock();
		try {
			var result = super.getWords();
			return result;

		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * intended for use by JsonWriter.java *
	 * 
	 * @return the word counts as an unmodifiable map
	 */
	@Override
	public Map<String, Integer> getWordCounts() {
		lock.readLock().lock();
		try {
			var result = super.getWordCounts();
			return result;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.writeLock().lock();
		try {
			super.addAll(other);
		} finally {
			lock.writeLock().unlock();

		}
	}

	/**
	 * @param path - path to write
	 * @throws IOException - throw IOexcetion if IOexcetion occurs while writing the
	 *                     index
	 */
	@Override
	public void writeJson(Path path) throws IOException {
		lock.readLock().lock();
		try {
			super.writeJson(path);
		} finally {
			lock.readLock().unlock();
		}
	}
}