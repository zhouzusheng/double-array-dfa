package org.text.algorithm.automa;

import lombok.Getter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 使用hash方法映射字符表，节约内存，但是效率略低
 * charhash 结构
 *  开始处 存放hashsize
 *  然后 是 hashsize+1 个数值，指向value 在 charhash处的开始位置
 *      该hash code 对应的value 个数没有保存，应为 下一个hashcode 的位置 减去本位制就是个数
 *  然后是各value
 *  注意合法的value > 0
 */
public class DoubleArrayCharHash extends DoubleArray {
    @Getter
    protected char[] charhash;

    private transient char hash_size;

    public void setCharhash(char[] charhash) {
        this.charhash = charhash;
        hash_size = charhash[0];
    }

    @Override
    public void write(ObjectOutput out) throws IOException {
        super.write(out);
        out.writeObject(charhash);
    }

    @Override
    public void read(ObjectInput in) throws IOException, ClassNotFoundException {
        super.read(in);
        charhash = (char[])in.readObject();
        hash_size = charhash[0];
    }

    @Override
    public char[] mapChars(char[] code, int pos, int end, boolean in_place) {
        char[] dest = in_place ? code : new char[code.length];
        for(int i = pos; i < end;i++) {
            char ch = code[i];
            if(ch >= min_symbol && ch <= max_symbol)
                dest[i] = mapCode(ch - min_symbol);
            else
                dest[i] = 0;
        }
        return dest;
    }

    public char mapChar(char ch) {
        if(ch >= min_symbol && ch <= max_symbol) {
            return mapCode(ch - min_symbol);
        }
        else
            return 0;
    }

    /**
     * 用 hash 方法映射字符集
     * @param code
     * @return
     */
    private char mapCode(int code) {

        int hash = (code % hash_size) ;
        int off = charhash[hash+1];
        int end = charhash[hash+2];
        if(end == off) {
            //没有value
            return 0;
        } else if(end == (off + 2)) {
            //只有一个value
            return (charhash[off] == code) ? charhash[off+1] : 0;
        } else {
            //多个value，二分查找
            int low = 0;
            int high = (end - off)>>>1;
            while (low <= high) {
                int mid = (low + high) >>> 1;
                int pos = (mid << 1) + off;
                char key = charhash[pos];
                if (key == code) {
                    return charhash[pos + 1];
                } else if (key < code) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }
            return 0;
        }
    }
}
