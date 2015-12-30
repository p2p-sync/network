package org.rmatil.sync.network.test.core.model;

import org.junit.Test;
import org.rmatil.sync.network.core.model.Data;

import static org.junit.Assert.*;

public class DataTest {

    @Test
    public void testData() {
        String content = "Fletch Skinner is a petty criminal.";

        Data data = new Data(
                content.getBytes(),
                false
        );

        Data encryptedData = new Data(
                content.getBytes(),
                true
        );

        assertArrayEquals("Content is not the same", content.getBytes(), data.getContent());
        assertFalse("Content should not be encrypted", data.isEncrypted());

        assertArrayEquals("Content is not the same", content.getBytes(), encryptedData.getContent());
        assertTrue("Content should not be encrypted", encryptedData.isEncrypted());
    }
}
