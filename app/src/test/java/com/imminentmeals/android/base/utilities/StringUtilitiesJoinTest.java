package com.imminentmeals.android.base.utilities;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * <p>Test suite for the {@link StringUtilities#join(String, Collection)}.</p>
 * @author Dandre Allison
 */
@RunWith(Parameterized.class)
public class StringUtilitiesJoinTest {

    public StringUtilitiesJoinTest(Collection<String> input, String expected) {
        _input = input;
        _expected = expected;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { newArrayList("Alice"), "Alice" },
                { newArrayList("Alice", "Charlie"),  "Alice, Charlie" },
                { newArrayList(), "" },
                { null, "" },
                { newArrayList("Alice", "Charlie", "John"), "Alice, Charlie, John" },
                { newArrayList("Alice", null, "John"), "Alice, John" }
        });
    }

    @Test
    public void testJoin() {
        assertThat(StringUtilities.join(", ", _input)).isEqualTo(_expected);
    }

    private Collection<String> _input;
    private String _expected;
}
