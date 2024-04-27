package com.cookie.app.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ImageUtilTest {

    @Test
    void testcompressImageWithNullData() {

        byte[] compressedImage = ImageUtil.compressImage(null);

        assertNotNull(compressedImage);
        assertEquals(0, compressedImage.length);
    }

    @Test
    void test_compressImageWithEmptyData() {

        byte[] compressedImage = ImageUtil.compressImage(new byte[0]);

        assertNotNull(compressedImage);
        assertEquals(0, compressedImage.length);
    }

    @Test
    void test_compressImageWithData() {
        byte[] inputData = new byte[100];
        Arrays.fill(inputData, (byte) 1);

        byte[] compressedImage = ImageUtil.compressImage(inputData);

        assertNotNull(compressedImage);
        assertNotEquals(inputData.length, compressedImage.length);
    }

    @Test
    void test_decompressImageWithNullInput() {

        byte[] decompressedImage = ImageUtil.decompressImage(null);

        assertNotNull(decompressedImage);
        assertEquals(0, decompressedImage.length);
    }

    @Test
    void test_decompressImageWithEmptyInput() {

        byte[] decompressedImage = ImageUtil.decompressImage(new byte[0]);

        assertNotNull(decompressedImage);
        assertEquals(0, decompressedImage.length);
    }

    @Test
    void test_decompressImageWithValidInput() {
        byte[] inputData = new byte[100];
        Arrays.fill(inputData, (byte) 1);

        byte[] compressedData = ImageUtil.compressImage(inputData);

        assertNotNull(compressedData);
        assertNotEquals(inputData.length, compressedData.length);

        byte[] decompressedImage = ImageUtil.decompressImage(compressedData);

        assertNotNull(decompressedImage);
        assertArrayEquals(inputData, decompressedImage);
    }
}
