import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * @author josh
 * 
 * The class containing the methods to perform single-threaded searching
 *
 */
public class QuerySearcher implements QuerySearchInterface
{

	/**
	 * results - The data structure to store query / InvertedIndex.SearchResults
	 */
	private final Map<String, ArrayList<InvertedIndex.SearchResult>> results;

	/**
	 * The inverted index to use
	 */
	private final InvertedIndex index; 

	/**
	 * Initializes the necessary data structures for this class
	 * @param index - The inverted index to use
	 */
	public QuerySearcher(InvertedIndex index)
	{
		this.results = new TreeMap<String, ArrayList<InvertedIndex.SearchResult>>();
		this.index = index;

	}
	/**
	 * search - Performs a line by line search on a text file
	 *
	 * @param line - The line query
	 * @param exactSearch - Flag to perform an exact search if set true
	 */
	@Override
	public void search(String line, boolean exactSearch)
	{
		Set<String> queries = TextStemmer.uniqueStems(line);
		String joined = String.join(" ", queries);

		if (queries.isEmpty() || containsQuery(joined))
		{
			return;
		}

		ArrayList<InvertedIndex.SearchResult> searches = index.search(queries, exactSearch);
		results.put(joined, searches);
	}

	/**
	 * containsQuery - Returns true if a given query is found in the data structure
	 * @param key - The search query
	 * @return Returns true if a given query is found in the data structure
	 */
	@Override
	public boolean containsQuery(String key)
	{
		return results.containsKey(key);
	}


	/**
	 * toJson - Outputs query/InvertedIndex.SearchResult pairings to a JSON file
	 * @param output - The output file path
	 * @throws IOException Throws an IOException if the path cannot be read
	 */
	@Override
	public void resultsToJson(Path output) throws IOException
	{
		JsonWriter.asNestedSearch(results, output);
	}
	
	/**
	 * getResults - Returns an unmodifiable view of the search results list given a search query
	 * @param query  - The search query
	 * @return Returns an unmodifiable view of the search results list given a search query
	 */
	@Override
	public List<InvertedIndex.SearchResult> getResults(String query)
	{
		if (!containsQuery(query))
		{
			return Collections.unmodifiableList(Collections.emptyList());
		}

		return Collections.unmodifiableList(results.get(query));	
	}
	
	/**
	 * toString - Outputs the query/InvertedIndex.SearchResult pairings to a JSON string
	 * @return Returns the JSON string format of the map
	 */
	@Override
	public String toString()
	{
		return JsonWriter.asNestedSearch(results);
	}

}
