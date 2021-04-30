package org.text.algorithm.automa;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 字节使用一张字符表映射字符集，效率最快，但相当浪费内存，特别是字符集很小的时候
 */
public class DoubleArrayCharMap extends DoubleArray {
    @Getter
    @Setter
    protected char[] charmap;

    @Override
    public void write(ObjectOutput out) throws IOException {
        super.write(out);
        out.writeObject(charmap);
    }

    @Override
    public void read(ObjectInput in) throws IOException, ClassNotFoundException {
        super.read(in);
        charmap = (char[])in.readObject();
    }

    @Override
    public char[] mapChars(char[] code, int pos, int end, boolean in_place) {
        char[] dest = in_place ? code : new char[code.length];
        for(int i = pos; i < end;i++) {
            char ch = code[i];
            if(ch >= min_symbol && ch <= max_symbol)
                dest[i] = charmap[ch - min_symbol];
            else
                dest[i] = 0;
        }
        return dest;
    }

    public char mapChar(char ch) {
        if(ch >= min_symbol && ch <= max_symbol)
            return charmap[ch - min_symbol];
        else
            return 0;
    }
}
