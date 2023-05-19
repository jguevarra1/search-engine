import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author josh
 *
 * ThreadSafeInvertedIndex - A thread-safe version of inverted index using a read/write lock.
 *
 */
public class ThreadSafeInvertedIndex extends InvertedIndex
{
	/** The lock used to protect concurrent access to the underlying set. */
	private final SimpleReadWriteLock lock;

	/**
	 *	Constructor to initialize the necessary data structures for the thread-safe inverted index
	 *	as well as the lock to use
	 */
	public ThreadSafeInvertedIndex()
	{
		super();
		lock = new SimpleReadWriteLock();
	}

	@Override
	public ArrayList<InvertedIndex.SearchResult> exactSearch(Set<String> lineQuery)
	{
		lock.readLock().lock();

		try
		{
			return super.exactSearch(lineQuery);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public ArrayList<InvertedIndex.SearchResult> partialSearch(Set<String> lineQuery)
	{
		lock.readLock().lock();

		try
		{
			return super.partialSearch(lineQuery);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}




	@Override
	public void add(String stem, String location, int position)
	{
		lock.writeLock().lock();

		try
		{
			super.add(stem, location, position);
		}

		finally
		{
			lock.writeLock().unlock();
		}
	}

	@Override
	public void addAll(InvertedIndex index)
	{
		lock.writeLock().lock();

		try
		{
			super.addAll(index);
		}

		finally
		{
			lock.writeLock().unlock();
		}
	}

	@Override
	public Set<String> getWords()
	{
		lock.readLock().lock();

		try
		{
			return super.getWords();
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<String> getLocations(String key)
	{
		lock.readLock().lock();

		try
		{
			return super.getLocations(key);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public Set<Integer> getPositions(String key, String location)
	{
		lock.readLock().lock();

		try
		{
			return super.getPositions(key, location);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsWord(String key)
	{
		lock.readLock().lock();

		try
		{
			return super.containsWord(key);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsLocation(String key, String location)
	{
		lock.readLock().lock();

		try
		{
			return super.containsLocation(key, location);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsPosition(String key, String location, int position)
	{
		lock.readLock().lock();

		try
		{
			return super.containsPosition(key, location, position);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public int sizeWords()
	{
		lock.readLock().lock();

		try
		{
			return super.sizeWords();
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public int sizeLocations(String key)
	{
		lock.readLock().lock();

		try
		{
			return super.sizeLocations(key);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public int sizePositions(String key, String location)
	{
		lock.readLock().lock();

		try
		{
			return super.sizePositions(key, location);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public void indexToJson(Path output) throws IOException
	{
		lock.readLock().lock();

		try
		{
			super.indexToJson(output);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public void countsToJson(Path output) throws IOException
	{
		lock.readLock().lock();

		try
		{
			super.countsToJson(output);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public boolean containsLocation(String location)
	{
		lock.readLock().lock();

		try
		{
			return super.containsLocation(location);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}

	@Override
	public int getCount(String location)
	{
		lock.readLock().lock();

		try
		{
			return super.getCount(location);
		}

		finally
		{
			lock.readLock().unlock();
		}
	}
	
	@Override
	public String toString()
	{
		lock.readLock().lock();
		
		try 
		{
			return super.toString();
		}
		
		finally
		{
			lock.readLock().unlock();
		}
	}
}
