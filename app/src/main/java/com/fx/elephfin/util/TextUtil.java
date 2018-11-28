package com.fx.elephfin.util;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本工具类
 * @author lishan
 *
 */
public class TextUtil {



	/**
	 * 判断字符串是否为空
	 * isEmpty:(这里用一句话描述这个方法的作用)
	 */
	public static boolean isEmpty(String str) {
		if (str == null || str.trim().length() == 0){
			return true;
		}
		else{
			return false;
		}
	}

	/**
	 * 字符串不能空
	 * @param value  要验证的字符串
	 * @return
	 */
	public static boolean stringIsNotNull(String value){
		return  value!=null && value.trim().length()>0;
	}

	/**
	 * 判断是否为中文
	 * @param str
	 * @return
	 */
	public static boolean isChinese(String str){
		//正则表达式
		Pattern p= Pattern.compile("^[\u4e00-\u9fa5]*$");
		Matcher m=p.matcher(str);
		if(m.matches()){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * 字符串为空
	 * @param value  要验证的字符串
	 * @return
	 */
	public static boolean stringIsNull(String value){
		return value ==null || value.trim().length()<=0;
	}

	/**
	 * 判断是否为字母
	 * @return
	 */
	public static boolean isEnglish(String fstrData){
		//根据首字母进行判断,是否为英文
		char   c   =   fstrData.charAt(0);
		if(((c>='a'&&c<='z')   ||   (c>='A'&&c<='Z'))){
			return   true;
		}else{
			return   false;
		}
	}
	/**
	 * 获取文本内容的长度，中文算一个字符，英文算半个字符，包括标点符号
	 * @param str
	 * @return
	 */
	public static int getTextLengthes(String str){
		int number=getTextLength(str);
		int length=number/2;
		if(number % 2 != 0){
			length+=1;
		}
		str=null;
		return length;
	}
	
	/**
	 * 获取文本内容的长度(中文算两个字符，英文算一个字符)
	 * @param str
	 * @return
	 */
	public static int getTextLength(String str){
		int length=0;
		try {
			str=new String(str.getBytes("GBK"), "ISO8859_1");
			length=str.length();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return length;
	}
	
	public static String F="%.2f";
	/**
	 * 加法
	 * @param a
	 * @param b
	 * @return
	 */
	public static String addition(String a, String b){
		if(TextUtil.stringIsNull(a)){
			return b;
		}
		if(TextUtil.stringIsNull(b)){
			return a;
		}
		double value= Double.parseDouble(a)+ Double.parseDouble(b);
		return format(value);
	}
	
	/**
	 * @author lishan
	 * @createdate 2013-4-10 下午6:41:45
	 * @Description: 格式化，保留二位小数
	 * @param value
	 * @return
	 */
	public static String format(double value){
		return String.format(F, value);
	}

	/**
	 * 验证密码,无特殊字符
	 * @param psd
     * @return
     */
	public static boolean ispsd(String psd) {
		Pattern p = Pattern
				.compile("^[a-zA-Z].*[0-9]|.*[0-9].*[a-zA-Z]");
		Matcher m = p.matcher(psd);

		return m.matches();
	}

	/**
	 * 正则表达式验证昵称
	 * @param nickName
	 * @return
	 */
	public static boolean rexCheckNickName(String nickName) {
		// 昵称格式：限16个字符，支持中英文、数字、减号或下划线
		String regStr = "^[\\u4e00-\\u9fa5_a-zA-Z0-9-]{1,16}$";
		return nickName.matches(regStr);
	}

	/**
	 * 正则表达式验证密码
	 * @param input
	 * @return
	 */
	public static boolean rexCheckPassword(String input) {
		// 6-20 位，字母、数字、字符
		//String reg = "^([A-Z]|[a-z]|[0-9]|[`-=[];,./~!@#$%^*()_+}{:?]){6,20}$";
		String regStr = "^([A-Z]|[a-z]|[0-9]|[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]){6,20}$";
		return input.matches(regStr);
	}
	/**
	 * 验证金额
	 * @param str
	 * @return
	 */
    public static boolean isSum(String str) {
    	String s="^(-)?(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){1,7})?$";
        java.util.regex.Pattern pattern=java.util.regex.Pattern.compile(s); // 判断小数点后一位的数字的正则表达式
        java.util.regex.Matcher match=pattern.matcher(str);
        if(match.matches()==false){ 
           return false; 
        } else { 
           return true; 
        } 
    }
	/**
	 * 判断电话号码是否符合格式.
	 *
	 * @param inputText the input text
	 * @return true, if is phone
	 */
	public static boolean isPhone(String inputText) {
		Pattern p = Pattern.compile("^((14[0-9])|(13[0-9])|(15[0-9])|(18[0-9])|(17[0-9]))\\d{8}$");
		Matcher m = p.matcher(inputText);
		return m.matches();
	}

	/**
	 * 判断邮编
	 * @param zipString
	 * @return
	 */
	public static boolean isZipNO(String zipString){
		String str = "^[1-9][0-9]{5}$";
		return Pattern.compile(str).matcher(zipString).matches();
	}

	/**
	 * 判断邮箱是否合法
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email){
		if (null==email || "".equals(email)) return false;
		//Pattern p = Pattern.compile("\\w+@(\\w+.)+[a-z]{2,3}"); //简单匹配
		Pattern p =  Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");//复杂匹配
		Matcher m = p.matcher(email);
		return m.matches();
	}
}
