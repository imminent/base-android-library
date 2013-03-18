package com.imminentmeals.android.base.utilities;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * <p>Test suite for the String utilities.</p>
 * @author Dandre Allison
 */
@RunWith(Parameterized.class)
public class StringUtilitiesJoinAndTest {

    public StringUtilitiesJoinAndTest(Collection<String> input, String expected) {
        _input = input;
        _expected = expected;
    }

    @SuppressWarnings("unchecked")
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { newArrayList("Alice"), "Alice" },
                { newArrayList("Alice", "Charlie"),  "Alice and Charlie" },
                { newArrayList(), "" },
                { null, "" },
                { newArrayList("Alice", "Charlie", "John"), "Alice, Charlie and John" },
                { newArrayList("Alice", null, "John"), "Alice and John" }
        });
    }

    @Test
    public void testJoinAnd() {
        assertThat(StringUtilities.joinAnd(", ", " and ", _input)).isEqualTo(_expected);
    }

    private Collection<String> _input;
    private String _expected;
}
