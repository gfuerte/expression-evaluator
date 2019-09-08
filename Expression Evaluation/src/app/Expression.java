package app;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import structures.Stack;

public class Expression {

	public static String delims = " \t*+-/()[]";

	public static void makeVariableLists(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {

		StringTokenizer tokenizer = new StringTokenizer(expr, "1234567890 \\t*+-/()]");
		String s = new String("");
		String brackets = "[";
		
		while (tokenizer.hasMoreTokens()) {
			s = tokenizer.nextToken();
			int index;
			boolean condition = true;
			while (condition == true) {
				if(!s.contains(brackets)) {
					Variable var = new Variable(s);
					if (vars.contains(var)) {
						condition = false;
					} else {
						vars.add(var);
						condition = false;
					}
				} else {
					index = s.indexOf(brackets);
					String arrayName = s.substring(0, index);
					Array arr = new Array(arrayName);
					if (!arrays.contains(arr)) {
						arrays.add(arr);
					}
					try {
						s = s.substring(index + 1);
					} catch (NullPointerException e) {
						condition = false;
					}
				}
			}
		}
		
	}

	public static void loadVariableValues(Scanner sc, ArrayList<Variable> vars, ArrayList<Array> arrays)
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
					StringTokenizer stt = new StringTokenizer(tok, " (,)");
					int index = Integer.parseInt(stt.nextToken());
					int val = Integer.parseInt(stt.nextToken());
					arr.values[index] = val;
				}
			}
		}
	}

	public static float evaluate(String expr, ArrayList<Variable> vars, ArrayList<Array> arrays) {
		float result = 0;
		boolean hasoperators = false;
		boolean hasbrackets = false;
		boolean hasparenthesis = false;

		String brackets = "[]";
		char[] br = brackets.toCharArray();
		for (int i = 0; i < br.length; i++) {
			String b = Character.toString(br[i]);
			if (expr.contains(b)) {
				hasbrackets = true;
			} else {
				hasbrackets = false;
			}
		}

		while (hasbrackets == true) {
			String arrayName = new String("");
			String cur = new String("");
			int count = 0;
			int endPointer = 0;
			StringTokenizer tokenizer = new StringTokenizer(expr, " \\t*+-/()]");

			while (tokenizer.hasMoreTokens()) {
				cur = tokenizer.nextToken();
				//System.out.println(cur);
				if (cur.indexOf("[") != -1) {
					arrayName = cur.substring(0, cur.indexOf("["));
					//System.out.println(arrayName);
					break;
				}
			}

			for (int i = expr.indexOf("["); count >= 0; i++) {
				if (expr.charAt(i) == ']') {
					count -= 1;
					if (count == 0) {
						endPointer = i;
						break;
					}
				} else if (expr.charAt(i) == '[') {
					count += 1;
				}
			}

			String insideBrackets = expr.substring(expr.indexOf("[") + 1, endPointer);
			//System.out.println(insideBrackets);
	

			float x = evaluate(insideBrackets, vars, arrays);
			//System.out.println(x);
			
			for (int i = 0; i < arrays.size(); i++) {
				//boolean check = arrays.get(i).name.equals(arrayName);
				//System.out.println(check);
				if (arrays.get(i).name.equals(arrayName)) {
					x = arrays.get(i).values[(int) x];
					//System.out.println(x);
				}
			}
			String before = expr.substring(0, expr.indexOf("[")-arrayName.length());
			String after = expr.substring(endPointer + 1);
			expr = before + Float.toString(x) + after;
			//System.out.println(expr);

			for (int i = 0; i < br.length; i++) {
				String b = Character.toString(br[i]);
				if (expr.contains(b)) {
					hasbrackets = true;
				} else {
					hasbrackets = false;
				}
			}
		}

		String parenthesis = "()";
		char[] pr = parenthesis.toCharArray();
		for (int i = 0; i < pr.length; i++) {
			String c = Character.toString(pr[i]);
			if (expr.contains(c)) {
				hasparenthesis = true;
			}
		}

		while (hasparenthesis == true) {
			int startPointer = expr.indexOf("(");
			int endPointer = 0;
			int count = 0;

			for (int i = startPointer; count >= 0; i++) {
				if (expr.charAt(i) == ')') {
					count -= 1;
					if (count == 0) {
						endPointer = i;
						break;
					}
				} else if (expr.charAt(i) == '(') {
					count += 1;
				}
			}

			String insideParenthesis = expr.substring(startPointer + 1, endPointer);
			String before = expr.substring(0, startPointer);
			String after = expr.substring(endPointer + 1);
			float x = evaluate(insideParenthesis, vars, arrays);
			expr = before + Float.toString(x) + after;

			for (int i = 0; i < pr.length; i++) {
				String c = Character.toString(pr[i]);
				if (expr.contains(c)) {
					hasparenthesis = true;
				} else {
					hasparenthesis = false;
				}
			}
		}

		while (expr.indexOf("+-") == 1) {
			int index = expr.indexOf("+-");
			String before = expr.substring(0, index);
			String after = expr.substring(index + 2);
			expr = before + "-" + after;
		}

		while (expr.indexOf("--") == 1) {
			int index2 = expr.indexOf("--");
			String before = expr.substring(0, index2);
			String after = expr.substring(index2 + 2);
			expr = before + "+" + after;
		}

		String operators = "+-*/";
		char[] o = operators.toCharArray();
		for (int i = 0; i < o.length; i++) {
			String a = Character.toString(o[i]);
			if (expr.contains(a)) {
				hasoperators = true;
			}
		}
		
		boolean check = false;
		if (expr.charAt(0) == '-' ) {
			try {
				Float.parseFloat(expr.substring(1));
				check = false;
			} catch (NumberFormatException e) {
				check = false;
			}
		}
		if ((hasoperators == false && hasbrackets == false && hasparenthesis == false) || check == true) {
			try {
				return Float.parseFloat(expr);
			} catch (NumberFormatException e) {
				for (int i = 0; i < vars.size(); i++) {
					if (vars.get(i).name.equals(expr)) {
						return vars.get(i).value;
					}
				}
			}
		} else if (hasoperators == true) {
			Stack<Float> values = new Stack<Float>();
			Stack<Float> numReverse = new Stack<Float>();
			Stack<Character> operator = new Stack<Character>();
			Stack<Character> opReverse = new Stack<Character>();

			StringTokenizer valTokenizer = new StringTokenizer(expr, delims);
			while (valTokenizer.hasMoreTokens()) {
				String s = valTokenizer.nextToken();
				// System.out.println("the current token is: " + s);
				numReverse.push(evaluate(s, vars, arrays));
				// System.out.println("check 1, current number pushed to reverse: " + s);
			}

			while (numReverse.isEmpty() == false) {
				values.push(numReverse.pop());
				// System.out.println("check 2");
			}

			for (int i = 0; i < expr.length(); i++) {
				if (expr.charAt(i) == '+' || expr.charAt(i) == '-' || expr.charAt(i) == '*' || expr.charAt(i) == '/') {
					opReverse.push(expr.charAt(i));
					// System.out.println("operator pushed into reverse: " + expr.charAt(i));
				}
			}

			while (opReverse.isEmpty() == false) {
				operator.push(opReverse.pop());
				// System.out.println("operator pushed into operator");
			}

			// values and operators are in their respective stack in the right order

			while (values.isEmpty() == false) {
				char c = operator.pop();
				try {
					char d = operator.peek();
					if ((c == '+' || c == '-') && (d == '*' || d == '/')) { // the dreaded order of operations
						float holdNum = values.pop();
						char holdOperator = c;
						c = operator.pop();
						float x = values.pop();
						float y = values.pop();
						if (c == '+') {
							result = x + y;
						}
						if (c == '-') {
							result = x - y;
						}
						if (c == '*') {
							result = x * y;
						}
						if (c == '/') {
							result = x / y;
						}
						values.push(result);
						values.push(holdNum);
						operator.push(holdOperator);
					} else {
						float x = values.pop();
						float y = values.pop();
						if (c == '+') {
							result = x + y;
						}
						if (c == '-') {
							result = x - y;
						}
						if (c == '*') {
							result = x * y;
						}
						if (c == '/') {
							result = x / y;
						}
						if (operator.isEmpty() == false) {
							values.push(result);
						}
					}
				} catch (NoSuchElementException e) {
					float x = values.pop();
					// System.out.println("the first number is: " + x);
					float y = values.pop();
					// System.out.println("the second number is: " + y);
					if (c == '+') {
						return x + y;
					}
					if (c == '-') {
						return x - y;
					}
					if (c == '*') {
						return x * y;
					}
					if (c == '/') {
						return x / y;
					}
				}
			}
		}
		return result;
	}
}
