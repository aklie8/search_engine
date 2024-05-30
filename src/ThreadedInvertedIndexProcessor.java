package edu.usfca.cs272;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.TreeSet;

/**
 * A Multi-threaded version of InvertedIndexProcessor
 */
public class ThreadedInvertedIndexProcessor {

	/**
	 * Reads 1 or more files and builds an inverted index
	 * 
	 * @param textPath - for individual file index the file, if directory index
	 *                 files in within the directory
	 * @param index    - inverted index being built
	 * @param queue    - queue containing the worker thread
	 * @throws IOException - if there is am issue reading the textPath
	 */
	public static void process(Path textPath, ThreadedInvertedIndex index, WorkQueue queue) throws IOException {

		// Get a list of files from the specified path
		List<Path> files = FileFinder.listText(textPath, textPath);

		// Iterate through the files and index their contents
		for (Path file : files) {
			Task task = new Task(index, file);
			queue.execute(task);
		}
		queue.finish();
	}

	/**
	 * Fetches and processes HTML content starting from the specified root URL,
	 * updating a threaded inverted index.
	 *
	 * @param rootUrl The root URL from which HTML content retrieval and processing
	 *                begin.
	 * @param index   The ThreadedInvertedIndex where the processed content will be
	 *                indexed.
	 * @param queue   The WorkQueue for managing and executing HTML processing tasks
	 *                concurrently.
	 * @param limit   The processing limit, indicating the maximum depth of link
	 *                traversal.
	 * @throws MalformedURLException - throwing MalformedURLException if seed url is
	 *                               Malformed
	 */
	public static void fetchAndProcessHtml(String rootUrl, ThreadedInvertedIndex index, WorkQueue queue, int limit)
			throws MalformedURLException {
		if (limit == 0) {
			return;
		}

		URL url = LinkFinder.cleanUri(LinkFinder.makeUri(rootUrl)).toURL();

		TreeSet<String> visited = new TreeSet<>();
		visited.add(url.toString());

		HtmlTask task = new HtmlTask(url, index, limit, queue, visited);
		queue.execute(task);
		queue.finish();
	}

	/**
	 * A task representing indexing a single file
	 */
	private static class Task implements Runnable {
		/**
		 * index - index to update
		 */
		private final ThreadedInvertedIndex index;
		/**
		 * path - path to file to be indexed
		 */
		private final Path path;

		/**
		 * @param index - index to update
		 * @param path  - path to file to be indexed
		 */
		public Task(ThreadedInvertedIndex index, Path path) {
			this.index = index;
			this.path = path;
		}

		@Override
		public void run() throws UncheckedIOException {

			try {
				InvertedIndex localIndex = new InvertedIndex();
				InvertedIndexProcessor.indexAll(localIndex, path);

				index.addAll(localIndex);

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

		}

	}

	/**
	 * A Runnable task for processing HTML content from a given URL and updating a
	 * threaded inverted index.
	 */
	private static class HtmlTask implements Runnable {

		/**
		 * The URL of the HTML content to process.
		 */
		private URL url;

		/**
		 * The threaded inverted index where the processed content will be indexed.
		 */
		private final ThreadedInvertedIndex index;

		/**
		 * The maximum number of urls to fetch
		 */
		private final int limit;

		/**
		 * queue the represents the WorkQueue
		 */
		private final WorkQueue queue;

		/**
		 * The set of visited url's
		 */
		private final TreeSet<String> visited;

		/**
		 * Constructs a new HtmlTask with the specified URL, threaded inverted index,
		 * and processing limit.
		 *
		 * @param url     The URL of the HTML content to process.
		 * @param index   The ThreadedInvertedIndex where the processed content will be
		 *                indexed.
		 * @param limit   The processing limit, indicating the maximum depth of link
		 *                traversal.
		 * @param queue   Queue the represents the WorkQueue
		 * @param visited The set of visited url's
		 */
		public HtmlTask(URL url, ThreadedInvertedIndex index, int limit, WorkQueue queue, TreeSet<String> visited) {
			this.url = url;
			this.index = index;
			this.limit = limit;
			this.queue = queue;
			this.visited = visited;
		}

		@Override
		public void run() {

			String content = HtmlFetcher.fetch(url, 3);
			content = HtmlCleaner.stripBlockElements(content);

			var links = LinkFinder.listUrls(url, content);

			content = HtmlCleaner.stripHtml(content);

			synchronized (visited) {
				for (var link : links) {
					if (visited.size() < limit && !visited.contains(link.toString())) {
						visited.add(link.toString());
						queue.execute(new HtmlTask(link, index, limit, queue, visited));
					}
				}
			}
			try

			{
				InvertedIndex localIndex = new InvertedIndex();
				InvertedIndexProcessor.indexAll(localIndex, content, url.toString());

				index.addAll(localIndex);

			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

		}

	}

}
