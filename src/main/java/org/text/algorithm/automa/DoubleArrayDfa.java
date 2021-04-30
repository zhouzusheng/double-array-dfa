package org.text.algorithm.automa;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

/**
 * 用双数组实现的 DFA 查询
 * 我们的正则 . 用字符 \x01 代替， 所以这种情况需要特殊处理(NFA)
 */

@Getter
@Setter
public class DoubleArrayDfa {

    private Object id;
    private DoubleArray array;

    public boolean match(String key) {
        return match(key.toCharArray(), 0, key.length());
    }
    public boolean match(String key, int pos, int end) {

        return match(key.toCharArray(), pos, end);
    }

    public boolean match(char[] code) {
        return match(code, 0, code.length);
    }

    public boolean match(char[] code, int pos, int end) {
        return !array.matchState(code, pos, end).isEmpty();
    }

    public void search(String code, int pos, int end, HitCallback callback) {
        if(end <=0) {
            end = code.length();
        }
        char[] dest = code.toCharArray();
        dest = array.mapChars(dest, pos, end, true);
        do_search(dest, pos, end, callback);
    }

    public void searchAll(String code, int pos, int end, HitCallback callback) {
        if(end <=0) {
            end = code.length();
        }
        char[] dest = code.toCharArray();
        dest = array.mapChars(dest, pos, end, true);

        for(int i = pos;i < end; i++) {
            do_search(dest, i, end, callback);
        }
    }

    public void search(char[] code, int pos, int end, HitCallback callback) {
        if(end <=0) {
            end = code.length;
        }
        code = array.mapChars(code, pos, end, false);
        do_search(code, pos, end, callback);
    }

    public void searchAll(char[] code, int pos, int end, HitCallback callback) {
        if(end <=0) {
            end = code.length;
        }
        code = array.mapChars(code, pos, end, false);
        for(int i = pos;i < end; i++) {
            do_search(code, i, end, callback);
        }
    }

    private void do_search(char[] code, int pos, int end, HitCallback callback) {
        Collection<HitItem> r = array.searchState(code, pos, end);
        for(HitItem item :r) {
            callback.hit(item.id, item.start, item.end);
        }
    }
}
