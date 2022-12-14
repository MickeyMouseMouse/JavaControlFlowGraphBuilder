package com.example.lab1_javacfg.model;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class JavaCFGTests {
    @Test
    public void test1() {
        String cfgDescription = JavaCFGBuilder.getCFGDescription("""
                public class MyClass {
                	int fib(int n) {
                		int a = 0, b = 1, c, i;
                		if (n < 2) return n;
                		for (i = 1; i < n; i++) {
                			c = a + b;
                			a = b;
                			b = c;
                		}
                		return c;
                    }
                }
                """);

        AssertJUnit.assertEquals(
                "digraph{1[label=\"int fib(int n)\",shape=\"oval\"]2[label=\"a = 0\",shape=\"box\"]3[label=\"b = 1\",shape=\"box\"]4[label=\"c\",shape=\"box\"]5[label=\"i\",shape=\"box\"]6[label=\"n < 2\",shape=\"diamond\"]7[label=\"return n\",shape=\"box\"]8[label=\"i = 1\",shape=\"box\"]9[label=\"i < n\",shape=\"diamond\"]10[label=\"c = a + b\",shape=\"box\"]11[label=\"a = b\",shape=\"box\"]12[label=\"b = c\",shape=\"box\"]13[label=\"i++\",shape=\"box\"]14[label=\"return c\",shape=\"box\"]2->3[color=\"\"]3->4[color=\"\"]4->5[color=\"\"]6->7[color=\"green\"]5->6[color=\"black\"]10->11[color=\"black\"]11->12[color=\"black\"]9->10[color=\"green\"]12->13[color=\"black\"]13->9[color=\"\"]8->9[color=\"black\"]6->8[color=\"black\"]9->14[color=\"black\"]1->2[color=\"black\"]}",
                cfgDescription);
    }

    @Test
    public void test2() {
        String cfgDescription = JavaCFGBuilder.getCFGDescription("""
                public class MyClass {
                	int fib(int n) {
                		for (;;) {
                            while (i < 10) {
                                if (i == 5)
                                    i++;
                                else
                                    break;
                            }
                            if (q)
                                continue;
                            else
                                otherFunction();
                		}
                    }
                }
                """);

        AssertJUnit.assertEquals(
                "digraph{1[label=\"int fib(int n)\",shape=\"oval\"]2[label=\"i < 10\",shape=\"diamond\"]3[label=\"i == 5\",shape=\"diamond\"]4[label=\"i++\",shape=\"box\"]5[label=\"q\",shape=\"diamond\"]6[label=\"otherFunction()\",shape=\"box\"]3->4[color=\"green\"]2->3[color=\"green\"]4->2[color=\"\"]5->6[color=\"red\"]3->5[color=\"black\"]2->5[color=\"black\"]6->2[color=\"\"]5->2[color=\"\"]1->2[color=\"black\"]}",
                cfgDescription);
    }

    @Test
    public void test3() {
        String cfgDescription = JavaCFGBuilder.getCFGDescription("""
                public class MyClass {
                	public int myFun(int n) {
                		for (int i = 1, j = 0; i < 10; i++, j--) {
                			if (b == 5) {
                				q = 5;
                				w = 6.6;
                				e = "rty";
                			} else {
                				m = six(x);
                				break;
                			}
                			if (test == 8) {
                				break;
                			} else {
                				continue;
                			}
                		}
                		end = true;
                	}
                }
                """);

        AssertJUnit.assertEquals(
                "digraph{1[label=\"int myFun(int n)\",shape=\"oval\"]2[label=\"i = 1\",shape=\"box\"]3[label=\"j = 0\",shape=\"box\"]4[label=\"i < 10\",shape=\"diamond\"]5[label=\"b == 5\",shape=\"diamond\"]6[label=\"q = 5\",shape=\"box\"]7[label=\"w = 6.6\",shape=\"box\"]8[label=\"e = \\\"rty\\\"\",shape=\"box\"]9[label=\"m = six(x)\",shape=\"box\"]10[label=\"test == 8\",shape=\"diamond\"]11[label=\"i++\",shape=\"box\"]12[label=\"j--\",shape=\"box\"]13[label=\"end = true\",shape=\"box\"]2->3[color=\"\"]6->7[color=\"black\"]7->8[color=\"black\"]5->6[color=\"green\"]5->9[color=\"red\"]8->10[color=\"black\"]4->5[color=\"green\"]11->12[color=\"black\"]12->4[color=\"\"]10->11[color=\"\"]3->4[color=\"black\"]9->13[color=\"black\"]10->13[color=\"black\"]4->13[color=\"black\"]1->2[color=\"black\"]}",
                cfgDescription);
    }

    @Test
    public void test4() {
        String cfgDescription = JavaCFGBuilder.getCFGDescription("""
                public class MyClass {
                	public int myFun(int n) {
                		while (1<5) {
                			for (;;) {
                				if (w==2) continue;
                				if (w==3) break;
                			}
                			a=6;
                		}
                		a=5;
                	}
                }
                """);

        AssertJUnit.assertEquals(
                "digraph{1[label=\"int myFun(int n)\",shape=\"oval\"]2[label=\"1 < 5\",shape=\"diamond\"]3[label=\"w == 2\",shape=\"diamond\"]4[label=\"w == 3\",shape=\"diamond\"]5[label=\"a = 6\",shape=\"box\"]6[label=\"a = 5\",shape=\"box\"]3->4[color=\"black\"]4->3[color=\"\"]3->3[color=\"\"]4->5[color=\"black\"]2->3[color=\"green\"]5->2[color=\"\"]2->6[color=\"black\"]1->2[color=\"black\"]}",
                cfgDescription);
    }

    @Test
    public void test5() {
        String cfgDescription = JavaCFGBuilder.getCFGDescription("""
                public class MyClass {
                    int fib(int n) {
                        switch(str) {
                            case "1" ->
                                a = 3;
                            case "2" -> {
                                b = 3;
                                c = 4;
                            }
                            case "3" -> {
                                x=sinx(t);
                            }
                            default -> {
                                s = 3;
                            }
                        }
                        end = true;
                    }
                }
                """);

        AssertJUnit.assertEquals(
                "digraph{1[label=\"int fib(int n)\",shape=\"oval\"]2[label=\"str == \\\"1\\\"\",shape=\"diamond\"]3[label=\"a = 3\",shape=\"box\"]4[label=\"str == \\\"2\\\"\",shape=\"diamond\"]5[label=\"b = 3\",shape=\"box\"]6[label=\"c = 4\",shape=\"box\"]7[label=\"str == \\\"3\\\"\",shape=\"diamond\"]8[label=\"x = sinx(t)\",shape=\"box\"]9[label=\"s = 3\",shape=\"box\"]10[label=\"end = true\",shape=\"box\"]2->3[color=\"green\"]5->6[color=\"black\"]4->5[color=\"green\"]2->4[color=\"black\"]7->8[color=\"green\"]4->7[color=\"black\"]7->9[color=\"black\"]3->10[color=\"black\"]6->10[color=\"black\"]8->10[color=\"black\"]9->10[color=\"black\"]1->2[color=\"black\"]}",
                cfgDescription);
    }
}
