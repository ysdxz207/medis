package com.puyixiaowo.medis;

import com.puyixiaowo.medis.controller.IndexController;
import com.puyixiaowo.medis.controller.TagController;
import spark.Spark;

import static spark.Spark.*;

public class Routes {
    public static void init() {
        Spark.staticFileLocation("static");

        get("/", ((request, response) -> IndexController.index(request, response)));


        path("/tag", () -> {

            get("/", ((request, response) -> TagController.tag(request, response)));
            get("/tags", ((request, response) -> TagController.tags(request, response)));
            get("/add", ((request, response) -> TagController.add(request, response)));
            get("/delete", ((request, response) -> TagController.delete(request, response)));
        });


        path("/redis", () -> {
            get("/keys", ((request, response) -> IndexController.redisKeys(request, response)));
            get("/get", ((request, response) -> IndexController.redisGet(request, response)));
            get("/delete", ((request, response) -> IndexController.redisDelete(request, response)));
            get("/edit", ((request, response) -> IndexController.editDelete(request, response)));
            get("/count", ((request, response) -> IndexController.count(request, response)));
            get("/connect", ((request, response) -> IndexController.saveConfAndConnect(request, response)));
            get("/conf/delete", ((request, response) -> IndexController.confDelete(request, response)));
        });
    }
}
