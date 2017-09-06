package com.puyixiaowo.medis.utils;

import com.alibaba.fastjson.JSON;
import com.puyixiaowo.medis.annotation.Id;
import com.puyixiaowo.medis.annotation.Transient;
import com.puyixiaowo.medis.domain.User;
import com.puyixiaowo.medis.enums.EnumsRedisKey;
import com.puyixiaowo.medis.exception.DBException;
import com.puyixiaowo.medis.exception.DBSqlException;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;
import org.sql2o.Sql2oException;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Moses
 * @date 2017-08-04 12:57
 */
public class DBUtils {

    private static final String FOLDER_SQL = "sql";
    private static final int SQL_TYPE_INSERT = 1;//添加
    private static final int SQL_TYPE_UPDATE = 2;//更新

    private static Sql2o sql2o;

    /**
     * 初始化数据库
     */
    public static void initDB() {
        String dbHost = (String) ResourceUtils.load("jdbc.properties").get("sqlite3.host");

        if (StringUtils.isBlank(dbHost)) {
            throw new DBException("There is no db host found.");
        }

        initDBConnection(dbHost);

        if (!new File(dbHost).exists()) {
            //创建数据库文件
            try (Connection conn = sql2o.open()) {

                File file = new File(ResourceUtils.getResourcePath() + FOLDER_SQL);
                String[] filenames = file.list();
                FileUtils.runResourcesSql(conn, FOLDER_SQL, filenames);
            }
            //清空redis
            EnumsRedisKey [] enumsRedisKeys = EnumsRedisKey.values();
            String [] keys = new String[enumsRedisKeys.length];
            for (int i = 0; i < enumsRedisKeys.length; i ++) {
                keys[i] = enumsRedisKeys[i].key + "*";
            }
            RedisUtils.delete(keys);
        }

    }

    /**
     * 初始化数据库连接
     *
     * @param dbHost
     */
    private static void initDBConnection(String dbHost) {

        sql2o = new Sql2o("jdbc:sqlite:" + dbHost, null, null);

        if (sql2o == null) {
            throw new DBException("Can not find db " + dbHost);
        }

    }

    public static <T> T selectOne(Class clazz,
                                  String sql,
                                  Map<String, Object> params) {

        List<T> list = selectList(clazz, sql, params);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public static <T> T selectOne(Class clazz,
                                  String sql,
                                  Object paramsObj) {

        List<T> list = selectList(clazz, sql, paramsObj);
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public static <E> List<E> selectList(Class clazz,
                                         String sql,
                                         Map<String, Object> params) {

        setCamelMapping(clazz);
        try (Connection conn = sql2o.open()) {
            Query query = conn.createQuery(sql).throwOnMappingFailure(false);

            if (params != null) {
                for (Map.Entry<String, Object> entry :
                        params.entrySet()) {
                    try {
                        query.addParameter(entry.getKey(), entry.getValue());
                    } catch (Sql2oException e) {
                        //ignore
                    }
                }
            }


            List<E> list = query.executeAndFetch(clazz);
            return list;
        }
    }

    public static <E> List<E> selectList(Class clazz,
                                         String sql,
                                         Object params) {
        Map<String, Object> map = JSON.toJavaObject(JSON.parseObject(JSON.toJSONString(params)), Map.class);
        return selectList(clazz, sql, map);
    }

    /**
     * 下划线映射为驼峰
     *
     * @param clazz
     */
    private static void setCamelMapping(Class clazz) {

        if (String.class.equals(clazz)) {
            return;
        }
        Field[] fields = ORMUtils.getFieldListByClass(clazz);
        Map<String, String> mapping = new HashMap<>();
        for (Field field :
                fields) {

            if (!"serialVersionUID".equals(field.getName())
                    && CamelCaseUtils.checkIsCamelCase(field.getName())) {
                mapping.put(CamelCaseUtils.toUnderlineName(field.getName()), field.getName());
            }
        }
        sql2o.setDefaultColumnMappings(mapping);
    }

    /**
     * insert or update
     *
     * @param obj The object to insert or update.
     * @return
     */
    public static Object insertOrUpdate(Object obj) {

        String tableName = ORMUtils.getTableNameByClass(obj.getClass());

        try (Connection conn = sql2o.open()) {
            Object primaryKey = null;
            int lines = 0;
            try {
                String sql_update = assembleSql(SQL_TYPE_UPDATE, tableName, obj);
                Query queryUpdate = conn.createQuery(sql_update).throwOnMappingFailure(false);
                primaryKey = queryUpdate.executeUpdate().getKey();
            } catch (Exception e) {
                ORMUtils.setId(obj);
                String sql_insert = assembleSql(SQL_TYPE_INSERT, tableName, obj);
                System.out.println(sql_insert);
                Query queryInsert = conn.createQuery(sql_insert).throwOnMappingFailure(false);
                lines = queryInsert.executeUpdate().getResult();
            }

            if (primaryKey != null) {
                return primaryKey;
            }
            return lines;
        }
    }


    private static String assembleSql(int sqlType,
                                      String tableName,
                                      Object obj) {

        //获取主键字段名和值map
        Map<String, Object> primaryKeyValueMap = ORMUtils.getPrimaryKeyValues(obj);

        //将值为0的主键值设为null
        for (Map.Entry entry :
                primaryKeyValueMap.entrySet()) {
            Object name = entry.getKey();
            Object value = entry.getValue();

            if (value != null
                    && value.toString().equals("0")) {
                primaryKeyValueMap.replace(name.toString(), null);
            }
        }

        Field[] filelds = obj.getClass().getDeclaredFields();

        StringBuilder sb_sql = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        String sql_1 = "";
        String sql_2 = "";


        switch (sqlType) {
            case SQL_TYPE_INSERT:

                //insert sql
                sb_sql.append("insert into ");
                sb_sql.append(tableName);
                sb_sql.append("(");


                for (int i = 0; i < filelds.length; i++) {
                    Field field = filelds[i];
                    if (field.getAnnotation(Transient.class) != null) {
                        continue;
                    }
                    field.setAccessible(true);
                    String fieldName = ORMUtils.getFieldColumnName(field);
                    Object fieldValue = "";
                    try {
                        fieldValue = field.get(obj);
                    } catch (IllegalAccessException e) {
                    }
                    if ("serialVersionUID".equals(fieldName) ||
                            fieldValue == null ||
                            StringUtils.isBlank(fieldValue.toString())) {
                        continue;
                    }
                    sb1.append("`");
                    sb1.append(fieldName);
                    sb1.append("`");
                    sb1.append(",");

                    //
                    sb2.append("'");
                    sb2.append(fieldValue);
                    sb2.append("'");
                    sb2.append(",");

                }
                sql_1 = sb1.toString();
                sb_sql.append(sql_1.substring(0, sql_1.length() - 1));

                sb_sql.append(") ");

                sb_sql.append("values(");
                sql_2 = sb2.toString();
                sb_sql.append(sql_2.substring(0, sql_2.length() - 1));
                sb_sql.append(") ");

                break;
            case SQL_TYPE_UPDATE:

                //update sql
                sb_sql.append("update ");
                sb_sql.append(tableName);
                sb_sql.append(" set ");


                for (int i = 0; i < filelds.length; i++) {
                    Field field = filelds[i];
                    if (field.getAnnotation(Transient.class) != null) {
                        continue;
                    }
                    field.setAccessible(true);
                    String fieldName = ORMUtils.getFieldColumnName(field);
                    Object fieldValue = "";
                    try {
                        fieldValue = field.get(obj);
                    } catch (IllegalAccessException e) {
                    }
                    if ("serialVersionUID".equals(fieldName) ||
                            fieldValue == null ||
                            StringUtils.isBlank(fieldValue.toString()) ||
                            field.getAnnotation(Id.class) != null ||
                            "id".equalsIgnoreCase(fieldName)) {
                        continue;
                    }
                    sb1.append("`");
                    sb1.append(fieldName);
                    sb1.append("`");
                    sb1.append("=");
                    sb1.append("'");
                    sb1.append(fieldValue);
                    sb1.append("'");
                    sb1.append(",");

                }
                sql_1 = sb1.toString();
                sb_sql.append(sql_1.substring(0, sql_1.length() - 1));
                sb_sql.append(" where ");

                Iterator<Map.Entry<String, Object>> it = primaryKeyValueMap.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry entry = it.next();
                    Object name = entry.getKey();
                    Object id = entry.getValue();
                    if (id == null) {
                        throw new DBSqlException("Update table error: primary key [" + name + "] is null");
                    }
                    sb_sql.append(name);
                    sb_sql.append("=");
                    sb_sql.append("'");
                    sb_sql.append(id);
                    sb_sql.append("'");
                    if (it.hasNext())
                        sb_sql.append(" and ");
                }

                break;
            default:
                //insert sql

        }

        return sb_sql.toString();
    }


    public static int count(String sql, Object paramObj) {
        try (Connection conn = sql2o.open()) {
            Query query = conn.createQuery(sql).throwOnMappingFailure(false).bind(paramObj);
            return query.executeScalar(Integer.class);
        }
    }

    public static Integer deleteByIds(Class clazz, String ids) {
        if (StringUtils.isBlank(ids)) {
            return 0;
        }
        List<String> list = Arrays.asList(ids.split(","));
        if (list == null
                || list.isEmpty()
                || list.get(0) == null) {
            return 0;
        }

        StringBuilder sb_del_sql = new StringBuilder("delete from ");
        String tableName = ORMUtils.getTableNameByClass(clazz);

        sb_del_sql.append("`");
        sb_del_sql.append(tableName);
        sb_del_sql.append("` where id in(");


        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            sb_del_sql.append("'");
            sb_del_sql.append(it.next());
            sb_del_sql.append("'");
            if (it.hasNext())
                sb_del_sql.append(", ");
        }

        sb_del_sql.append(")");

        System.out.println(sb_del_sql.toString());
        try (Connection conn = sql2o.open()) {
            Query query = conn.createQuery(sb_del_sql.toString()).throwOnMappingFailure(false);

            return query.executeUpdate().getResult();
        }
    }

    public static Object executeSql(String sql, Map<String, Object> params) {
        try (Connection conn = sql2o.open()) {
            Query query = conn.createQuery(sql).throwOnMappingFailure(false);

            if (params != null) {
                for (Map.Entry<String, Object> entry :
                        params.entrySet()) {
                    query.addParameter(entry.getKey(), entry.getValue());
                }
            }
            return query.executeUpdate().getResult();
        }
    }

    public static Object executeSql(String sql, Object paramsObj) {
        try (Connection conn = sql2o.open()) {
            Query query = conn.createQuery(sql).throwOnMappingFailure(false).bind(paramsObj);
            return query.executeUpdate().getResult();
        }
    }

    public static void main(String[] args) throws Exception {

        DBUtils.initDBConnection("D:\\workspace\\idea\\f-blog\\f_blog.db");
        List<User> userList = DBUtils.selectList(User.class,
                "select * from user " +
                        "where loginname =:loginname",
                new HashMap<String, Object>() {
                    {
                        put("loginname", "feihong");
                    }
                });
        System.out.println(userList.get(0).getId());
    }
}
