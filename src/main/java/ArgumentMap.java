import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses and stores command-line arguments into simple flag/value pairs.
 *
 * @author CS 272 Software Development (University of San Francisco)
 * @version Fall 2021
 */
public class ArgumentMap
{
	/**
	 * Stores command-line arguments in flag/value pairs.
	 */
	private final Map<String, String> argMap;

	/**
	 * Initializes this argument map.
	 */
	public ArgumentMap()
	{
		this.argMap = new HashMap<String, String>();
	}

	/**
	 * Initializes this argument map and then parsers the arguments into
	 * flag/value pairs where possible. Some flags may not have associated values.
	 * If a flag is repeated, its value is overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public ArgumentMap(String[] args)
	{
		this();
		parse(args);
	}

	/**
	 * Parses the arguments into flag/value pairs where possible. Some flags may
	 * not have associated values. If a flag is repeated, its value will be
	 * overwritten.
	 *
	 * @param args the command line arguments to parse
	 */
	public void parse(String[] args)
	{
		for (int i = 0; i < args.length; i++)
		{
			if (i + 1 < args.length && isValue(args[i + 1]))
			{
				argMap.put(args[i], args[i + 1]);
			}
			else
			{
				argMap.put(args[i], null);
			}
		}
	}

	/**
	 * Determines whether the argument is a flag. The argument is considered a
	 * flag if it is a dash "-" character followed by any letter character.
	 *
	 * @param arg the argument to test if its a flag
	 * @return {@code true} if the argument is a flag
	 *
	 * @see String#startsWith(String)
	 * @see String#length()
	 * @see String#codePointAt(int)
	 * @see Character#isLetter(int)
	 */
	public static boolean isFlag(String arg)
	{
		if (arg == null || arg.length() == 1)
		{
			return false;
		}

		return (arg.startsWith("-") && Character.isLetter(arg.codePointAt(1)));
	}

	/**
	 * Determines whether the argument is a value. Anything that is not a flag is
	 * considered a value.
	 *
	 * @param arg the argument to test if its a value
	 * @return {@code true} if the argument is a value
	 */
	public static boolean isValue(String arg)
	{
		return !isFlag(arg);
	}

	/**
	 * Returns the number of unique flags.
	 *
	 * @return number of unique flags
	 */
	public int numFlags()
	{
		return argMap.keySet().size();
	}

	/**
	 * Determines whether the specified flag exists.
	 *
	 * @param flag the flag check
	 * @return {@code true} if the flag exists
	 */
	public boolean hasFlag(String flag)
	{
		return argMap.containsKey(flag);
	}

	/**
	 * Determines whether the specified flag is mapped to a non-null value.
	 *
	 * @param flag the flag to find
	 * @return {@code true} if the flag is mapped to a non-null value
	 */
	public boolean hasValue(String flag)
	{
		return (argMap.get(flag) != null);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or null if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped or {@code null} if
	 *   there is no mapping
	 */
	public String getString(String flag)
	{
		return argMap.get(flag);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link String}
	 * or the default value if there is no mapping.
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @param defaultValue the default value to return if there is no mapping
	 * @return the value to which the specified flag is mapped, or the default
	 *   value if there is no mapping
	 */
	public String getString(String flag, String defaultValue)
	{
		if (argMap.get(flag) == null || !(hasFlag(flag)))
		{
			return defaultValue;
		}

		return argMap.get(flag);
	}

	/**
	 * Returns the value to which the specified flag is mapped as a {@link Path},
	 * or {@code null} if unable to retrieve this mapping (including being unable
	 * to convert the value to a {@link Path} or no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value is to be returned
	 * @return the value to which the specified flag is mapped, or {@code null} if
	 *   unable to retrieve this mapping
	 *
	 * @see Path#of(String, String...)
	 */
	public Path getPath(String flag)
	{
		if (!(hasValue(flag)))
		{
			return null;
		}

		return Path.of(argMap.get(flag));
	}

	/**
	 * Returns the value the specified flag is mapped as a {@link Path}, or the
	 * default value if unable to retrieve this mapping (including being unable to
	 * convert the value to a {@link Path} or if no value exists).
	 *
	 * This method should not throw any exceptions!
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param defaultValue the default value to return if there is no valid
	 *   mapping
	 * @return the value the specified flag is mapped as a {@link Path}, or the
	 *   default value if there is no valid mapping
	 */
	public Path getPath(String flag, Path defaultValue)
	{
		if (!(hasValue(flag)))
		{
			return defaultValue;
		}

		return Path.of(argMap.get(flag));
	}

	/**
	 * Returns the value the specified flag is mapped as an Integer value, or the
	 * default value if unable to retrieve this mapping (including being unable to
	 * convert the value to an Integer or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @param defaultValue the default value to return if there is no valid
	 *   mapping
	 * @return the value the specified flag is mapped as a Integer, or the default
	 *   value if there is no valid mapping
	 */
	public Integer getInteger(String flag, Integer defaultValue)
	{
		try
		{
			return Integer.parseInt(argMap.get(flag));
		}

		catch (NumberFormatException | NullPointerException e)
		{
			return defaultValue;
		}
	}

	/**
	 * Returns the value the specified flag is mapped as an Integer value, or null
	 * default value if unable to retrieve this mapping (including being unable to
	 * convert the value to an Integer or if no value exists).
	 *
	 * @param flag the flag whose associated value will be returned
	 * @return the value the specified flag is mapped as a Integer, or null if there
	 *   is no valid mapping
	 */
	public Integer getInteger(String flag)
	{
		try
		{
			return Integer.parseInt(argMap.get(flag));
		}

		catch (NumberFormatException | NullPointerException e)
		{
			return null;
		}
	}

	/**
	 *	toString - The function that returns the contents of the ArgumentMap
	 */
	@Override
	public String toString()
	{
		return argMap.toString();
	}
}
