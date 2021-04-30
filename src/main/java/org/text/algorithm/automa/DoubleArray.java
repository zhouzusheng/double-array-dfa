package org.text.algorithm.automa;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;

@Getter
@Setter
public abstract class DoubleArray {
    protected int[] base;
    protected int[] check;
    protected int min_symbol;    // minimum symbol value
    protected int max_symbol;    // maximum symbol value

    public void write(ObjectOutput out) throws IOException {
        out.writeObject(base);
        out.writeObject(check);
        out.writeInt(min_symbol);
        out.writeInt(max_symbol);
    }


    public void read(ObjectInput in) throws IOException, ClassNotFoundException {
        base = (int[])in.readObject();
        check = (int[])in.readObject();
        min_symbol = in.readInt();
        max_symbol = in.readInt();
    }

    /**
     * 匹配状态机
     * @param code 原始输入字符串
     * @param pos
     * @param end
     * @return 命中的 数据列表（base 中存储的命中数据，一般是内部id， 多模式情况下，是描述id的位置）
     */
    public Collection<Integer> matchState(char[] code, int pos, int end) {
        if (min_symbol == 1) {
            // 1 代表任意字符，因此我们要特殊处理，查询算法会慢一些
            return matchStateEx(code, pos, end);
        } else {
            return matchStateFast(code, pos, end);
        }
    }

    /**
     * 搜索状态机
     * @param code source 字符串， 必须不是原始字符串，是调用 mapChars 映射过
     * @param pos
     * @param end
     * @return 命中的 数据列表（base 中存储的命中数据，一般是内部id， 多模式情况下，是描述id的位置）
     */
    public Collection<HitItem> searchState(char[] code, int pos, int end) {
        if (min_symbol == 1) {
            // 1 代表任意字符，因此我们要特殊处理，查询算法会慢一些
            return searchStateEx(code, pos, end);
        } else {
            return searchStateFast(code, pos, end);
        }
    }

    /**
     * 映射字符集
     * @param code
     * @return
     */
    public abstract char[] mapChars(char[] code, int pos, int end, boolean in_place);

    /**
     * 映射单个字符
     * @param ch
     * @return
     */
    public abstract char mapChar(char ch);

    /**
     * 快速匹配，没有任意字符
     * @param code
     * @param pos
     * @param end
     * @return
     */
    protected Collection<Integer> matchStateFast(char[] code, int pos, int end) {
        if (end <= 0)
            end = code.length;

        Collection<Integer> r = new ArrayList<>(1);

        int size = base.length;
        if(size == 0) {
            return r;
        }

        int b = base[0];
        int p;
        char symbol;

        for (int i = pos; i < end; i++)
        {
            char ch = code[i];
            symbol = mapChar(ch);
            if(symbol == 0) {
                return r;
            }
            p = b + symbol;
            if (p < size && b == check[p])
                b = base[p];
            else
                return r;
        }

        p = b;
        int n = base[p];
        if (b == check[p] && n < 0)
        {
            int id = -n - 1;
            r.add(id);
        }
        return r;
    }

    protected Collection<Integer> matchStateEx(char[] code, int pos, int end) {
        if (end <= 0)
            end = code.length;
        Collection<Integer> r = new TreeSet<>();
        int size = base.length;
        if(size == 0) {
            return r;
        }

        LinkedList<Integer> pending = new LinkedList<>();
        Set<Integer> temp = new HashSet<>();
        pending.add(base[0]);

        //检查当下是否有任意字符,如果有， 也需要处理
        // 任意字符为1
        char anyChar = mapChar((char)1);

        int p;
        char symbol;
        for (int i = pos; i < end; i++) {
            char ch = code[i];
            while(!pending.isEmpty()) {
                int b = pending.pop();
                symbol = mapChar(ch);
                if(symbol != 0) {
                    p = b + symbol;
                    if (p < size && b == check[p]) {
                        temp.add(base[p]);
                    }
                }
                p = b + anyChar;
                if (p < size && b == check[p]) {
                    temp.add(base[p]);
                }
            }
            if(temp.isEmpty()) {
                return r;
            }
            pending.addAll(temp);
            temp.clear();
        }
        while(!pending.isEmpty()) {
            int b = pending.pop();
            p = b;
            int n = base[p];
            if (b == check[p] && n < 0)
            {
                int id = -n - 1;
                r.add(id);
            }
        }
        return r;
    }

    /**
     * 快速搜索，没有任意字符
     * @param code
     * @param pos
     * @param end
     * @return
     */
    protected Collection<HitItem> searchStateFast(char[] code, int pos, int end) {
        if (end <= 0)
            end = code.length;

        Collection<HitItem> r = new ArrayList<>(1);

        int size = base.length;
        if(size == 0) {
            return r;
        }

        int b = base[0];
        int p;

        for (int i = pos; i < end; i++)
        {
            char symbol = code[i];
            if(symbol == 0) {
                break;
            }
            //
            p = b + symbol;
            if (p < size && b == check[p]) {
                b = base[p];
                int n = base[b];
                if(n < 0) {
                    makeHitItem(r, -n-1, pos, i);
                }
            } else {
                break;
            }
        }
        return r;
    }

    protected Collection<HitItem> searchStateEx(char[] code, int pos, int end) {
        if (end <= 0) {
            end = code.length;
        }

        Collection<HitItem> r = new TreeSet<>(DoubleArray::compareHitItem);
        int size = base.length;
        if(size == 0) {
            return r;
        }
        LinkedList<Integer> pending = new LinkedList<>();
        Set<Integer> temp = new HashSet<>();
        pending.add(base[0]);

        int p;
        //检查当下是否有任意字符,如果有， 也需要处理
        // 任意字符为1
        char anyChar = mapChar((char)1);

        for (int i = pos; i < end; i++)
        {
            char symbol = code[i];
            while(!pending.isEmpty()) {
                int b = pending.pop();
                if(symbol != 0) {
                    p = b + symbol;
                    if (p < size && b == check[p]) {
                        int bb = base[p];
                        temp.add(bb);
                        int n = base[bb];
                        if (n < 0) {
                            makeHitItem(r, -n - 1, pos, i);
                        }
                    }
                }
                //检查任意字符
                p = b + anyChar;
                if (p < size && b == check[p]) {
                    int bb = base[p];
                    temp.add(bb);
                    p = bb;
                    int n = base[p];
                    if(n < 0) {
                        makeHitItem(r, -n-1, pos, i);
                    }
                }
            }
            pending.addAll(temp);
            temp.clear();
        }
        return r;
    }

    private void makeHitItem(Collection<HitItem> r, int id, int start, int i) {
        HitItem item = new HitItem();
        item.id = id;
        item.start = start;
        item.end = i + 1;
        r.add(item);
    }

    public static int compareHitItem(HitItem a, HitItem b) {
        int r  = Integer.compare(a.start, b.start);
        if(r == 0) {
            r  = Integer.compare(a.id, b.id);
            if(r == 0) {
                r  = Integer.compare(a.end, b.end);
            }
        }
        return r;
    }
}
