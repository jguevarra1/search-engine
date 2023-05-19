import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Josh Guevarra
 *
 *	Inverted index data structure containing all stemmed words in alphabetical order,
 *	with their path locations and their position within the file
 */
public class InvertedIndex
{
	/**
	 * invertedIndex - The inverted index data structure
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndex;

	/**
	 * wordCounts - The data structure containing file locations mapped to their word counts
	 */
	private final Map<String, Integer> wordCounts;

	/**
	 * Initializes the necessary data structures for the inverted index
	 */
	public InvertedIndex()
	{
		 this.invertedIndex = new TreeMap<String, TreeMap<String, TreeSet<Integer>>>();
		 this.wordCounts = new TreeMap<String, Integer>();
	}

	/**
	 * search - Returns an exactSearch or partialSearch on a line query
	 *
	 * @param lineQuery - The query line
	 * @param exactSearch - Performs an exact search if true
	 *
	 * @return Returns a list of searchResults
	 */
	public ArrayList<InvertedIndex.SearchResult> search(Set<String> lineQuery, boolean exactSearch)
	{
		return exactSearch ? exactSearch(lineQuery) : partialSearch(lineQuery);
	}

	/**
	 * exactSearch - Performs an exact search of words in a file. Counts the number of all exact matches
	 *
	 * @param lineQuery - The query to parse
	 * @return - Returns the set of SearchResults found associated with the query
	 */
	public ArrayList<InvertedIndex.SearchResult> exactSearch(Set<String> lineQuery)
	{
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		Map<String, SearchResult> lookup = new HashMap<String, SearchResult>();

		for (String key : lineQuery)
		{
			if (invertedIndex.containsKey(key))
			{
				searchLoop(key, results, lookup);
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * partialSearch - Performs an partial search of words in a file. Counts the number of all words that begin with the query
	 *
	 * @param lineQuery - The query to parse
	 * @return - Returns the set of SearchResults found associated with the query
	 */
	public ArrayList<InvertedIndex.SearchResult> partialSearch(Set<String> lineQuery)
	{
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		Map<String, SearchResult> lookup = new HashMap<String, SearchResult>();

		for (String query : lineQuery)
		{
			for (String key : invertedIndex.tailMap(query).keySet())
			{
				if (key.startsWith(query))
				{
					searchLoop(key, results, lookup);
				}

				else break;
			}
		}

		Collections.sort(results);
		return results;
	}

	/**
	 * searchLoop - Helper function to perform search loop operations
	 *
	 * @param key - The query key
	 * @param results - The list of search results
	 * @param lookup - Lookup map containing locations paired to SearchResult pairings
	 */
	private void searchLoop(String key, List<SearchResult> results, Map<String, SearchResult> lookup)
	{
		for (String location : invertedIndex.get(key).keySet())
		{
			if (!lookup.containsKey(location))
			{
				SearchResult result = new SearchResult(location);
				lookup.put(location, result);
				results.add(result);
			}

			lookup.get(location).update(key);
		}
	}

	/**
	 * add - The function that adds a word stem entry into the inverted index data structure
	 *       as well as the path of the file it was found in and the position it was located at
	 *
	 * @param stem	- The word stem to add into the index
	 * @param location	- The path location the stem was found in
	 * @param position	- The position of the word stem in the file
	 */
	public void add(String stem, String location, int position)
	{
		invertedIndex.putIfAbsent(stem, new TreeMap<String, TreeSet<Integer>>());
		invertedIndex.get(stem).putIfAbsent(location, new TreeSet<Integer>());

		boolean modified = invertedIndex.get(stem).get(location).add(position);

		if (modified)
		{
			wordCounts.putIfAbsent(location, 0);
			wordCounts.put(location, wordCounts.get(location) + 1);
		}
	}

	/**
	 * addAll - Function that adds all the elements of one inverted index to this one
	 *
	 * @param other - THe other inverted index to copy from
	 */
	public void addAll(InvertedIndex other)
	{
		for (String key : other.invertedIndex.keySet())
		{
			if (this.invertedIndex.containsKey(key))
			{
				for (String location : other.invertedIndex.get(key).keySet())
				{
					if (this.invertedIndex.get(key).containsKey(location))
					{
						this.invertedIndex.get(key).get(location).addAll(other.invertedIndex.get(key).get(location));
					}

					else
					{
						TreeSet<Integer> inner = other.invertedIndex.get(key).get(location);
						this.invertedIndex.get(key).put(location, inner);
					}
				}
			}

			else
			{
				TreeMap<String, TreeSet<Integer>> inner = other.invertedIndex.get(key);
				this.invertedIndex.put(key, inner);
			}
		}

		for (String location : other.wordCounts.keySet())
		{
			if (this.wordCounts.containsKey(location))
			{
				this.wordCounts.put(location, this.wordCounts.get(location) + other.wordCounts.get(location));
			}

			else
			{
				this.wordCounts.put(location, other.wordCounts.get(location));
			}
		}

	}

	/**
	 * getWords - Returns an unmodifiable view of the set of words found in the inverted index
	 *
	 * @return Returns an unmodifiable view of the the words found in the inverted index
	 */
	public Set<String> getWords()
	{
		return Collections.unmodifiableSet(invertedIndex.keySet());
	}

	/**
	 * getLocations - Returns an unmodifiable view of the set of locations a word was found in
	 * 				  in the inverted index
	 *
	 * @param key - The word in the inverted index
	 * @return - Returns an unmodifiable view of the set of locations a word was found in
	 * 			 in the inverted index
	 */
	public Set<String> getLocations(String key)
	{
		if (!containsWord(key))
		{
			return Collections.unmodifiableSet(Collections.emptySet());
		}

		return Collections.unmodifiableSet(invertedIndex.get(key).keySet());
	}

	/**
	 * getPositions - Returns an unmodifiable view of the set of positions a word was found in
	 * 				  a file in the inverted index
	 *
	 * @param key - The word in the inverted index
	 * @param location - The location of the word in the inverted index
	 * @return - Returns an unmodifiable view of the set of positions a word was found in
	 * 				  a file in the inverted index
	 */
	public Set<Integer> getPositions(String key, String location)
	{
		if(!containsLocation(key, location))
		{
			return Collections.unmodifiableSet(Collections.emptySet());
		}

		return Collections.unmodifiableSet(invertedIndex.get(key).get(location));
	}

	/**
	 * containsWord - Returns true if a word was found in the inverted index
	 *
	 * @param key - The word to check in the inverted index
	 * @return - Returns true if a word was found in the inverted index
	 */
	public boolean containsWord(String key)
	{
		return invertedIndex.containsKey(key);
	}

	/**
	 * containsLocation - Returns true if a word was found in a specified file
	 * 					  in the inverted index
	 *
	 * @param key - The word in the inverted index
	 * @param location - The file location of the word to check in the inverted index
	 * @return - Returns true if a word was found in a specified file
	 * 			 in the inverted index
	 */
	public boolean containsLocation(String key, String location)
	{
		return invertedIndex.containsKey(key) && invertedIndex.get(key).containsKey(location);
	}

	/**
	 * containsPosition - Returns true if a word in a specified file was found at that position
	 * 					  in the inverted index
	 *
	 * @param key - The word in the inverted index
	 * @param location - The location of the word in the inverted index
	 * @param position - The position of the word in the file to check in the inverted index
	 *
	 * @return - Returns true if a word in a specified file was found at that position
	 * 					  in the inverted index
	 */
	public boolean containsPosition(String key, String location, int position)
	{
		return containsLocation(key, location) && invertedIndex.get(key).get(location).contains(position);
	}

	/**
	 * sizeWords - Returns the number of words in the inverted index
	 * @return - Returns the number of words in the inverted index
	 */
	public int sizeWords()
	{
		if (invertedIndex.isEmpty())
		{
			return 0;
		}

		return invertedIndex.keySet().size();
	}

	/**
	 * sizeLocations - Returns the number of locations a word was found in
	 *
	 * @param key - The word in the index
	 * @return - Returns the number of file locations a word was found in
	 */
	public int sizeLocations(String key)
	{
		if (invertedIndex.isEmpty() || !containsWord(key))
		{
			return 0;
		}

		return invertedIndex.get(key).keySet().size();
	}

	/**
	 * sizePositions - Returns the number of times a word was found in a specified location
	 *
	 * @param key - The word in the index
	 * @param location - The file location of the word in the index
	 * @return - Returns the number of times a word was found in a specified location
	 */
	public int sizePositions(String key, String location)
	{
		if (invertedIndex.isEmpty() || !containsLocation(key, location))
		{
			return 0;
		}

		return invertedIndex.get(key).get(location).size();
	}

	/**
	 * toJson - Outputs the inverted index to a JSON format file
	 * 			(Does not break encapsulation this way)
	 *
	 * @param output - The path to output the index to
	 * @throws IOException - Throws an IOException if the path file cannot be read
	 */
	public void indexToJson(Path output) throws IOException
	{
		JsonWriter.asNestedMap(invertedIndex, output);
	}

	/**
	 * countsToJson - Outputs the wordCounts data structure in JSON format
	 *
	 * @param output - The location to output to
	 * @throws IOException - Throws IOException if the location can't be read
	 */
	public void countsToJson(Path output) throws IOException
	{
		JsonWriter.asObject(wordCounts, output);
	}

	/**
	 * containsLocation - Returns true if a file location is in the word count data structure
	 * @param location - The file location
	 *
	 * @return Returns true if a file location is in the word count data structure
	 */
	public boolean containsLocation(String location)
	{
		return wordCounts.containsKey(location);
	}

	/**
	 * getCount - Returns the total number of words found in a given location
	 * @param location - The file location
	 *
	 * @return - Returns the total number of words found in a given location
	 */
	public int getCount(String location)
	{
		if (!containsLocation(location))
		{
			return 0;
		}

		return wordCounts.get(location);
	}

	/**
	 * Returns a string view of the index
	 *
	 * @return - returns a string view of the index
	 */
	@Override
	public String toString()
	{		
		return JsonWriter.asNestedMap(invertedIndex);
	}

	/**
	 * @author Josh Guevarra
	 *
	 * SearchResult - The class containing the data members for a Search Result
	 *
	 */
	public class SearchResult implements Comparable<SearchResult>
	{
		/**
		 * numMatches - Number of times a query matched words in a file
		 */
		private int numMatches;

		/**
		 * location - The file location
		 */
		private String location;

		/**
		 * score - The score of the search result
		 */
		private double score;

		/** Initalizes a SearchResult object
		 *
		 * @param location - The file location assocaited with the SearchResult
		 */
		public SearchResult(String location)
		{
			this.location = location;
			this.numMatches = 0;
			this.score = 0;
		}

		/**
		 * update - Updates the properties of a SearchResult object
		 * @param key - The query key
		 */
		private void update(String key)
		{
			numMatches += invertedIndex.get(key).get(location).size();
			double score = (double) numMatches / wordCounts.get(location);

			this.score = score;
		}


		/**
		 * getNumMatches - Returns the number of query matches in a file
		 * @return Returns the number of query matches in a file
		 */
		public int getNumMatches()
		{
			return this.numMatches;
		}

		/**
		 * getLocation - Returns the file location of a search query
		 * @return Returns the file location of a search query
		 */
		public String getLocation()
		{
			return this.location;
		}

		/**
		 * getScore - Returns the score of the search result (total matches / total words)
		 * @return Returns the score of the search result (total matches / total words)
		 */
		public double getScore()
		{
			return this.score;
		}

		/**
		 * compareTo - Compare method for SearchResult objects
		 *
		 * The comparisons are ranked as followed:
		 * 		1) Score - Highest scores will come first
		 * 		2) Number of Matches - If scores are the same, then objects are sorted by number of matches
		 * 							   in descending order
		 * 		3) Location - If scores and num matches are both the same, then objects are sorted
		 * 				      by alphabetical order of their locations
		 */
		@Override
		public int compareTo(SearchResult other)
		{
			if (this.getScore() != other.getScore())
			{
				if (this.getScore() > other.getScore())
				{
					return -1;
				}

				if (this.getScore() == other.getScore())
				{
					return 0;
				}

				return 1;
			}

			if (this.getNumMatches() != other.getNumMatches())
			{
				if (this.getNumMatches() > other.getNumMatches())
				{
					return -1;
				}

				if (this.getNumMatches() == other.getNumMatches())
				{
					return 0;
				}

				return 1;
			}

			return this.getLocation().compareToIgnoreCase(other.getLocation());
		}
	}
}
