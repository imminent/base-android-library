package com.imminentmeals.android.base.utilities;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;

import org.fest.assertions.api.StringAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * <p>Test suite for some related {@link StringUtilities} methods.</p>
 * @author Dandre Allison
 */
@RunWith(Parameterized.class)
public class StringUtilitiesEmptinessTest {

    public StringUtilitiesEmptinessTest(String input, boolean expected) {
            _input = input;
            _expected = expected;
        }

        @Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    { "Alice", true },
                    { "",  false },
                    { "   ", false },
                    { null, false }
            });
        }

        @Test
        public void testNotEmpty() {
            assertThat(StringUtilities.notEmpty(_input)).isEqualTo(_expected);
        }

        @Test
        public void testIsEmpty() {
            assertThat(StringUtilities.isEmpty(_input)).isEqualTo(!_expected);
        }

        @Test
        public void testEmptyIfNull() {
            final StringAssert assert_that = assertThat(StringUtilities.emptyIfNull(_input));
            if (_input == null)
                assert_that.isEmpty();
            else
                assert_that.isEqualTo(_input);
        }

        private String _input;
        private boolean _expected;
}

