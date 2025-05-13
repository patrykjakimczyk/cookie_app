package com.cookie.app.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Slf4j
public class ImageUtil {

    private ImageUtil() {}

    public static byte[] compressImage(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
            compressor.setInput(data);
            compressor.finish();

            byte[] tmp = new byte[4 * 1024];
            int readCount;
            while (!compressor.finished()) {
                readCount = compressor.deflate(tmp);
                if (readCount > 0) {
                    outputStream.write(tmp, 0, readCount);
                }
            }
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Compressing image failed due to IOException", e);
            return new byte[0];
        }
    }

    public static byte[] decompressImage(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length)) {
            Inflater decompressor = new Inflater();
            decompressor.setInput(data);

            byte[] tmp = new byte[4 * 1024];
            int readCount;
            while (!decompressor.finished()) {
                readCount = decompressor.inflate(tmp);
                outputStream.write(tmp, 0, readCount);
            }
            return outputStream.toByteArray();

        } catch (IOException | DataFormatException e) {
            log.error("Decompressing image failed", e);
            return new byte[0];
        }
    }
}
