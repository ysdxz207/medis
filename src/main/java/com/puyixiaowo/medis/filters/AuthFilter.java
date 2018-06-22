package com.puyixiaowo.medis.filters;

import com.puyixiaowo.medis.constants.Constants;
import com.puyixiaowo.medis.controller.LoginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static spark.Spark.before;
import static spark.Spark.halt;

/**
 *
 * @author Moses
 * @date 2017-12-19
 * 用户权限控制过滤器
 */
public class AuthFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    public static void init() {
        before("/*", (request, response) -> {

            String uri = request.uri();
            if (!isIgnorePath(uri)
                    && (request.session().attribute(Constants.SESSION_USER_KEY) == null)) {

                LoginController.rememberMeLogin(Constants.COOKIE_LOGIN_KEY_BOOK,
                        request, response);
                halt();
            }
        });
    }


    private static boolean isIgnorePath(String uri) {

        List<String> ignores = Constants.IGNORE_AUTH_PATHS;

        for (String path : ignores) {
            if (removeFirstSeparator(path).equals(removeFirstSeparator(uri))) {
                return true;
            }
        }
        return false;
    }

    private static String removeFirstSeparator(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

}
