package com.puyixiaowo.medis.exception;

/**
 * @author Moses
 * @date 2017-09-02 23:41
 */
public class JedisConfigException extends RuntimeException {
    public JedisConfigException() {
    }

    public JedisConfigException(String message) {
        super(message);
    }
}
