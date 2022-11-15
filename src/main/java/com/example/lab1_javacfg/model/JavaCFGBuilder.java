package com.example.lab1_javacfg.model;

import com.example.lab1_javacfg.model.cfg.CFGConnection;
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
        return getNestedBlockCFG(block, null, null);
    }

    private static ControlFlowGraph nestedBlockProcessing(List<Node> block, ArrayList<CFGNode> breakNodes, ArrayList<CFGNode> continueNodes) {
        return getNestedBlockCFG(block, breakNodes, continueNodes);
    }

    private static ControlFlowGraph getNestedBlockCFG(List<Node> block, ArrayList<CFGNode> breakNodes, ArrayList<CFGNode> continueNodes) {
        ControlFlowGraph cfg = new ControlFlowGraph();
        for (Node node: block) {
            switch (node.getClass().getSimpleName()) {
                case "BlockStmt" -> {
                    cfg.plus(nestedBlockProcessing(node.getChildNodes()));
                }
                case "ExpressionStmt" ->
                    cfg.plus(expressionProcessing(node.getChildNodes()));
                case "UnaryExpr" ->
                    cfg.plus(expressionProcessing(new ArrayList<>(List.of(node))));
                case "IfStmt" ->
                    cfg.plus(ifProcessing((IfStmt) node, breakNodes, continueNodes));
                case "ReturnStmt" ->
                    cfg.plus(returnProcessing((ReturnStmt) node));
                case "ForStmt" ->
                    cfg.plus(forProcessing((ForStmt) node));
                case "WhileStmt" ->
                    cfg.plus(whileProcessing((WhileStmt) node));
                case "BreakStmt" -> {
                    ControlFlowGraph breakCFG = new ControlFlowGraph();
                    CFGNode breakNode = breakCFG.addNode("break", "box");
                    if (cfg.plus(breakCFG) && breakNodes != null)
                        breakNodes.add(breakNode);
                }
                case "ContinueStmt" -> {
                    ControlFlowGraph continueCFG = new ControlFlowGraph();
                    CFGNode continueNode = continueCFG.addNode("continue", "box");
                    if (cfg.plus(continueCFG) && continueNodes != null)
                        continueNodes.add(continueNode);
                }
                case "SwitchStmt" -> {
                    cfg.plus(switchProcessing((SwitchStmt) node));
                }
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
                case "AssignExpr", "UnaryExpr" -> {
                    ControlFlowGraph nestedCFG = new ControlFlowGraph();
                    CFGNode node = nestedCFG.addNode(item.toString(), "box");
                    nestedCFG.addLeave(node);
                    cfg.plus(nestedCFG);
                }
            }
        }
        return cfg;
    }

    private static ControlFlowGraph ifProcessing(
            IfStmt ifStmt, ArrayList<CFGNode> breakNodes, ArrayList<CFGNode> continueNodes) {
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

        ArrayList<CFGNode> elseBlockLeaves = new ArrayList<>();
        if (elseBlock == null) {
            elseBlockLeaves.add(conditionNode);
        } else {
            ControlFlowGraph elseBlockCFG = nestedBlockProcessing(elseBlock, breakNodes, continueNodes);
            cfg.plus(elseBlockCFG, "red");
            elseBlockLeaves = new ArrayList<>(cfg.getLeaves());

            cfg.clearLeaves();
            cfg.addLeave(conditionNode);
        }

        ControlFlowGraph thenBlockCFG = nestedBlockProcessing(thenBlock, breakNodes, continueNodes);
        cfg.plus(thenBlockCFG, "green");
        cfg.addLeaves(elseBlockLeaves);

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

        ArrayList<CFGNode> breakNodes = new ArrayList<>();
        ArrayList<CFGNode> continueNodes = new ArrayList<>();

        loopBodyCFG.plus(nestedBlockProcessing(block, breakNodes, continueNodes), "green");

        // finding "continue"-leaves
        // example: for(int i=1;i<5;i++) {continue;}
        if (!loopBodyCFG.getNodes().isEmpty() && loopBodyCFG.getLeaves().isEmpty()) {
            for (CFGNode node: loopBodyCFG.getNodes()) {
                if (node.getLabel().equals("continue")) {
                    boolean isLeave = true;
                    for (CFGConnection connection: loopBodyCFG.getConnections()) {
                        if (connection.getFirst().equals(node)) {
                            isLeave = false;
                            break;
                        }
                    }
                    if (isLeave) {
                        loopBodyCFG.addLeave(node);
                        continueNodes.remove(node);
                    }
                }
            }
        }

        // FOR-update block
        ControlFlowGraph loopUpdateCFG = expressionProcessing(
                new ArrayList(Arrays.asList(forStmt.getUpdate().toArray())));

        loopBodyCFG.plus(loopUpdateCFG);

        for(CFGNode node: loopBodyCFG.getLeaves()) {
            loopBodyCFG.addConnection(node, loopBodyCFG.getNodes().get(0), "");
        }

        loopBodyCFG.clearLeaves();
        if (compareNode != null) loopBodyCFG.addLeave(compareNode);
        loopBodyCFG.addLeaves(breakNodes); // "break" processing

        // "continue" processing
        CFGNode loopStartWith;
        if (loopUpdateCFG.getNodes().isEmpty()) {
            loopStartWith = loopBodyCFG.getNodes().get(0);
        } else {
            loopStartWith = loopUpdateCFG.getNodes().get(0);
        }
        for(CFGNode node: continueNodes) {
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

        ArrayList<CFGNode> breakNodes = new ArrayList<>();
        ArrayList<CFGNode> continueNodes = new ArrayList<>();

        cfg.plus(nestedBlockProcessing(block, breakNodes, continueNodes), "green");

        for(CFGNode node: cfg.getLeaves()) {
            cfg.addConnection(node, cfg.getNodes().get(0), "");
        }

        cfg.clearLeaves();
        cfg.addLeave(conditionNode);
        cfg.addLeaves(breakNodes);

        for(CFGNode node: continueNodes) {
            cfg.addConnection(node, cfg.getNodes().get(0), "");
        }

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
