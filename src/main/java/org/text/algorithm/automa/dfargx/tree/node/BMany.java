package org.text.algorithm.automa.dfargx.tree.node;

import org.text.algorithm.automa.dfargx.automata.NFA;
import org.text.algorithm.automa.dfargx.stack.OperatingStack;
import org.text.algorithm.automa.dfargx.stack.ShuntingStack;

/**
 * Created on 2015/5/10.
 */
public class BMany extends BranchNode {
    @Override
    public String toString() {
        return "[M]";
    }

    @Override
    public void accept(NFA nfa) {
        nfa.visit(this);
    }

    @Override
    public Node copy() {
        return new BMany();
    }

    @Override
    public void accept(OperatingStack operatingStack) {
        operatingStack.visit(this);
    }

    @Override
    public void accept(ShuntingStack shuntingStack) {
        shuntingStack.visit(this);
    }

    @Override
    public int getPri() {
        return 2;
    }
}
