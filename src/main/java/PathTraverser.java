import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 *  @author Josh Guevarra
 *
 *	Utility class for traversing over path directories.
 *
 */
public class PathTraverser
{
	/**
	 * isTextFile - Checks if a file ends with ".text" or ".txt"
	 *
	 * @param file - The file to check
	 * @return Returns true/false if a file ends with ".text" or ".txt"
	 */
	public static boolean isTextFile(Path file)
	{
		String fileLower = file.toString().toLowerCase();

		return (fileLower.endsWith(".txt") || fileLower.endsWith(".text"));
	}

	/**
	 * traverseDirectory - Traverses over a directory path
	 * 					   If the file isn't a directory, it is simply added into a set, regardless
	 * 					   of the file extension
	 *
	 * @param start	- The starting path to check
	 * @return Returns a set containing all the file paths found within a directory (if it is a directory)
	 *
	 * @throws IOException Thrown if the path is invalid
	 */
	public static Set<Path> traverseDirectory(Path start) throws IOException
	{
		Set<Path> paths = new HashSet<Path>();

		if (Files.isDirectory(start))
		{
			traverseDirectory(start, paths);
		}

		else
		{
			paths.add(start);
		}

		return paths;
	}

	/**
	 * traverseDirectory - Traverses over all sub-directory paths recursively, storing all the file paths into a set
	 * 					   If the file isn't a directory, but is in a sub-directory it is added into the set
	 * 					   if the file is considered to be a text file
	 *
	 * @param directory	- The starting path to check
	 * @param paths - The set containing all file paths found in a directory
	 * @throws IOException Thrown if the path is invalid
	 */
	private static void traverseDirectory(Path directory, Set<Path> paths) throws IOException
	{
		try (DirectoryStream<Path> listings = Files.newDirectoryStream(directory))
		{
			for (Path path : listings)
			{
				if (Files.isDirectory(path))
				{
					traverseDirectory(path, paths);
				}

				else if (isTextFile(path))
				{
					paths.add(path);
				}
			}
		}
	}
}
