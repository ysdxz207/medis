package com.puyixiaowo.medis.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puyixiaowo.medis.exception.JedisConfigException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Moses
 * @date 2017-08-03 9:29
 */
public class RedisUtils {
    private static JedisPool jedisPool;

    private static int maxActive;
    private static int maxIdle;
    private static int maxWait;

    private static final int TIME_OUT = 3000;

    static {
        //读取相关的配置
        ResourceBundle resourceBundle = ResourceBundle.getBundle("redis");
        maxActive = Integer.parseInt(resourceBundle.getString("redis.pool.maxActive"));
        maxIdle = Integer.parseInt(resourceBundle.getString("redis.pool.maxIdle"));
        maxWait = Integer.parseInt(resourceBundle.getString("redis.pool.maxWait"));
    }

    public static boolean isConnected(){
        return testConnection();
    }

    public static void init(String host,
                            int port,
                            String pass) {

        // 建立连接池配置参数
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(maxActive);
        // 设置最大阻塞时间
        config.setMaxWaitMillis(maxWait);
        // 设置空间连接
        config.setMaxIdle(maxIdle);
        if (StringUtils.isNotBlank(pass)) {
            jedisPool = new JedisPool(config, host, port, TIME_OUT, pass);
        } else {
            jedisPool = new JedisPool(config, host, port);
        }
        testConnection();
    }

    public static boolean testConnection() {
        try {
            getJedis(0).set("TEST_CONNECTION", "connected");
            getJedis(0).del("TEST_CONNECTION");
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private static Jedis getJedis(int dbIndex) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(dbIndex);
        } catch (Exception e) {
            if (e.getCause() instanceof JedisDataException) {

                throw new JedisConfigException(
                        "Jedis 连接配置错误，请检查redis.properties文件。异常信息："
                                + e.getCause().getMessage());
            }
            if (e.getCause() instanceof JedisConnectionException) {
                throw new JedisConnectionException("Redis可能未启动。异常信息："
                        + e.getCause().getMessage());
            }
        } finally {
            if (jedis != null)
                jedis.close();
        }
        return jedis;
    }

    public static String get(int dbIndex, String key) {
        return getJedis(dbIndex).get(key);
    }

    public static <T> T get(int dbIndex, String key, Class<T> clazz) {
        String str = get(dbIndex, key);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return JSONObject.parseObject(str, clazz);
    }

    public static void set(int dbIndex, String key, String value) {
        getJedis(dbIndex).set(key, value);
    }

    public static long delete(int dbIndex, String... keys) {
        boolean pattern = JSON.toJSONString(keys).indexOf("*") != -1;
        long num = 0;
        if (pattern) {
            for (String key :
                    keys) {
                num += delete(dbIndex, key);
            }
            return num;
        }
        return getJedis(dbIndex).del(keys);
    }

    public static Long delete(int dbIndex, String pattern) {
        Set<String> keysSet = RedisUtils.keys(0, pattern);
        String[] keys = keysSet.toArray(new String[keysSet.size()]);
        if (keys.length == 0) {
            return 0L;
        }
        return RedisUtils.delete(dbIndex, keys);
    }

    public static Set<String> keys(int dbIndex, String pattern) {
        return getJedis(dbIndex).keys(pattern);
    }

    public static <T> T getDefault(int dbIndex, String key, Class<T> clazz, T defaultValue) {
        String str = getJedis(dbIndex).get(key);
        if (StringUtils.isBlank(str)) {
            return defaultValue;
        }
        return JSONObject.parseObject(str, clazz);
    }


    public static JSONArray count() {
        String[] keyspaceArr = getJedis(0).info("keyspace").split("(\\r\\n)|(\\n)");

        JSONArray arr = new JSONArray();

        for (String keyspace :
                keyspaceArr) {
            if (keyspace.indexOf("keyspace") == -1) {
                String[] keyspaceInfo = keyspace.split(",");
                for (String keyInfo :
                        keyspaceInfo) {
                    if (keyInfo.indexOf("keys") != -1) {
                        JSONObject json = new JSONObject();
                        String dbname = keyInfo.substring(0, keyInfo.indexOf(":"));
                        String keyCount = keyInfo.substring(keyInfo.indexOf("=") + 1);
                        json.put("name", dbname);
                        json.put("count", keyCount);
                        arr.add(json);
                    }
                }
            }
        }

        return arr;
    }

    public static int count(String dbIndex) {
        JSONArray arr = count();
        Iterator<Object> it = arr.iterator();

        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof JSONObject
                    && ((JSONObject) obj).getString("name").equals(dbIndex)) {
                return ((JSONObject) obj).getIntValue("count");
            }
        }
        return 0;
    }

    public static JSONArray dbList() {
        return null;
    }
}
