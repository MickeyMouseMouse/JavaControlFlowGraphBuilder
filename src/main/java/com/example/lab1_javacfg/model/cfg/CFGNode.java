package com.example.lab1_javacfg.model.cfg;

import java.util.Objects;

public class CFGNode {
    private int index;
    private final String label, shape;

    public CFGNode(int index, String label, String shape) {
        this.index = index;
        this.label = label.replaceAll("\"", "\\\\\""); // example: b < 2
        this.shape = shape; // example: diamond
    }

    public int getIndex() { return index; }

    public String getLabel() { return label; }

    public void setIndex(int index) { this.index = index; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CFGNode graphNode = (CFGNode) o;
        return Objects.equals(index, graphNode.getIndex());
    }

    @Override
    public String toString() {
        return index + "[label=\"" + label + "\",shape=\"" + shape + "\"]";
    }
}
