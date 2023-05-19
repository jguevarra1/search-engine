import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * @author Josh Guevarra
 *
 * The class containing the helper functions that build the inverted index
 *
 */
public class InvertedIndexBuilder
{
	/**
	 *	The default stemmer algorithm used in this class
	 */
	public static final SnowballStemmer.ALGORITHM ENGLISH = SnowballStemmer.ALGORITHM.ENGLISH;

	/**
	 * findTextFiles - The function that gets the files found in the "-text" flag to create the inverted index
	 *
	 * @param input - The input directory
	 * @param index	- The inverted index data structure
	 *
	 * @throws IOException Throws and exception if the file cannot be read
	 */
	public static void findTextFiles(Path input, InvertedIndex index) throws IOException
	{
		Set<Path> listPaths = PathTraverser.traverseDirectory(input);

		for (Path path : listPaths)
		{
			createIndex(path, index);
		}
	}

	/**
	 * createIndex - The function that creates the inverted index data structure
	 * 				 The function adds a stemmed word along with its path location
	 * 				 and position in the text file into the inverted index
	 *
	 * @param path - The path of the file
	 * @param index - The inverted index data structure
	 *
	 * @throws IOException Throws an exception if the file cannot be read
	 */
	public static void createIndex(Path path, InvertedIndex index) throws IOException
	{
		String line = null;
		int position = 1;
		Stemmer stemmer  = new SnowballStemmer(ENGLISH);
		String location = path.toString();
		
		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
		{
			while ((line = reader.readLine()) != null)
			{
				for (String word : TextParser.parse(line))
				{
					index.add(stemmer.stem(word).toString(), location, position);
					position++;
				}
			}
		}
	}
}

