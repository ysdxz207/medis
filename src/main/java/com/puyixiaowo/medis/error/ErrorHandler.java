package com.puyixiaowo.medis.error;

import com.puyixiaowo.medis.enums.EnumsRedisKey;
import com.puyixiaowo.medis.exception.NoPermissionsException;
import com.puyixiaowo.medis.utils.RedisUtils;
import com.puyixiaowo.medis.utils.ResourceUtils;
import spark.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import static spark.Spark.exception;
import static spark.Spark.notFound;

public class ErrorHandler {

    /**
     * 处理错误信息
     */
    public static void handleErrors() {

        handle404();
        handleNoPermissions();
    }

    private static void handle404(){

        notFound((request, response) -> {
            String html = RedisUtils.get(EnumsRedisKey.REDIS_KEY_404_PAGE.key);
            if (StringUtils.isNotBlank(html)) {
                return html;
            }
            try {
                InputStream inputStream = ResourceUtils.readFile("error/404.html");

                Scanner sc = new Scanner(inputStream, "UTF-8");
                StringBuilder sb = new StringBuilder();
                while (sc.hasNextLine()) {
                    sb.append(sc.nextLine());
                }
                // note that Scanner suppresses exceptions
                if (sc.ioException() != null) {
                    throw sc.ioException();
                }

                RedisUtils.set(EnumsRedisKey.REDIS_KEY_404_PAGE.key, sb.toString());
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                RedisUtils.set(EnumsRedisKey.REDIS_KEY_404_PAGE.key, "404");
                return "404";
            }
        });
    }

    private static void handleNoPermissions(){
        exception(NoPermissionsException.class, (e, request, response) -> {
            response.body("您没有访问权限！");
        });
    }


}
