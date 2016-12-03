package com.fedming.webtonativedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

/**
 * WebView访问本地方法的三种方法Demo
 *
 * @author bruce
 */

public class MainActivity extends Activity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        /**
         * 第一种
         * 原理：直接构造本地化对象，映射到js页面中访问，底层原理借助了V8引擎（Android4.2 以下系统有WebView漏洞）
         */
        webView.addJavascriptInterface(new JSObject(), "myObj");

        /**
         * 第二种
         * 回调方法中（shouldOverrideUrlLoading）拦截请求url,分析url格式以及自定义协议、参数名称可得到具体参数
         * tips：需要再次调用页面js方法获取返回值
         */
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                //协议url:"js://demo?arg1=111"
                try {
                    //处理自定义协议
                    String protocol = getUrlScheme(url);
                    if ("js".equals(protocol)) {
                        HashMap<String, String> map = getUrlParams(url);
                        String arg1 = map.get("arg1");//获取指定key的参数值，调用本地方法
                        String res = getPwd(arg1);
                        webView.loadUrl("javascript:clicktworesult(" + res + ")");
                    }
                } catch (Exception e) {
                    Log.i("fdm", "error:" + Log.getStackTraceString(e));
                }
                return true;
            }

        });

        /**
         * 第三种
         * 借助WebChromeClient的回调方法（共三个），拦截JS中的三个方法：alert,confirm,prompt，解析参数，得到指定格式数据
         * tips：需要页面和本地解析格式做一个约束
         */
        webView.setWebChromeClient(new WebChromeClient() {

            //拦截JS的alert方法
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            //拦截JS的confirm方法
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            //拦截JS的prompt方法
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                      JsPromptResult result) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                Log.i("fdm", "url:" + url + ",message:" + message);
                //协议url:"js://demo?arg1=111"
                try {
                    //只处理指定协议
                    String protocol = getUrlScheme(message);
                    if ("js".equals(protocol)) {
                        HashMap<String, String> map = getUrlParams(message);
                        //获取到指定key的参数值，调用本地方法
                        String arg1 = map.get("arg1");
                        String res = getPwd(arg1);
                        result.confirm(res);//返回值
                    }
                } catch (Exception e) {
                    Log.i("fdm", "error:" + Log.getStackTraceString(e));
                }

                return true;
            }

        });

        //加载asset目录中的网页
        webView.loadUrl("file:///android_asset/js_demo.html");

    }

    /**
     * 本地化JS对象
     */
    class JSObject {
        @JavascriptInterface //sdk17版本以上加上注解
        public String getPwd(String txt) {
            Log.i("fdm", "get pwd...");
            return "123456";
        }
    }

    public String getPwd(String txt) {
        return "123456";
    }

    /**
     * 获取链接中的参数
     *
     * @param url url
     * @return args
     */
    private HashMap<String, String> getUrlParams(String url) {
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

    /**
     * 获取链接中的协议
     *
     * @param url url
     * @return protocol
     */

    private String getUrlScheme(String url) {
        int index = url.indexOf(":");
        return url.substring(0, index);
    }

}