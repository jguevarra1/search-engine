import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Set;

import opennlp.tools.stemmer.snowball.SnowballStemmer;

/**
 * @author josh
 *
 *	MultithreadedBuilder - The class containing the methods to create a thread-safe inverted index
 */
public class MultithreadedBuilder
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
	 * @param tasks - The Work Queue to use
	 *
	 * @throws IOException Throws and exception if the file cannot be read
	 */
	public static void findTextFiles(Path input, ThreadSafeInvertedIndex index, WorkQueue tasks) throws IOException
	{
		Set<Path> listPaths = PathTraverser.traverseDirectory(input);

		for (Path path : listPaths)
		{
			tasks.execute(new Task(path, index));
		}

		tasks.finish();
	}

	/**
	 * The task class that will update the shared paths and pending
	 * members in our task manager instance.
	 */
	private static class Task implements Runnable
	{
		/**
		 *	input - The input file path
		 */
		private final Path input;

		/**
		 *	index - The thread-safe inverted index
		 */
		private final ThreadSafeInvertedIndex index;

		/**
		 * Initializes the members for this class
		 *
		 * @param input - The input file path
		 * @param index - The thread-safe inverted index
		 */
		public Task(Path input, ThreadSafeInvertedIndex index)
		{
			this.input = input;
			this.index = index;
		}

		@Override
		public void run()
		{
			InvertedIndex local = new InvertedIndex();

			try
			{
				InvertedIndexBuilder.createIndex(input, local);
			}

			catch (IOException e)
			{
				throw new UncheckedIOException(e);
			}

			
			index.addAll(local);
			
		}
	}
}
