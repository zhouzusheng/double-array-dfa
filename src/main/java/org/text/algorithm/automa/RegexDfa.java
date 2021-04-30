package org.text.algorithm.automa;

import org.text.algorithm.automa.dfargx.automata.DFA;
import org.text.algorithm.automa.dfargx.automata.NFA;
import org.text.algorithm.automa.dfargx.tree.SyntaxTree;

/**
 * 简易的 dfa 风格的正则
 * 支持简单的语法，不能支持回溯
 */
public class RegexDfa {
    public static Dfa build(Object id, int rootId, String regex) {
        SyntaxTree syntaxTree = new SyntaxTree(regex);
        NFA nfa = new NFA(syntaxTree.getRoot());
        DFA dfa = new DFA(nfa.asBitmapStateManager(), rootId);
        dfa.getDfa().setId(id);
        return dfa.getDfa();
    }
}
