package com.puyixiaowo.medis.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.puyixiaowo.medis.annotation.admin.RequiresPermissions;
import com.puyixiaowo.medis.bean.admin.MenuBean;
import com.puyixiaowo.medis.bean.sys.PageBean;
import com.puyixiaowo.medis.bean.sys.ResponseBean;
import com.puyixiaowo.medis.controller.BaseController;
import com.puyixiaowo.medis.enums.EnumsRedisKey;
import com.puyixiaowo.medis.exception.MenuException;
import com.puyixiaowo.medis.freemarker.FreeMarkerTemplateEngine;
import com.puyixiaowo.medis.service.MenuService;
import com.puyixiaowo.medis.utils.DBUtils;
import com.puyixiaowo.medis.utils.RedisUtils;
import com.puyixiaowo.medis.utils.StringUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.List;

/**
 * @author feihong
 * @date 2017-08-13 18:58
 */
public class MenuController extends BaseController {


    public static Object navMenus(Request request, Response response) {
        String typeStr = request.params(":type");
        if (StringUtils.isBlank(typeStr)) {
            throw new MenuException("菜单类型不可为空");
        }
        Integer type = Integer.parseInt(typeStr);

        List<MenuBean> menuBeanList = MenuService.selectNavMenuList(type);

        return buildMenus(menuBeanList);
    }

    /**
     * @param list
     * @return
     */
    private static JSONArray buildMenus(List<MenuBean> list) {
        JSONArray result = new JSONArray();

        for (MenuBean menuBean : list) {
            JSONObject menus = new JSONObject();
            if (StringUtils.isNotBlank(menuBean.getCode())) {
                menus.put("id", menuBean.getCode());
            }
            if (StringUtils.isNotBlank(menuBean.getMenuName())) {
                menus.put("name", menuBean.getMenuName());
            }
            menus.put("target", "navtab");
            if (StringUtils.isNotBlank(menuBean.getHref())) {
                menus.put("url", menuBean.getHref());
            }
            List<MenuBean> menuList = menuBean.getMenuBeanList();
            if (menuList != null && menuList.size() > 0) {
                JSONArray children = buildMenus(menuList);
                menus.put("children", children);
            }
            result.add(menus);
        }
        return result;
    }

    @RequiresPermissions(value = {"menu:view"})
    public static String menus(Request request, Response response) {
        Boolean data = Boolean.valueOf(request.params(":data"));

        if (!data) {
            return new FreeMarkerTemplateEngine()
                    .render(new ModelAndView(null,
                            "admin/menu/menu_list.html"));
        }
        PageBean pageBean = getPageBean(request);
        try {
            MenuBean menuBean = getParamsEntity(request, MenuBean.class, false);
            List<MenuBean> list = MenuService.selectMenuList(menuBean,
                    pageBean);
            pageBean.setList(list);

            int count = MenuService.selectCount(menuBean);
            pageBean.setTotalCount(count);
        } catch (Exception e) {
            pageBean.error(e);
        }
        return pageBean.serialize();
    }

    @RequiresPermissions(value = {"menu:edit"})
    public static String edit(Request request, Response response) {
        System.out.println("edit");
        ResponseBean responseBean = new ResponseBean();
        List<MenuBean> menuBeanList = getParamsEntityJson(request, MenuBean.class, true);
        try {

            for (MenuBean menuBean :
                    menuBeanList) {
                DBUtils.insertOrUpdate(menuBean);
            }

            //删除缓存，下次刷新
            RedisUtils.delete(EnumsRedisKey.REDIS_KEY_MENU_LIST.key + "*");
            responseBean.setMessage("操作成功，请手动刷新页面。");

        } catch (Exception e) {
            responseBean.error(e);
        }
        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"menu:delete"})
    public static String delete(Request request, Response response) {
        ResponseBean responseBean = new ResponseBean();

        try {
            DBUtils.deleteByIds(MenuBean.class,
                    request.queryParams("id"));
            //删除缓存，下次刷新
            RedisUtils.delete(EnumsRedisKey.REDIS_KEY_MENU_LIST.key + "*");
            responseBean.setMessage("操作成功，请手动刷新页面。");
        } catch (Exception e) {
            responseBean.error(e);
        }

        return responseBean.serialize();
    }

    @RequiresPermissions(value = {"menu:view"})
    public static String array(Request request, Response response) {
        String parent = request.params(":parent");
        List<MenuBean> list = DBUtils.selectList(MenuBean.class,
                "select * from menu where pid "
                        + ("yes".equals(parent) ? " = 0"
                        : ("no".equals(parent) ? " > 0" : ">= 0")),
                null);

        MenuBean bean = new MenuBean();
        bean.setId(0L);
        bean.setMenuName("无");
        list.add(0, bean);

        return JSON.toJSONString(list);
    }


}
