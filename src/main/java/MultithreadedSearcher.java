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
 *	MultithreadedSearcher - The class containing the methods to perform search operations
 *							using a work queue
 */
public class MultithreadedSearcher implements QuerySearchInterface
{
	/**
	 * results - The data structure to store query / InvertedIndex.SearchResults
	 */
	private final Map<String, ArrayList<InvertedIndex.SearchResult>> results;
 
	/**
	 * index - The thread safe inverted index data structure
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * The lock used to protect concurrent access to the underlying set.
	 */
	private final SimpleReadWriteLock lock;

	/**
	 * tasks - The Work Queue to use
	 */
	private final WorkQueue tasks;

	/**
	 * Initializes the inverted index to use for this class
	 *
	 * @param index - The inverted index
	 * @param tasks - The Work Queue to use 
	 */
	public MultithreadedSearcher(ThreadSafeInvertedIndex index, WorkQueue tasks)
	{
		results = new TreeMap<String, ArrayList<InvertedIndex.SearchResult>>();
		lock = new SimpleReadWriteLock();
		this.index = index;
		this.tasks = tasks;
	}

	/**
	 * search - Performs a line by line search on a text file
	 *
	 * @param path - The path of the file
	 * @param exactSearch - Flag to perform an exact search if set true
	 *
	 * @throws IOException throws an IOException if the file cannot be read
	 */
	@Override
	public void search(Path path, boolean exactSearch) throws IOException
	{
		QuerySearchInterface.super.search(path, exactSearch);
		tasks.finish();
	}

	/*
	 * search - Performs a multi-threaded search on a single line
	 * 
	 * @param line - The line query
	 * @param exactSearch - Flag to perform an exact search if set true
	 */
	@Override
	public void search(String line, boolean exactSearch)
	{
		tasks.execute(new Task(line, exactSearch));
		tasks.finish();

	}

	/**
	 * containsQuery - Returns true if a given query is found in the data structure
	 * @param key - The search query
	 * @return Returns true if a given query is found in the data structure
	 */
	@Override
	public boolean containsQuery(String key)
	{
		lock.readLock().lock();

		try
		{
			return results.containsKey(key);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * toJson - Outputs query/InvertedIndex.SearchResult pairings to a JSON file
	 * @param output - The output file path
	 * @throws IOException Throws an IOException if the path cannot be read
	 */
	@Override
	public void resultsToJson(Path output) throws IOException
	{
		lock.readLock().lock();

		try
		{
			JsonWriter.asNestedSearch(results, output);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}
	
	/**
	 * getResults - Returns an unmodifiable view of the search results list given a search query
	 * @param query  - The search query
	 * @return Returns an unmodifiable view of the search results list given a search query
	 */
	@Override
	public List<InvertedIndex.SearchResult> getResults(String query)
	{
		lock.readLock().lock();

		try
		{
			if (!containsQuery(query))
			{
				return Collections.unmodifiableList(Collections.emptyList());
			}

			return Collections.unmodifiableList(results.get(query));
		}

		finally
		{
			lock.readLock().unlock();
		}
	}
	
	/**
	 * toString - Outputs the query/InvertedIndex.SearchResult pairings to a JSON string
	 * @return Returns the JSON string format of the map
	 */
	@Override
	public String toString()
	{
		lock.readLock().lock();
		
		try 
		{
			return JsonWriter.asNestedSearch(results);
		}
		
		finally
		{
			lock.readLock().unlock();
		}
	}

	/**
	 * The task class that will update the shared paths and pending
	 * members in our task manager instance.
	 */
	private class Task implements Runnable
	{
		/**
		 * The line query
		 */
		private final String line;

		/**
		 * exactSearch - Flag to perform an exact search if set true
		 */
		private final boolean exactSearch;

		/**
		 * Initializes the members with the new task
		 *
		 * @param line - The line query
		 * @param exactSearch - Flag to perform an exact search if set true
		 */
		public Task(String line, boolean exactSearch)
		{
			this.line = line;
			this.exactSearch = exactSearch;
		}

		@Override
		public void run()
		{
			Set<String> queries = TextStemmer.uniqueStems(line);
			String joined = String.join(" ", queries);

			if (queries.isEmpty() || containsQuery(line))
			{
				return;
			}

			ArrayList<InvertedIndex.SearchResult> searches = index.search(queries, exactSearch);

			lock.writeLock().lock();

			try
			{
				results.put(joined, searches);
			}

			finally
			{
				lock.writeLock().unlock();
			}
		}

	}
}
