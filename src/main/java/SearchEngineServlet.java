import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * @author Josh Guevarra
 *
 *	Class containing the server servlet to handle interactions between our web page and the back-end.
 */
public class SearchEngineServlet extends HttpServlet
{
	/**
	 * (unused)
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Title - The title to use for the webpage
	 */
	private static final String TITLE = "Search Engine";
	
	/**
	 * htmlTemplate - The HTML template to use
	 */
	private final String headTemplate;
	private final String footTemplate;
	private final String textTemplate;

	/**
	 * searcher - The searcher class to interact with
	 */
	private QuerySearchInterface searcher;
	
	/**
	 * searchQuery - The search query
	 */
	private String searchQuery;
	
	/**
	 * links - Contains the most relevant links of a search query
	 */
	private ConcurrentLinkedQueue<String> links; 
	
	/**
	 * BASE - Base path to our folder containing the HTML templates
	 */
	private static final Path BASE = Path.of("src", "main", "resources", "html");
	
	/**
	 * exactSearch - Flag to check if exact search is enabled
	 */
	private boolean exactSearch;
	
	/**
	 * MAX_LINKS - The maximum number of links to display on the web page
	 */
	private static int MAX_LINKS = 10; 
	
	/**
	 * Constructor to initialize the necessary members for this class
	 * @param searcher - The searcher class object to use to perform exact/partial searches
	 * @throws IOException - Throws exception of the file cannot be read
	 */
	public SearchEngineServlet(QuerySearchInterface searcher, boolean exactSearch) throws IOException
	{
		super();
		this.searcher = searcher;
		this.exactSearch = exactSearch;
		
		headTemplate = Files.readString(BASE.resolve("bulma-head.html"));
		footTemplate = Files.readString(BASE.resolve("bulma-foot.html"));
		textTemplate = Files.readString(BASE.resolve("bulma-text.html"));
		
		links = new ConcurrentLinkedQueue<String>();
		
	}
	
	/**
	 * doGet - Performs GET operations for our web page. This method is responsible for displaying the hero bar,
	 * 		   the current time stamp, and the search query form.
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");
		
		Map<String, String> values = new HashMap<>();
		
		/** Display all the page components */
		values.put("title", TITLE);
		values.put("thread", Thread.currentThread().getName());
		values.put("updated", getDate());
		values.put("query", searchQuery);	
		values.put("method", "POST");
		values.put("action",  request.getServletPath());
				
		StringSubstitutor replacer = new StringSubstitutor(values);
		String head = replacer.replace(headTemplate);
		String text = replacer.replace(textTemplate);
		
		PrintWriter out = response.getWriter();
	
		out.println(head);
		out.println(text);
		
		/** Display all the relevant links in our queue */
		for (String link : links)
		{
			out.println(link);
		}
		
		out.flush();
		
		response.setStatus(HttpServletResponse.SC_OK);		
	}
	
	/**
	 * doPost - The method responsible for taking input from the search query form. Uses the search query to perform
	 * 			search operations on the inverted index. Displays the most relevant results. 
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html");
		
		/** Take in the search query input, clean it, and feed it into the search function */
		searchQuery = request.getParameter("search");	
		searchQuery = StringEscapeUtils.escapeHtml4(searchQuery);	
		searcher.search(searchQuery, exactSearch);
		
		/** Clear the links data structure so that old links are appended */
		links.clear();
		
		/** In the search function, search query is broken apart and joined which results in a different key for the map 
		 *  containing <query, SearchResult> pairings. We need to do this to have the matching key for when we call getResults
		 */
		String searchKey = String.join(" ", TextStemmer.uniqueStems(searchQuery));

		Map<String, String> values = new HashMap<>();		
		List<InvertedIndex.SearchResult> results = searcher.getResults(searchKey);

		int counter = 0; 
		
		/** Loop through our list of SearchResult objects. We want to break if we're at the max number of links to display.
		 *  If the amount of SearchResults objects is less than our max number of links we will just display them all.
		 */
		while (counter != results.size())
		{
			if (counter == MAX_LINKS)
			{
				break;
			}

			/** Display it twice to enter the link part in the a href tag, and for the text part (see bulma-text.html) */
			values.put("result1", results.get(counter).getLocation());
			values.put("result2", results.get(counter).getLocation());
			
			StringSubstitutor replacer = new StringSubstitutor(values);
			String formatted = replacer.replace(footTemplate);
			
			/** Add the HTML formatted string to our queue to display */
			links.add(formatted);
			counter++;
		}
		
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.sendRedirect(request.getServletPath());
	}
	
	public static String getDate()
	{
		String format = "hh:mm a 'on' EEEE, MMMM, dd, yyyy";
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(new Date());
	}
}
