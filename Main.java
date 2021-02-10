package com.StringMatching;

import java.util.List;

public class Main {

    public static void main(String[] args) {
	    var list = List.of("ate", "an", "at", "be");
        var temp = new Trie(list);
        System.out.println(temp);
        System.out.println(temp.findMatches("ateanatbebebebeateanananaananananbe"));
    }
}
