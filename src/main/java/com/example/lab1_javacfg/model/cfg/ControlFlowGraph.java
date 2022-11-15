package com.example.lab1_javacfg.model.cfg;

import java.util.ArrayList;

public class ControlFlowGraph {
    private int nodeCounter = 0;

    private final ArrayList<CFGNode> nodes = new ArrayList<>();

    private final ArrayList<CFGConnection> connections = new ArrayList<>();

    private final ArrayList<CFGNode> leaves = new ArrayList<>();

    public ControlFlowGraph() {}

    public int getNodeCounter() { return nodeCounter; }

    public ArrayList<CFGNode> getNodes() { return nodes; }

    public ArrayList<CFGConnection> getConnections() { return connections; }

    public CFGNode addNode(String label, String shape) {
       CFGNode newNode = new CFGNode(++nodeCounter, label, shape);
       nodes.add(newNode);
       return newNode;
    }

    public void addConnection(CFGNode node1, CFGNode node2, String color) {
       connections.add(new CFGConnection(node1, node2, color));
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("digraph{");
        for (CFGNode node: nodes) {
            str.append(node.toString());
        }
        for (CFGConnection connection: connections) {
            str.append(connection.toString());
        }
        str.append("}");
        return str.toString();
    }

    public ArrayList<CFGNode> getLeaves() { return leaves; }

    public void clearLeaves() { this.leaves.clear(); }

    public void addLeave(CFGNode node) { leaves.add(node); }

    public void addLeaves(ArrayList<CFGNode> nodes) { leaves.addAll(nodes); }

    private void updateNodeIndexes(int startWith) {
        for (CFGNode node: nodes)
            node.setIndex(startWith++);
    }

    public boolean plus(ControlFlowGraph other) {
        return plusCFG(other, "black");
    }

    public boolean plus(ControlFlowGraph other, String edgeColor) {
        return plusCFG(other, edgeColor);
    }

    private boolean plusCFG(ControlFlowGraph other, String edgeColor) {
        // there are nodes, but no leaves (example: {a=1; return; b=2;} )
        if (!this.nodes.isEmpty() && this.leaves.isEmpty()) return false;
        // nothing to connect
        if (other.getNodes().isEmpty()) return false;

        other.updateNodeIndexes(this.nodeCounter + 1);
        this.nodes.addAll(other.getNodes());
        this.connections.addAll(other.getConnections());
        for (CFGNode leave: this.leaves)
            this.addConnection(leave, this.nodes.get(this.nodeCounter), edgeColor);
        this.nodeCounter += other.getNodeCounter();
        this.clearLeaves();
        this.getLeaves().addAll(other.getLeaves());
        return true;
    }
}
