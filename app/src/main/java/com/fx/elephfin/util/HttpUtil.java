package com.fx.elephfin.util;

/**
 * 发送http请求并保持同步session的工具类
 * Created by 莫仁周 on 2017/1/19 0019.
 */

import android.text.TextUtils;

import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import java.util.List;


public class HttpUtil {

    public static final int METHOD_GET=0;
    public static final int METHOD_POST=1;

    static HttpResponse resp=null;
    public static final String USERAPI_WEB_URL = CommonUtil.BASE_WEB_URL+"userApi/";
    /**
     * 设置请求的Cookie头信息
     * @param reqMsg
     */
    private static void  setRequestCookies(HttpMessage reqMsg) {
        if(!TextUtils.isEmpty(CommonUtil.SHARE_COOKIES)){
            reqMsg.setHeader("Cookie", CommonUtil.SHARE_COOKIES);
        }
    }

    /**
     * 把新的Cookie头信息附加到旧的Cookie后面
     * 用于下次Http请求发送
     * @param resMsg
     */
    private static void appendCookies(HttpMessage resMsg) {
        /*response 所有的header ：[Date: Thu, 17 Mar 2016 05:37:00 GMT, Server: Apache-Coyote/1.1, Access-Control-Allow-Origin: *, Content-Type: text/html;charset=UTF-8, Vary: Accept-Encoding,
        Set-Cookie: JSESSIONID=6D46660DA233E7462A5FAEF219A9C85A; Path=/enroll, Set-Cookie: geexek.race=060c463b72ad0fd48a324af3ebd1b8ae; Expires=Sat, 16-Apr-2016 05:37:01 GMT; Path=/, Keep-Alive: timeout=5, max=100, Connection: Keep-Alive, Transfer-Encoding: chunked]*/
        Header setCookieHeader=resMsg.getFirstHeader("Set-Cookie");
        if (setCookieHeader != null && !TextUtils.isEmpty(setCookieHeader.getValue())) {
            String cookieHeader=setCookieHeader.getValue();
            if(TextUtils.isEmpty(CommonUtil.SHARE_COOKIES)){
                CommonUtil.SHARE_COOKIES=cookieHeader;
            }else{
                CommonUtil.SHARE_COOKIES=CommonUtil.SHARE_COOKIES+"; "+cookieHeader;
            }
        }
    }


    /**
     * 发送请求
     * @param method,uri,pairs
     */
    public static HttpResponse send(int method,String uri, List<BasicNameValuePair> pairs,Boolean setCookie)
    {
        //CommonUtil.debugMsg("HttpUtil","url:"+uri);
        try {
            HttpClient client=new DefaultHttpClient();
            HttpUriRequest request=null;
            switch (method) {
                case METHOD_GET:
                    request=new HttpGet(uri);
                    request.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    if (setCookie){
                        setRequestCookies(request);//处理session同步
                    }
                    break;
                case METHOD_POST:
                    HttpPost post=new HttpPost(uri);
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pairs, "utf-8");
                    post.setEntity(entity);
                    post.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    if (setCookie){
                        setRequestCookies(post);//处理session同步
                    }
                    request=post;
                    break;
            }

            resp=client.execute(request);

            // 解析返回结果 是否上传成功
            if (resp.getStatusLine().getStatusCode() == 200) {
                appendCookies(resp);//保存session
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return resp;
    }
}