import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;
import java.util.Formatter;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.Collection;
import java.util.Stack;

public class DPLL{
	HashMap<Integer, Clause> kb;		// knowledge base
	HashMap<String, Integer> model;		// model, 0 unassigned, -1 false, 1 true
	ArrayList<String> modelSymbolOrder;		// model's literals in order
	Stack<Integer> backupStack;
	int nodeSearched;
	int heuristic;
	StringBuilder builder;
	Formatter formatter;
	PrintWriter outputFile;
	
	public DPLL() throws IOException{
		kb = new HashMap<Integer, Clause>();
		model = new HashMap<String, Integer>();
		backupStack = new Stack<Integer>();
		builder = new StringBuilder();
		formatter = new Formatter(builder);
		nodeSearched = 0;
		
		readFile();
		modelSymbolOrder = new ArrayList<String>(model.keySet());
		Collections.sort(modelSymbolOrder);
		
		formatter.format("props:%n");
		for (String literal : modelSymbolOrder)
			formatter.format("%s ", literal);
		formatter.format("%n");
		
		formatter.format("initial clauses:%n");
		for (int i = 0; i < kb.size(); i++)
			formatter.format("%d: (%s)%n", i, kb.get(i));
		formatter.format("---------------------------%n");
		
		if (search()){
			formatter.format("---------------------------%n");
			formatter.format("node searched=%d%nsolution:%n", nodeSearched);
			System.out.printf("node searched=%d%nsolution:%n", nodeSearched);
			for (String literal : modelSymbolOrder)
				formatter.format("%s=%b%n", literal, model.get(literal) == 1);
		
			formatter.format("true props:%n");
			for (String literal : modelSymbolOrder){
				if (model.get(literal) == 1){
					formatter.format("%s%n", literal);
					System.out.printf("%s%n", literal);
				}
			}
		}
		else{
			formatter.format("---------------------------%n");
			formatter.format("failure, no solution%n");
			System.out.printf("failure, no solution%n");
		}
		
		formatter.close();
		outputFile.print(builder);
		outputFile.close();
	}
	
	public void readFile() throws IOException{
		Scanner keyboard = new Scanner(System.in);
		System.out.printf("Choose DPLL mode.%n1 backtracking alone%n2 backtracking with Unit Clause heuristic%n3 backtracking with Pure heuristic%n4 backtracking with Unit and Pure heuristics%n");
		heuristic = keyboard.nextInt();
		keyboard.nextLine();
		System.out.print("Enter the file name of knowledge base.");
		String name = keyboard.nextLine();
		outputFile = new PrintWriter("transcript_" + name + "_" + heuristic + ".txt");
		File kbSeed = new File(name);
		Scanner kbScanner = new Scanner(kbSeed);
		while (kbScanner.hasNext()){
			String str = kbScanner.nextLine();
			str = str.trim();
			if (str.length() == 0 || str.charAt(0) == '#' || (str.length() >= 4 && str.substring(0, 4).equals("file")) || (str.length() >= 2 && str.substring(0, 2).equals("--")))
				continue;
			StringTokenizer strTokenizer = new StringTokenizer(str);
			ArrayList<String> literals = new ArrayList<String>();
			while (strTokenizer.hasMoreTokens()){
				String token = strTokenizer.nextToken();
				literals.add(token);
				String literal = token.charAt(0) == '-' ? token.substring(1, token.length()) : token;
				if (!model.containsKey(literal))
					model.put(literal, 0);
			}
			kb.put(kb.size(), new Clause(literals));
		}
		kbScanner.close();
	}
	
	public boolean search(){
		formatter.format("model= {", model);
		for (int i = 0; i < modelSymbolOrder.size(); i++){
			if (model.get(modelSymbolOrder.get(i)) != 0)
				formatter.format("'%s': %b, ", modelSymbolOrder.get(i), model.get(modelSymbolOrder.get(i)) == 1);
		}
		if (builder.charAt(builder.length() - 1) != '{')
			builder.delete(builder.length() - 2, builder.length());
		formatter.format("}%n");
		
		nodeSearched++;
		
		boolean falseClauseFlag = false;
		for (int i = 0; i < kb.size() && !falseClauseFlag; i++){
			if (kb.get(i).judgeValue(model) == -1)
				falseClauseFlag = true;
		}
		if (falseClauseFlag)
			return false;
		
		if (complete())
			return true;
		
		if (heuristic == 3 || heuristic == 4){
			int[] symbolValue = Clause.findPureSymbol(kb, model, modelSymbolOrder);
			if (symbolValue[0] != -1){
				formatter.format("pure_symbol on %s=%b%n", modelSymbolOrder.get(symbolValue[0]), symbolValue[1] == 1);
				model.put(modelSymbolOrder.get(symbolValue[0]), symbolValue[1]);
				backupStack.push(symbolValue[0]);
				return search();
			}
		}
		
		if (heuristic == 2 || heuristic == 4){
			int[] clauseSymbolValue = Clause.findUnitClause(kb, model, modelSymbolOrder);
			if (clauseSymbolValue[0] != -1){
				formatter.format("unit_clause on (%s) implies %s=%b%n", kb.get(clauseSymbolValue[0]), modelSymbolOrder.get(clauseSymbolValue[1]), clauseSymbolValue[2] == 1);
				model.put(modelSymbolOrder.get(clauseSymbolValue[1]), clauseSymbolValue[2]);
				backupStack.push(clauseSymbolValue[1]);
				return search();
			}
		}
		
		int i;
		for (i = 0; i < modelSymbolOrder.size(); i++){
			if (model.get(modelSymbolOrder.get(i)) == 0)
				break;
		}
		formatter.format("trying %s=T%n", modelSymbolOrder.get(i));
		model.put(modelSymbolOrder.get(i), 1);
		backupStack.push(i);
		if (search())
			return true;
		else {
			formatter.format("backuptracking%n");
			while (backupStack.peek() != i)
				model.put(modelSymbolOrder.get(backupStack.pop()), 0);
			formatter.format("trying %s=F%n", modelSymbolOrder.get(i));
			model.put(modelSymbolOrder.get(i), -1);
			return search();
		}
	}

	public boolean complete(){
		Collection values = model.values();
		return !values.contains(0);
	}
	
	public static void main(String[] args) throws IOException{
		DPLL object = new DPLL();
	}
}

class Clause{
	ArrayList<String> literals;
	
	public Clause(ArrayList<String> literals){
		this.literals = literals;
	}
	
	/**
		@return the clause is 1 true, -1 false, 0 not fully assigned
	*/
	
	public int judgeValue(HashMap<String, Integer> model){
		int unassignedCount = 0;
		for (String literal : literals){
			int positive = literal.charAt(0) == '-' ? -1 : 1;
			String symbol = positive == 1 ? literal : literal.substring(1, literal.length());
			if (positive * model.get(symbol) == 1)
				return 1;
			else if (model.get(symbol) == 0)
				unassignedCount++;
		}
		return unassignedCount == 0 ? -1 : 0;
	}
	
	/**
		@return {symbol #, value}, if (symbol == -1), there's no pure symbol
	*/
	
	public static int[] findPureSymbol(HashMap<Integer, Clause> kb, HashMap<String, Integer> model, ArrayList<String> modelSymbolOrder){
		for (String symbol : modelSymbolOrder){
			if (model.get(symbol) != 0)
				continue;
			String negSymbol = "-" + symbol;
			boolean symbolFlag = false;
			boolean negSymbolFlag = false;
			for (int i = 0; !(symbolFlag && negSymbolFlag) && i < kb.size(); i++){
				if (kb.get(i).judgeValue(model) == 1)
					continue;
				symbolFlag = kb.get(i).literals.contains(symbol) ? true : symbolFlag;
				negSymbolFlag = kb.get(i).literals.contains(negSymbol) ? true : negSymbolFlag;
			}
			if (symbolFlag ^ negSymbolFlag){
				int value = symbolFlag ? 1 : -1;
				int[] symbolValue = {modelSymbolOrder.indexOf(symbol), value};
				return symbolValue;
			}
			else
				continue;
		}
		int[] symbolValue = {-1, 0};
		return symbolValue;
	}
	
	/**
		@return {clause #, symbol #, value}, if (clause == -1), there's no unit clause
	*/
	
	public static int[] findUnitClause(HashMap<Integer, Clause> kb, HashMap<String, Integer> model, ArrayList<String> modelSymbolOrder){
		for (int i = 0; i < kb.size(); i++){
			if (kb.get(i).judgeValue(model) == 1)
				continue;
			int[] clauseSymbolValue = new int[3];
			int unassignedCount = 0;
			for (String literal : kb.get(i).literals){
				boolean positive = literal.charAt(0) == '-' ? false : true;
				String symbol = positive ? literal : literal.substring(1, literal.length());
				if (model.get(symbol) == 0){
					unassignedCount++;
					if (unassignedCount > 1)
						break;
					int sign = positive ? 1 : -1;
					clauseSymbolValue[0] = i;
					clauseSymbolValue[1] = modelSymbolOrder.indexOf(symbol);
					clauseSymbolValue[2] = sign;
				}	
			}
			if (unassignedCount == 1)
				return clauseSymbolValue;
			else
				continue;
		}
		int[] clauseSymbolValue = {-1, -1, 0};
		return clauseSymbolValue;
	}
	
	public String toString(){
		if (literals.size() == 0)
			return "";
		StringBuilder builder = new StringBuilder();
		Formatter formatter = new Formatter(builder);
		for (String s : literals)
			formatter.format("%s v ", s);
		formatter.close();
		builder.delete(builder.length() - 3, builder.length());
		return builder.toString();
	}
}