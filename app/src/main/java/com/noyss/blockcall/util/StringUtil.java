package com.noyss.blockcall.util;

public class StringUtil {

    public static String digitTwo(int num){
        if(num < 10){
            return "0" + num;
        }
        return num+"";
    }
}
