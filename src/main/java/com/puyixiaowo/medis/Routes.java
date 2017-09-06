package com.puyixiaowo.medis;

import com.puyixiaowo.medis.controller.IndexController;
import spark.Spark;

import static spark.Spark.*;

public class Routes {
    public static void init() {
        Spark.staticFileLocation("static");

        get("/", ((request, response) -> IndexController.index(request, response)));
    }
}
