package com.example.lab1_javacfg.model.cfg;

public class CFGConnection {
    private final CFGNode first, second;

    private final String edgeColor;

    public CFGConnection(CFGNode first, CFGNode second, String edgeColor) {
        this.first = first;
        this.second = second;
        this.edgeColor = edgeColor;
    }

    @Override
    public String toString() {
        return first.getIndex() + "->" + second.getIndex() + "[color=\"" + edgeColor + "\"]";
    }

}
