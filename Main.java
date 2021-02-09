package com.StringMatching;

import java.util.List;

public class Main {

    public static void main(String[] args) {
	    var list = List.of("ate", "an", "at", "be");
        System.out.println(new Trie(list));
    }
}
