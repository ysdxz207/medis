package com.puyixiaowo.medis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * @author Moses
 * @date 2017-08-04 12:59
 */
public class ResourceUtils {
    private static Properties properties;

    public static InputStream readFile(String path) {
        InputStream inputStream = ResourceUtils.class
                .getClassLoader().getResourceAsStream(path);

        if (inputStream == null) {
            throw new RuntimeException("Can not find file " + path);
        }


        return inputStream;
    }

    public static Properties load(String path) {

        properties = new Properties();
        try {
            properties.load(readFile(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return properties;
    }

    public static String get(String key) {
        if (properties == null) {
            throw new RuntimeException("Properties not load yet.");
        }

        return (String)properties.get(key);
    }

    public static String getResourcePath() {
        return ResourceUtils.class.getResource("/").getPath();
    }

    public static URL getResource(String filepath) {
        return ResourceUtils.class.getClass().getResource(filepath);
    }
}
