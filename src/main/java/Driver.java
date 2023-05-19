import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Class responsible for running this project based on the provided command-line
 * arguments. See the README for details.
 *
 * @author Josh Guevarra
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class Driver
{
	/**
	 * defaultPath - The default output path
	 */
	private static final Path defaultPath = Path.of("index.json");

	/**
	 * defaultCountPath - The default counts path
	 */
	private static final Path defaultCountPath = Path.of("counts.json");

	/**
	 * defaultJsonPath - The default JSON output path
	 */
	private static final Path defaultJsonPath = Path.of("results.json");

	/**
	 * defaultNumThreads - The default number of threads
	 */
	private static final int defaultNumThreads = 5;
	
	/**
	 * port - The default server port
	 */
	private static final int PORT = 8080;

	/**
	 * Initializes the classes necessary based on the provided command-line
	 * arguments. This includes (but is not limited to) how to build or search an
	 * inverted index.
	 *
	 * @param args flag/value pairs used to start this program
	 * @throws MalformedURLException
	 */
	public static void main(String[] args) throws MalformedURLException
	{
		//store initial start time
		Instant start = Instant.now();

		ArgumentMap argMap = new ArgumentMap(args);
		InvertedIndex index = null;

		ThreadSafeInvertedIndex threadSafeIndex = null;
		WorkQueue tasks = null;
		QuerySearchInterface searcher = null;

		WebCrawler crawler = null;

		if (argMap.hasFlag("-threads"))
		{
			int numThreads;

			if (argMap.getInteger("-threads") == null || argMap.getInteger("-threads") < 1)
			{
				numThreads = defaultNumThreads;
			}

			else 
			{
				numThreads = argMap.getInteger("-threads");
			}

			threadSafeIndex = new ThreadSafeInvertedIndex();
			index = threadSafeIndex;

			tasks = new WorkQueue(numThreads);
			searcher = new MultithreadedSearcher(threadSafeIndex, tasks);
		}

		else
		{
			index = new InvertedIndex();
			searcher = new QuerySearcher(index);
		}

		if (argMap.hasFlag("-html"))
		{
			if (!argMap.hasFlag("-threads"))
			{
				threadSafeIndex = new ThreadSafeInvertedIndex();
				tasks = new WorkQueue(defaultNumThreads);
				searcher = new MultithreadedSearcher(threadSafeIndex, tasks);
			}

			int maxCrawlLimit;

			if (argMap.hasFlag("-max"))
			{
				maxCrawlLimit = argMap.getInteger("-max");
			}

			else
			{
				maxCrawlLimit = 1;
			}

			crawler = new WebCrawler(threadSafeIndex, tasks, maxCrawlLimit);
			crawler.crawlUrl(new URL(argMap.getString("-html")));
			index = threadSafeIndex;
			
			if (argMap.hasFlag("-server"))
			{
				Server server = new Server(argMap.getInteger(argMap.getString("-server"), PORT));
				ServletHandler handler = new ServletHandler();
				try 
				{
					handler.addServletWithMapping(new ServletHolder(new SearchEngineServlet(searcher, 
							argMap.hasFlag("-exact"))), "/search");
					
					server.setHandler(handler);
					server.start();
					server.join();
				} 
				
				catch (Exception e) 
				{
					e.printStackTrace();	
				}
			}	
		}

		if (argMap.hasFlag("-text"))
		{
			try
			{
				if (tasks != null && threadSafeIndex != null)
				{
					MultithreadedBuilder.findTextFiles(argMap.getPath("-text", defaultPath), threadSafeIndex, tasks);
				}

				else
				{
					InvertedIndexBuilder.findTextFiles(argMap.getPath("-text", defaultPath), index);
				}
			}

			catch (IOException e)
			{
				System.out.println("Unable to build the inverted index from the path: " + argMap.getPath("-text", defaultPath));
			}
		}

		if (argMap.hasFlag("-query") && argMap.getPath("-query") != null)
		{
			try
			{
				searcher.search(argMap.getPath("-query"), argMap.hasFlag("-exact"));

			}

			catch (IOException e)
			{
				System.out.println("Unable to obtain the parsed queries from the path: " + argMap.getPath("-query"));
			}
		}

		if (argMap.hasFlag("-index"))
		{
			try
			{
				index.indexToJson(argMap.getPath("-index", defaultPath));
			}

			catch (IOException e)
			{
				System.out.println("Unable to build the inverted index from the path:" + argMap.getPath("-index", defaultPath));
			}
		}

		if (argMap.hasFlag("-counts"))
		{
			try
			{
				index.countsToJson(argMap.getPath("-counts", defaultCountPath));
			}

			catch (IOException e)
			{
				System.out.println("Unable to display the word counts to the path: " + argMap.getPath("-counts", defaultCountPath));
			}
		}

		if (argMap.hasFlag("-results"))
		{
			try
			{
				searcher.resultsToJson(argMap.getPath("-results", defaultJsonPath));

			}

			catch (IOException e)
			{
				System.out.println("Unable to display the search results to the path: " + argMap.getPath("-results", defaultJsonPath));
			}
		}

		if (tasks != null)
		{
			tasks.shutdown();
		}

		// calculate time elapsed and output
		Duration elapsed = Duration.between(start, Instant.now());
		double seconds = (double) elapsed.toMillis() / Duration.ofSeconds(1).toMillis();
		System.out.printf("Elapsed: %f seconds%n", seconds);
	}
}
