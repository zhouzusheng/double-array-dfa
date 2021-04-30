package org.text.algorithm.automa;

import java.util.TreeSet;


public class CharHash {
    private static class Entry {
        char key;
        char value;

        public Entry(char k, char v) {
            this.key = k;
            this.value = v;
        }
    }
    private static int compareEntry(Entry a, Entry b) {
        int code = Character.compare(a.key, b.key);
        if(code == 0) {
            code = Character.compare(a.value, b.value);
        }
        return code;
    }

    /**
     * 使用hash方法映射字符表，节约内存，但是效率略低
     * charhash 结构
     *  开始处 存放hashsize
     *  然后 是 hashsize+1 个数值，指向value 在 charhash处的开始位置
     *      该hash code 对应的value 个数没有保存，应为 下一个hashcode 的位置 减去本位制就是个数
     *  然后是各value
     *  注意合法的 value > 0
     */
    public static char[] build(char[] charmap, int hash_size) {
        TreeSet<Entry>[] hash_table = new TreeSet[hash_size];
        int number_of_value = 0;
        for(char i = 0,size = (char)charmap.length; i < size;i++) {
            char v = charmap[i];
            if(v != 0) {
                number_of_value++;
                int hash_code = i % hash_size;
                TreeSet<Entry> set = hash_table[hash_code];
                if(set == null) {
                    set = new TreeSet<>(CharHash::compareEntry);
                    hash_table[hash_code] = set;
                }
                set.add(new Entry(i, v));
            }
        }
        //compress
        char[] result = new char[hash_size + number_of_value * 2 + 2];
        result[0] = (char)hash_size;

        char hash_offset = (char)(hash_size + 2) ;
        for(int i = 0; i < hash_size; i++) {
            TreeSet<Entry> set = hash_table[i];
            result[i+1] = hash_offset;
            if(set != null) {
                for(Entry e: set) {
                    result[hash_offset++] = e.key;
                    result[hash_offset++] = e.value;
                }
            }
        }
        result[hash_size+1] = hash_offset;
        return result;
    }
}
