package com.StringMatching;

import java.util.*;
import java.util.function.Consumer;

public class Trie {
    private final Node root;
    private final List<String> words;

    public Trie(List<String> patterns) {
        root = new Node(false, -1);
        words = new ArrayList<>();
        for (String s : patterns) {
            words.add(s);
            Node activeNode = root;
            for (int i = 0; i < s.length(); ++i) {
                if (!activeNode.hasEdge(s.charAt(i)))
                    activeNode.addEdge(new Edge(s.charAt(i), false, -1));
                activeNode = activeNode.getEdge(s.charAt(i)).getTo();
            }
            activeNode.setWordEnd(true, words.size() - 1);
        }
    }

    public List<String> getWords() {
        return words;
    }

    public List<HashSet<Integer>> findMatches(String text) {
        ArrayList<HashSet<Integer>> matches = new ArrayList<>();
        for (String s : words)
            matches.add(new HashSet<>());
        outerLoop:
        for (int i = 0; i < text.length(); ++i) {
            Node activeNode = root;
            int offset = i;
            while (offset < text.length() && activeNode.numEdges() != 0) {
                char current = text.charAt(offset);
                if (activeNode.hasEdge(current)) {
                    activeNode = activeNode.getEdge(current).getTo();
                    if (activeNode.isWordEnd())
                        matches.get(activeNode.getWordIndex()).add(i);
                    ++offset;
                } else
                    break;
            }
        }
        return matches;
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
        private int wordIndex;

        public Node(boolean wordEnd, int wordIndex) {
            this.wordEnd = wordEnd;
            this.wordIndex = wordIndex;
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

        public void setWordEnd(boolean wordEnd, int wordIndex) {
            this.wordEnd = wordEnd;
            this.wordIndex = wordIndex;
        }

        public List<Edge> edgeList() {
            return new ArrayList<>(edges.values());
        }

        public int numEdges() {
            return edges.size();
        }

        public int getWordIndex() {
            if (!wordEnd)
                throw new IllegalArgumentException("Cannot get wordIndex from non-terminal node!");
            else
                return wordIndex;
        }
    }

    private class Edge {
        private final char c;
        private final Node to;

        public Edge(char c, boolean wordEnd, int wordIndex) {
            this.c = c;
            to = new Node(wordEnd, wordIndex);
        }

        public char getChar() {
            return c;
        }

        public Node getTo() {
            return to;
        }
    }
}
