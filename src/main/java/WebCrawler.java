import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author josh
 *
 * Class containing the methods to crawl URLs
 */
public class WebCrawler
{
	/**
	 * The thread safe inverted index to use
	 */
	private final ThreadSafeInvertedIndex index;

	/**
	 * The work queue to use
	 */
	private final WorkQueue tasks;

	/**
	 * Max number of URL links to crawl
	 */
	private final int MAX;

	/**
	 * Data structure to track links that have been processed
	 */
	private Set<URL> crawledLinks;

	/**
	 * Initializes the necessary data structures for this class
	 *
	 * @param index The thread safe inverted index to use
	 * @param tasks The work queue to use
	 * @param MAX Max number of URL links to crawl
	 */
	public WebCrawler(ThreadSafeInvertedIndex index, WorkQueue tasks, int MAX)
	{
		this.index = index;
		this.tasks = tasks;
		this.MAX = MAX;
		crawledLinks = new HashSet<URL>();
	}

	/**
	 * crawlUrl - Crawls the seed URL and processes other links using a work queue
	 *
	 * @param url - The seed url
	 */
	public void crawlUrl(URL url)
	{
		crawledLinks.add(url);
		tasks.execute(new Task(index, url, tasks, crawledLinks, MAX));
		tasks.finish();
	}

	/**
	 * @author josh
	 * Task class to handle several URL crawling
	 */
	private static class Task implements Runnable
	{
		/**
		 * The thread safe inverted index to use
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * The url to crawl
		 */
		private final URL url;

		/**
		 * The work queue to use
		 */
		private final WorkQueue tasks;

		/**
		 * Max number of URL links to crawl
		 */
		private final int MAX;

		/**
		 * Data structure to track links that have been processed
		 */
		private Set<URL> crawledLinks;

		/**
		 * Initializes the members for this class
		 *
		 * @param index - The thread safe inverted index to use
		 * @param url - The url to crawl
		 * @param tasks - The work queue to use
		 * @param crawledLinks - Data structure to track links that have been processed
		 * @param MAX - Max number of URL links to crawl
		 */
		public Task(ThreadSafeInvertedIndex index, URL url, WorkQueue tasks, Set<URL> crawledLinks, int MAX)
		{
			this.index = index;
			this.url = url;
			this.tasks = tasks;
			this.crawledLinks = crawledLinks;
			this.MAX = MAX;
		}

		@Override
		public void run()
		{
			int position = 1;
			ArrayList<URL> links = null;
			InvertedIndex local = new InvertedIndex();
			String location = url.toString();

			String html = HtmlFetcher.fetch(url, 3);

			if (html != null)
			{
				html = HtmlCleaner.stripBlockElements(html);
				links = LinkParser.getValidLinks(url, html);

				synchronized(crawledLinks)
				{
					for (URL url : links)
					{
						if (crawledLinks.size() >= MAX)
						{
							break;
						}

						if (!crawledLinks.contains(url))
						{
							crawledLinks.add(url);
							tasks.execute(new Task(index, url, tasks, crawledLinks, MAX));
						}
					}
				}
				
				for (String stem : TextStemmer.listStems(HtmlCleaner.stripHtml(html)))
				{
					local.add(stem, location, position);
					position++;
				}
			}
			
			else
			{
				return; 
			}
			
			synchronized(index)
			{
				index.addAll(local);
			}
		}
	}

}
