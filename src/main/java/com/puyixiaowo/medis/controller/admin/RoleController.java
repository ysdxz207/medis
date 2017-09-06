package com.puyixiaowo.medis.controller.admin;

import com.alibaba.fastjson.JSON;
import com.puyixiaowo.medis.annotation.admin.RequiresPermissions;
import com.puyixiaowo.medis.bean.admin.RoleBean;
import com.puyixiaowo.medis.bean.admin.other.MenuPermissionBean;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.bean.sys.ResponseBean;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.service.MenuService;
import com.puyixiaowo.medis.service.RoleService;
import com.puyixiaowo.medis.utils.DBUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Moses
 * @date 2017-08-26 21:23:47
 * 
 */
public class RoleController extends BaseController {

    @RequiresPermissions(value = {"role:view"})
    public static String roles(Request request, Response response) {
        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(null,
                            "admin/role/role_list.html"));
        }
        PageBean pageBean = getPageBean(request);
        RoleBean roleBean = getParamsEntity(request, RoleBean.class, false);
        List<RoleBean> list = RoleService.selectRoleList(roleBean,
                pageBean);
        pageBean.setList(list);

        int count = RoleService.selectCount(roleBean);
        pageBean.setTotalCount(count);
        return pageBean.serialize();
    }


    @RequiresPermissions(value = {"role:edit"})
    public static String edit(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();
        List<RoleBean> roleBeanList = getParamsEntityJson(request, RoleBean.class, true);
        try {

            for (RoleBean roleBean :
                    roleBeanList) {
                DBUtils.insertOrUpdate(roleBean);
            }
        } catch (Exception e) {
            responseBean.error(e);
        }
        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"role:delete"})
    public static String delete(Request request, Response response){
        ResponseBean responseBean = new ResponseBean();

        try {
            DBUtils.deleteByIds(RoleBean.class,
                    request.queryParams("id"));
        } catch (Exception e) {
            responseBean.error(e);
        }

        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"role:edit"})
    public static Object setPermission(Request request, Response response) {
        ResponseBean responseBean = new ResponseBean();

        Long roleId = Long.parseLong(request.queryParams("roleId"));
        String permissionIds = request.queryParams("permissionIds");

        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            Map<String, Object> model = new HashMap<>();
            model.put("model", RoleService.selectByPrimaryKey(roleId));
            //权限列表
            List<MenuPermissionBean> menuList = MenuService.selectValidMenuPermissions(roleId);
            model.put("menuList", menuList);

            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(model,
                            "admin/role/role_permission_edit.html"));
        }

        try {
            RoleService.setPermission(roleId, permissionIds);

        } catch (Exception e) {
            responseBean.error(e);
        }
        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"role:view"})
    public static String allArray(Request request) {
        List<RoleBean> list = DBUtils.selectList(RoleBean.class,
                "select * from role ",
                null);

        return JSON.toJSONString(list);
    }

}
