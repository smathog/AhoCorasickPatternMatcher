package com.StringMatching;

import java.util.*;
import java.util.function.Consumer;

public class Trie {
    private final Node root;
    private final List<String> words;

    //Constructs an Aho-Corasick Automaton for a given List of patterns
    public Trie(List<String> patterns) {
        root = new Node(null, false, -1, 0);
        words = new ArrayList<>();
        //Step 1: Build the trie
        buildTrie(patterns);
        //Step 2: Construct failure/suffix and dictionary links
        buildLinks();
    }

    public List<String> getWords() {
        return words;
    }

    //Naive, by-character trie-based search for matches in a given text
    //Time complexity O(N*L) where N = |text|, L is the length of the longest pattern in the trie
    public List<HashSet<Integer>> findMatches(String text) {
        ArrayList<HashSet<Integer>> matches = new ArrayList<>();
        for (String s : words)
            matches.add(new HashSet<>());
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

    //Aho-Corasick search algorithm over the fully constructed trie
    //Time complexity O(N + Z) where N = |text|, Z is the number of matches over all patterns found in text
    public List<HashSet<Integer>> findMatchesAC(String text) {
        ArrayList<HashSet<Integer>> matches = new ArrayList<>();
        for (String s : words)
            matches.add(new HashSet<>());
        Node activeNode = root;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            while (!activeNode.hasEdge(c)) {
                if (activeNode == root)
                    break;
                else
                    activeNode = activeNode.getFailureLink();
            }
            //If activeNode has an edge matching the current char inspected, follow it
            if (activeNode.hasEdge(c))
                activeNode = activeNode.getEdge(c).getTo();
            //If activeNode now corresponds to a word, save the match
            if (activeNode.isWordEnd())
                matches.get(activeNode.getWordIndex()).add(i - activeNode.getDepth() + 1);
            //Follow any valid chain of dictionary links to save these possible matches
            Node dictNode = activeNode.getDictionaryLink();
            while (dictNode != null) {
                matches.get(dictNode.getWordIndex()).add(i - dictNode.getDepth() + 1);
                dictNode = dictNode.getDictionaryLink();
            }
        }
        return matches;
    }

    //Constructs the base trie for the list of patterns
    //Time complexity O(M) where M is the sum of the lengths of all patterns
    private void buildTrie(List<String> patterns) {
        for (String s : patterns) {
            words.add(s);
            Node activeNode = root;
            for (int i = 0; i < s.length(); ++i) {
                if (!activeNode.hasEdge(s.charAt(i)))
                    activeNode.addEdge(new Edge(activeNode, s.charAt(i), false, -1));
                activeNode = activeNode.getEdge(s.charAt(i)).getTo();
            }
            activeNode.setWordEnd(true, words.size() - 1);
        }
    }

    //Helper function that constructs the failure and dictionary links for a newly constructed trie
    //Time complexity O(M) where M i the sum of the lengths of all patterns
    private void buildLinks() {
        ArrayDeque<Node> nodeQueue = new ArrayDeque<>();
        //Failure Rule 1: root has no failure link
        //Failure Rule 2: nodes one layer deeper than root have failure links pointing to root
        //Note: all root and depth 1 nodes have null dictionary links, so no work to be done there
        for (Edge e : root.edges.values()) {
            Node current = e.getTo();
            current.setFailureLink(root);
            for (Edge edge : current.edges.values())
                nodeQueue.add(edge.getTo());
        }
        //BFS while applying other rules
        while (!nodeQueue.isEmpty()) {
            Node current = nodeQueue.pollFirst();
            //First: find current's failure link node
            Node linked = current.getEdgeIn().getFrom().getFailureLink();
            while (true) {
                if (linked.hasEdge(current.getEdgeIn().getChar())) {
                    //Failure Rule 3: if linked has a edge and node corresponding to current's edgeIn character,
                    //link current to that node
                    current.setFailureLink(linked.getEdge(current.getEdgeIn().getChar()).getTo());
                    break;
                } else if (root == linked) {
                    //Failure Rule 4: if linked is root and doesn't satisfy rule 3, current is linked to root
                    current.setFailureLink(root);
                    break;
                } else
                    //Failure Rule 5: update linked and repeat until rule 3 or 4 applies
                    linked = linked.getFailureLink();
            }
            //Second: use the failure link to find current's dictionaryLink
            Node currentLink = current.getFailureLink();
            if (currentLink.isWordEnd()) {
                //Dictionary Rule 1: if the failure link corresponds to a word, set current's dictionary link to it
                current.setDictionaryLink(currentLink);
            } else {
                //Dictionary Rule 2: if rule 1 doesn't apply, set current's DL to it's failure link's DL.
                current.setDictionaryLink(currentLink.getDictionaryLink());
            }
            //Third: add next layer of nodes into queue to continue BFS
            for (Edge e : current.edges.values())
                nodeQueue.add(e.getTo());
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
        private final Edge edgeIn;
        private boolean wordEnd;
        private final HashMap<Character, Edge> edges;
        private Node failureLink;
        private Node dictionaryLink;
        private int wordIndex;
        private final int depth;

        public Node(Edge edgeIn, boolean wordEnd, int wordIndex, int depth) {
            this.edgeIn = edgeIn;
            this.failureLink = null;
            this.dictionaryLink = null;
            this.wordEnd = wordEnd;
            this.wordIndex = wordIndex;
            this.depth = depth;
            edges = new HashMap<>();
        }

        public Edge getEdgeIn() {
            return edgeIn;
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

        public void setFailureLink(Node other) {
            this.failureLink = other;
        }

        public Node getFailureLink() {
            return failureLink;
        }

        public void setDictionaryLink(Node other) {
            this.dictionaryLink = other;
        }

        public Node getDictionaryLink() {
            return dictionaryLink;
        }

        public int getDepth() {
            return depth;
        }
    }

    private class Edge {
        private final char c;
        private final Node to;
        private final Node from;

        public Edge(Node from, char c, boolean wordEnd, int wordIndex) {
            this.from = from;
            this.c = c;
            to = new Node(this, wordEnd, wordIndex, from.getDepth() + 1);
        }

        public char getChar() {
            return c;
        }

        public Node getFrom() {
            return from;
        }

        public Node getTo() {
            return to;
        }
    }
}
