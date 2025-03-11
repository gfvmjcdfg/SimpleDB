package org.example.comon;

/**
 * 用于定义数据库中的简单错误
 */
public class Error {
    //tm
    public static final Exception XID_FILE_FORMAT_ERROR=new RuntimeException("xid文件格式错误");

    //dm

    public static final Exception CacheFullException=new RuntimeException("缓存区已满!");

}
