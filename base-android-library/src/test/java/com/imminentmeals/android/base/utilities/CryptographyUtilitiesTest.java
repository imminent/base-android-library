package com.imminentmeals.android.base.utilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;


/**
 * <p>Test suite for {@link CryptographyUtilities}.</p>
 * @author Dandre Allison
 */
@RunWith(Parameterized.class)

public class CryptographyUtilitiesTest {

    public CryptographyUtilitiesTest(String input) {
        _input = input;
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { "This message  is good." },
                { "~!@#$%^&*()_+,./';l][\\]:'>}`" },
                { "\they\nyou\r\nover there   . I see youé" },
                { "" },
                { null },
                { "How about trying this one on for size? It's a medium length message, so it should be good; yeah?????" }
        });
    }

    @Before
    public void initialize() {
        try {
            _crypto = new CryptographyUtilities(CryptographyUtilities.generateKey());
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalArgumentException(error);
        }
    }

    @Test
    public void testEncryptAndDecipher() {
        try {
            assertThat(_crypto.decipher(_crypto.encrypt(_input))).isEqualTo(_input);
            if (_input == null) failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (Exception exception) {
            assertThat(exception).isInstanceOf(NullPointerException.class);
        }
    }

    private String _input;
    private CryptographyUtilities _crypto;
}
