package org.text.algorithm.automa;

import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.TreeSet;

/**
 * 用双数组实现的 多个 DFA 查询， 返回匹配的DFA id 列表
 * 我们的正则 . 用字符 \x01 代替， 所以这种情况需要特殊处理(NFA)
 */
@Getter
@Setter
public class TripleArrayDfa {
    private Object[] ids;
    private int[] accepts;
    private DoubleArray array;

    /**
     * 完整匹配 key， 返回匹配到的内部编号
     * @param key
     * @return
     */
    public Collection<Integer> match(String key) {
        return match(key.toCharArray(), 0, key.length());
    }

    /**
     * 从 pos 到end 完整匹配key
     * @param key
     * @param pos
     * @param end
     * @return
     */
    public Collection<Integer> match(String key, int pos, int end) {

        return match(key.toCharArray(), pos, end);
    }

    /**
     * 完整匹配code
     * @param code
     * @return
     */
    public Collection<Integer> match(char[] code) {
        return match(code, 0, code.length);
    }
    public Collection<Integer> match(char[] code, int pos, int end) {
        Collection<Integer> innerPoses = array.matchState(code, pos, end);
        Collection<Integer> r = new TreeSet<>();
        for(int innerPos : innerPoses) {
            int count = accepts[innerPos];
            for(int i = 0; i < count; i++) {
                r.add(accepts[innerPos + i + 1]);
            }
        }
        return r;
    }

    /**
     * 完整匹配，但是返回匹配到的id
     * @param key
     * @return
     */
    public Collection<Object> matchId(String key) {
        return matchId(key.toCharArray(), 0, key.length());
    }
    public Collection<Object> matchId(String key, int pos, int end) {
        return matchId(key.toCharArray(), pos, end);
    }

    public Collection<Object> matchId(char[] code, int pos) {
        return matchId(code, pos, code.length);
    }

    public Collection<Object> matchId(char[] code, int pos, int end) {
        Collection<Integer> innerPoses = array.matchState(code, pos, end);
        Collection<Object> r = new TreeSet<>();
        for(int innerPos : innerPoses) {
            int count = accepts[innerPos];
            for(int i = 0; i < count; i++) {
                r.add(ids[accepts[innerPos + i + 1]]);
            }
        }
        return r;
    }

    public void search(String code, int pos, int end, HitCallback callback) {
        if(end <= 0)
            end = code.length();
        char[] chars = code.toCharArray();
        chars = array.mapChars(chars, pos, end, true);
        do_search(chars, pos, end, callback);
    }

    public void searchAll(String code, int pos, int end, HitCallback callback) {
        if(end <=0) {
            end = code.length();
        }
        char[] chars = code.toCharArray();
        chars = array.mapChars(chars, pos, end, true);
        for(int i = pos; i< end; i++) {
            do_search(chars, i, end, callback);
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
        for(int i = pos; i< end; i++) {
            do_search(code, i, end, callback);
        }
    }



    public void searchId(String code, int pos, int end, HitCallback2 callback) {
        if(end <= 0)
            end = code.length();
        char[] chars = code.toCharArray();
        chars = array.mapChars(chars, pos, end, true);
        do_searchId(chars, pos, end, callback);
    }

    public void searchIdAll(String code, int pos, int end, HitCallback2 callback) {
        if(end <= 0)
            end = code.length();
        char[] chars = code.toCharArray();
        chars = array.mapChars(chars, pos, end, true);
        for(int i = pos; i< end; i++) {
            do_searchId(chars, i, end, callback);
        }
    }

    public void searchIdAll(char[] code, int pos, int end, HitCallback2 callback) {
        if(end <=0) {
            end = code.length;
        }
        code = array.mapChars(code, pos, end, false);
        for(int i = pos; i< end; i++) {
            do_searchId(code, i, end, callback);
        }
    }

    /**
     *
     * @param code 已经map 过了的输入
     * @param pos
     * @param end
     * @param callback
     */
    private void do_search(char[] code, int pos, int end, HitCallback callback) {
        Collection<HitItem> r = array.searchState(code, pos, end);
        for(HitItem item :r) {
            int innerPos = item.id;
            int count = accepts[item.id];
            for(int i = 0; i < count; i++) {
                callback.hit(accepts[innerPos + i + 1], item.start, item.end);
            }
        }
    }

    /**
     *
     * @param code 已经map 过了的输入
     * @param pos
     * @param end
     * @param callback
     */
    private void do_searchId(char[] code, int pos, int end, HitCallback2 callback) {
        Collection<HitItem> r = array.searchState(code, pos, end);
        for(HitItem item :r) {
            int innerPos = item.id;
            int count = accepts[item.id];
            for(int i = 0; i < count; i++) {
                Object id = ids[accepts[innerPos + i + 1]];
                callback.hit(id, item.start, item.end);
            }
        }
    }
}
