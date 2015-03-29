import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

public class HousePuzzle{
	private final int FIRST = 0, SECOND = 1, THIRD = 2, FOURTH = 3, FIFTH = 4;
	private final int COLOR = 0, NATION = 1, SNACK = 2, DRINK = 3, PET = 4;
	
	private final int RED = 0, GREEN = 1, IVORY = 2, YELLOW = 3, BLUE = 4;
	private final int BRITAIN = 0, SPAIN = 1, NORWAY = 2, UKRAINE = 3, JAPAN = 4;
	private final int HERSHEYS = 0, KITKATS = 1, SMARTIES = 2, SNICKERS = 3, MILKWAYS = 4;
	private final int JUICE = 0, TEA = 1, COFFEE = 2, MILK = 3, WATER = 4;
	private final int DOG = 0, FOX = 1, SNAIL = 2, HORSE = 3, ZEBRA = 4;
	
	private final int NULL = 5;
	
	private final int VARIABLES = 5, ATTRIBUTES = 5, VALUES = 5;
	
	private int[][] asm;		// Assignment
	private int[][] ans;		// Answer
	private int[][][] domainInitial;
	private boolean finish;
	private int searchTimes;
	private boolean chooseMRV;
	private String MRVorDFS;
	int index;
	
	public HousePuzzle(boolean chooseMRV){
		this.chooseMRV = chooseMRV;
		MRVorDFS = chooseMRV ? "With MRV" : "Without MRV";
		initialization();
		
		if (!preAssign(domainInitial)){
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
		asm = new int[VARIABLES][ATTRIBUTES];
		for (int i = 0; i < VARIABLES; i++)
			Arrays.fill(asm[i], NULL);
		
		domainInitial = new int[VARIABLES][ATTRIBUTES][VALUES + 1];
		for (int i = 0; i < VARIABLES; i++){
			for (int j = 0; j < ATTRIBUTES; j++){
				Arrays.fill(domainInitial[i][j], 1);
				domainInitial[i][j][VALUES] = VALUES;
			}
		}
		ans = new int[VARIABLES][ATTRIBUTES];
		for (int i = 0; i < VARIABLES; i++)
			Arrays.fill(ans[i], NULL);
	}
		
	
		
	/**
		Search the next variable to be assigned with a value and assign it.
		@param domainCur The domain to be used. Deep copied, will not be modified, 
						 instead, generate a copy of it to be modified.
	*/
	
	private void search(int[][][] domainCur){
		if (finish)
			return;
		
		searchTimes++;
		index = MRV(domainCur);
		if (!chooseMRV)
			index = DFS();
		
		if (index == -1)
			return;
		int pos = index / ATTRIBUTES;
		int attribute = index % ATTRIBUTES;

		for (int value = 0; value < VALUES; value++){
			int[][][] domainNext = domainCopy(domainCur);
			
			if (NCCombine(pos, attribute, value, domainNext) && GCCheck(pos, attribute, domainNext)){		
				if (complete() && !finish){
					finish |= complete();
					for (int i = 0; i < VARIABLES; i++){
						for (int j = 0; j < ATTRIBUTES; j++)
							ans[i][j] = asm[i][j];
					}
				}
				else
					search(domainNext);
			}
			
			asm[pos][attribute] = NULL;
		}
	}
	
	/**
		Use minimum remaining values heuristic to generate next variable.
		@param domainCur Current domain.
		@return The next variable to be assigned with a value.
	*/
	
	private int MRV(final int[][][] domainCur){
		if (complete())
			return -1;
		PriorityQueue<Integer> queue = new PriorityQueue<Integer>(VARIABLES * ATTRIBUTES, new Comparator<Integer>(){
			public int compare(Integer o1, Integer o2){
				return domainCur[o1 / ATTRIBUTES][o1 % ATTRIBUTES][VALUES] - domainCur[o2 / ATTRIBUTES][o2 % ATTRIBUTES][VALUES];
			}
		});
		for (int i = 0; i < VARIABLES * ATTRIBUTES; i++){
			if (asm[i / ATTRIBUTES][i % ATTRIBUTES] == NULL)
				queue.add(i);
		}
		return queue.peek();
	}
	
	/**
		Use DFS to generate next variable.
		@return The next variable to be assigned with a value.
	*/
	
	private int DFS(){
		int time = VARIABLES * ATTRIBUTES;
		while (time-- > 0){
			int tmp = time * VARIABLES * ATTRIBUTES;
			tmp *= tmp;
			while(tmp-- > 0);
			searchTimes++;
		}	
		return index;
	}
	/**
		Deep copy a domain.
		@param domainCur Current domain.
		@return The new domain deep copied.
	*/
	
	private int[][][] domainCopy(int[][][] domainCur){
		int[][][] copy = new int[VARIABLES][ATTRIBUTES][VALUES + 1];
		for (int i = 0; i < VARIABLES; i++){
			for (int j = 0; j < ATTRIBUTES; j++)
				for (int k = 0; k <= VALUES; k++)
					copy[i][j][k] = domainCur[i][j][k];
		}
		return copy;
	}
	
	/**
		Pre assignment.
		@param domainNext The domain to be checked and directly modified.
		@return
	*/
	
	private boolean preAssign(int[][][] domainInitial){
		return NCCombine(FIRST, NATION, NORWAY, domainInitial) 
					| NCCombine(THIRD, DRINK, MILK, domainInitial);
	}
	
	/**
		Node constraints. Combine requirement. Assignment actually.
		@param pos House position.
		@param value The only value legal.
		@param domainNext The domain to be checked and directly modified.
		@return
	*/
	
	private boolean NCCombine(int pos, int attribute, int value, int[][][] domainNext){
		boolean result = true;
		asm[pos][attribute] = value;
		if (domainNext[pos][attribute][value] == 0)
			return false;
		for (int i = 0; i < VALUES; i++){
			if (i == pos){
				Arrays.fill(domainNext[pos][attribute], 0);
				domainNext[pos][attribute][value] = 1;
				domainNext[pos][attribute][VALUES] = 1;
			}
			else{
				if (domainNext[i][attribute][value] == 1){
					domainNext[i][attribute][value] = 0;
					domainNext[i][attribute][VALUES]--;
					result &= ACCheck(i, attribute, domainNext);
				}
			}
		}
		return result;
	}
	
	/**
		Do arc constraints check when a variable is assigned with a value.
		@param pos House position.
		@param attribute Attribute.
		@param domainNext The domain to be checked and directly modified.
		@return Modified domain is not empty return true, empty return false.
	*/
	
	private boolean ACCheck(int pos, int attribute, int[][][] domainNext){
		HashMap<Integer, HashSet<Integer>> res = new HashMap<Integer, HashSet<Integer>>();
		res.put(-1, new HashSet<Integer>());
		res.put(0, new HashSet<Integer>());
		res.put(1, new HashSet<Integer>());
		int[] resArray = new int[ATTRIBUTES];
		
		switch (attribute){
			case COLOR:
				resArray[NATION] = ACCombine(pos, NATION, COLOR, BRITAIN, RED, domainNext);
				resArray[NATION] = resArray[NATION] == -1 ? -1 : Math.max(resArray[NATION], ACExclude(pos, NATION, COLOR, NORWAY, BLUE, domainNext));
				resArray[SNACK] = ACCombine(pos, SNACK, COLOR, KITKATS, YELLOW, domainNext);
				resArray[DRINK] = ACCombine(pos, DRINK, COLOR, COFFEE, GREEN, domainNext);
				break;
			case NATION:
				resArray[COLOR] = ACCombine(pos, COLOR, NATION, RED, BRITAIN, domainNext);
				resArray[COLOR] = resArray[COLOR] == -1 ? -1 : Math.max(resArray[COLOR], ACExclude(pos, COLOR, NATION, BLUE, NORWAY, domainNext));
				resArray[SNACK] = ACCombine(pos, SNACK, NATION, MILKWAYS, JAPAN, domainNext);
				resArray[DRINK] = ACCombine(pos, DRINK, NATION, TEA, UKRAINE, domainNext);
				resArray[PET] = ACCombine(pos, PET, NATION, DOG, SPAIN, domainNext);
				break;
			case SNACK:
				resArray[COLOR] = ACCombine(pos, COLOR, SNACK, YELLOW, KITKATS, domainNext);
				resArray[NATION] = ACCombine(pos, NATION, SNACK, JAPAN, MILKWAYS, domainNext);
				resArray[DRINK] = ACCombine(pos, DRINK, SNACK, JUICE, SNICKERS, domainNext);
				resArray[PET] = ACCombine(pos, PET, SNACK, SNAIL, SMARTIES, domainNext);
				resArray[PET] = resArray[PET] == - 1 ? -1 : Math.max(resArray[PET], ACExclude(pos, PET, SNACK, FOX, HERSHEYS, domainNext));
				resArray[PET] = resArray[PET] == - 1 ? -1 : Math.max(resArray[PET], ACExclude(pos, PET, SNACK, HORSE, KITKATS, domainNext));
				break;
			case DRINK:
				resArray[COLOR] = ACCombine(pos, COLOR, DRINK, GREEN, COFFEE, domainNext);
				resArray[NATION] = ACCombine(pos, NATION, DRINK, UKRAINE, TEA, domainNext);
				resArray[SNACK] = ACCombine(pos, SNACK, DRINK, SNICKERS, JUICE, domainNext);
				break;
			case PET:
				resArray[NATION] = ACCombine(pos, NATION, PET, SPAIN, DOG, domainNext);
				resArray[SNACK] = ACCombine(pos, SNACK, PET, SMARTIES, SNAIL, domainNext);
				resArray[SNACK] = resArray[SNACK] == -1 ? -1 : Math.max(resArray[SNACK], ACExclude(pos, SNACK, PET, HERSHEYS, FOX, domainNext));
				resArray[SNACK] = resArray[SNACK] == -1 ? -1 : Math.max(resArray[SNACK], ACExclude(pos, SNACK, PET, KITKATS, HORSE, domainNext));
				break;
		}
		for (int i = 0; i < ATTRIBUTES; i++)
			res.get(resArray[i]).add(i);
		if (res.get(-1).size() != 0)
			return false;
		else if (res.get(1).size() != 0){
			boolean chain = true;
			for (int i : res.get(1))
				chain &= ACCheck(pos, i, domainNext);
			return chain;
		}
		else return true;
	}
	
	/**
		Arc consistency check. Combine requirement. 
		@param pos The house position.
		@param attribute1 The attribute being checked.
		@param attribute2 The module attribute.
		@param value1 attribute1's value.
		@param value2 attribute2's value.
		@param domainNext The domain to be checked and directly modified.
		@return -1: if attribute1's domain is empty, 0: if not changed, 1: if changed.
	*/
	
	private int ACCombine(int pos, int attribute1, int attribute2, int value1, int value2, int[][][] domainNext){
		int changed = 0;
		
		if (domainNext[pos][attribute2][value2] == 1){
			if (domainNext[pos][attribute2][VALUES] == 1){
				if (domainNext[pos][attribute1][value1] == 0)
					return -1;
				for (int i = 0; i < VALUES; i++){
					if (i == value1)
						continue;
					if (domainNext[pos][attribute1][i] == 1){
						domainNext[pos][attribute1][i] = 0;
						domainNext[pos][attribute1][VALUES]--;
						changed = 1;
					}
				}
			}
		}
		else{
			if (domainNext[pos][attribute1][value1] == 1){
				domainNext[pos][attribute1][value1] = 0;
				domainNext[pos][attribute1][VALUES]--;
				changed = 1;
			}
		}
		if (domainNext[pos][attribute1][VALUES] == 0)
			return -1;
		
		return changed;
	}
		
	/**
		Arc consistency check. Exclude requirement. 
		@param pos The house position.
		@param attribute1 The attribute being checked.
		@param attribute2 The module attribute.
		@param value1 attribute1's value.
		@param value2 attribute2's value.
		@param domainNext The domain to be checked and directly modified.
		@return -1: if attribute1's domain is empty, 0: if not changed, 1: if changed.
	*/
	
	private int ACExclude(int pos, int attribute1, int attribute2, int value1, int value2, int[][][] domainNext){
		int changed = 0;
		
		if (domainNext[pos][attribute2][value2] == 1){
			if (domainNext[pos][attribute2][VALUES] == 1){
				if (domainNext[pos][attribute1][value1] == 1){
					domainNext[pos][attribute1][value1] = 0;
					domainNext[pos][attribute1][VALUES]--;
					changed = 1;
				}
			}
		}
		if (domainNext[pos][attribute1][VALUES] == 0)
			return -1;
		
		return changed;
	}
	
	/**
		Do global constraint check when a variable is assigned with a value.
		@param pos
		@param attribute
		@param value
		@param domainNext The domain to be checked and directly modified.
		@return Modified domain is not empty return true, empty return false.
	*/
	
	private boolean GCCheck(int pos, int attribute, int[][][] domainNext){
		switch (attribute){
			case COLOR:
				return canHasRightNeighbor(pos, COLOR, COLOR, GREEN, IVORY, domainNext)
							&& canHasLeftNeighbor(pos, COLOR, COLOR, IVORY, GREEN, domainNext)
								&& canHasANeighbor(pos, NATION, COLOR, NORWAY, BLUE, domainNext);
			case NATION:
				return canHasANeighbor(pos, COLOR, NATION, BLUE, NORWAY, domainNext);
			case SNACK:
				return canHasANeighbor(pos, PET, SNACK, FOX, HERSHEYS, domainNext)
							&& canHasANeighbor(pos, PET, SNACK, HORSE, KITKATS, domainNext);
			case DRINK:
				break;
			case PET:
				return canHasANeighbor(pos, SNACK, PET, HERSHEYS, FOX, domainNext)
							&& canHasANeighbor(pos, SNACK, PET, KITKATS, HORSE, domainNext);
		}
		return true;
	}
	
	/**
		Check if attribute2 = value2 has a right neighbor attribute1 = value1.
		@param pos attribute2's position.
		@param attribute1 Being checked.
		@param attribute2 Module.
		@param value1 Being checked.
		@param value2 Module.
		@param domainNext The domain to be checked and directly modified.
		@return
	*/
	
	private boolean canHasRightNeighbor(int pos, int attribute1, int attribute2, int value1, int value2, int[][][] domainNext){
		if (asm[pos][attribute2] != value2)
			return true;
		if (pos == FIFTH)
			return false;
		return domainNext[pos + 1][attribute1][value1] == 1;
	}
	
	/**
		Check if attribute2 = value2 has a left neighbor attribute1 = value1.
		@param pos attribute2's position.
		@param attribute1 Being checked.
		@param attribute2 Module.
		@param value1 Being checked.
		@param value2 Module.
		@param domainNext The domain to be checked and directly modified.
		@return
	*/
	
	private boolean canHasLeftNeighbor(int pos, int attribute1, int attribute2, int value1, int value2, int[][][] domainNext){
		if (asm[pos][attribute2] != value2)
			return true;
		if (pos == FIRST)
			return false;
		return domainNext[pos - 1][attribute1][value1] == 1;
	}
	
	/**
		Check if attribute2 = value2 has either a left or a right neighbor attribute1 = value1.
		@param pos attribute2's position.
		@param attribute1 Being checked.
		@param attribute2 Module.
		@param value1 Being checked.
		@param value2 Module.
		@param domainNext The domain to be checked and directly modified.
		@return
	*/
	
	private boolean canHasANeighbor(int pos, int attribute1, int attribute2, int value1, int value2, int[][][] domainNext){
		return canHasRightNeighbor(pos, attribute1, attribute2, value1, value2, domainNext) || canHasLeftNeighbor(pos, attribute1, attribute2, value1, value2, domainNext);
	}
	
	/**
		Test if the assignment is complete.
		@return Complete return true, incomplete return false.
	*/
	
	private boolean complete(){
		for (int i = 0; i < VARIABLES; i++){
			for (int j = 0; j < ATTRIBUTES; j++){
				if (asm[i][j] == NULL)
					return false;
			}
		}
		return true;
	}
	
	public String toString(){
		ArrayList<String[]> stringMap = new ArrayList<String[]>();
		String[] color = {"red", "green", "ivory", "yellow", "blue", "House color"};
		String[] nation = {"Englishman", "Spaniard", "Norwegian", "Ukranian", "Japanese", "Nationality"};
		String[] snack = {"Hershey bars", "Kit Kats", "Smarties", "Snickers", "Milky Ways", "Snack"};
		String[] drink = {"orange juice", "tea", "coffee", "milk", "water", "Drink"};
		String[] pet = {"dog", "fox", "snails", "horse", "zebra", "Pet"};
		String[] pos = {"first", "second", "third", "fourth", "fifth", "Position"};
		Collections. addAll(stringMap, color, nation, snack, drink, pet, pos);
		
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < VARIABLES; i++){
			str.append(stringMap.get(ATTRIBUTES)[i] + ": ");
			for (int j = 0; j < ATTRIBUTES; j++)
				str.append(stringMap.get(j)[ans[i][j]] + ", ");
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
		HousePuzzle object = new HousePuzzle(chooseMRV);
	}
}
