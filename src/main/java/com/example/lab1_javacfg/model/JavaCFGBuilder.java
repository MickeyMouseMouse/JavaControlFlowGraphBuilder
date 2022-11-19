package com.example.lab1_javacfg.model;

import com.example.lab1_javacfg.model.cfg.ControlFlowGraph;
import com.example.lab1_javacfg.model.cfg.CFGNode;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JavaCFGBuilder {
    public static String getCFGDescription(String code) throws ParseProblemException {
        CompilationUnit ast = StaticJavaParser.parse(code);
        return methodProcessing((MethodDeclaration) ast
                .getChildNodes().get(0)
                .getChildNodes().get(2)).toString();
    }

    private static ControlFlowGraph methodProcessing(MethodDeclaration method) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        StringBuilder methodName = new StringBuilder();
        methodName.append(String.format("%s %s(", method.getType(), method.getName()));
        for(int i = 0; i < method.getParameters().size(); i++) {
            if (i != 0) methodName.append(", ");
            methodName.append(method.getParameters().get(i).toString());
        }
        methodName.append(")");
        CFGNode methodNameNode = cfg.addNode(methodName.toString(), "oval");
        cfg.addLeave(methodNameNode);

        if (method.getBody().isPresent()) {
            cfg.plus(nestedBlockProcessing(method.getBody().get().getChildNodes()));
        }

        return cfg;
    }

    private static ControlFlowGraph nestedBlockProcessing(List<Node> block) {
        return getNestedBlockCFG(block, null);
    }

    private static ControlFlowGraph nestedBlockProcessing(List<Node> block, ControlFlowGraph superCFG) {
        return getNestedBlockCFG(block, superCFG);
    }

    private static ControlFlowGraph getNestedBlockCFG(List<Node> block, ControlFlowGraph superCFG) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        boolean break_or_continue = false;
        for (Node node: block) {
            if (break_or_continue) break;
            switch (node.getClass().getSimpleName()) {
                case "BlockStmt" ->
                    cfg.plus(nestedBlockProcessing(node.getChildNodes()));
                case "ExpressionStmt" ->
                    cfg.plus(expressionProcessing(node.getChildNodes()));
                case "UnaryExpr" ->
                    cfg.plus(expressionProcessing(new ArrayList<>(List.of(node))));
                case "IfStmt" ->
                    cfg.plus(ifProcessing((IfStmt) node));
                case "ReturnStmt" ->
                    cfg.plus(returnProcessing((ReturnStmt) node));
                case "ForStmt" ->
                    cfg.plus(forProcessing((ForStmt) node));
                case "WhileStmt" ->
                    cfg.plus(whileProcessing((WhileStmt) node));
                case "BreakStmt" -> {
                    if (superCFG != null) {
                        CFGNode previousNode = cfg.getLastNode();
                        if (previousNode == null) { // example: while (a == 1) { break; }
                            previousNode = superCFG.getNodes().get(0);
                            superCFG.addBreakNode(previousNode);
                            superCFG.removeLeave(previousNode);
                        } else { // example: while (a == 1) { b = 2; break; }
                            cfg.addBreakNode(previousNode);
                            cfg.clearLeaves();
                        }
                        break_or_continue = true;
                    }
                }
                case "ContinueStmt" -> {
                    if (superCFG != null) {
                        CFGNode previousNode = cfg.getLastNode();
                        if (previousNode == null) { // example: while (a == 1) { continue; }
                            previousNode = superCFG.getNodes().get(0);
                            superCFG.addContinueNode(previousNode);
                            superCFG.removeLeave(previousNode);
                        } else { // example: while (a == 1) { b = 2; continue; }
                            cfg.addContinueNode(previousNode);
                            cfg.clearLeaves();
                        }
                        break_or_continue = true;
                    }
                }
                case "SwitchStmt" ->
                    cfg.plus(switchProcessing((SwitchStmt) node));
            }
        }
        return cfg;
    }

    private static ControlFlowGraph expressionProcessing(List<Node> expression) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        for (Node item: expression) {
            switch (item.getClass().getSimpleName()) {
                case "VariableDeclarationExpr" -> {
                    ArrayList<CFGNode> nodes = new ArrayList<>();
                    for (Node varDecExpr: item.getChildNodes())
                        nodes.add(cfg.addNode(varDecExpr.toString(), "box"));
                    for(int i = 0; i < nodes.size() - 1; i++)
                        cfg.addConnection(nodes.get(i), nodes.get(i + 1), "");
                    cfg.addLeave(nodes.get(nodes.size() - 1));
                }
                case "AssignExpr", "UnaryExpr", "MethodCallExpr" -> {
                    ControlFlowGraph nestedCFG = new ControlFlowGraph();
                    CFGNode node = nestedCFG.addNode(item.toString(), "box");
                    nestedCFG.addLeave(node);
                    cfg.plus(nestedCFG);
                }
            }
        }
        return cfg;
    }

    private static ControlFlowGraph ifProcessing(IfStmt ifStmt) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        String condition = ifStmt.getCondition().toString();
        CFGNode conditionNode = cfg.addNode(condition, "diamond");
        cfg.addLeave(conditionNode);

        List<Node> thenBlock, elseBlock;
        if (ifStmt.getThenStmt().getClass() == BlockStmt.class) {
            thenBlock = ifStmt.getThenStmt().getChildNodes();
        } else {
            thenBlock = new ArrayList<>();
            thenBlock.add(ifStmt.getThenStmt());
        }
        if (ifStmt.getElseStmt().isPresent()) {
            if (ifStmt.getElseStmt().get().getClass() == BlockStmt.class) {
                elseBlock = ifStmt.getElseStmt().get().getChildNodes();
            } else {
                elseBlock = new ArrayList<>();
                elseBlock.add(ifStmt.getElseStmt().get());
            }
        } else {
            elseBlock = null;
        }

        ControlFlowGraph thenBlockCFG = nestedBlockProcessing(thenBlock, cfg);
        cfg.plus(thenBlockCFG, "green");

        if (elseBlock == null) {
            cfg.addLeave(conditionNode);
        } else { // there is else-branch
            ArrayList<CFGNode> thenBlockLeaves = new ArrayList<>(cfg.getLeaves());
            cfg.clearLeaves();
            cfg.addLeave(conditionNode);
            ControlFlowGraph elseBlockCFG = nestedBlockProcessing(elseBlock, cfg);
            cfg.plus(elseBlockCFG, "red");
            cfg.addLeaves(thenBlockLeaves);
        }

        return cfg;
    }


    private static ControlFlowGraph returnProcessing(ReturnStmt returnStmt) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        StringBuilder returnDescription = new StringBuilder("return ");
        if (returnStmt.getExpression().isPresent()) {
            returnDescription.append(returnStmt.getExpression().get());
        }
        cfg.addNode(returnDescription.toString(), "box");
        return cfg;
    }

    private static ControlFlowGraph forProcessing(ForStmt forStmt) {
        // FOR-loop initialization block
        ControlFlowGraph loopCFG = expressionProcessing(
                new ArrayList(Arrays.asList(forStmt.getInitialization().toArray())));

        // FOR-compare block + FOR-body
        ControlFlowGraph loopBodyCFG = new ControlFlowGraph();
        CFGNode compareNode = null;
        if (forStmt.getCompare().isPresent()) {
            compareNode = loopBodyCFG.addNode(forStmt.getCompare().get().toString(), "diamond");
            loopBodyCFG.addLeave(compareNode);
        }
        List<Node> block;
        if (forStmt.getBody().getClass() == BlockStmt.class) {
            block = new ArrayList(Arrays.asList(forStmt.getBody().getChildNodes().toArray()));
        } else {
            block = new ArrayList<>();
            block.add(forStmt.getBody());
        }

        loopBodyCFG.plus(nestedBlockProcessing(block, loopBodyCFG), "green");

        // FOR-update block
        ControlFlowGraph loopUpdateCFG = expressionProcessing(
                new ArrayList(Arrays.asList(forStmt.getUpdate().toArray())));

        loopBodyCFG.plus(loopUpdateCFG);
        if (loopBodyCFG.getNodes().isEmpty()) return loopCFG;

        // looping
        for(CFGNode node: loopBodyCFG.getLeaves()) {
            loopBodyCFG.addConnection(node, loopBodyCFG.getNodes().get(0), "");
        }

        loopBodyCFG.clearLeaves();
        loopBodyCFG.addLeaves(loopBodyCFG.getBreakNodes()); // "break" processing
        if (compareNode != null)
            if (!loopBodyCFG.getLeaves().contains(compareNode))
                    loopBodyCFG.addLeave(compareNode);

        // "continue" processing
        CFGNode loopStartWith;
        if (loopUpdateCFG.getNodes().isEmpty()) {
            loopStartWith = loopBodyCFG.getNodes().get(0);
        } else {
            loopStartWith = loopUpdateCFG.getNodes().get(0);
        }
        for(CFGNode node: loopBodyCFG.getContinueNodes()) {
            loopBodyCFG.addConnection(node, loopStartWith, "");
        }

        loopCFG.plus(loopBodyCFG);
        return loopCFG;
    }

    private static ControlFlowGraph whileProcessing(WhileStmt whileStmt) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        CFGNode conditionNode = cfg.addNode(whileStmt.getCondition().toString(), "diamond");
        cfg.addLeave(conditionNode);

        List<Node> block;
        if (whileStmt.getBody().getClass() == BlockStmt.class) {
            block = new ArrayList(Arrays.asList(whileStmt.getBody().getChildNodes().toArray()));
        } else {
            block = new ArrayList<>();
            block.add(whileStmt.getBody());
        }

        cfg.plus(nestedBlockProcessing(block, cfg), "green");

        // looping
        for(CFGNode node: cfg.getLeaves()) {
            cfg.addConnection(node, cfg.getNodes().get(0), "");
        }
        cfg.clearLeaves();

        for(CFGNode node: cfg.getContinueNodes()) {
            cfg.addConnection(node, cfg.getNodes().get(0), "");
        }
        cfg.clearContinueNodes();

        cfg.addLeaves(cfg.getBreakNodes());
        cfg.clearBreakNodes();
        if (!cfg.getLeaves().contains(conditionNode))
            cfg.addLeave(conditionNode);

        return cfg;
    }

    private static ControlFlowGraph switchProcessing(SwitchStmt switchStmt) {
        ControlFlowGraph cfg = new ControlFlowGraph();

        ArrayList<CFGNode> switchLeaves = new ArrayList<>();

        String startCondition = switchStmt.getSelector().toString() + " ==";
        for(SwitchEntry entry: switchStmt.getEntries()) {
            ControlFlowGraph entryCFG = new ControlFlowGraph();

            CFGNode conditionNode = null;
            if (!entry.getLabels().isEmpty()) {
                StringBuilder condition = new StringBuilder(startCondition);
                for(Node label: entry.getLabels())
                    condition.append(" ").append(label.toString());
                conditionNode = entryCFG.addNode(condition.toString(), "diamond");
                entryCFG.addLeave(conditionNode);
            }

            entryCFG.plus(nestedBlockProcessing(
                    new ArrayList(List.of(entry.getStatements().toArray()))),
                    "green");

            switchLeaves.addAll(entryCFG.getLeaves());
            entryCFG.clearLeaves();
            if (conditionNode != null) entryCFG.addLeave(conditionNode);
            cfg.plus(entryCFG);
        }

        cfg.clearLeaves();
        cfg.addLeaves(switchLeaves);
        return cfg;
    }
}
