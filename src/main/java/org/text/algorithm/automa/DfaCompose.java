package org.text.algorithm.automa;

import org.text.algorithm.utils.ArrayUtil;

import java.util.*;

/**
 * 多个DFA 合并为一个DFA
 * 总的DFA的根以 0 开始
 */
public class DfaCompose {

    /**
     * 多个dfa的并集， ids 保存每个dfa 的 id
     */
    private Object[] ids;

    /**
     * 所有单dfa 编译成的整体DFA的状态表
     * 第一维是所有的状态
     * 第二维 代表每个单状态的转换边， 是二元组格式，因此大小是该状态的边个数*2
     * 二元组含义： 边 转换到的目标状态
     **/
    private int[][] dfaTables;

    /**
     * 每个状态匹配完成的词Id
     * 第一维是所有的状态
     * 第二维是该状态匹配到的词id列表
     */
    private int[][] dfaAccepts;

    public Object[] getIds() {
        return ids;
    }

    public void setIds(Object[] ids) {
        this.ids = ids;
    }

    public int[][] getDfaTables() {
        return dfaTables;
    }

    public void setDfaTables(int[][] dfaTables) {
        this.dfaTables = dfaTables;
    }

    public int[][] getDfaAccepts() {
        return dfaAccepts;
    }

    public void setDfaAccepts(int[][] dfaAccepts) {
        this.dfaAccepts = dfaAccepts;
    }

    //完整匹配时的id
    public Set<Integer> matchIds(String text, int start) {
        Set<Integer> r = new TreeSet<>();
        Collection<Integer> endStates = matchState(text, start);
        for(int state : endStates) {
            if (state != -1) {
                int[] ids = dfaAccepts[state];
                if (ids != null) {
                    for (Integer id : ids) {
                        r.add(id);
                    }
                }
            }
        }
        return r;
    }

    public Set<Object> matchIds2(String text, int start) {
        Set<Object> r = new TreeSet<>();
        Collection<Integer> endStates = matchState(text, start);
        for(int state : endStates) {
            int[] ids = dfaAccepts[state];
            if (ids != null) {
                Object[] dfaIds = this.getIds();
                for (Integer id : ids) {
                    r.add(dfaIds[id]);
                }
            }
        }
        return r;
    }

    /**
     * 部分匹配
     *
     * @param text
     * @param start
     * @return
     */
    public void search(String text, int start, int end, HitCallback callback) {
        Collection<HitItem> r = searchStates(text, start, end);
        for(HitItem item : r) {
            callback.hit(item.id, item.start, item.end);
        }

    }

    /**
     * 部分匹配
     *
     * @param text
     * @param start
     * @return
     */
    public void search2(String text, int start, int end, HitCallback2 callback) {
        Collection<HitItem> r = searchStates(text, start, end);
        Object[] dfaIds = getIds();
        for(HitItem item : r) {
            callback.hit(dfaIds[item.id], item.start, item.end);
        }
    }

    /**
     * 匹配状态，需要考虑一下任意文本
     * @param text
     * @param start
     * @return
     */
    private Collection<Integer> matchState(String text, int start) {
        LinkedList<Integer> pending = new LinkedList<>();
        Set<Integer> temp = new HashSet<>();
        pending.add(0);
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            while(!pending.isEmpty()) {
                int state = pending.pop();
                int[] table = dfaTables[state];
                if (table == null) {
                    break;
                }
                int destState = ArrayUtil.binSearch(table, ch, -1);
                if(destState != -1) {
                    temp.add(destState);
                }
                destState = ArrayUtil.binSearch(table, 1, -1);
                if(destState != -1) {
                    temp.add(destState);
                }
            }
            pending.addAll(temp);
            temp.clear();
        }
        return pending;
    }

    private Collection<HitItem> searchStates(String text, int start, int end) {
        if (end <= 0) {
            end = text.length();
        }

        Collection<HitItem> r = new TreeSet<>(DoubleArray::compareHitItem);

        LinkedList<Integer> pending = new LinkedList<>();
        Set<Integer> temp = new HashSet<>();

        int limit = Math.min(text.length(), end);
        for (int i = start; i < limit; i++) {
            pending.clear();
            pending.add(0);
            for (int j = i; j < limit; j++) {
                char ch = text.charAt(j);
                while(!pending.isEmpty()) {
                    int state = pending.pop();
                    int[] table = dfaTables[state];
                    if(table == null) {
                        break;
                    }
                    int destState = ArrayUtil.binSearch(table, ch, -1);
                    if(destState != -1) {
                        temp.add(destState);
                        addHitItem(r, dfaAccepts[destState],i,j);
                    }
                    destState = ArrayUtil.binSearch(table, 1, -1);
                    if(destState != -1) {
                        temp.add(destState);
                        addHitItem(r, dfaAccepts[destState], i, j);
                    }
                }
                pending.addAll(temp);
                temp.clear();
            }
        }
        return r;
    }

    private void addHitItem(Collection<HitItem> r, int[] ids, int i, int j) {
        if(ids != null) {
            for (int id : ids) {
                HitItem item = new HitItem();
                item.id = id;
                item.start = i;
                item.end = j + 1;
                r.add(item);
            }
        }
    }
}


