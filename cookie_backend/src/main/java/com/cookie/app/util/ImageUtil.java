package com.cookie.app.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
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

        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
        compressor.setInput(data);
        compressor.finish();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4 * 1024];
        int readCount = 0;

        while (!compressor.finished()) {
            readCount = compressor.deflate(tmp);
            if (readCount > 0) {
                outputStream.write(tmp, 0, readCount);
            }
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            log.info("Compressing image failed due to IOException. Returning empty byte array");
            return new byte[0];
        }

        return outputStream.toByteArray();
    }

    public static byte[] decompressImage(byte[] data) {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        Inflater decompressor = new Inflater();
        decompressor.setInput(data);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] tmp = new byte[4*1024];
        int readCount = 0;

        try {
            while (!decompressor.finished()) {
                readCount = decompressor.inflate(tmp);
                outputStream.write(tmp, 0, readCount);
            }
            outputStream.close();
        } catch (IOException e) {
            log.info("Decompressing image failed due to IOException. Returning empty byte array", e);
            return new byte[0];
        } catch (DataFormatException e) {
            log.info("Decompressing recipe image failed due to DataFormatException. Returning empty byte array", e);
            return new byte[0];
        }

        return outputStream.toByteArray();
    }
}
