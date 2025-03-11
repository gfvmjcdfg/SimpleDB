package org.example.backend.common;

/**
 * 共享数组的实现
 * @param <T>
 */
public class SubArray<T> {
    T[] objs;

    int start;

    int end;

    public SubArray(T[] objs,int start,int end){
        this.objs=objs;
        this.start=start;
        this.end=end;
    }
}
