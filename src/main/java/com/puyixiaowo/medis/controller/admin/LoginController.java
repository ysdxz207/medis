package com.puyixiaowo.medis.controller.admin;

import com.puyixiaowo.medis.bean.admin.UserBean;
import com.puyixiaowo.medis.constants.Constants;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.service.LoginService;
import com.puyixiaowo.medis.utils.DesUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Moses
 * @date 2017-08-08 13:40:25
 * 登录
 */
public class LoginController extends BaseController {


    /**
     * 登录页面
     *
     * @param request
     * @param response
     * @return
     */
    public static ModelAndView loginPage(Request request, Response response) {
        return new ModelAndView(null, "login.html");
    }

    /**
     * 登录
     *
     * @param request
     * @param response
     * @return
     */
    public static ModelAndView doLogin(Request request,
                                       Response response) {

        Map<String, Object> model = new HashMap<>();
        Map<String, Object> params = new HashMap<>();

        params.put("loginname", request.queryParams("uname"));
        params.put("password", DesUtils.encrypt(request.queryParams("upass")));

        UserBean userBean = null;

        try {
            userBean = LoginService.login(params);
            if (userBean == null) {
                model.put("message", "用户名或密码不正确");
            } else {

                request.session().attribute(Constants.SESSION_USER_KEY, userBean);
                response.redirect("/admin/");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.put("message", e.getMessage());
        }


        return new ModelAndView(model, "login.html");
    }

    /**
     * 退出登录
     *
     * @param request
     * @param response
     * @return
     */
    public static Object logout(Request request, Response response) {
        request.session().removeAttribute(Constants.SESSION_USER_KEY);

        response.redirect("/admin/loginPage");
        return "";
    }

}
