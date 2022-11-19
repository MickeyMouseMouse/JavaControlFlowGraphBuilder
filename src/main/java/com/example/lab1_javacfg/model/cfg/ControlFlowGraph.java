package com.example.lab1_javacfg.model.cfg;

import java.util.ArrayList;

public class ControlFlowGraph {
    private int nodeCounter = 0;

    private final ArrayList<CFGNode> nodes = new ArrayList<>();

    private final ArrayList<CFGConnection> connections = new ArrayList<>();

    private final ArrayList<CFGNode> leaves = new ArrayList<>();
    private final ArrayList<CFGNode> breakNodes = new ArrayList<>();
    private final ArrayList<CFGNode> continueNodes = new ArrayList<>();

    public ControlFlowGraph() {}

    public int getNodeCounter() { return nodeCounter; }

    public ArrayList<CFGNode> getNodes() { return nodes; }

    public CFGNode getLastNode() {
        if (nodes.isEmpty())
            return null;
        else
            return nodes.get(nodes.size() - 1);
    }

    public ArrayList<CFGConnection> getConnections() { return connections; }

    public ArrayList<CFGNode> getBreakNodes() { return breakNodes; }

    public ArrayList<CFGNode> getContinueNodes() { return continueNodes; }

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

    public void removeLeave(CFGNode node) { this.leaves.remove(node); }

    public void addLeave(CFGNode node) { leaves.add(node); }

    public void addLeaves(ArrayList<CFGNode> nodes) { leaves.addAll(nodes); }

    public void clearBreakNodes() { this.breakNodes.clear(); }

    public void addBreakNode(CFGNode node) { breakNodes.add(node); }

    public void addBreakNodes(ArrayList<CFGNode> nodes) { breakNodes.addAll(nodes); }

    public void clearContinueNodes() { this.continueNodes.clear(); }

    public void addContinueNode(CFGNode node) { continueNodes.add(node); }

    public void addContinueNodes(ArrayList<CFGNode> nodes) { continueNodes.addAll(nodes); }

    private void updateNodeIndexes(int startWith) {
        for (CFGNode node: nodes)
            node.setIndex(startWith++);
    }

    public void plus(ControlFlowGraph other) {
        plusCFG(other, "black");
    }

    public void plus(ControlFlowGraph other, String edgeColor) {
        plusCFG(other, edgeColor);
    }

    private void plusCFG(ControlFlowGraph other, String edgeColor) {
        if (!this.getNodes().isEmpty() && this.getLeaves().isEmpty() &&
                this.getContinueNodes().isEmpty()) return; // no leaves
        if (other.getNodes().isEmpty()) return; // nothing to connect (no nodes)

        other.updateNodeIndexes(this.nodeCounter + 1);
        this.nodes.addAll(other.getNodes());
        this.connections.addAll(other.getConnections());
        for (CFGNode leave: this.leaves)
            this.addConnection(leave, this.nodes.get(this.nodeCounter), edgeColor);
        this.nodeCounter += other.getNodeCounter();
        this.clearLeaves();
        this.addLeaves(other.getLeaves());
        this.addBreakNodes(other.getBreakNodes());
        this.addContinueNodes(other.getContinueNodes());
    }
}
