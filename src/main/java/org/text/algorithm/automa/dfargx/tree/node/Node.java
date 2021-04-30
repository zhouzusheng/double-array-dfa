package org.text.algorithm.automa.dfargx.tree.node;

import org.text.algorithm.automa.dfargx.automata.NFA;
import org.text.algorithm.automa.dfargx.stack.OperatingStack;
import org.text.algorithm.automa.dfargx.stack.ShuntingStack;

/**
 * Created on 5/5/15.
 */
public abstract class Node {

    private Node left;
    private Node right;

    public Node() {
        left = right = null;
    }

    public Node right() {
        return right;
    }

    public Node left() {
        return left;
    }

    public boolean hasLeft() {
        return left != null;
    }

    public boolean hasRight() {
        return right != null;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public abstract void accept(NFA nfa);

    public abstract Node copy();

    public abstract void accept(OperatingStack operatingStack);

    public abstract void accept(ShuntingStack shuntingStack);

    @Override
    public abstract String toString();
}
