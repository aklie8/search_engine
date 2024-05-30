package edu.usfca.cs272;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using spaces.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class JsonWriter {
	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param indent the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(Writer writer, int indent) throws IOException {
		while (indent-- > 0) {
			writer.write("  ");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeIndent(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer  the writer to use
	 * @param indent  the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void writeQuote(String element, Writer writer, int indent) throws IOException {
		writeIndent(writer, indent);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}

	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Writer writer, int indent) throws IOException {
		writer.write("[");

		var it = elements.iterator();

		if (it.hasNext()) {
			writer.write("\n");
			writeIndent(it.next().toString(), writer, indent + 1);
		}
		while (it.hasNext()) {
			writer.write(",\n");
			writeIndent(it.next().toString(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);

	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static void writeArray(Collection<? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArray(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArray(Collection, Writer, int)
	 */
	public static String writeArray(Collection<? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArray(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Writer writer, int indent)
			throws IOException {
		writer.write("{");

		var it = elements.entrySet().iterator();

		if (it.hasNext()) {
			writer.write("\n");
			var entry = it.next();
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": " + entry.getValue());
		}
		while (it.hasNext()) {
			writer.write(",\n");
			var entry = it.next();
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": " + entry.getValue());
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObject(Map, Writer, int)
	 */
	public static void writeObject(Map<String, ? extends Number> elements, Path path) throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObject(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObject(Map, Writer, int)
	 */
	public static String writeObject(Map<String, ? extends Number> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObject(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation levels
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeArray(Collection)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("{");

		var it = elements.entrySet().iterator();

		if (it.hasNext()) {
			writer.write("\n");
			var entry = it.next();
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": ");
			writeArray(entry.getValue(), writer, indent + 1);
		}
		while (it.hasNext()) {
			writer.write(",\n");
			var entry = it.next();
			writeQuote(entry.getKey(), writer, indent + 1);
			writer.write(": ");
			writeArray(entry.getValue(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);

	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static void writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeObjectArrays(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeObjectArrays(Map<String, ? extends Collection<? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeObjectArrays(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects. The generic
	 * notation used allows this method to be used for any type of collection with
	 * any type of nested map of String keys to number objects.
	 *
	 * @param elements the elements to write
	 * @param writer   the writer to use
	 * @param indent   the initial indent level; the first bracket is not indented,
	 *                 inner elements are indented by one, and the last bracket is
	 *                 indented at the initial indentation level
	 * @throws IOException if an IO error occurs
	 *
	 * @see Writer#write(String)
	 * @see #writeIndent(Writer, int)
	 * @see #writeIndent(String, Writer, int)
	 * @see #writeObject(Map)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Writer writer,
			int indent) throws IOException {
		writer.write("[");

		var it = elements.iterator();

		if (it.hasNext()) {
			writer.write("\n");
			writeObject(it.next(), writer, indent + 1);
		}
		while (it.hasNext()) {
			writer.write(",\n");
			writeObject(it.next(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);

	}

	/**
	 * Writes the elements as a pretty JSON array with nested objects to file.
	 *
	 * @param elements the elements to write
	 * @param path     the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see Files#newBufferedReader(Path, java.nio.charset.Charset)
	 * @see StandardCharsets#UTF_8
	 * @see #writeArrayObjects(Collection)
	 */
	public static void writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeArrayObjects(elements, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array with nested objects.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeArrayObjects(Collection)
	 */
	public static String writeArrayObjects(Collection<? extends Map<String, ? extends Number>> elements) {
		try {
			StringWriter writer = new StringWriter();
			writeArrayObjects(elements, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @param backwardsIndex - backwardsIndex data structure being converted into
	 *                       'pretty JSON format'
	 * @param writer         - the writer to use
	 * @param indent         - the initial indent level
	 * @throws IOException - IOException if an IO error occurs
	 * 
	 *                     OG
	 */
	public static void writeIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> backwardsIndex, Writer writer,
			int indent) throws IOException {
		writer.write("{");
		var it = backwardsIndex.entrySet().iterator();

		if (it.hasNext()) {
			writeIndexEntry(it.next(), writer, indent);
		}
		while (it.hasNext()) {
			writer.write(",");
			writeIndexEntry(it.next(), writer, indent);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes an index entry to a Writer, including a key and a nested map of
	 * collections of numbers.
	 *
	 * @param entry  The entry to write to the writer.
	 * @param writer The Writer to write the entry to.
	 * @param indent The number of spaces to use for indentation.
	 * @throws IOException If an I/O error occurs while writing to the Writer.
	 */
	public static void writeIndexEntry(
			Map.Entry<String, ? extends Map<String, ? extends Collection<? extends Number>>> entry, Writer writer,
			int indent) throws IOException {
		writer.write("\n");
		writeQuote(entry.getKey(), writer, indent + 1);
		writer.write(": ");
		writeObjectArrays(entry.getValue(), writer, indent + 1);
	}

	/**
	 * @param backwardsIndex - backwardsIndex data structure being converted into
	 *                       'pretty JSON format
	 * @param path           - path to file to write to
	 * @throws IOException - IOException if an IO error occurs
	 */
	public static void writeIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> backwardsIndex, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeIndex(backwardsIndex, writer, 0);
		}
	}

	/**
	 * Returns the invereted index as a pretty JSON object with nested arrays.
	 *
	 * @param backwardsIndex the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see StringWriter
	 * @see #writeObjectArrays(Map, Writer, int)
	 */
	public static String writeIndex(
			Map<String, ? extends Map<String, ? extends Collection<? extends Number>>> backwardsIndex) {
		try {
			StringWriter writer = new StringWriter();
			writeIndex(backwardsIndex, writer, 0);
			return writer.toString();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Writes a list of search results as a JSON array to a Writer.
	 * 
	 * @param results The list of search results to write.
	 * @param writer  The Writer to which the JSON data will be written.
	 * @param indent  The indentation level for formatting the JSON.
	 * @throws IOException If an I/O error occurs while writing to the Writer.
	 */
	public static void writeSearchResultArray(List<InvertedIndex.SearchResult> results, Writer writer, int indent)
			throws IOException {
		writer.write("[");
		var it = results.iterator();
		if (it.hasNext()) {
			writer.write("\n");
			writeIndent(writer, indent + 1);
			writeResult(it.next(), writer, indent + 1);
		}
		while (it.hasNext()) {
			writer.write(",\n");
			writeIndent(writer, indent + 1);
			writeResult(it.next(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("]", writer, indent);
	}

	/**
	 * Writes a single search result as a JSON object to a Writer.
	 * 
	 * @param result The search result to write.
	 * @param writer The Writer to which the JSON data will be written.
	 * @param indent The indentation level for formatting the JSON.
	 * @throws IOException If an I/O error occurs while writing to the Writer.
	 */
	public static void writeResult(InvertedIndex.SearchResult result, Writer writer, int indent) throws IOException {
		writer.write("{\n");
		writeQuote("count", writer, indent + 1);
		writer.write(": " + result.getMatchCount());

		writer.write(", ");
		writer.write("\n");

		writeQuote("score", writer, indent + 1);
		writer.write(": " + String.format("%.8f", result.getScore()));

		writer.write(", ");
		writer.write("\n");

		writeQuote("where", writer, indent + 1);
		writer.write(": \"" + result.getLocation() + "\"");

		writer.write("\n");

		writeIndent("}", writer, indent);

	}

	/**
	 * Writes a map of queries and their corresponding search results as a JSON
	 * object to a Writer.
	 *
	 * @param queries The map of queries and search results to write.
	 * @param writer  The Writer to which the JSON data will be written.
	 * @param indent  The indentation level for formatting the JSON.
	 * @throws IOException If an I/O error occurs while writing to the Writer.
	 */
	public static void writeQueryResults(Map<String, List<InvertedIndex.SearchResult>> queries, Writer writer,
			int indent) throws IOException {

		writeIndent("{\n", writer, indent);
		var it = queries.entrySet().iterator();

		if (it.hasNext()) {
			Map.Entry<String, List<InvertedIndex.SearchResult>> queryResults = it.next();
			writeQuote(queryResults.getKey(), writer, indent + 1);
			writer.write(": ");
			writeSearchResultArray(queryResults.getValue(), writer, indent + 1);
		}

		while (it.hasNext()) {
			writer.write(",\n");
			Map.Entry<String, List<InvertedIndex.SearchResult>> queryResults = it.next();
			writeQuote(queryResults.getKey(), writer, indent + 1);
			writer.write(": ");
			writeSearchResultArray(queryResults.getValue(), writer, indent + 1);
		}
		writer.write("\n");
		writeIndent("}", writer, indent);
	}

	/**
	 * Writes a map of queries and their corresponding search results as a JSON
	 * object to a file specified by a Path.
	 *
	 * @param queries The map of queries and search results to write.
	 * @param path    The Path to the file where the JSON data will be written.
	 * @throws IOException If an I/O error occurs while writing to the file.
	 */
	public static void writeQueryResults(Map<String, List<InvertedIndex.SearchResult>> queries, Path path)
			throws IOException {
		try (BufferedWriter writer = Files.newBufferedWriter(path, UTF_8)) {
			writeQueryResults(queries, writer, 0);
		}
	}
}
