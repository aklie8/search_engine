package edu.usfca.cs272;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * A specialized version of {@link HttpsFetcher} that follows redirects and
 * returns HTML content if possible.
 *
 * @see HttpsFetcher
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2023
 */
public class HtmlFetcher {
	/**
	 * Returns {@code true} if and only if there is a "Content-Type" header and the
	 * first value of that header starts with the value "text/html"
	 * (case-insensitive).
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return {@code true} if the headers indicate the content type is HTML
	 */
	public static boolean isHtml(Map<String, List<String>> headers) {
		List<String> contentTypeHeader = headers.get("Content-Type");
		if (contentTypeHeader != null && !contentTypeHeader.isEmpty()) {
			String contentType = contentTypeHeader.get(0).toLowerCase();
			return contentType.startsWith("text/html");
		}
		return false;
	}

	/**
	 * Parses the HTTP status code from the provided HTTP headers, assuming the
	 * status line is stored under the {@code null} key.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the HTTP status code or -1 if unable to parse for any reasons
	 */
	public static int getStatusCode(Map<String, List<String>> headers) {
		List<String> statusLine = headers.get(null);
		if (statusLine != null && !statusLine.isEmpty()) {
			String[] parts = statusLine.get(0).split(" ");
			if (parts.length >= 2) {
				try {
					return Integer.parseInt(parts[1]);
				} catch (NumberFormatException e) {
					// Handle the case where status code cannot be parsed
				}
			}
		}
		return -1; // Return -1 if unable to parse the status code
	}

	/**
	 * If the HTTP status code is between 300 and 399 (inclusive) indicating a
	 * redirect, returns the first redirect location if it is provided. Otherwise
	 * returns {@code null}.
	 *
	 * @param headers the HTTP/1.1 headers to parse
	 * @return the first redirected location if the headers indicate a redirect
	 */
	public static String getRedirect(Map<String, List<String>> headers) {
		int statusCode = getStatusCode(headers);
		if (statusCode >= 300 && statusCode <= 399) {
			List<String> locationHeader = headers.get("Location");
			if (locationHeader != null && !locationHeader.isEmpty()) {
				return locationHeader.get(0);

			}
		}
		return null; // Return null if no redirect or no location header
	}

	/**
	 * Fetches the resource at the URL using HTTP/1.1 and sockets. If the status
	 * code is 200 and the content type is HTML, returns the HTML as a single
	 * string. If the status code is a valid redirect, will follow that redirect if
	 * the number of redirects is greater than 0. Otherwise, returns {@code null}.
	 * 
	 *
	 * 
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see HttpsFetcher#openConnection(URL)
	 * @see HttpsFetcher#printGetRequest(PrintWriter, URL)
	 * @see HttpsFetcher#getHeaderFields(BufferedReader)
	 *
	 * @see String#join(CharSequence, CharSequence...)
	 *
	 * @see #isHtml(Map)
	 * @see #getRedirect(Map)
	 */
	public static String fetch(URL url, int redirects) {
		String html = null;

		try (Socket socket = HttpsFetcher.openConnection(url);
				PrintWriter request = new PrintWriter(socket.getOutputStream());
				InputStreamReader input = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
				BufferedReader response = new BufferedReader(input)) {
			HttpsFetcher.printGetRequest(request, url);

			Map<String, List<String>> headers = HttpsFetcher.getHeaderFields(response);

			if (getStatusCode(headers) == 200 && isHtml(headers)) {
				// Fetch the HTML content
				StringBuilder htmlBuilder = new StringBuilder();
				String line;

				while ((line = response.readLine()) != null) {
					htmlBuilder.append(line).append("\n");
				}

				html = htmlBuilder.toString();

			} else if (redirects > 0) {
				String newURL = headers.get("Location").get(0);
				redirects--;

				html = fetch(newURL, redirects);
			}
		} catch (IOException e) {
			// Handle the exception gracefully and log it for debugging
			System.err.println("Error fetching HTML");
			html = null;
		}

		return html;
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)}.
	 *
	 * @param url       the url to fetch
	 * @param redirects the number of times to follow redirects
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url, int redirects) {
		try {
			return fetch(new URL(url), redirects);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Converts the {@link String} url into a {@link URL} object and then calls
	 * {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 *
	 * @see #fetch(URL, int)
	 */
	public static String fetch(String url) {
		return fetch(url, 0);
	}

	/**
	 * Calls {@link #fetch(URL, int)} with 0 redirects.
	 *
	 * @param url the url to fetch
	 * @return the html or {@code null} if unable to fetch the resource or the
	 *         resource is not html
	 */
	public static String fetch(URL url) {
		return fetch(url, 0);
	}
}
