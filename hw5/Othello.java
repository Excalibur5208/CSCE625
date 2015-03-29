import java.util.Scanner;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;
import java.util.Formatter;

public class Othello{
	private int size;
	private char maxColor;
	private char minColor;
	private int depthLimit;
	
	private Board curBoard;
	private int curMove = -1;
	private int stuck;
	
	private Scanner keyboard;
	String[] input;
	
	public Othello(){
		keyboard = new Scanner(System.in);
		input = keyboard.nextLine().split(" ");
		size = Integer.parseInt(input[1]);
		maxColor = input[2].charAt(0);
		minColor = maxColor == 'B' ? 'W' : 'B';
		depthLimit = Integer.parseInt(input[3]);
		
		curBoard = new Board(size, maxColor);
		commands();
	}
	
	public void commands(){
		input = keyboard.nextLine().split(" ");
		while (!input[0].equals("quit")){
			if (input[0].equals("init")){
				curBoard.init();
				System.out.print(curBoard);
			}
			else if (input[0].equals("put")){
				char color = input[1].charAt(0);
				int x = Integer.parseInt(input[2]);
				int y = Integer.parseInt(input[3]);
				if (!curBoard.legalMoves(color).containsKey(x * size + y))
					System.out.println("# illegal input coordinate. Enter again.");
				else{
					curBoard.makeMoves(color, x, y);
					System.out.printf("# score=%d%n", curBoard.utility());
					System.out.print(curBoard);
				}
			}
			else if (input[0].equals("move")){
				char color = input[1].charAt(0);
				System.out.printf("# making move for %c...%n", color);
				move(color);
			}
			else if (input[0].equals("reset")){
				curBoard.clear();
				System.out.print(curBoard);
			}
			else if (input[0].equals("quit")){
				break;
			}
			if (stuck == 2 || curBoard.isFull())
				break;
			input = keyboard.nextLine().split(" ");
		}
		System.out.printf("game over. score=%d%n", curBoard.utility());
	}
	
	public Map.Entry<Integer, Board> miniMax(char color, int depthRemain, Board board){
		if (depthRemain == 0){
			board.utilValue = board.utility();
			return null;
		}
		
		Set<Map.Entry<Integer, Board>> nextEntries = board.legalMoves(color).entrySet();
		if (nextEntries.size() == 0){
			board.utilValue = board.utility();
			return null;
		}
		
		char opponent = color == 'B' ? 'W' : 'B';
		
		for (Map.Entry<Integer, Board> nextEntry : nextEntries)
			miniMax(opponent, depthRemain - 1, nextEntry.getValue());
		
		Comparator<Map.Entry<Integer, Board>> cpr = new Comparator<Map.Entry<Integer, Board>>(){
			public int compare(Map.Entry<Integer, Board> o1, Map.Entry<Integer, Board> o2){
				return o1.getValue().utilValue - o2.getValue().utilValue;
			}
		};
		
		Map.Entry<Integer, Board> cand;
		cand = color == maxColor ? Collections.max(nextEntries, cpr) : Collections.min(nextEntries, cpr);
		board.utilValue = cand.getValue().utilValue;
		
		if (depthRemain == depthLimit){
			StringBuilder builder = new StringBuilder();
			Formatter formatter = new Formatter(builder);
			
			for (Map.Entry<Integer, Board> nextEntry : nextEntries)
				formatter.format("# considering: (%d,%d), mm=%d%n", 
							nextEntry.getKey() / size, nextEntry.getKey() % size, nextEntry.getValue().utilValue);
			formatter.close();
			System.out.print(builder);
			
			return cand;
		}
		
		return null;
	}
	
	public void move(char color){
		Map.Entry<Integer, Board> next = miniMax(color, depthLimit, curBoard);
		
		if (next != null){
			stuck = 0;
			curMove = next.getKey();
			curBoard = next.getValue();
			System.out.printf("(%d %d)%n", curMove / size, curMove % size);
			System.out.printf("# score=%d%n", curBoard.utility());
			System.out.print(curBoard);
		}
		else{
			stuck++;
			if (stuck == 2)
				System.out.println("# Game over");
			else{
				char curColor = color == 'B' ? 'W' : 'B';
				System.out.printf("# No more moves.%nmaking move for %c...%n", curColor);
				move(curColor);
			}
		}
	}

	public static void main(String[] args){
		Othello object = new Othello();
	}	
}