package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

    public static boolean hm1(String t) {
        try {
            Float f = Float.parseFloat(t);
            return Boolean.parseBoolean("true");
        } catch (Exception error) { }

        return Boolean.parseBoolean("false");
    }

    public static boolean hm2(String t) {
        for(Character x : delims.toCharArray()) {
            if(t.charAt(0) == x)
                return Boolean.parseBoolean("true");
        }

        return Boolean.parseBoolean("false");
    }

    public static boolean hm3(String s, String p) {
        if(s.equals("*") || s.equals("/")) {
            if(p.equals("/") || p.equals("*"))
                return Boolean.parseBoolean("true");
            if(p.equals("+") || p.equals("-"))
                return Boolean.parseBoolean("false");
        } else if(s.equals("+") || s.equals("-")) {
            if(p.equals("*") || p.equals("/") || p.equals("+") || p.equals("-"))
                return Boolean.parseBoolean("true");
        }

        return false;
    }

    public static Float hm4(String operation, Float n1, Float n2) {
        switch(operation) {
            case "+":
                return n1 + n2;
            case "-":
                return n1 - n2;
            case "*":
                return n1 * n2;
            case "/":
                return n1 / n2;
        }

        return 0f;
    }

    public static boolean hm5(String t, ArrayList<Variable> x) {
        for(Variable v : x) {
            if (v.name.equals(t)) {
                return Boolean.parseBoolean("true");
            }
        }
        return false;
    }

    public static Float hm6(String t, ArrayList<Variable> v) {
        for(Variable x : v)
            if(x.name.equals(t)) {
                Float f = Float.valueOf((float) x.value);
                return f;
            }

        return 0f;
    }

    public static boolean hm7(String t, ArrayList<Array> arr) {
        for(Array a : arr) {
            if (a.name.equals(t)) {
                return Boolean.parseBoolean("true");
            }
        }
        return Boolean.parseBoolean("false");
    }

    public static Array hm8(String t, ArrayList<Array> arr) {
        Array array = null;

        for(Array a : arr) {
            if (a.name.equals(t)) {
                return a;
            }
        }
        return array;
    }

    public static Float getNumber(String t) {
        Float f = null;

        try {
            f = Float.parseFloat(t);
        } catch (Exception error) { }

        return f;
    }


    /**
     * Populates the vars list with simple variables, and arrays lists with arrays
     * in the expression. For every variable (simple or array), a SINGLE instance is created 
     * and stored, even if it appears more than once in the expression.
     * At this time, values for all variables and all array items are set to
     * zero - they will be loaded from a file in the loadVariableValues method.
     * 
     * @param expr The expression
     * @param vars The variables array list - already created by the caller
     * @param arrays The arrays array list - already created by the caller
     */
    public static void 
    makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        expr = expr.replaceAll("\\s+","");
        StringTokenizer s = new StringTokenizer(expr, delims, true);
        String i = null;
        String n = null;

        n = s.nextToken();

        while(s.hasMoreTokens()) {
            i = n;
            n = s.nextToken();
            if(!hm1(i) && !hm2(i)) {
                if(n.equals("[")) {
                    Array arr = new Array(i);
                    if(!arrays.contains(arr))
                        arrays.add(arr);
                } else {
                    Variable a = new Variable(i);
                    if(!vars.contains(a))
                        vars.add(a);
                }
            }
        }

        i = n;
        if(!hm1(i) && !hm2(i)) {
            Variable b = new Variable(i);
            if(!vars.contains(b))
                vars.add(b);
        }
    }
    
    /**
     * Loads values for variables and arrays in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     * @param vars The variables array list, previously populated by makeVariableLists
     * @param arrays The arrays array list - previously populated by makeVariableLists
     */
    public static void 
    loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String tok = st.nextToken();
            Variable var = new Variable(tok);
            Array arr = new Array(tok);
            int vari = vars.indexOf(var);
            int arri = arrays.indexOf(arr);
            if (vari == -1 && arri == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                vars.get(vari).value = num;
            } else { // array symbol
            	arr = arrays.get(arri);
            	arr.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    arr.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression.
     * 
     * @param vars The variables array list, with values for all variables in the expression
     * @param arrays The arrays array list, with values for all array items
     * @return Result of evaluation
     */
    public static float 
    evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
        Stack<Float> num = new Stack<Float>();
        Stack<String> op = new Stack<String>();
        expr = expr.replaceAll("\\s+","");
        StringTokenizer s = new StringTokenizer(expr, delims, true);
        int pCount = 0;
        int bCount = 0;
        boolean pt = false;
        boolean arr = false;
        String par = "";
        String brack = "";
        Array array = null;

        while(s.hasMoreTokens()) {
            String t = s.nextToken();

            if(pt) {
                if(t.equals("("))
                    pCount++;
                else if(t.equals(")") && pCount > 1)
                    pCount--;
                else if(t.equals(")") && pCount == 1) {
                    num.push(evaluate(par, vars, arrays));
                    pCount--;
                    pt = false;
                    par = "";
                    continue;
                }
                par += t;
            } else if(arr) {
                if(t.equals("[") && bCount == 0) {
                    bCount++;
                    continue;
                } else if(t.equals("["))
                    bCount++;
                else if(t.equals("]") && bCount > 1)
                    bCount--;
                else if(t.equals("]") && bCount == 1) {
                    int index = (int) evaluate(brack, vars, arrays);
                    num.push((float) array.values[index]);
                    arr = false;
                    array = null;
                    bCount--;
                    brack = "";
                    continue;
                }
                brack += t;
            } else {
                if(hm7(t, arrays)) {
                    arr = true;
                    array = hm8(t, arrays);
                    continue;
                }
                if(t.equals("(")) {
                    pt = true;
                    pCount++;
                    continue;
                }
                if(hm1(t)) {
                    num.push(getNumber(t));
                }
                if(hm5(t, vars)) {
                    num.push(hm6(t, vars));
                }
                if(hm2(t)) {
                    if(op.isEmpty()) {
                        op.push(t);
                        continue;
                    }
                    boolean foo = false;
                    while(!op.isEmpty()) {
                        String pOp = op.peek();
                        if(hm3(t, pOp)) {
                            Float num2 = num.pop();
                            Float num1 = num.pop();
                            String oper = op.pop();
                            num.push(hm4(oper,num1, num2));
                        } else {
                            op.push(t);
                            foo = true;
                            break;
                        }
                    }
                    if(!foo) op.push(t);
                }
            }
        }

        while(!op.isEmpty()) {
            Float num2 = num.pop();
            String oper = op.pop();
            Float num1 = num.pop();
            num.push(hm4(oper,num1, num2));
        }

        return num.pop().floatValue();
    }
}
