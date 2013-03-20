package com.imminentmeals.android.base.utilities;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import javax.crypto.SecretKey;
import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

/**
 * <p>Test suite for {@link CryptographyUtilities}.</p>
 * @author Dandre Allison
 */
@RunWith(Parameterized.class)
public class CryptographyUtilitiesTest {
    @Inject CryptographyUtilities crypto;

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
        ObjectGraph.create(new TestModule()).inject(this);
    }

    @Test
    public void testEncryptAndDecipher() {
        try {
            assertThat(crypto.decipher(crypto.encrypt(_input))).isEqualTo(_input);
            if (_input == null) failBecauseExceptionWasNotThrown(NullPointerException.class);
        } catch (Exception exception) {
            assertThat(exception).isInstanceOf(NullPointerException.class);
        }
    }

    @Module(
            entryPoints = CryptographyUtilitiesTest.class
    )
    /* package */static class TestModule {
        @Provides SecretKey provideSecretKey() {
            try {
                return CryptographyUtilities.generateKey();
            } catch (NoSuchAlgorithmException error) {
                throw new RuntimeException(error);
            }
        }
    }

    private String _input;
}
