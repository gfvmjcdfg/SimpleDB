package org.example.backend.tm;

public interface TransactionManager {
    long begin();   //创建一个事务
    boolean commit(long xID);   //提交一个事务
    boolean abort(long xID);    //取消一个事务
    boolean isActive(long xID); //判断事务是否是运行状态
    boolean isCommit(long xID); //判断事务是否已经提交
    boolean isAborted(long xID);    //判断事务是否取消
    void close();   //关闭TM
}
