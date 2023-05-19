import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Josh Guevarra
 *
 * querySearcher - The class containing the methods to perform query searches in a file
 */
public interface QuerySearchInterface
{
	/**
	 * search - Performs a line by line search on a text file
	 *
	 * @param path - The path of the file
	 * @param exactSearch - Flag to perform an exact search if set true
	 *
	 * @throws IOException throws an IOException if the file cannot be read
	 */
	public default void search(Path path, boolean exactSearch) throws IOException
	{
		String line = null;

		try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
		{
			while ((line = reader.readLine()) != null)
			{
				search(line, exactSearch);
			}
		}
	}

	/**
	 * search - Performs a line by line search on a text file
	 *
	 * @param line - The line query
	 * @param exactSearch - Flag to perform an exact search if set true
	 */
	public void search(String line, boolean exactSearch);

	/**
	 * containsQuery - Returns true if a given query is found in the data structure
	 * @param key - The search query
	 * @return Returns true if a given query is found in the data structure
	 */
	public boolean containsQuery(String key);

	/**
	 * toJson - Outputs query/InvertedIndex.SearchResult pairings to a JSON file
	 * @param output - The output file path
	 * @throws IOException Throws an IOException if the path cannot be read
	 */
	public void resultsToJson(Path output) throws IOException;
	
	/**
	 * toString - Outputs the query/InvertedIndex.SearchResult pairings to a JSON string
	 * @param results - The map containing query/InvertedIndex.SearchResult pairings
	 * @return Returns the JSON string format of the map
	 */
	public String toString();
	
	/**
	 * getResults - Returns an unmodifiable view of the search results list given a search query
	 * @param query  - The search query
	 * @return Returns an unmodifiable view of the search results list given a search query
	 */
	public List<InvertedIndex.SearchResult> getResults(String query);
	
}