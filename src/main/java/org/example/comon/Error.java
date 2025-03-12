package org.example.comon;

/**
 * 用于定义数据库中的简单错误
 */
public class Error {
    //tm
    public static final Exception XID_FILE_FORMAT_ERROR=new RuntimeException("xid文件格式错误");

    //dm
    public static final Exception CacheFullException=new RuntimeException("缓存区已满!");
    public static final Exception FileAlreadyExitsException=new RuntimeException("文件已经存在!");
    public static final Exception FileCannotRWException=new RuntimeException("文件不可读或不可写!");
    public static final Exception MemTooSmallException=new RuntimeException("页面缓存池设置的太小，需要大于10!");

}
