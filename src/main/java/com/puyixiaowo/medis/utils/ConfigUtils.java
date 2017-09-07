package com.puyixiaowo.medis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Moses
 * @date 2017-09-07 18:05
 * 
 */
public class ConfigUtils {
    public static final String BASE_PATH = System.getProperty("user.dir");
    public static final String REDIS_CONF = BASE_PATH + "/conf/redis.json";
    public static final String TAGS = BASE_PATH + "/conf/tags.json";


    private static File fileTags;
    private static File fileRedis;


    public static void init() {
        fileTags = new File(TAGS);
        fileRedis = new File(REDIS_CONF);

        try {
            if (!fileTags.exists()) {
                fileTags.getParentFile().mkdirs();
                fileTags.createNewFile();
            }
            if (!fileRedis.exists()) {
                fileRedis.getParentFile().mkdirs();
                fileRedis.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray readTags(){
        JSONArray arr = new JSONArray();
        String str = FileUtils.readFile(TAGS);
        if (StringUtils.isBlank(str)) {
            return arr;
        }
        return JSON.parseArray(str);
    }

    public static JSONArray readRedisConf(){
        JSONArray json = JSON.parseArray(FileUtils.readFile(REDIS_CONF));
        return json == null ? new JSONArray() : json;
    }

    public static void saveRedisConf(String str) {
        FileUtils.writeFile(REDIS_CONF, str);
    }

    public static void saveTags(String str) {
        FileUtils.writeFile(TAGS, str);
    }
}
