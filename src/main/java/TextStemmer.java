import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * Utility class for parsing and stemming text and text files into collections
 * of stemmed words.
 *
 * @see TextParser
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class TextStemmer
{
	/** The default stemmer algorithm used by this class. */
	public static final SnowballStemmer.ALGORITHM ENGLISH = SnowballStemmer.ALGORITHM.ENGLISH;

	/** The default character set used by this class. */
	public static final Charset UTF8 = StandardCharsets.UTF_8;

	/**
	 *  stemLine - The helper function that takes in a String line of words and stems them
	 *  		   into a collection data structure.
	 *
	 * @param line		The String containing the words to parse
	 * @param stemmer	The stemmer algorithm used to "clean" the words
	 * @param stems		The collection containing the cleaned/stemmed words
	 */
	public static void stemLine(String line, Stemmer stemmer, Collection<String> stems)
	{
		for (String word : TextParser.parse(line))
		{
			stems.add(stemmer.stem(word).toString());
		}
	}

	/**
	 * Parses each line into cleaned and stemmed words.
	 *
	 * @param line the line of words to clean, split, and stem
	 * @param stemmer the stemmer to use
	 * @return a list of cleaned and stemmed words in parsed order
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static List<String> listStems(String line, Stemmer stemmer)
	{
		List<String> stems = new ArrayList<String>();
		stemLine(line, stemmer, stems);

		return stems;
	}

	/**
	 * Parses each line into cleaned and stemmed words using the default stemmer.
	 *
	 * @param line the line of words to parse and stem
	 * @return a list of cleaned and stemmed words in parsed order
	 *
	 * @see SnowballStemmer
	 * @see #ENGLISH
	 * @see #listStems(String, Stemmer)
	 */
	public static List<String> listStems(String line)
	{
		return listStems(line, new SnowballStemmer(ENGLISH));
	}

	/**
	 * Reads a file line by line, parses each line into cleaned and stemmed words
	 * using the default stemmer.
	 *
	 * @param input the input file to parse and stem
	 * @return a list of stems from file in parsed order
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #UTF8
	 * @see #uniqueStems(String, Stemmer)
	 * @see TextParser#parse(String)
	 */
	public static List<String> listStems(Path input) throws IOException
	{
		List<String> stems = new ArrayList<String>();
		String line = null;
		Stemmer stemmer = new SnowballStemmer(ENGLISH);

		try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8))
		{
			while ((line = reader.readLine()) != null)
			{
				stemLine(line, stemmer, stems);
			}
		}

		return stems;
	}

	/**
	 * Parses the line into unique, sorted, cleaned, and stemmed words.
	 *
	 * @param line the line of words to parse and stem
	 * @param stemmer the stemmer to use
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see Stemmer#stem(CharSequence)
	 * @see TextParser#parse(String)
	 */
	public static Set<String> uniqueStems(String line, Stemmer stemmer)
	{
		Set<String> sortedStems = new TreeSet<String>();
		stemLine(line, stemmer, sortedStems);

		return sortedStems;
	}

	/**
	 * Parses the line into unique, sorted, cleaned, and stemmed words using the
	 * default stemmer.
	 *
	 * @param line the line of words to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words
	 *
	 * @see SnowballStemmer
	 * @see #ENGLISH
	 * @see #uniqueStems(String, Stemmer)
	 */
	public static Set<String> uniqueStems(String line)
	{
		return uniqueStems(line, new SnowballStemmer(ENGLISH));
	}

	/**
	 * Reads a file line by line, parses each line into unique, sorted, cleaned, and
	 * stemmed words using the default stemmer.
	 *
	 * @param input the input file to parse and stem
	 * @return a sorted set of unique cleaned and stemmed words from file
	 * @throws IOException if unable to read or parse file
	 *
	 * @see #UTF8
	 * @see #uniqueStems(String, Stemmer)
	 * @see TextParser#parse(String)
	 */
	public static Set<String> uniqueStems(Path input) throws IOException
	{
		Set<String> sortedStems = new TreeSet<String>();
		String line = null;
		Stemmer stemmer = new SnowballStemmer(ENGLISH);

		try (BufferedReader reader = Files.newBufferedReader(input, StandardCharsets.UTF_8))
		{
			while ((line = reader.readLine()) != null)
			{
				stemLine(line, stemmer, sortedStems);
			}
		}

		return sortedStems;
	}
}
