package com.fedming.webtonativedemo;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.HashMap;

/**
 * <pre>
 *     author : fdm
 *     time   : 2018/03/08
 *     desc   : WebView与本地方法交互的三种方法Demo
 *     version: 1.0
 * </pre>
 */

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(false);
        webSettings.setSupportZoom(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webView.setWebChromeClient(new WebChromeClient());

        /**
         * 第一种
         * 原理：直接构造本地化对象，映射到js页面中访问，底层原理借助了V8引擎（Android4.2 以下系统有WebView漏洞）
         */
        webView.addJavascriptInterface(new JSInterface(), "MyObj");

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
                //协议url:"js://demo?arg=111"
                try {
                    String protocol = Utils.getUrlScheme(url);
                    if ("js".equals(protocol)) {
                        HashMap<String, String> map = Utils.getUrlParams(url);
                        String arg = map.get("arg");
                        String res = getLocalString(arg);
                        //再次调用web中js方法,将参数传回web去
                        webView.loadUrl("javascript:click_result(" + res + ")");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return super.onJsConfirm(view, url, message, result);
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                //协议url:"js://demo?arg=111"
                try {
                    String protocol = Utils.getUrlScheme(message);
                    if ("js".equals(protocol)) {
                        HashMap<String, String> map = Utils.getUrlParams(message);
                        String arg = map.get("arg");
                        String res = getLocalString(arg);
                        result.confirm(res);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });

        //加载asset中的网页
        webView.loadUrl("file:///android_asset/js_demo.html");

    }

    /**
     * 本地化JS对象，供web调用
     * sdk17版本以上需要加上注解
     */
    class JSInterface {
        @JavascriptInterface
        public String getPwd(String pwd) {
            //执行本地方法，返回结果到web
            return getLocalString(pwd);
        }
    }

    /**
     * 本地方法
     *
     * @param txt txt
     * @return txt
     */
    public String getLocalString(String txt) {
        return txt;
    }

}