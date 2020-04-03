package com.tydic.bp.util;

import com.tydic.bp.exception.DCFileException;
import com.tydic.bp.exception.ERRORS;

public class DesUtils {

    public static String encode(String text)
    {
        StringBuilder sb = new StringBuilder();
        int len = text.length();
        int[] code = new int[len];

        for (int i = 0; i < len; i++) {
            code[i] = mod((byte)text.charAt(i), 7, 161);
            sb.append(String.format("%02X", new Object[] { Integer.valueOf(code[i]) }));
        }

        return sb.toString();
    }

    public static String decode(String text) throws DCFileException {
        int len = text.length();
        if ((len == 0) || (len % 2 != 0)) {
            throw ERRORS.ERR_INVALID_ARGUMENT.ERROR();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i += 2) {
            if (!Character.isLetterOrDigit(text.charAt(i))) {
                throw ERRORS.ERR_INVALID_ARGUMENT.ERROR();
            }
            int code = mod(Integer.parseInt(text.substring(i, i + 2), 16), 19, 161);
            sb.append((char)code);
        }

        return sb.toString();
    }

    private static int mod(int a, int b, int m) {
        int r = 1;
        for (int j = 0; j < b; j++)
        {
            r = r * a % m;
        }
        return r;
    }
    public static void main(String[] args) {
        System.out.println(encode("dcfile"));
        try {
            System.out.println(decode("3A783C935749"));
        }
        catch (DCFileException e) {
            e.printStackTrace();
        }
    }
}
