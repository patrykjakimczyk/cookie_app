package com.cookie.app.util;

import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;

class ImageUtilTest {

    @Test
    void testcompressImageWithNullData() {

        byte[] compressedImage = ImageUtil.compressImage(null);

        assertThat(compressedImage).isNotNull();
        assertThat(compressedImage).hasSize(0);
    }

    @Test
    void test_compressImageWithEmptyData() {

        byte[] compressedImage = ImageUtil.compressImage(new byte[0]);

        assertThat(compressedImage).isNotNull();
        assertThat(compressedImage).hasSize(0);
    }

    @Test
    void test_compressImageWithData() {
        byte[] inputData = new byte[100];
        Arrays.fill(inputData, (byte) 1);

        byte[] compressedImage = ImageUtil.compressImage(inputData);

        assertThat(compressedImage).isNotNull();
        assertThat(compressedImage.length).isNotEqualTo(inputData.length);
    }

    @Test
    void test_decompressImageWithNullInput() {

        byte[] decompressedImage = ImageUtil.decompressImage(null);

        assertThat(decompressedImage).isNotNull();
        assertThat(decompressedImage).hasSize(0);
    }

    @Test
    void test_decompressImageWithEmptyInput() {

        byte[] decompressedImage = ImageUtil.decompressImage(new byte[0]);

        assertThat(decompressedImage).isNotNull();
        assertThat(decompressedImage).hasSize(0);
    }

    @Test
    void test_decompressImageWithValidInput() {
        byte[] inputData = new byte[100];
        Arrays.fill(inputData, (byte) 1);

        byte[] compressedData = ImageUtil.compressImage(inputData);

        assertThat(compressedData).isNotNull();
        assertThat(compressedData.length).isNotEqualTo(inputData.length);

        byte[] decompressedImage = ImageUtil.decompressImage(compressedData);

        assertThat(decompressedImage).isNotNull();
        assertThat(decompressedImage).containsExactly(inputData);
    }
}