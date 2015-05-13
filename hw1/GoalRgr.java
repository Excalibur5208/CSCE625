import java.util.Queue;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.io.*;
import java.util.Formatter;
import java.util.StringTokenizer;

public class GoalRgr{
	private Set<Oper> opers;
	private Set<String> init;
	private Set<String> goal;
	private Queue<State> queue;
	private Set<State> visited;
	private int iter;
	private Formatter formatter;
	private PrintWriter outputfile;
	
	public GoalRgr(String opersName, String initName) throws IOException{
		opers = new HashSet<Oper>();
		init = new HashSet<String>();
		goal = new HashSet<String>();
		
		queue = new LinkedList<State>();
		visited = new HashSet<State>();
		StringBuilder sb = new StringBuilder();
		formatter = new Formatter(sb);
		FileWriter file = new FileWriter("transcript.txt", true);
		outputfile = new PrintWriter(file);
		
		readGoal();
		formatter.format("initiating goal regression...%n");
		readOpers(opersName);
		readInit(initName);
		regression();
		
		System.out.print(sb.toString());
		formatter.format("-------------------------------------------------%n");
		formatter.close();
		outputfile.print(sb.toString());
		outputfile.close();
	}
	
	private void readOpers(String opersName) throws IOException{
		File opersFile = new File(opersName);
		Scanner opersScanner = new Scanner(opersFile);
		String command = "";
		Oper oper = null;
		while (opersScanner.hasNext()){
			String line = opersScanner.nextLine();
			if (line.startsWith("#") || line.trim().length() == 0)
				continue;
			command = command + line;
			if (line.endsWith("+"))
				continue;
			StringTokenizer tokens = new StringTokenizer(command, ": +");
			String type = tokens.nextToken();
			if (type.equals("OPER")){
				oper = new Oper();
				oper.name = tokens.nextToken();
			}
			else if (type.equals("END"))
				opers.add(oper);
			else {
				Set<String> set = new HashSet<String>();;
				if (type.equals("precond"))
					oper.precond = set;
				else if (type.equals("addlist"))
					oper.addlist = set;
				else if (type.equals("dellist"))
					oper.dellist = set;
				else
					oper.conflict = set;
				while (tokens.hasMoreTokens())
					set.add(tokens.nextToken());
			}
			command = "";
		}
		opersScanner.close();
	}
	
	private void readInit(String initName) throws IOException{
		File initFile = new File(initName);
		Scanner initScanner = new Scanner(initFile);
		while (initScanner.hasNext()){
			String line = initScanner.nextLine();
			if (line.startsWith("#") || line.trim().length() == 0)
				continue;
			init.add(line);
		}
		initScanner.close();
	}
	
	private void readGoal(){
		System.out.print("Enter the goal. ");
		Scanner inScanner = new Scanner(System.in);
		String line = inScanner.nextLine();
		formatter.format("%s%n", line);
		StringTokenizer tokens = new StringTokenizer(line);
		while (tokens.hasMoreTokens())
			goal.add(tokens.nextToken());
	}
	
	private void regression(){
		State state = new State(goal);
		queue.add(state);
		while (!queue.isEmpty()){
			iter++;
			formatter.format("%niter=%d, queue=%d%n", iter, queue.size());
			State stateCur = queue.remove();
			if (stateCur.goalTest(init)){
				formatter.format("solution found!%nplan;%n");
				for (Oper trace: stateCur.plan)
					formatter.format("%s%n", trace.name);
				break;
			}
			formatter.format(" context:%n goal stack: %s%n", printSet(stateCur.goal));
			for (Oper oper: opers){
				String relatedClauses = relatedState(oper, stateCur);
				if (!relatedClauses.equals("") && !hasConflict(oper, stateCur)){
					formatter.format(" considering using %s to achieve %s%n", oper.name, relatedClauses);
					State stateNext = new State(stateCur);
					stateNext.goal.removeAll(oper.addlist);
					stateNext.goal.addAll(oper.precond);
					if (!isVisited(stateNext)){
						queue.add(stateNext);
						visited.add(stateNext);
					}
					stateNext.plan.add(oper);
				}
			}
		}
	}
	
	private boolean isVisited(State state){
		for (State seen : visited){
			if (sameSet(seen.goal, state.goal))
				return true;
		}
		return false;
	}
	
	private static boolean sameSet(Set<String> set1, Set<String> set2){
		if (set1.size() != set2.size())
			return false;
		for (String s : set1){
			if (!set2.contains(s))
				return false;
		}
		return true;
	}
	
	private static String relatedState(Oper oper, State stateCur){
		StringBuilder sb = new StringBuilder();
		for (String s : stateCur.goal){
			if (oper.addlist.contains(s)){
				sb.append(s);
				sb.append(" ");
			}
		}
		if (sb.length() > 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	private static boolean hasConflict(Oper oper, State stateCur){
		for (String s : stateCur.goal){
			if (oper.dellist.contains(s) || oper.conflict.contains(s))
				return true;
		}
		return false;
	}
	
	private static String printSet(Set set){
		String tmp = set.toString();
		return tmp.equals("") ? tmp : tmp.substring(1, tmp.length() - 1);
	}
	
	public static void main(String[] args) throws IOException{
		GoalRgr object = new GoalRgr("blocksworld.opers", "init.kb");
	}
}

class Oper{
	String name;
	Set<String> precond;
	Set<String> addlist;
	Set<String> dellist;
	Set<String> conflict;
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);
		formatter.format("name: %s%n", name);
		formatter.format("precond: %s%n", precond);
		formatter.format("addlist: %s%n", addlist);
		formatter.format("dellist: %s%n", dellist);
		formatter.format("conflict: %s%n", conflict);
		formatter.close();
		return sb.toString();
	}
}

class State{
	Set<String> goal;
	Queue<Oper> plan;
	
	public State(Set<String> goal){
		this.goal = new HashSet<String>(goal);
		plan = new LinkedList<Oper>();
	}
	
	public State(State state){
		goal = new HashSet<String>(state.goal);
		plan = new LinkedList<Oper>(state.plan);
	}
	
	public boolean goalTest(Set<String> init){
		for (String s : goal){
			if (!init.contains(s))
				return false;
		}
		return true;
	}
}