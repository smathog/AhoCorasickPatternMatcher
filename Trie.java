package com.StringMatching;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class Trie {
    private final Node root;

    public Trie(List<String> patterns) {
        root = new Node(false);
        for (String s : patterns) {
            Node activeNode = root;
            for (int i = 0; i < s.length(); ++i) {
                if (!activeNode.hasEdge(s.charAt(i)))
                    activeNode.addEdge(new Edge(s.charAt(i), false));
                activeNode = activeNode.getEdge(s.charAt(i)).getTo();
            }
            activeNode.setWordEnd(true);
        }
    }

    //Print function based on the one found in this article:
    //https://www.baeldung.com/java-print-binary-tree-diagram
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        traverseNode(sb, "", "", root, false);
        return sb.toString();
    }

    private void traverseNode(StringBuilder sb, String padding, String pointer, Node node, boolean nodeAfter) {
        if (node != null) {
            sb.append(padding);
            sb.append(pointer);
            if (node.isWordEnd())
                sb.append("(\u2588)");
            else
                sb.append("()");
            sb.append('\n');

            String terminalPointer = "└──";
            String linkPointer = "├──";

            Consumer<String> nextCall = s -> {
                List<Edge> edgeList = node.edgeList();
                for (int i = 0; i < edgeList.size(); ++i) {
                    Edge edge = edgeList.get(i);
                    String edgePointer = "<" + edge.getChar() + ">──";
                    if (i == edgeList.size() - 1)
                        traverseNode(sb, s, terminalPointer + edgePointer, edge.getTo(), false);
                    else
                        traverseNode(sb, s, linkPointer + edgePointer, edge.getTo(), true);
                }
            };

            if (node == root) {
                nextCall.accept("");
            } else {
                int length = pointer.length();
                StringBuilder pb = new StringBuilder(padding);
                if (nodeAfter)
                    pb.append("|" + " ".repeat(length));
                else
                    pb.append(" " + " ".repeat(length));
                nextCall.accept(pb.toString());
            }
        }
    }

    private class Node {
        private boolean wordEnd;
        private final HashMap<Character, Edge> edges;

        public Node(boolean wordEnd) {
            this.wordEnd = wordEnd;
            edges = new HashMap<>();
        }

        public void addEdge(Edge edge) {
            if (edges.containsKey(edge.getChar()))
                throw new IllegalArgumentException("Node already contains edge with char " + edge.getChar());
            else
                edges.put(edge.getChar(), edge);
        }

        public boolean hasEdge(char c) {
            return edges.containsKey(c);
        }

        public Edge getEdge(char c) {
            if (!hasEdge(c))
                throw new IllegalArgumentException("No such edge " + c + " exists in this node!");
            else
                return edges.get(c);
        }

        public boolean isWordEnd() {
            return wordEnd;
        }

        public void setWordEnd(boolean wordEnd) {
            this.wordEnd = wordEnd;
        }

        public List<Edge> edgeList() {
            return new ArrayList<>(edges.values());
        }
    }

    private class Edge {
        private final char c;
        private final Node to;

        public Edge(char c, boolean wordEnd) {
            this.c = c;
            to = new Node(wordEnd);
        }

        public char getChar() {
            return c;
        }

        public Node getTo() {
            return to;
        }
    }
}
