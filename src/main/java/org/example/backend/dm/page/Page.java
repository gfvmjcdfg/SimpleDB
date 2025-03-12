package org.example.backend.dm.page;

/**
 * 定义了我们页面的一些共有属性，比如页面的大小，db文件的后缀名
 */
public interface Page {
    //将缓存中页面大小定为8k
    public static int PAGE_SIZE=1 << 13;

    //定义磁盘上存储页面的文件名后缀
    public static final String DB_SUFFIX=".db";
}
