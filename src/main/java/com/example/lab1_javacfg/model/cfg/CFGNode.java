package com.example.lab1_javacfg.model.cfg;

public class CFGNode {
    private int index;
    private final String label, shape;

    public CFGNode(int index, String label, String shape) {
        this.index = index;
        this.label = label.replaceAll("\"", "\\\\\""); // example: b < 2
        this.shape = shape; // example: diamond
    }

    public int getIndex() { return index; }

    public void setIndex(int index) { this.index = index; }

    @Override
    public String toString() {
        return index + "[label=\"" + label + "\",shape=\"" + shape + "\"]";
    }
}
