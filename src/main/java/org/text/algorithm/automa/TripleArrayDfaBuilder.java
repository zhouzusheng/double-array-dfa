package org.text.algorithm.automa;

import java.util.*;

import static org.text.algorithm.utils.PrimeUtil.lessPrimeNumber;

public class TripleArrayDfaBuilder {
    private DfaCompose compose;
    private int[] base;        // indexed by state
    private int[] check;       // indexed by offset
    private int[] accepts;

    private int number_of_states; // length of the table |base|
    private int table_size;       // length of the tables |check|
    private int max_check;
    private int max_state;

    private int min_symbol;    // minimum symbol value
    private int max_symbol;    // maximum symbol value
    private int number_of_symbols;

    private int offset_base;
    private char[] charmap;

    private int[] accepts_map;

    private int max_map_symbol;


    public void build(DfaCompose compose) {
        this.compose = compose;
        init();

        //我们不递归, id 也不是从 0 开始
        int[][] dfaTables = compose.getDfaTables();
        for(int i = 0; i < dfaTables.length; i++){
            insertState(i, dfaTables[i]);
        }
        //合并base check
        fixed();
    }

    public TripleArrayDfa toTripleArrayDfa() {
        TripleArrayDfa dest = new TripleArrayDfa();
        dest.setIds(compose.getIds());
        dest.setAccepts(accepts);
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

    public void build(Collection<Dfa> dfas) {
        build(Dfa.compose(dfas));
    }

    private void init() {
        int[][] dfaTables = compose.getDfaTables();
        base = null;
        check = null;
        number_of_states = dfaTables.length;
        table_size = 0;
        max_check = 0;
        max_state = 0;
        min_symbol = Integer.MAX_VALUE;
        max_symbol = Integer.MIN_VALUE;
        offset_base = 1;

        for(int state = 0; state < dfaTables.length; state++){
            int[] trans = dfaTables[state];
            if(trans != null) {
                for(int i = 0, size= trans.length; i < size; i+=2) {
                    int symbol = trans[i];
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
        for(int state = 0; state < dfaTables.length; state++){
            int[] trans = dfaTables[state];
            if(trans != null) {
                for(int i = 0, size= trans.length; i < size; i+=2) {
                    int symbol = trans[i] - min_symbol;
                    if (charmap[symbol] == 0) {
                        charmap[symbol] = (char)++max_map_symbol;
                    }
                }
            }
        }
        createTables(4 * (number_of_symbols + 1), number_of_states);
        initAccepts();
    }

    private void initAccepts() {
        int[][] dfaAccepts = compose.getDfaAccepts();
        accepts_map = new int[dfaAccepts.length];
        for(int i = 0; i < accepts_map.length; i++) {
            accepts_map[i] = -1;
        }
        Map<TreeSet<Integer>, Integer> tmpMap = new HashMap<>(1000);
        List<Integer> accepts_ids = new ArrayList<>(1000);
        for(int i = 0, size = dfaAccepts.length; i < size; i++) {
            int[] sets = dfaAccepts[i];
            if(sets != null && sets.length > 0) {
                TreeSet<Integer> key = new TreeSet<>();
                for(int id : sets) {
                    key.add(id);
                }
                Integer pos = tmpMap.get(key);
                if(pos == null) {
                    pos = accepts_ids.size();
                    tmpMap.put(key, pos);
                    accepts_ids.add(sets.length);
                    for(int  id : sets) {
                        accepts_ids.add(id);
                    }
                }
                accepts_map[i] = pos;
            }
        }
        accepts = new int[accepts_ids.size()];
        int order = 0;
        for(int id : accepts_ids) {
            accepts[order++] = id;
        }
    }

    private void insertState(int id, int[] trans) {
        if (id > max_state) {
            max_state = id;
        }
        if((trans == null || trans.length == 0) && accepts_map[id] == -1) {
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
                    for(int i = 0, size= trans.length; i < size; i+=2) {
                        int symbol = charmap[trans[i] - min_symbol];
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
                        for(int i = 0, size= trans.length; i < size; i+=2) {
                            int symbol = charmap[trans[i] - min_symbol];
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
        int[][] dfaTables = compose.getDfaTables();

        int[] base = new int[max_check + 1];
        int[] check = new int[max_check + 1];

        int[] old_base = this.base;
        base[0] = old_base[0];
        for(int i = 0; i <= max_state; i++) {
            int offset = old_base[i];
            int[] trans = dfaTables[i];
            int accepts_pos = accepts_map[i];
            if(accepts_pos != -1) {
                base[offset] = -accepts_pos -1;
                check[offset] = offset;
            }
            if(trans != null) {
                for(int t = 0, size= trans.length; t < size; t+=2) {
                    int symbol = charmap[trans[t] - min_symbol];
                    int dest = trans[t+1];
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
