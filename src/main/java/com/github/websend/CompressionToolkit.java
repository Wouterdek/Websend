package com.github.websend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

public class CompressionToolkit {

    /**
     * DEFLATE: usable with DEFLATE Compressed Data Format Specification version 1.3 as used by PHP's gzdeflate
     */
    public static byte[] deflateString(String str) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(1, true);
        DeflaterOutputStream out = new DeflaterOutputStream(baos, deflater);
        byte[] data = str.getBytes("UTF-8");
        out.write(data);
        out.flush();
        out.close();
        return baos.toByteArray();
    }

    public static String inflateString(byte[] strData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Inflater inflater = new Inflater(true);
        InflaterOutputStream out = new InflaterOutputStream(baos, inflater);
        out.write(strData);
        out.flush();
        out.close();
        return baos.toString("UTF-8");
    }

    /**
     * GZIP: usable with GZIP file format specification version 4.3 as used by PHP's gzencode
     */
    public static byte[] gzipString(String str) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(baos);
        byte[] data = str.getBytes("UTF-8");
        out.write(data);
        out.flush();
        out.close();
        return baos.toByteArray();
    }

    public static String ungzipString(byte[] strData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(strData));
        while (in.available() != 0) {
            int readBytes = in.read(buffer);
            if (readBytes != 0) {
                baos.write(buffer, 0, readBytes);
            }
        }
        return baos.toString("UTF-8");
    }
}
