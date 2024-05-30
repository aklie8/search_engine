package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Class that is responsible for processing one or more files and indexes those
 * files
 */
public class InvertedIndexProcessor {

	/**
	 * Reads 1 or more files and builds an inverted index
	 * 
	 * @param textPath - for individual file index the file, if directory index
	 *                 files in within the directory
	 * @param index    - inverted index being built
	 * @throws IOException - if there is am issue reading the textPath
	 */
	public static void process(Path textPath, InvertedIndex index) throws IOException {

		// Get a list of files from the specified path
		List<Path> files = FileFinder.listText(textPath, textPath);

		// Iterate through the files and index their contents
		for (Path file : files) {
			indexAll(index, file);
		}

	}

	/**
	 * Method that indexes the words in a file, associating each word with the
	 * document's file name and position, and maintains a word count for the
	 * document in the given inverted index.
	 *
	 * @param index - The inverted index to update with the indexed words.
	 * @param path  - The path to the text document to be indexed.
	 * @throws IOException If an error occurs while reading the text document.
	 */
	public static void indexAll(InvertedIndex index, Path path) throws IOException {
		int count = 0;
		String fileName = path.toString();

		SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

		String[] words = new String[0]; // Reuse this array

		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Clean and split the line into words
				words = FileStemmer.parse(line);

				for (String word : words) {
					String stemmedWord = stemmer.stem(word).toString();
					index.insertWord(stemmedWord.toString(), fileName, ++count);
				}
			}
		}

	}

	/**
	 * Indexes the content of a document in the given inverted index.
	 *
	 * @param index    The InvertedIndex object to which the document content will
	 *                 be indexed.
	 * @param content  The content of the document to be indexed.
	 * @param location The location or identifier of the document.
	 * @throws IOException If an I/O error occurs while reading the document
	 *                     content.
	 */
	public static void indexAll(InvertedIndex index, String content, String location) throws IOException {
		int count = 0;

		SnowballStemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);

		String[] words = new String[0]; // Reuse this array

		try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
			String line;
			while ((line = reader.readLine()) != null) {
				// Clean and split the line into words
				words = FileStemmer.parse(line);

				for (String word : words) {
					String stemmedWord = stemmer.stem(word).toString();
					if (!stemmedWord.equals("")) {
						index.insertWord(stemmedWord.toString(), location, ++count);
					}
				}
			}
		}

	}
}
