// Package declaration for the current Java file
package edu.usfca.cs272;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Aklile Tesfaye
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class Driver {

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 */
	public static void main(String[] args) {
		// store initial start time
		Instant start = Instant.now();

		ArgumentParser parser = new ArgumentParser(args);

		InvertedIndex index;

		SearchProcessorInterface processor;

		ThreadedInvertedIndex threadedIndex = null;

		WorkQueue queue = null;

		if (parser.hasFlag("-threads") || parser.hasFlag("-html")) {
			int numThreads = parser.getInteger("-threads", 5);

			if (numThreads < 1) {
				numThreads = 5;
			}

			threadedIndex = new ThreadedInvertedIndex();
			queue = new WorkQueue(numThreads);
			processor = new ThreadedSearchProcessor(threadedIndex, parser.hasFlag("-partial"), queue);

			index = threadedIndex;

		} else {
			index = new InvertedIndex();
			processor = new SearchProcessor(index, parser.hasFlag("-partial"));

		}

		if (parser.hasFlag("-text")) {
			Path textPath = parser.getPath("-text");
			try {
				if (textPath != null) {
					if (threadedIndex != null && queue != null) {
						ThreadedInvertedIndexProcessor.process(textPath, threadedIndex, queue);
					} else {
						InvertedIndexProcessor.process(textPath, index);
					}
				} else {
					System.out.println("Error: Cannot find path to the text files");
				}
			} catch (IOException e) {
				System.out.println("Error while processing input file");
			}
		}

		if (parser.hasFlag("-html")) {
			String seedUrl = parser.getString("-html");

			if (seedUrl != null) {
				try {
					ThreadedInvertedIndexProcessor.fetchAndProcessHtml(seedUrl, threadedIndex, queue,
							parser.getInteger("-crawl", 1));
				} catch (MalformedURLException e) {
					System.out.println("Error: seed url is Malformed");

				}
			} else {
				System.out.println("Error: Cannot find seed url");
			}
		}

		// if the query flag is found
		if (parser.hasFlag("-query"))

		{
			// get the file path the query is associated with
			Path queryPath = parser.getPath("-query");
			// if the -query flag was provided without a valid file path argument
			if (queryPath == null) {
				System.out.println("Query path not provided");
			} else {
				// if a valid query path is provided, this line calls the processQueryFile &
				// processes a search query
				try {
					processor.processQueryFile(queryPath);
				} catch (IOException e) {
					System.out.println("Error writing to " + queryPath);
				}
			}

		}

		if (queue != null) {
			queue.shutdown();
		}

		// If a countPath is provided, write the word counts to a JSON file
		if (parser.hasFlag("-counts")) {
			Path countPath = parser.getPath("-counts", Path.of("counts.json"));
			try {
				JsonWriter.writeObject(index.getWordCounts(), countPath);
			} catch (IOException e) {
				System.out.println("Error writing to " + countPath);
			}
		}

		// If a indexPath is provided, write the word index to a JSON file
		if (parser.hasFlag("-index")) {
			Path indexPath = parser.getPath("-index", Path.of("index.json"));
			try {
				index.writeJson(indexPath);
			} catch (IOException e) {
				System.out.println("Error writing to " + indexPath);
			}
		}

		if (parser.hasFlag("-results")) {
			Path resultsPath = parser.getPath("-results", Path.of("results.json"));
			try {
				processor.writeJson(resultsPath);
			} catch (IOException e) {
				System.out.println("Error writing to " + resultsPath);
			}
		}

		// Calculate time elapsed and output it
		long elapsed = Duration.between(start, Instant.now()).toMillis();
		double seconds = (double) elapsed / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
