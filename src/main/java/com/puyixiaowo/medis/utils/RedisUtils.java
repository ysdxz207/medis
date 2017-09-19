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

import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * @author Moses
 * @date 2017-08-03 9:29
 */
public class RedisUtils {
    private static int maxActive;
    private static int maxIdle;
    private static int maxWait;
    private static int timeout;

    private static JedisPool jedisPool;

    static {
        //读取相关的配置
        ResourceBundle resourceBundle = ResourceBundle.getBundle("redis");
        maxActive = Integer.parseInt(resourceBundle.getString("redis.pool.maxActive"));
        maxIdle = Integer.parseInt(resourceBundle.getString("redis.pool.maxIdle"));
        maxWait = Integer.parseInt(resourceBundle.getString("redis.pool.maxWait"));
        timeout = Integer.parseInt(resourceBundle.getString("redis.pool.timeout"));
    }

    public static void init(String host,
                            int port,
                            String password) {
        // 建立连接池配置参数
        JedisPoolConfig config = new JedisPoolConfig();
        // 设置最大连接数
        config.setMaxTotal(maxActive);
        // 设置最大阻塞时间
        config.setMaxWaitMillis(maxWait);
        // 设置空间连接
        config.setMaxIdle(maxIdle);
        if (StringUtils.isNotBlank(password)) {
            jedisPool = new JedisPool(config, host, port, timeout, password);
        } else {
            jedisPool = new JedisPool(config, host, port);
        }
    }

    public static boolean testConnection() {
        try (Jedis jedis = getJedis(0)) {
            jedis.set("TEST_CONNECTION", "connected");
            jedis.del("TEST_CONNECTION");
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public static boolean isConnected(){
        return testConnection();
    }

    /**
     * 需要关闭jedis
     * @return
     */
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
        }
        return jedis;
    }

    public static String get(int dbIndex, String key) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.get(key);
        }
    }

    public static <T> T get(int dbIndex, String key, Class<T> clazz) {
        String str = get(dbIndex, key);
        if (StringUtils.isBlank(str)) {
            return null;
        }
        return JSON.parseObject(str, clazz);
    }

    public static void set(int dbIndex, String key, String value) {
        try (Jedis jedis = getJedis(dbIndex)) {
            jedis.set(key, value);
        }
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
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.del(keys);
        }
    }

    public static Long delete(int dbIndex, String pattern) {
        Set<String> keysSet = RedisUtils.keys(dbIndex, pattern);
        String[] keys = keysSet.toArray(new String[keysSet.size()]);
        if (keys.length == 0) {
            return 0L;
        }
        return RedisUtils.delete(dbIndex, keys);
    }

    public static Set<String> keys(int dbIndex, String pattern) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.keys(pattern);
        }
    }

    public static <T> T getDefault(int dbIndex, String key, Class<T> clazz, T defaultValue) {
        String str = get(dbIndex, key);
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
}
