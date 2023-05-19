import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Outputs several simple data structures in "pretty" JSON format where newlines
 * are used to separate elements and nested elements are indented using tabs.
 *
 * Warning: This class is not thread-safe. If multiple threads access this class
 * concurrently, access must be synchronized externally.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class JsonWriter
{
	/**
	 * Writes the elements as a pretty JSON array.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asArray(TreeSet<Integer> elements, Writer writer, int level) throws IOException
	{
		var iterator = elements.iterator();

		writer.write("[");

		if (iterator.hasNext())
		{
			writer.write("\n");
			indent(iterator.next().toString(), writer, level + 1);
		}

		while (iterator.hasNext())
		{
			writer.write(",\n");
			indent(iterator.next().toString(), writer, level + 1);
		}

		writer.write("\n");
		indent("]", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object
	 *
	 * @param elements - The elements to write
	 * @param writer - The writer to use
	 * @param level - The initial indent level
	 * @throws IOException Throws an IOException if the writer cannot write to the file path
	 */
	public static void asObject(Map<String, Integer> elements, Writer writer, int level) throws IOException
	{
		var iterator = elements.keySet().iterator();
		String key = null;

		writer.write("{");

		if (iterator.hasNext())
		{
			writer.write("\n");
			key = iterator.next();
			quote(key.toString(), writer, level + 1);
			writer.write(": ");
			writer.write(elements.get(key).toString());
		}

		while (iterator.hasNext())
		{
			key = iterator.next();
			writer.write(",\n");
			quote(key.toString(), writer, level + 1);
			writer.write(": ");
			writer.write(elements.get(key).toString());
		}

		writer.write("\n");
		indent("}", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object.
	 *
	 * @param elements the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedObject(Map<String, TreeSet<Integer>> elements, Writer writer, int level) throws IOException
	{
		var iterator = elements.keySet().iterator();
		String key = null;

		writer.write("{");

		if (iterator.hasNext())
		{
			writer.write("\n");
			key = iterator.next();
			quote(key.toString(), writer, level + 1);
			writer.write(": ");
			asArray(elements.get(key), writer, level + 1);
		}

		while (iterator.hasNext())
		{
			key = iterator.next();
			writer.write(",\n");
			quote(key.toString(), writer, level + 1);
			writer.write(": ");
			asArray(elements.get(key), writer, level + 1);
		}

		writer.write("\n");
		indent("}", writer, level);
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays. The generic
	 * notation used allows this method to be used for any type of map with any type
	 * of nested collection of integer objects.
	 *
	 * @param index the elements to write
	 * @param writer the writer to use
	 * @param level the initial indent level
	 * @throws IOException if an IO error occurs
	 */
	public static void asNestedMap(Map<String, TreeMap<String, TreeSet<Integer>>> index, Writer writer, int level) throws IOException
	{
		var iterator = index.keySet().iterator();
		String key = null;

		writer.write("{");

		if (iterator.hasNext())
		{
			writer.write("\n");
			key = iterator.next();
			quote(key, writer, level + 1);
			writer.write(": ");
			asNestedObject(index.get(key), writer, level + 1);
		}

		while (iterator.hasNext())
		{
			writer.write(",\n");
			key = iterator.next();
			quote(key, writer, level + 1);
			writer.write(": ");
			asNestedObject(index.get(key), writer, level + 1);
		}

		writer.write("\n}");
	}

	/**
	 * asSingleQuery - Outputs the InvertedIndex.SearchResult objects in a pretty JSON format
	 *
	 * @param searches - The list of InvertedIndex.SearchResults
	 * @param writer - The writer to use
	 * @param level - The initial indent level
	 * @throws IOException Throws an IOException if the writer cannot write to the file path
	 */
	public static void asSingleQuery(ArrayList<InvertedIndex.SearchResult> searches, Writer writer, int level) throws IOException
	{
		DecimalFormat FORMATTER = new DecimalFormat("0.00000000");

		var iterator = searches.iterator();

		InvertedIndex.SearchResult current = null;

		writer.write("[");

		if (iterator.hasNext())
		{
			current = iterator.next();

			if(current.getNumMatches() == 0)
			{
				current = iterator.next();
			}

			writer.write("\n");
			indent("{", writer, level + 1);

			writer.write("\n");
			quote("count", writer, level + 2);
			writer.write(": " + Integer.toString(current.getNumMatches()));

			writer.write(",\n");
			quote("score", writer, level + 2);
			writer.write(": " + FORMATTER.format(current.getScore()));

			writer.write(",\n");
			quote("where", writer, level + 2);
			writer.write(": ");
			quote(current.getLocation(), writer, level - 1);

			writer.write("\n");
			indent("}", writer, level + 1);
		}

		while (iterator.hasNext())
		{
			current = iterator.next();

			if (current.getNumMatches() == 0)
			{
				writer.write("\n");
				indent("]", writer, level);
				return;
			}

			writer.write(",\n");

			indent("{", writer, level + 1);

			writer.write("\n");
			quote("count", writer, level + 2);
			writer.write(": " + Integer.toString(current.getNumMatches()));

			writer.write(",\n");
			quote("score", writer, level + 2);
			writer.write(": " + FORMATTER.format(current.getScore()));

			writer.write(",\n");
			quote("where", writer, level + 2);
			writer.write(": ");
			quote(current.getLocation(), writer, level - 1);

			writer.write("\n");
			indent("}", writer, level + 1);
		}

		writer.write("\n");
		indent("]", writer, level);
	}

	/**
	 * asNestedSearch - Outputs search queries to a pretty JSON format
	 *
	 * @param queries - The data structure containing query/InvertedIndex.SearchResult parings
	 * @param writer - The writer to use
	 * @param level - The initial indent level
	 * @throws IOException Throws an IOException if the writer cannot write to the file path
	 */
	public static void asNestedSearch(Map<String, ArrayList<InvertedIndex.SearchResult>> queries, Writer writer, int level) throws IOException
	{
		var iterator = queries.keySet().iterator();
		String key = null;

		writer.write("{");

		if (iterator.hasNext())
		{
			key = iterator.next();
			writer.write("\n");
			quote(key, writer, level + 1);
			writer.write(": ");
			asSingleQuery(queries.get(key), writer, level + 1);
		}

		while (iterator.hasNext())
		{
			key = iterator.next();
			writer.write(",\n");
			quote(key, writer, level + 1);
			writer.write(": ");
			asSingleQuery(queries.get(key), writer, level + 1);
		}

		writer.write("\n");
		writer.write("}");
	}

	/**
	 * Writes the elements as a pretty JSON array to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asArray(TreeSet, Writer, int)
	 */
	public static void asArray(TreeSet<Integer> elements, Path path) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
		{
			asArray(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file
	 *
	 * @param elements - The elements to write
	 * @param path - The file output path
	 * @throws IOException Throws an IOException if the writer cannot write to the file path
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static void asObject(Map<String, Integer> elements, Path path) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
		{
			asObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object to file.
	 *
	 * @param elements the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static void asNestedObject(Map<String, TreeSet<Integer>> elements, Path path) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
		{
			asNestedObject(elements, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param map the elements to write
	 * @param path the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedMap(Map, Writer, int)
	 */
	public static void asNestedMap(Map<String, TreeMap<String, TreeSet<Integer>>> map, Path path) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
		{
			asNestedMap(map, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param queries - the elements to write
	 * @param output - the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asSingleQuery(ArrayList, Writer, int)
	 */
	public static void asSingleQuery(ArrayList<InvertedIndex.SearchResult> queries, Path output) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8))
		{
			asSingleQuery(queries, writer, 0);
		}
	}

	/**
	 * Writes the elements as a pretty JSON object with nested arrays to file.
	 *
	 * @param results the elements to write
	 * @param output the file path to use
	 * @throws IOException if an IO error occurs
	 *
	 * @see #asNestedSearch(Map, Writer, int)
	 */
	public static void asNestedSearch(Map<String, ArrayList<InvertedIndex.SearchResult>> results, Path output) throws IOException
	{
		try (BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8))
		{
			asNestedSearch(results, writer, 0);
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asArray(TreeSet, Writer, int)
	 */
	public static String asArray(TreeSet<Integer> elements)
	{
		try
		{
			StringWriter writer = new StringWriter();
			asArray(elements, writer, 0);
			return writer.toString();
		}

		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON array.
	 *
	 * @param wordCounts the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asObject(Map, Writer, int)
	 */
	public static String asObject(Map<String, Integer> wordCounts)
	{
		try
		{
			StringWriter writer = new StringWriter();
			asObject(wordCounts, writer, 0);
			return writer.toString();
		}

		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedObject(Map, Writer, int)
	 */
	public static String asNestedObject(TreeMap<String, TreeSet<Integer>> elements)
	{
		try
		{
			StringWriter writer = new StringWriter();
			asNestedObject(elements, writer, 0);
			return writer.toString();
		}

		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param elements the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedMap(Map, Writer, int)
	 */
	public static String asNestedMap(TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements)
	{
		try
		{
			StringWriter writer = new StringWriter();
			asNestedMap(elements, writer, 0);
			return writer.toString();
		}

		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param results the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asSingleQuery(ArrayList, Writer, int)
	 */
	public static String asSingleQuery(ArrayList<InvertedIndex.SearchResult> results)
	{
		try
		{
			StringWriter writer = new StringWriter();
			asSingleQuery(results, writer, 0);
			return writer.toString();
		}

		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * Returns the elements as a pretty JSON object with nested arrays.
	 *
	 * @param results the elements to use
	 * @return a {@link String} containing the elements in pretty JSON format
	 *
	 * @see #asNestedSearch(Map, Writer, int)
	 */
	public static String asNestedSearch(Map<String, ArrayList<InvertedIndex.SearchResult>> results)
	{
		try
		{
			StringWriter writer = new StringWriter();
			asNestedSearch(results, writer, 0);
			return writer.toString();
		}

		catch (IOException e)
		{
			return null;
		}
	}

	/**
	 * Indents the writer by the specified number of times. Does nothing if the
	 * indentation level is 0 or less.
	 *
	 * @param writer the writer to use
	 * @param level the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(Writer writer, int level) throws IOException
	{
		while (level-- > 0)
		{
			writer.write("\t");
		}
	}

	/**
	 * Indents and then writes the String element.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param level the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void indent(String element, Writer writer, int level) throws IOException
	{
		indent(writer, level);
		writer.write(element);
	}

	/**
	 * Indents and then writes the text element surrounded by {@code " "} quotation
	 * marks.
	 *
	 * @param element the element to write
	 * @param writer the writer to use
	 * @param level the number of times to indent
	 * @throws IOException if an IO error occurs
	 */
	public static void quote(String element, Writer writer, int level) throws IOException
	{
		indent(writer, level);
		writer.write('"');
		writer.write(element);
		writer.write('"');
	}
}
