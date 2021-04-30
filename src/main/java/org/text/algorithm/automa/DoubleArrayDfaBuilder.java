package org.text.algorithm.automa;

import java.util.Map;
import java.util.TreeMap;

import static org.text.algorithm.utils.PrimeUtil.lessPrimeNumber;

public class DoubleArrayDfaBuilder {

    private Dfa dfa;
    private int[] base;        // indexed by state
    private int[] check;       // indexed by offset
    private int number_of_states; // length of the table |base|
    private int table_size;       // length of the tables |check|
    private int max_check;
    private int max_state;

    private int min_symbol;    // minimum symbol value
    private int max_symbol;    // maximum symbol value
    private int number_of_symbols;

    private int max_map_symbol;

    private int offset_base;
    private char[] charmap;

    public void build(Dfa dfa) {
        this.dfa = dfa;
        init();

        //我们不递归, id 也不是从 0 开始
        for(Dfa.DfaState state : dfa.getStates().values()){
            insertState(state);
        }
        //合并base check
        fixed();
    }

    public DoubleArrayDfa toDoubleArrayDfa() {
        DoubleArrayDfa dest = new DoubleArrayDfa();
        dest.setId(dfa.getId());

        if((max_map_symbol * 50) > number_of_symbols) {
            DoubleArrayCharMap array = new DoubleArrayCharMap();
            array.setCharmap(charmap);
            array.setBase(base);
            array.setCheck(check);
            array.setMin_symbol(min_symbol);
            array.setMax_symbol(max_symbol);
            dest.setArray(array);
        } else {
            int hash_size = lessPrimeNumber(max_map_symbol * 10+1);

            char[] hash_table = CharHash.build(charmap, hash_size);
            DoubleArrayCharHash array = new DoubleArrayCharHash();
            array.setCharhash(hash_table);
            array.setBase(base);
            array.setCheck(check);
            array.setMin_symbol(min_symbol);
            array.setMax_symbol(max_symbol);
            dest.setArray(array);
        }

        return dest;
    }

    private void init() {
        base = null;
        check = null;
        number_of_states = dfa.getStateCount();
        table_size = 0;
        max_check = 0;
        max_state = 0;
        min_symbol = Integer.MAX_VALUE;
        max_symbol = Integer.MIN_VALUE;
        offset_base = 1;

        for(Dfa.DfaState state : dfa.getStates().values()){
            TreeMap<Integer, Integer> trans =  state.getTrans();
            if(trans != null) {
                for(int symbol : trans.keySet()) {
                    if (symbol > max_symbol) {
                        max_symbol = symbol;
                    }
                    if (symbol < min_symbol) {
                        min_symbol = symbol;
                    }
                }
            }
        }
        number_of_symbols = max_symbol - min_symbol + 1;
        charmap = new char[number_of_symbols];
        max_map_symbol = 0;
        for(Dfa.DfaState state : dfa.getStates().values()) {
            TreeMap<Integer, Integer> trans = state.getTrans();
            if (trans != null) {
                for (int symbol : trans.keySet()) {
                    symbol = symbol - min_symbol;
                    if(charmap[symbol] == 0)
                        charmap[symbol] = (char)++max_map_symbol;
                }
            }
        }
        createTables(4 * (number_of_symbols + 1), number_of_states);
    }

    private void insertState(Dfa.DfaState state) {
        int id = state.id - dfa.getRoot();
        if (id > max_state) {
            max_state = id;
        }
        TreeMap<Integer, Integer> trans = state.getTrans();
        if((trans == null || trans.isEmpty()) && !state.isAccept()) {
            //不应该存在
            return;
        }
        {
            //offset 的 起始位置
            int i, limit;
            for (i = offset_base, limit = table_size; i < limit; i++) {
                if (check[i] == 0)
                    break;
            }
            offset_base = i;
        }

        int offset = Math.max(-min_symbol + offset_base, 1);
        for (; ; ) {
            int limit = table_size - max_symbol - 1;
            if (limit <= offset) {
                grow_tables(table_size * 3 / 2 + offset - limit);
            }

            for (; offset < limit; offset++) {
                boolean try_again =  check[offset] != 0;
                if(trans != null && !try_again) {
                    for(int symbol : trans.keySet()) {
                        symbol = charmap[symbol - min_symbol];
                        if (check[offset + symbol] != 0) {
                            try_again = true;
                            break;
                        }
                    }
                }
                if (!try_again) {
                    base[id] = offset;
                    check[offset] = id + 1;
                    if (offset > max_check) {
                        max_check = offset;
                    }

                    if(trans != null) {
                        for(Map.Entry<Integer,Integer> entry : trans.entrySet()) {
                            int symbol = entry.getKey();
                            symbol = charmap[symbol - min_symbol];
                            check[offset + symbol] = id + 1;
                            if (offset + symbol > max_check) {
                                max_check = offset + symbol;
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    private void fixed() {
        int[] base = new int[max_check + 1];
        int[] check = new int[max_check + 1];

        int[] old_base = this.base;
        base[0] = old_base[0];
        for(int i = 0; i <= max_state; i++) {
            int offset = old_base[i];
            Dfa.DfaState state = dfa.getStates().get(i);
            if(state.isAccept()) {
                base[offset] = -1;
                check[offset] = offset;
            }
            TreeMap<Integer, Integer> trans = state.getTrans();
            if(trans != null) {
                for(Map.Entry<Integer,Integer> entry : trans.entrySet()) {
                    int symbol = entry.getKey();
                    symbol = charmap[symbol - min_symbol];
                    int dest = entry.getValue();
                    check[offset + symbol] = offset;
                    base[offset + symbol] = old_base[dest];
                }
            }
        }
        this.check = check;
        this.base = base;
    }

    private void createTables(int size, int states) {
        table_size = size;
        number_of_states = states;
        max_check = 0;
        max_state = 0;
        base = new int[number_of_states];
        check = new int[table_size];
    }

    private void grow_tables(int increment)
    {
        int[] new_check = new int [ table_size + increment ];
        System.arraycopy(check, 0, new_check, 0, check.length);

        check = new_check;
        table_size += increment;
    }
}
