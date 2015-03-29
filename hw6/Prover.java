import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.PriorityQueue;
import java.util.Comparator;

public class Prover{
	HashMap<Integer, Clause> kb;
	PriorityQueue<ResolveLabel> rls;
	StringBuilder sb;
	Formatter formatter;
	
	public Prover() throws IOException{
		kb = new HashMap<Integer, Clause>();
		rls = new PriorityQueue<ResolveLabel>(10, new Comparator<ResolveLabel>(){
			public int compare(ResolveLabel o1, ResolveLabel o2){
				return Math.min(kb.get(o1.c1).literals.size(), kb.get(o1.c2).literals.size()) - Math.min(kb.get(o2.c1).literals.size(), kb.get(o2.c2).literals.size());
			}
		});
		sb = new StringBuilder();
		formatter = new Formatter(sb);
		PrintWriter outputFile = new PrintWriter ("transcript.txt");
		
		readFile();
		
		formatter.format("initial clauses%n");
		for (int i = 0; i < kb.size(); i++)
			formatter.format("%d: (%s)%n", i, kb.get(i));
		formatter.format("-----------%n");
		
		if (resolution()){
			System.out.println("success - empty clause!");
			formatter.format("success - empty clause!%n");
			formatter.format("-----------%n");
			formatter.format("proof trace:%n");
			trace(kb.size() - 1, 0, formatter);
		}
			
		else{
			System.out.println("failure");
			formatter.format("failure%n");
		}
		
		formatter.close();
		outputFile.print(sb);
		outputFile.close();
	}
	
	public void trace(int c, int depth, Formatter formatter){
		for (int i = 0; i < depth; i++)
			formatter.format("  ");
		formatter.format("%d: (%s) ", c, kb.get(c));
		int c1 = kb.get(c).c1;
		int c2 = kb.get(c).c2;
		if (c1 == -1)
			formatter.format("input%n");
		else{
			formatter.format("[%d,%d]%n", c1, c2);
			trace(c1, depth + 1, formatter);
			trace(c2, depth + 1, formatter);
		}
	}
	
	public void readFile() throws IOException{
		System.out.print("Enter the file name of knowledge base.");
		Scanner keyboard = new Scanner(System.in);
		String name = keyboard.nextLine();
		File kbFile = new File(name);
		Scanner kbScanner = new Scanner(kbFile);
		while (kbScanner.hasNext()){
			String str = kbScanner.nextLine();
			str = str.trim();
			if (str.length() == 0 || str.charAt(0) == '#' || (str.length() >= 4 && str.substring(0, 4).equals("file")) || (str.length() >= 2 && str.substring(0, 2).equals("--")))
				continue;
			StringTokenizer strTokenizer = new StringTokenizer(str);
			ArrayList<String> literals = new ArrayList<String>();
			while (strTokenizer.hasMoreTokens()){
				String token = strTokenizer.nextToken();
				if (!literals.contains(token))
					literals.add(token);
			}
			Collections.sort(literals);
			Clause clause = new Clause(literals);
			if (!kbContains(clause))
				kb.put(kb.size(), clause);
		}
		kbScanner.close();
	}
	
	public boolean kbContains(Clause clause){
		for (int i = 0; i < kb.size(); i++){
			if (kb.get(i).equals(clause))
				return true;
		}
		return false;
	}
	
	public boolean resolution(){
		for (int i = 0; i < kb.size(); i++){
			for (int j = i + 1; j < kb.size(); j++){
				for (String str : kb.get(i).literals){
					ResolveLabel rl = new ResolveLabel(i, j, str);
					if (Clause.resolvable(rl, kb))
						rls.add(rl);
				}
			}
		}
		while (rls.size() != 0){
			ResolveLabel rl = rls.poll();
			Clause c = Clause.resolve(rl, kb);
			c.c1 = rl.c1;
			c.c2 = rl.c2;
			formatter.format("[Qsize=%d] resolving %d and %d on %s: (%s) and (%s) -> (%s)%n", rls.size(), rl.c1, rl.c2, rl.s, kb.get(rl.c1), kb.get(rl.c2), c);
			if (kbContains(c))
				continue;
			int label = kb.size();
			kb.put(label, c);
			formatter.format("%d: (%s)%n", label, c);
			if (c.literals.size() == 0)
				return true;
			for (int i = 0; i < kb.size(); i++){
				for (String str : c.literals){
					rl = new ResolveLabel(i, label, str);
					if (Clause.resolvable(rl, kb))
						rls.add(rl);
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) throws IOException{
		Prover object = new Prover();
		
	}
}

class Clause{
	ArrayList<String> literals;
	int c1;
	int c2;
	
	public Clause(ArrayList<String> literals){
		this.literals = literals;
		c1 = -1;
		c2 = -1;
	}
	
	public static boolean resolvable(ResolveLabel rl, HashMap<Integer, Clause> kb){
		String s = rl.s;
		Clause c1 = kb.get(rl.c1);
		Clause c2 = kb.get(rl.c2);
		String negS = s.charAt(0) == '-' ? s.substring(1,s.length()) : "-" + s;
		if ((c1.literals.contains(s) && c2.literals.contains(negS)) || (c1.literals.contains(negS) && c2.literals.contains(s)))
			return true;
		return false;
	}
	
	public static Clause resolve(ResolveLabel rl, HashMap<Integer, Clause> kb){
		String s = rl.s;
		String negS = s.charAt(0) == '-' ? s.substring(1, s.length()) : "-" + s;
		Clause c1 = kb.get(rl.c1);
		Clause c2 = kb.get(rl.c2);
		if (c1.literals.contains(negS) && c2.literals.contains(s)){
			String tmp = s;
			s = negS;
			negS = tmp;
		}
		
		ArrayList<String> c = new ArrayList<String>();
		c.addAll(c1.literals);
		c.remove(s);
		for (String str: c2.literals){
			if (!c.contains(str) && !str.equals(negS))
				c.add(str);
		}
		Collections.sort(c);
		return new Clause(c);
	}
	
	public boolean equals(Clause object2){
		if (literals.size() != object2.literals.size())
			return false;
		for (int i = 0; i < literals.size(); i++){
			if (!literals.get(i).equals(object2.literals.get(i)))
				return false;
		}
		return true;
	}
	
	public String toString(){
		if (literals.size() == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		for (int i = 0; i < literals.size(); i++)
			formatter.format("%s v ", literals.get(i));
		formatter.close();
		sb.delete(sb.length() - 3, sb.length());
		return sb.toString();
	}
}

class ResolveLabel{
	int c1;
	int c2;
	String s;
	
	public ResolveLabel(int c1, int c2, String s){
		this.c1 = c1;
		this.c2 = c2;
		this.s = s;
	}
}
