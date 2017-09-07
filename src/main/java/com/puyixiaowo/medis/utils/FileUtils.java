package com.puyixiaowo.medis.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import java.io.*;

/**
 * @author Moses
 * @date 2017-09-03 20:48
 */
public class FileUtils {
    public static final String PREFIX = "f_blog_stream2file";
    public static final String SUFFIX = ".tmp";

    public static String readResourceFile(String filePath) {
        LineIterator it = null;
        StringBuilder sb = new StringBuilder();
        try {
            it = org.apache.commons.io.FileUtils.lineIterator(stream2file(ResourceUtils.readFile(filePath)), "UTF-8");
            while (it.hasNext()) {
                sb.append(it.nextLine() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LineIterator.closeQuietly(it);
        }
        return sb.toString();
    }

    public static File stream2file(InputStream in) throws IOException {
        final File tempFile = File.createTempFile(PREFIX, SUFFIX);
        tempFile.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(tempFile)) {
            IOUtils.copy(in, out);
        }
        return tempFile;
    }


    public static void writeFile(String filePath, String text) {
        try {
            org.apache.commons.io.FileUtils.write(new File(filePath), text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String filePath) {
        LineIterator it = null;
        StringBuilder sb = new StringBuilder();
        try {
            it = org.apache.commons.io.FileUtils.lineIterator(new File(filePath), "UTF-8");
            while (it.hasNext()) {
                sb.append(it.nextLine() + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            LineIterator.closeQuietly(it);
        }
        return sb.toString();
    }
}
