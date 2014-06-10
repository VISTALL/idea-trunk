package com.siyeh.igtest.bugs;

public class ImplicitArrayToString {

    void foo() {
        System.out.println("new String[10]" + new String[10]);
        final String[] var = new String[10];
        System.out.println("new String[10]" + var);
        System.out.println("new String[10]" + meth());
    }

    private String[] meth() {
        return new String[10];
    }

	public static void main(String[] args) {
		char[] charArray = new char[]{'A', 'B', 'C'};
		System.out.println(charArray); // should not warn
		System.out.println(args); // should indeed warn

	}
}
