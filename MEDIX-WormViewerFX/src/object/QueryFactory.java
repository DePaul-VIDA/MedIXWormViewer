/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package object;

/**
 *
 * @author mingfeishao
 */

public final class QueryFactory {
    private static final char[] ESCAPED_CHARACTERS = new char[] {0, '\n', '\r', '\\', '\'', '"', 0x1a};
    private static final String SUBSTITUTION_VALUE = "?";

    private final String query;
    private final String[] queryValues;
    private final char quote;

    /**
     * Constructs a new query factory with the specified query, values and quote.
     *
     * <p>
     *     This constructor is only for internal use when chaining calls.
     * </p>
     *
     * @param query the query for the factory
     * @param queryValues the query values for the factory
     * @param quote the character to wrap escaped strings in when building the query
     */
    private QueryFactory(final String query, final String[] queryValues, final char quote) {
        this.query = query;
        this.queryValues = queryValues;
        this.quote = quote;
    }

    /**
     * Constructs a new query factory with the specified query and quote.
     *
     * @param query the query for the factory
     * @param quote the character to wrap escaped strings in when building the query
     */
    public QueryFactory(final String query, final char quote) {
        this(query, new String[0], quote);
    }

    /**
     * Constructs a new query factory with the specified query.
     *
     * <p>
     *     The quote is set to a single quote by this constructor, which is the same as calling
     *     {@link #QueryFactory(String, char) QueryFactory('\'')}.
     * </p>
     *
     * @param query the query for the factory
     */
    public QueryFactory(final String query) {
        this(query, '\'');
    }

    /**
     * Returns a copy of the {@link #queryValues} array with the specified length.
     *
     * <p>
     *     If {@code newLength} is greater than the length of {@link #queryValues} then the remaining components are
     *     {@code null}.
     * </p>
     *
     * @param newLength the new length for the copied array
     * @return a copy of the {@link #queryValues} array
     */
    private String[] copyQueryValues(final int newLength) {
        // Ensure we don't try and copy more elements than we have in queryValues
        final int copyLength = Math.min(newLength, queryValues.length);
        final String[] queryValuesCopy = new String[newLength];

        System.arraycopy(queryValues, 0, queryValuesCopy, 0, copyLength);

        return queryValuesCopy;
    }

    /**
     * Escapes the specified value.
     *
     * <p>
     *     This method sanitizes the specified value so that it can be safely used in a <strong>quoted</strong> SQL
     *     string.
     * </p>
     *
     * <p>
     *     The {@link #ESCAPED_CHARACTERS} list is based off the list used by the MySQL
     *     <a href="http://dev.mysql.com/doc/refman/5.7/en/mysql-real-escape-string.html">mysql_real_escape_string</a>
     *     function.
     * </p>
     *
     * @param value the value to escape
     * @return an escaped copy of the specified value
     */
    private static String escape(final String value) {
        final StringBuilder escaped = new StringBuilder();
        for (final char character : value.toCharArray()) {
            for (final char escapeCharacter : ESCAPED_CHARACTERS) {
                if (character == escapeCharacter) {
                    escaped.append('\\');
                    break;
                }
            }

            escaped.append(character);
        }

        return escaped.toString();
    }

    /**
     * Sets the specified parameter index to the specified value.
     *
     * @param index the index of the parameter to set
     * @param value the value for the parameter
     * @param escapeValue whether or not to escape the specified value
     * @return a new {@link QueryFactory} instance with the specified parameter set
     */
    public QueryFactory set(final int index, final String value, final boolean escapeValue) {
        // Work out how long our new array needs to be if we're inserting past the end of queryValues
        final int newQueryValuesLength = Math.max(queryValues.length, index + 1);
        final String[] newQueryValues = copyQueryValues(newQueryValuesLength);

        if (escapeValue) {
            newQueryValues[index] = quote + escape(value) + quote;
        } else {
            newQueryValues[index] = value;
        }

        return new QueryFactory(query, newQueryValues, quote);
    }

    /**
     * Sets the specified parameter index to the specified value.
     *
     * <p>
     *     This method escapes the specified value and is the same as calling
     *     {@link #set(int, String, boolean) set(index, value, true)}.
     * </p>
     *
     * @param index the index of the parameter to set
     * @param value the value for the parameter
     * @return a new {@link QueryFactory} instance with the specified parameter set
     */
    public QueryFactory set(final int index, final String value) {
        return set(index, value, true);
    }

    /**
     * Sets the next available parameter to the specified value.
     * @param value the value for the parameter
     * @param escapeValue whether or not to escape the specified value
     * @return a new {@link QueryFactory} instance with the specified parameter set
     */
    public QueryFactory set(final String value, final boolean escapeValue) {
        return set(queryValues.length, value, escapeValue);
    }

    /**
     * Sets the next available parameter to the specified value.
     *
     * <p>
     *     This method escapes the specified value and is the same as calling
     *     {@link #set(String, boolean) set(value, true)}.
     * </p>
     *
     * @param value the value for the parameter
     * @return a new {@link QueryFactory} instance with the specified parameter set
     */
    public QueryFactory set(final String value) {
        return set(value, true);
    }

    /**
     * Converts the factory to a query string.
     * @return the built query
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(query);
        int nextIndex = -1;

        // Iterate all of our query values in order and find the next substitution value in the query string
        for (final String queryValue : queryValues) {
            nextIndex = builder.indexOf(SUBSTITUTION_VALUE, nextIndex + 1);

            // If there is another substitution value then we delete it and insert our query value instead
            if (nextIndex != -1) {
                builder.deleteCharAt(nextIndex);
                builder.insert(nextIndex, queryValue);
            } else {
                break;
            }
        }

        return builder.toString();
    }
}
