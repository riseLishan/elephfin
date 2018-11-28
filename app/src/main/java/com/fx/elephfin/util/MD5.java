package com.fx.elephfin.util;

/**
 * Created by Administrator on 2017/2/28 0028.
 */

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 类名称：MD5
 * 类描述：MD5加密工具
 * 创建人：王亚超
 * 创建时间：2014-3-13 下午8:38:05
 * @version 1.0
 */
public class MD5 {

    /**
     * md5加密
     *
     * @param str
     * @return
     */
    public static String getMD5Str(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] byteDigest = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < byteDigest.length; offset++) {
                i = byteDigest[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            // 32位加密
            return buf.toString();
            // 16位的加密
            // return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
