package com.fedming.webtonativedemo;

import java.util.HashMap;

/**
 * <pre>
 *     author : fdm
 *     time   : 2018/03/08
 *     desc   : Utils
 *     version: 1.0
 * </pre>
 */

public class Utils {

    public Utils() {
    }

    /**
     * 获取url中的协议
     *
     * @param url url
     * @return protocol
     */

    public static String getUrlScheme(String url) {
        int index = url.indexOf(":");
        return url.substring(0, index);
    }

    /**
     * 获取url中的参数
     *
     * @param url url
     * @return args
     */
    public static HashMap<String, String> getUrlParams(String url) {
        int index = url.indexOf("?");
        String argStr = url.substring(index + 1);
        String[] argAry = argStr.split("&");
        HashMap<String, String> argMap = new HashMap<String, String>(argAry.length);
        for (String arg : argAry) {
            System.out.println("arg:" + arg);
            String[] argAryT = arg.split("=");
            argMap.put(argAryT[0], argAryT[1]);
        }
        return argMap;
    }

}
