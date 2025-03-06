package org.example.backend.utils;


public class ProgramExit {
    /**
     * 提供程序终止功能，当出现如xid文件不合法时会将其程序终止
     * @param e
     */
    public static void programExit(Exception e){
        e.printStackTrace();
        System.exit(1);
    }
}
