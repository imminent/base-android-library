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
        final String alice = "Alice";
        return Arrays.asList(new Object[][]{
                { newArrayList(alice), alice },
                { newArrayList(alice, "Charlie"),  "Alice, Charlie" },
                { newArrayList(), "" },
                { null, "" },
                { newArrayList(alice, "Charlie", "John"), "Alice, Charlie, John" },
                { newArrayList(alice, null, "John"), "Alice, John" }
        });
    }

    @Test
    public void testJoin() {
        assertThat(StringUtilities.join(", ", _input)).isEqualTo(_expected);
    }

    private Collection<String> _input;
    private String _expected;
}
