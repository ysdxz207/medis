package com.puyixiaowo.medis.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author feihong
 * @date 2017-08-10 23:21
 */
public class StringUtils {

    public static boolean isBlank(Object obj) {
        return obj == null || spark.utils.StringUtils.isBlank(obj.toString());
    }

    public static boolean isNotBlank(Object obj){
         return !isBlank(obj);
    }

    public static Integer parseInteger(String str) {
        String regEx="[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return Integer.valueOf(m.replaceAll("").trim());
    }

    /**
     * 首字母转大写
     * @param name
     * @return
     */
    public static String firstToUpperCase(String name) {
        return StringUtils.isBlank(name) ? "" : name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 首字母转小写
     * @param name
     * @return
     */
    public static String firstToLowerCase(String name) {
        return StringUtils.isBlank(name) ? "" : name.substring(0, 1).toLowerCase() + name.substring(1);
    }
    public static void main(String[] args) {
        String str = "int(12)";
        System.out.println(parseInteger(str));
    }

    public static String join(Object[] strAry, String join){
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<strAry.length;i++){
            if(i==(strAry.length-1)){
                sb.append(strAry[i]);
            }else{
                sb.append(strAry[i]).append(join);
            }
        }

        return new String(sb);
    }
}
