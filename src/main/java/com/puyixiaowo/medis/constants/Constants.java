package com.puyixiaowo.medis.constants;

import java.util.ArrayList;
import java.util.List;

public class Constants {

    public static final String ENCODING = "UTF-8";
    /**
     * 后台用户session key
     */
    public static final String SESSION_USER_KEY = "medis_session_user_key";
    public static final int DEFAULT_PAGE_SIZE = 10;
    /**
     * 验证码session key
     */
    public static final String KAPTCHA_SESSION_KEY = "KAPTCHA_SESSION_KEY_MEDIS";

    /**
     * book登录cookie key
     */
    public static final String COOKIE_LOGIN_KEY_BOOK = "COOKIE_LOGIN_KEY_MEDIS";
    /**
     * 成功状态码
     */
    public static final int RESPONSE_STATUS_CODE_SUCCESS = 200;
    /**
     * 错误状态码
     */
    public static final int RESPONSE_STATUS_CODE_ERROR = 300;
    /**
     * 成功描述
     */
    public static final String RESPONSE_SUCCESS_MESSAGE = "操作成功";

    /**
     * 忽略权限的路径路径
     */
    public static final List<String> IGNORE_AUTH_PATHS = new ArrayList<>();
}
