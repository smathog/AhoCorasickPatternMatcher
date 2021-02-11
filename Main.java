package com.StringMatching;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
	    var list = List.of("at", "art", "oars", "soar");
        var temp = new Trie(list);
        System.out.println(temp);
        String s = scanner.nextLine();
        System.out.println(temp.findMatches(s));
        System.out.println(temp.findMatchesAC(s));
    }
}
