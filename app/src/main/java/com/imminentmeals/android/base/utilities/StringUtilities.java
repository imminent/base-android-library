package com.imminentmeals.android.base.utilities;

import java.io.*;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.util.*;
import java.util.Map.Entry;

import javax.annotation.MatchesPattern;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

import static com.google.common.collect.Maps.newHashMap;

/**
 * <p>A collection of String utilities.</p>
 * @author Dandré Allison
 */
public final class StringUtilities {

    /**
     * <p>Thrown to indicate that an error occurred while reading/writing from/to a source.<p>
     * @author Dandre Allison
     */
    @SuppressWarnings("serial")
    public static final class ReadWriteException extends RuntimeException {

        /**
         * <p>Constructs a {@link ReadWriteException}.</p>
         * @param error The error that occurred
         */
        public ReadWriteException(Throwable error) {
            super("Error while reading/writing", error);
        }
    }

    /**
     * <p>Thrown to indicate an error occurred while generating a MD5 digest.<p>
     * @author Dandre Allison
     */
    @SuppressWarnings("serial")
    public static final class Md5DigestException extends RuntimeException {

        /**
         * <p>Constructs a {@link Md5DigestException}.</p>
         * @param error The error that occurred
         */
        public Md5DigestException(Throwable error) {
            super(error);
        }
    }

    /**
     * <p>Joins a collection of objects together into a String that allows for the last object to be joined with a
     * delimiter that is different than the rest. For example, to create English sentences such
     * as "Alice, Bob and Charlie" use ", " and " and " as the delimiters.</p>
     *
     * @param delimiter Delimiter between all pairs of objects that don't include the last object
     * @param last_delimiter Delimiter between the final pair of objects
     * @param objects The collection of objects
     * @param <T> The type of objects in the collection
     * @return The string of the objects joined by the delimiters
     */
    @Nonnull public static <T> String joinAnd(@MatchesPattern(".+") final String delimiter, final String last_delimiter,
                                              final Collection<T> objects) {
        // Returns an empty String if there are no objects in the collection
        if (objects == null || objects.isEmpty())
            return "";

        final Iterator<T> list_of_objects = objects.iterator();
        final StringBuilder buffer = new StringBuilder(StringUtilities.toString(list_of_objects.next()));
        int i=1;
        while (list_of_objects.hasNext()) {
            i++;
            final T object = list_of_objects.next();
            if (notEmpty(object))
                buffer.append(i == objects.size() ? last_delimiter : delimiter).append(StringUtilities.toString(object));
        }

        return buffer.toString();
    }

    /**
     * <p>Joins a collection of objects together into a String that allows for the last object to be joined with a
     * delimiter that is different than the rest. For example, to create English sentences such
     * as "Alice, Bob and Charlie" use ", " and " and " as the delimiters.</p>
     *
     * @param delimiter Delimiter between all pairs of objects that don't include the last object
     * @param last_delimiter Delimiter between the final pair of objects
     * @param objects The collection of objects
     * @param <T> The type of objects in the collection
     * @return The string of the objects joined by the delimiters
     */
    @Nonnull public static <T> String joinAnd(@MatchesPattern(".+") final String delimiter, final String last_delimiter,
                                              final T... objects) {
        return joinAnd(delimiter, last_delimiter, Arrays.asList(objects));
    }

    /**
     * <p>Joins a collection of objects together into a String. For example, to create English sentences such
     * as "Alice, Bob, Charlie" use ", " as the delimiter.</p>
     *
     * @param delimiter Delimiter between all pairs of objects that don't include the last object
     * @param objects The collection of objects
     * @param <T> The type of objects in the collection
     * @return The string of the objects joined by the delimiters
     */
    @Nonnull public static <T> String join(@MatchesPattern(".+") final String delimiter, final Collection<T> objects) {
        // Returns an empty String if there are no objects in the collection
        if (objects == null || objects.isEmpty())
            return "";

        final Iterator<T> list_of_objects = objects.iterator();
        final StringBuilder buffer = new StringBuilder(StringUtilities.toString(list_of_objects.next()));
        while (list_of_objects.hasNext()) {
            final T object = list_of_objects.next();
            if (notEmpty(object))
                buffer.append(delimiter).append(StringUtilities.toString(object));
        }
        return buffer.toString();
    }

    /**
     * <p>Joins a collection of objects together into a String. For example, to create English sentences such
     * as "Alice, Bob, Charlie" use ", " as the delimiter.</p>
     *
     * @param delimiter Delimiter between all pairs of objects that don't include the last object
     * @param objects The collection of objects
     * @param <T> The type of objects in the collection
     * @return The string of the objects joined by the delimiters
     */
    @Nonnull public static <T> String join(@MatchesPattern(".+") final String delimiter, final T... objects ) {
        return join(delimiter, Arrays.asList(objects));
    }

    /**
     * <p>Converts an {@link InputStream} to a String.</p>
     * @param input The given InputStream
     * @return The converted String
     */
    @Nonnull public static String toString(InputStream input) {
        final StringWriter writer = new StringWriter();
        copy(new InputStreamReader(input, Charset.forName("UTF-8")), writer);
        return writer.toString();
    }

    /**
     * <p>Converts an {@link Reader} to a String.</p>
     * @param input The given Reader
     * @return The converted String
     * @throws {@link ReadWriteException} when there is an {@link IOException} reading or writing
     */
    @Nonnull public static String toString(@Nonnull Reader input) {
        final StringWriter writer = new StringWriter();
        copy(input, writer);
        return writer.toString();
    }

    /**
     * <p>Copies the input to the output.</p>
     * @param input The input
     * @param output The output
     * @return The number of characters copied, or {@code -1} if the size exceeds the size of {@code int}
     * @throws {@link ReadWriteException} when there is an {@link IOException} reading or writing
     */
    public static int copy(@Nonnull Reader input, @Nonnull Writer output) {
        final long count = copyLarge(input, output);
        return count > Integer.MAX_VALUE ? -1 : (int) count;
    }

    /**
     * <p>Copies the input to the output, much like {@link #copy(java.io.Reader, java.io.Writer)}, but with the
     * expectation that the number of characters copied will exceed the size of {@code int}. </p>
     * @param input The input
     * @param output The output
     * @return The number of characters copied
     * @throws {@link ReadWriteException} when there is an {@link IOException} reading or writing
     */
    @Nonnegative public static long copyLarge(@Nonnull Reader input, @Nonnull Writer output) {
        try {
            final char[] buffer = new char[_DEFAULT_BUFFER_SIZE];
            long count = 0;
            int n;
            while ((n = input.read(buffer)) != -1) {
                output.write(buffer, 0, n);
                count += n;
            }
            return count;
        } catch(IOException error) {
            throw new ReadWriteException(error);
        }
    }

    /**
     * <p>Performs a no-op if the given String is not {@code null}, otherwise it converts it to an empty String.</p>
     * @param string The given String
     * @return A non {@code null} String
     */
    @Nonnull public static String emptyIfNull(final String string) {
        return string == null? "" : string;
    }

    /**
     * <p>Converts the given object to a String.</p>
     * @param Object the given object
     * @return A non {@code null} String
     */
    @Nonnull public static String toString(final Object object) {
        return toString(object,"");
    }

    /**
     * <p>Converts the given object to a String or the given default String if the object is {@code null}.</p>
     * @param object The given object
     * @param default_string The default String
     * @return A non {@code null} String
     */
    @Nonnull public static String toString(final Object object, final String default_string) {
        return object == null ? (default_string == null ? "" : default_string) :
                object instanceof InputStream ? toString((InputStream) object) :
                        object instanceof Reader ? toString((Reader)object) :
                                object instanceof Object[] ? StringUtilities.join(", ", (Object[]) object) :
                                        object instanceof Collection
                                                ? StringUtilities.join(", ", (Collection<?>) object)
                                                : object.toString();
    }

    /**
     * <p>Determines if the given object produces an blank String.</p>
     * @param object The given object
     * @return {@code true} indicates that the given object produces an empty String
     */
    public static boolean isEmpty(final Object object) {
        return toString(object).trim().length() == 0;
    }

    /**
     * <p>Determines if the given object does not produce an blank String.</p>
     * @param object the given object
     * @return {@code true} indicates that the given object doesn't produce an empty String
     */
    public static boolean notEmpty(final Object object) {
        return toString(object).trim().length() != 0;
    }

    /**
     * <p>Digests the given String a produces a encrypted String.</p>
     * @param message The given message
     * @return The encrypted String
     * @throws {@link Md5DigestException} when there is an {@link Exception}
     */
    @Nonnull public static String md5(@Nonnull String message) {
        // http://stackoverflow.com/questions/1057041/difference-between-java-and-php5-md5-hash
        // http://code.google.com/p/roboguice/issues/detail?id=89
        try {

            final byte[] hash = MessageDigest.getInstance("MD5").digest(message.getBytes("UTF-8"));
            final StringBuilder digest = new StringBuilder();

            for (byte byte_character : hash) {
                String hex = Integer.toHexString(byte_character);

                if (hex.length() == 1) {
                    digest.append('0');
                    digest.append(hex.charAt(hex.length() - 1));
                } else
                    digest.append(hex.substring(hex.length() - 2));
            }

            return digest.toString();

        } catch (Exception error) {
            throw new Md5DigestException(error);
        }
    }

    /**
     * <p>Capitalizes the given String by capitalizing the first letter of the given String.</p>
     * @param string The given String
     * @return A capitalized version of the given String
     */
    @Nonnull public static String capitalize(String string) {
        final String capitalized_string = StringUtilities.toString(string);
        return capitalized_string.length() >= 2
                ? capitalized_string.substring(0, 1).toUpperCase() + capitalized_string.substring(1)
                : capitalized_string.length() >= 1 ? capitalized_string.toUpperCase() : capitalized_string;
    }

    /**
     * <p>Determines if the two given objects produce the same String.</p>
     * @param a A given object
     * @param b Another given object
     * @return {@code true} indicates that the two objects produce identical Strings
     */
    public static boolean textuallyEquivalent(Object a, Object b) {
        return StringUtilities.toString(a).equals(StringUtilities.toString(b));
    }

    /**
     * <p>Determines if the two given objects produce the same String while ignoring differences in their
     * capitalization.</p>
     * @param a A given object
     * @param b Another given object
     * @return {@code true} indicates that the two objects produce equivalent Strings
     */
    public static boolean textuallyEquivalentIgnoringCase(Object a, Object b) {
        return StringUtilities.toString(a).equalsIgnoreCase(StringUtilities.toString(b));
    }

    /**
     * <p>Partitions the given string into chunks of the requested size.</p>
     * @param string The given String
     * @param chunk_size The requested chunk size
     * @return A list of the given String partitioned by the given chunk size
     */
    @Nonnull public static String[] chunk(String string, @Nonnegative int chunk_size) {
        if (isEmpty(string) || chunk_size == 0)
            return new String[0];

        final int length = string.length();
        // Computes the number of chunks in the String based on the chunk size
        final int number_of_chunks = ((length - 1)/chunk_size) + 1;
        final String[] chunks = new String[number_of_chunks];
        for (int i = 0; i < number_of_chunks; ++i)
            chunks[i] = string.substring(i * chunk_size,
                    (i * chunk_size) + chunk_size < length ? (i * chunk_size) + chunk_size : length);

        return chunks;
    }

    /**
     * <p>Searches the given String for the keys in the set of substitutions and replaces them with the given
     * value for the key. The keys must be marked with a {@code '$'}. This is an alternative to
     * {@link String#format(String, Object...)} that allows the format parameters to be named.</p>
     * @param string The given String
     * @param substitutions The set of substitutions
     * @return The String after the substitutions have been made
     */
    @Nonnull public static String namedFormat(@Nonnull String string, @Nonnull Map<String, String> substitutions) {
        for (Entry<String, String> entry : substitutions.entrySet())
            string = string.replace('$' + entry.getKey(), entry.getValue());

        return string;
    }

    /**
     * <p>Searches the given String for the keys in the set of substitutions and replaces them with the given
     * value for the key. The keys must be marked with a {@code '$'}. This is an alternative to
     * {@link String#format(String, Object...)} that allows the format parameters to be named. This method
     * expects a key object parameter followed by the value for that key, such as
     * {@code namedFormat(string, "key1", "value1", "key2", "value2");}</p>
     * @param string The given String
     * @param key_value_pairs The set of substitutions
     * @return The String after the substitutions have been made
     * @throws InvalidParameterException when their is a mismatch between the key and value pairs
     */
    @Nonnull public static String namedFormat(@Nonnull String string, Object... key_value_pairs) {
        if (key_value_pairs.length % 2 != 0)
            throw new InvalidParameterException("You must include one value for each parameter");

        final HashMap<String,String> map = newHashMap();
        for (int i = 0; i < key_value_pairs.length; i += 2)
            map.put(StringUtilities.toString(key_value_pairs[i]), StringUtilities.toString(key_value_pairs[i + 1]));

        return namedFormat(string,map);
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link StringUtilities} class. */
    private StringUtilities() { }

    /** The default size for a buffer */
    private static final int _DEFAULT_BUFFER_SIZE = 1024 * 4;
}
