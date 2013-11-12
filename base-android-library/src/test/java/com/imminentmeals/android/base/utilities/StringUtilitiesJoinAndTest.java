package com.imminentmeals.android.base.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * <p>Test suite for the {@link StringUtilities#joinAnd(String, String, Collection)}.</p>
 * @author Dandre Allison
 */
@RunWith(Parameterized.class)
public class StringUtilitiesJoinAndTest {

    public StringUtilitiesJoinAndTest(Collection<String> input, String expected) {
        _input = input;
        _expected = expected;
    }

    @Parameters
    public static Collection<Object[]> data() {
        final String alice = "Alice";
        return Arrays.asList(new Object[][]{
                { newArrayList(alice), alice },
                { newArrayList(alice, "Charlie"),  "Alice and Charlie" },
                { newArrayList(), "" },
                { null, "" },
                { newArrayList(alice, "Charlie", "John"), "Alice, Charlie and John" },
                { newArrayList(alice, null, "John"), "Alice and John" }
        });
    }

    @Test
    public void testJoinAnd() {
        assertThat(StringUtilities.joinAnd(", ", " and ", _input)).isEqualTo(_expected);
    }

    private Collection<String> _input;
    private String _expected;
}
