package org.text.algorithm.automa;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutomaTest {

    static List<String> preparePatterns() {
        List<String> patterns = new ArrayList<>();
        patterns.add("aaa");
        patterns.add("bbb");
        patterns.add("abc");
        patterns.sort((a,b)->a.compareTo(b));
        return patterns;
    }

    static List<String> preparePatternsNum() {
        List<String> patterns = new ArrayList<>();
        patterns.add("123");
        patterns.add("456");
        patterns.add("1423");
        patterns.sort((a,b)->a.compareTo(b));
        return patterns;
    }
    @Test
    public void testStringDfa() {

        Dfa dfa = StringDfa.build(0, 0, preparePatterns());
        System.out.println(dfa.match("bbb", 0));
        System.out.println(dfa.startsWith("bbb is a valid string", 0));

        DoubleArrayDfaBuilder builder = new DoubleArrayDfaBuilder();
        builder.build(dfa);
        DoubleArrayDfa doubleArrayDfa = builder.toDoubleArrayDfa();
        System.out.println(doubleArrayDfa.match("abc"));
        System.out.println(doubleArrayDfa.match("aaa"));
        doubleArrayDfa.searchAll("bbb is a valid string aaa, abc", 0, 0,(id, start, end)->{
            System.out.println("id="+ id +",start=" + start + ",end=" + end);
        });
    }

    @Test
    public void testDfaCompose() {
        Dfa dfa1 = StringDfa.build("a", 0, preparePatterns());
        Dfa dfa2 = StringDfa.build("b", dfa1.getRoot() + dfa1.getStateCount(), preparePatternsNum());
        DfaCompose compose = Dfa.compose(Arrays.asList(dfa1, dfa2));
        System.out.println(compose.matchIds2("123", 0));

        TripleArrayDfaBuilder builder = new TripleArrayDfaBuilder();
        builder.build(compose);
        TripleArrayDfa tripleArrayDfa = builder.toTripleArrayDfa();
        System.out.println(tripleArrayDfa.match("abc"));
        System.out.println(tripleArrayDfa.matchId("abc"));
        tripleArrayDfa.searchIdAll("123 is  a valid string abc", 0, 0, (id, start, end)->{
            System.out.println("id="+ id +",start=" + start + ",end=" + end);
        });
    }

    @Test
    public void testRegex() {
        Dfa dfa = RegexDfa.build("a", 0, "[a我][b他]四?");
        DoubleArrayDfaBuilder builder = new DoubleArrayDfaBuilder();
        builder.build(dfa);
        DoubleArrayDfa doubleArrayDfa = builder.toDoubleArrayDfa();
        System.out.println(doubleArrayDfa.match("我他四"));

        doubleArrayDfa.searchAll("我他四我他四", 0, 0,(id, start, end)->{
            System.out.println("id="+ id +",start=" + start + ",end=" + end);
        });
    }

    @Test
    public void testRegexSet() {
        Dfa dfa1 = RegexDfa.build("a", 0, "[a我]和[b他]都?");
        Dfa dfa2 = RegexDfa.build("b", dfa1.getRoot() + dfa1.getStateCount(), "国(.?)党");
        DfaCompose compose = Dfa.compose(Arrays.asList(dfa1, dfa2));

        compose.search2("我和他都不喜欢国民党", 0, 0, (id, start, end)->{
            System.out.println("id="+ id +",start=" + start + ",end=" + end);
        });

        System.out.println("-----------------------------------");
        TripleArrayDfaBuilder builder = new TripleArrayDfaBuilder();
        builder.build(compose);
        TripleArrayDfa tripleArrayDfa = builder.toTripleArrayDfa();
        tripleArrayDfa.searchIdAll("我和他都不喜欢国民党", 0, 0, (id, start, end)->{
            System.out.println("id="+ id +",start=" + start + ",end=" + end);
        });
    }

}
