import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.Scanner;
import java.util.ArrayList;

public class JobPuzzle{
	private final int CHEF = 0, GUARD = 1, NURSE = 2, CLERK = 3, POLICE = 4, TEACHER = 5, ACTOR = 6, BOXER = 7;
	private final int ROBERTA = 0, THELMA = 1, STEVE = 2, PETE = 3, NULL = 4;
	private final int JNO = 8, PNO = 4;
	
	private int[][] domainInitial;
	private int[] asm;		// Assignment
	private int[] ans;		// Answer
	private boolean finish;
	private int[] count;	// Used times for each person
	private int searchTimes;
	private boolean chooseMRV;
	private String MRVorDFS;
	
	public JobPuzzle(boolean chooseMRV){
		this.chooseMRV = chooseMRV;
		MRVorDFS = chooseMRV ? "With MRV" : "Without MRV";
		initialization();
		
		if (!preCheck(domainInitial)){
			System.out.println("Fail in preAssign.");
			return;
		}
		
		search(domainInitial);
		
		if (finish){
			System.out.println("Success.");
			System.out.printf("%s, number of states explored: %d%nSolution:%n", MRVorDFS, searchTimes);
			System.out.printf(toString());
		}
		else
			System.out.println("No solution.");
	}

	
	/**
		Initialization.
	*/
	
	private void initialization(){
		asm = new int[JNO];
		Arrays.fill(asm, NULL);
		
		domainInitial = new int[JNO][PNO + 1];
		for (int i = 0; i < JNO; i++){
			Arrays.fill(domainInitial[i], 1);
			domainInitial[i][PNO] = PNO;
		}
			
		count = new int[PNO];
		ans = new int[JNO];
	}
	
	/**
		Do preCheck by check node consistency.
		@param domainInitial The domain to be checked. Shallow copied, will be modified directly.
		@return Modified domain is not empty return true, empty return false.
	*/
	
	private boolean preCheck(int[][] domainInitial){
		HashSet<Integer> res = new HashSet<Integer>();
		Collections.addAll(res, NC(CHEF, ROBERTA, domainInitial), NC(NURSE, PETE, domainInitial),
								NC(POLICE, PETE, domainInitial), NC(POLICE, ROBERTA, domainInitial),
								NC(TEACHER, PETE, domainInitial), NC(BOXER, ROBERTA, domainInitial));
		if (res.contains(-1))
			return false;
		return true;
	}
	
	/**
		Search the next variable to be assigned with a value.
		@param domainCur The domain to be used. Deep copied, will not be modified, 
						 instead, generate a copy of it to be modified.
	*/
	
	private void search(int[][] domainCur){
		if (finish)
			return;
		
		if (!finish)
			searchTimes++;
		
		int curJ = chooseMRV ? MRV(domainCur) : DFS();
		if (curJ == -1)
			return;
		for (int curP = 0; curP < PNO; curP++){
			if (domainCur[curJ][curP] == 0 || count[curP] >= 2)
				continue;
			asm[curJ] = curP;
			count[curP]++;
			int[][] domainNext = domainCopy(domainCur);
			Arrays.fill(domainNext[curJ], 0);
			domainNext[curJ][curP] = 1;
			domainNext[curJ][PNO] = 1;
			
			if (consistencyCheck(curJ, domainNext)){
				if (complete() && !finish){
					finish |= complete();
					for (int i = 0; i < asm.length; i++)
						ans[i] = asm[i];
				}
				else
					search(domainNext);
			}
			asm[curJ] = NULL;
			count[curP]--;
		}
		return;
	}
	
	/**
		Use minimum remaining values heuristic to generate next variable.
		@param domainCur Current domain.
		@return The next variable to be assigned with a value.
	*/
	
	private int MRV(final int[][] domainCur){
		if (complete())
			return -1;
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(JNO, new Comparator<Integer>(){
			public int compare(Integer o1, Integer o2){
				return domainCur[o1][PNO] - domainCur[o2][PNO];
			}
		});
		for (int i = 0; i < JNO; i++){
			if (asm[i] == NULL)
				queue.add(i);
		}
		return queue.peek();
	}
	
	/**
		Use DFS to generate next variable.
		@return The next variable to be assigned with a value.
	*/
	
	private int DFS(){
		for (int i = 5; i < JNO + 5; i ++){
			int num = i % JNO;
			if (asm[num] == NULL)
				return num;
		}
		return -1;
	}
	
	/**
		Deep copy a domain.
		@param domainCur Current domain.
		@return The new domain deep copied.
	*/
	
	private int[][] domainCopy(int[][] domainCur){
		int[][] copy = new int[JNO][PNO + 1];
		for (int i = 0; i < JNO; i++){
			for (int j = 0; j <= PNO; j++)
				copy[i][j] = domainCur[i][j];
		}
		return copy;
	}
	
	/**
		Do consistency check when a variable is assigned with a value.
		@param domainNext The domain to be checked and directly modified.
		@return Modified domain is not empty return true, empty return false.
	*/
	
	private boolean consistencyCheck(int job, int[][] domainNext){
		HashMap<Integer, HashSet<Integer>> res = new HashMap<Integer, HashSet<Integer>>();
		res.put(-1, new HashSet<Integer>());
		res.put(0, new HashSet<Integer>());
		res.put(1, new HashSet<Integer>());
		int[] resArray = new int[JNO];
		switch (job){
			case CHEF:
				resArray[ACTOR] = AC(ACTOR, job, domainNext);
				resArray[CLERK] = AC(CLERK, job, domainNext);
				resArray[NURSE] = AC(NURSE, job, domainNext);
				resArray[POLICE] = AC(POLICE, job, domainNext);
				break;
			case GUARD:
				break;
			case NURSE:
				resArray[CHEF] = AC(CHEF, job, domainNext);
				break;
			case CLERK:
				resArray[CHEF] = AC(CHEF, job, domainNext);
				break;
			case POLICE:
				resArray[CHEF] = AC(CHEF, job, domainNext);
				break;
			case TEACHER:
				break;
			case ACTOR:
				resArray[CHEF] = AC(CHEF, job, domainNext);
				break;
			case BOXER:
				break;
		}
		for (int i = 0; i < JNO; i++)
			res.get(resArray[i]).add(i);
		if (res.get(-1).size() != 0)
			return false;
		else if (res.get(1).size() != 0){
			boolean chain = true;
			for (int i : res.get(1))
				chain &= consistencyCheck(i, domainNext);
			return chain;
		}
		else return true;
	}
	
	/**
		Node consistency check.
		@param j The variable being checked.
		@param p The value can not choose.
		@domainNext The domain to be checked and modified.
		@return -1: if the modified domain is empty, 0: if not changed, 1: if changed.
	*/
	
	private int NC(int j, int p, int[][] domainNext){
		int changed = 0;
		for (int i = 0; i < PNO; i++){
			if (domainNext[j][i] == 0)
				continue;
			if (i == p){
				changed = 1;
				domainNext[j][i] = 0;
				domainNext[j][PNO]--;
				break;
			}
		}
		if (domainNext[j][PNO] == 0)
			return -1;
		return changed;
	}
	
	/**
		Arc consistency check.
		@param j1 The variable being checked.
		@param j2 The pair variable.
		@domainNext The domain to be checked and modified.
		@return -1: if the modified domain is empty, 0: if not changed, 1: if changed.
	*/
	
	private int AC(int j1, int j2, int[][] domainNext){
		int changed = 0;
		for(int i = 0; i < PNO; i++){
			if (domainNext[j1][i] == 0)
				continue;
			boolean flag = false;
				
			for (int j = 0; j < PNO; j++){
				if (domainNext[j2][j] == 0)
					continue;
				if (i != j){
					flag = true;
					break;
				}
			}
			if (!flag){
				changed = 1;
				domainNext[j1][i] = 0;
				domainNext[j1][PNO]--;
			}
		}
		if (domainNext[j1][PNO] == 0)
			return -1;
		return changed;
	}
		
	/**
		Test if the assignment is complete.
		@return Complete return true, incomplete return false.
	*/
	
	private boolean complete(){
		for (int i : asm){
			if (i == NULL)
				return false;
		}
		return true;
	}
		
	public String toString(){
		ArrayList<String[]> stringMap = new ArrayList<String[]>();
		String[] jobs = {"chef", "guard", "nurse", "clerk", "police officer", "teacher", "actor", "boxer"};
		String[] people = {"Roberta", "Thelma", "Steve", "Pete"};
		Collections. addAll(stringMap, jobs, people);
		
		HashMap<Integer, HashSet<Integer>> sortByPeople = new HashMap<Integer, HashSet<Integer>>();
		for (int i = 0; i < PNO; i++)
			sortByPeople.put(i, new HashSet<Integer>());
		for (int i = 0; i < JNO; i++)
			sortByPeople.get(ans[i]).add(i);
		
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < PNO; i++){
			str.append(stringMap.get(1)[i] + ": ");
			for (int j : sortByPeople.get(i))
				str.append(stringMap.get(0)[j] + ", ");
			str.delete(str.length() - 2, str.length());
			str.append("%n");
		}
		return str.toString();
	}
		
	public static void main(String[] args){
		Scanner keyboard = new Scanner(System.in);
		
		System.out.print("Do you want to use MRV? Yes: y, No: n.");
		char c = keyboard.nextLine().charAt(0);
		while (c != 'y' && c != 'n'){
			System.out.print("Error input. Do you want to use MRV? Yes: y, No: n.");
			c = keyboard.nextLine().charAt(0);
		}
		
		boolean chooseMRV = c == 'y' ? true : false;
		JobPuzzle object = new JobPuzzle(chooseMRV);
	}
}
