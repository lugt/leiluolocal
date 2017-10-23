package earth.server.utils;

import earth.server.Constant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Frapo on 2017/1/22.
 */
public class Verifier {
    /**
     * 手机号验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isMobile(String str) {
        Pattern p = null;
        Matcher m = null;
        boolean b = false;
        p = Pattern.compile("^[1][3,4,5,8][0-9]{9}$"); // 验证手机号
        m = p.matcher(str);
        b = m.matches();
        return b;
    }

    /**
     * 电话号码验证
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isPhone(String str) {
        Pattern p1 = null, p2 = null;
        Matcher m = null;
        boolean b = false;
        p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的
        if (str.length() > 9) {
            m = p1.matcher(str);
            b = m.matches();
        } else {
            m = p2.matcher(str);
            b = m.matches();
        }
        return b;
    }

    public static boolean isValidH64(String s) {
        try {
            return doCheckBase64(s);
        } catch (Exception e) {
            return false;
        }
    }

    public static final char[] BASE64_CODE = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', '+', '/', '='};

    public static final int MAX_BUFF_SIZE = 4000000;

    public static boolean doCheckBase64(String s) throws Exception {
        // 检查最后两个字节
        final byte[] src = s.getBytes();
        // 等号个数
        int equalsNum = 0;
        for (int i = 0; i < src.length; i++) {
            final char c = (char) src[i];
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '+' || c == '/') {
                if (equalsNum > 0) return false;
                continue;
            }

            if (i >= src.length - 2 && c == '=') {
                equalsNum++;
                continue;
            }

            return false;
        }

        if ((src.length - equalsNum) % 4 != 0) {
            // 不做长度校验
            //return false;
        }

        return true;
    }

    public static boolean isValidEtid(long h) {
        return !(h <= Constant.MINIMAL_ETID || h >= Constant.MAX_ETID);
    }


    public static boolean isValidB69(String s) {
        try{
            final byte[] src = s.getBytes();
            // 等号个数
            for (int i = 0; i < src.length; i++) {
                final char c = (char) src[i];
                if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '+' || c == '/' || c == '[' || c == ']' || c == '(' || c == ')' || c == '.') {
                    continue;
                }
                return false;
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }

    public static boolean isValidB64(String s) {
        try{
            final byte[] src = s.getBytes();
            // 等号个数
            int equalsNum = 0;
            for (int i = 0; i < src.length; i++) {
                final char c = (char) src[i];
                if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c == '+' || c == '/') {
                    if (equalsNum > 0) return false;
                    continue;
                }

                if (i >= src.length - 2 && c == '=') {
                    equalsNum++;
                    continue;
                }

                return false;
            }
            return true;
        }catch (Exception e){
            return false;
        }
    }
}
