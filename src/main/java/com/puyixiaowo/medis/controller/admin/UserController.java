package com.puyixiaowo.medis.controller.admin;

import com.puyixiaowo.medis.annotation.admin.RequiresPermissions;
import com.puyixiaowo.medis.bean.admin.UserBean;
import com.puyixiaowo.medis.bean.admin.UserRoleBean;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.bean.sys.ResponseBean;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.service.RolePermissionService;
import com.puyixiaowo.medis.service.UserRoleService;
import com.puyixiaowo.medis.service.UserService;
import com.puyixiaowo.medis.utils.DBUtils;
import com.puyixiaowo.medis.utils.DesUtils;
import com.puyixiaowo.medis.utils.StringUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * @author Moses
 * @date 2017-08-10 9:57
 */
public class UserController extends BaseController{


    @RequiresPermissions(value = {"user:view"})
    public static String users(Request request,
                                       Response response){

        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(null,
                            "admin/user/user_list.html"));
        }

        PageBean pageBean = getPageBean(request);
        try {
            UserBean params = getParamsEntity(request, UserBean.class, false);
            List<UserBean> list =
                    UserService.selectUserList(
                            getParamsEntity(request, UserBean.class, false));

            for (UserBean userBean :
                    list) {
                userBean.setPassword(DesUtils.decrypt(userBean.getPassword()));
            }
            pageBean.setList(list);
            int count = UserService.selectCount(params);
            pageBean.setTotalCount(count);
        } catch (Exception e) {
            pageBean.error(e);
        }
        return pageBean.serialize();
    }
    /**
     * 添加或修改用户
     * @param request
     * @param response
     * @return
     */
    @RequiresPermissions(value = {"user:edit"})
    public static String edit(Request request, Response response) {
        ResponseBean responseBean = new ResponseBean();
        try {
            List<UserBean> userBeanList = getParamsEntityJson(request, UserBean.class, true);

            for (UserBean userBean :
                    userBeanList) {
                if (userBean.getId() == null) {
                    //是否已存在用户
                    int count = DBUtils.count("select count(*) from user where `loginname` = :loginname", userBean);
                    if (count > 0) {
                        responseBean.errorMessage("用户名已存在");
                        return responseBean.serialize();
                    }
                }

                if (StringUtils.isBlank(userBean.getNickname())) {
                    userBean.setNickname("大帅比");
                }

                userBean.setPassword(DesUtils.encrypt(userBean.getPassword()));

                DBUtils.insertOrUpdate(userBean);
                //用户角色
                UserRoleBean userRoleBean = new UserRoleBean();
                userRoleBean.setRoleId(userBean.getRoleId());
                userRoleBean.setUserId(userBean.getId());
                DBUtils.executeSql("delete from user_role where user_id = :userId", userRoleBean);
                DBUtils.insertOrUpdate(userRoleBean);
            }
        } catch (Exception e) {
            responseBean.errorMessage(e.getMessage());
        }

        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"user:delete"})
    public static String delete(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();

        try {
            String ids = request.queryParams("id");
            DBUtils.deleteByIds(UserBean.class,
                    ids);

            //删除user_role
            UserRoleService.deleteByUserIds(ids);
            //删除role_permission
            RolePermissionService.deleteByUserIds(ids);
        } catch (Exception e) {
            responseBean.error(e);
        }

        return responseBean.serialize();
    }
}
