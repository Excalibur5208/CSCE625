import java.util.ArrayList;
import java.util.Scanner;
import java.io.*;

public class BlocksWorld{
	private BlocksState initialState;
	private ArrayList<BlocksState> queue;
	private ArrayList<BlocksState> visited;
	private ArrayList<BlocksState> path;
	private int stacksNo;
	private int blocksNo;
	private final BlocksState GOALSTATE;
	private boolean findGoal;
	private BlocksState currentState;
	private String strForPrintWriter;
	private int iter;
	private int maxQueueSize;
	private boolean h0;
	
	/**
		Initialize a BlocksWorld game. 
		@param stacksNo The number of stacks.
		@param blocksNo The number of blocks.
	*/
	
	public BlocksWorld(int stacksNo, int blocksNo, boolean h0){
		this.stacksNo = stacksNo;
		this.blocksNo = blocksNo;
		this.h0 = h0;
		initialState = new BlocksState(stacksNo, blocksNo, h0);		// A blank BlocksState is generated
		initialState.allocateBlocks();		// Randomly allocate blocks into empty initialState.
		queue = new ArrayList<BlocksState>(100);
		visited = new ArrayList<BlocksState>(100);
		path = new ArrayList<BlocksState>(100);
		queue.add(initialState);			// Add the random BlocksState to queue
		
		GOALSTATE = initialState.goalState();
		strForPrintWriter = "";
	}
	
	/**
		Tell if a BlocksState is already in queue. 
		If already in queue, and its depth is smaller, replace original by it.
		@param state The BlocksState to be looked for in queue.
	*/
	
	public boolean queueContains(BlocksState state){
		for (int i = 0; i < queue.size(); i++){
			if (queue.get(i).equals(state)){
				if (queue.get(i).getDepth() > state.getDepth())
					queue.set(i, state);
				return true;
			}
		}
		return false;
	}
	
	/**
		Tell if a BlocksState is already in visited.
		@param state The BlocksState to be looked for in visited.
	*/
	
	public boolean visitedContains(BlocksState state){
		for (int i = 0; i < visited.size(); i++){
			if (visited.get(i).equals(state))
				return true;
		}
		return false;
	}
	
	/**
		Find all the next potential BlocksState, add into queue the BlocksState never encountered before. 
		@param currentState Current BlocksState.
	*/
	
	public void addNextPotentialState(){
		for (int i = 0; i < stacksNo; i++){
			if (!currentState.getStacks().get(i).isEmpty()){
				for (int j = 0; j < stacksNo; j++){
					if (j == i)
						continue;
					BlocksState nextState = currentState.moveBlock(i, j);
					if (!visitedContains(nextState) && !queueContains(nextState))
						queue.add(nextState);
				}
			}
		}
	}
	
	/**
		Search. Find currentState by heuristic, and add next potential state into queue.
	*/
	
	public void search(){
		while (!queue.isEmpty() && !findGoal){
			int minDepthPlusHeuristic = Integer.MAX_VALUE;
			int minDepthPlusHeuristicOrdinal = -1;
			for (int i = 0; i < queue.size(); i++){
				if (queue.get(i).getHeuristic() + queue.get(i).getDepth() < minDepthPlusHeuristic){
					minDepthPlusHeuristic = queue.get(i).getHeuristic() + queue.get(i).getDepth();
					minDepthPlusHeuristicOrdinal = i;
				}
			}
			currentState = queue.remove(minDepthPlusHeuristicOrdinal);
			strForPrintWriter += "iter=" + iter + ", queue=" + queue.size() + 
							 ", f=g+h=" + (currentState.getHeuristic() + currentState.getDepth()) +
							 ", depth=" + currentState.getDepth() + "%n";
			if (maxQueueSize < queue.size())
				maxQueueSize = queue.size();
			if (currentState.equals(GOALSTATE))
				findGoal = true;
			visited.add(currentState);
			addNextPotentialState();
			iter++;
		}
	}
	
	/**
		Find the path from initial state to current state.
		@param Current state.
	*/
	
	public void path(){
		path.add(currentState);
		while (currentState.getPrevious() != null){
			currentState = currentState.getPrevious();
			path.add(0, currentState);
		}
	}
	
	/**
		Print the path store in ArrayList<BlocksState> path.
	*/
	
	public String printPath(){
		if (!findGoal){
			String str = "fail!";
			strForPrintWriter += "fail!%n";
			return str;
		}
		String str = "success! depth=" + currentState.getDepth() +
					 ", total_goal_tests=" + iter +
					 ", max_queue_size=" + maxQueueSize + "\n";
		
		strForPrintWriter += "success! depth=" + currentState.getDepth() +
							 ", total_goal_tests=" + iter +
							 ", max_queue_size=" + maxQueueSize + "%n";
					 
		path.add(currentState);
		while (currentState.getPrevious() != null){
			currentState = currentState.getPrevious();
			path.add(0, currentState);
		}
		
		for (int i = 0; i < path.size(); i++){
			str += path.get(i);
			strForPrintWriter += path.get(i).toStringPrintWriter();
		}
		strForPrintWriter += "------------------------------------------------------------------------%n" + 
							 "------------------------------------------------------------------------%n";
		return str;
	}
	
	public static void main(String[] args) throws IOException{
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter the number of stacks. ");
		int stacksNo = keyboard.nextInt();
		System.out.print("Enter the number of blocks. ");
		int blocksNo = keyboard.nextInt();
		keyboard.nextLine();
		System.out.print("Do you want to use the default heuristic? Enter 'Y' or 'y' for yes. ");
		char c = keyboard.nextLine().charAt(0);
		boolean h0 = false;
		if ( c == 'Y' || c == 'y')
			h0 = true;
		
		BlocksWorld game = new BlocksWorld(stacksNo, blocksNo, h0);
		
		if (h0){
			System.out.println("Use the default heuristic.");
			game.strForPrintWriter += "Use the default heuristic.%n";
		}
		
		System.out.print("initial state:\n" + game.initialState);
		game.strForPrintWriter += "initial state:%n" + game.initialState.toStringPrintWriter();
		game.search();
		System.out.println(game.printPath());
		
		FileWriter outputTxt = new FileWriter("BlocksWorld.txt", true);
		PrintWriter outputFile = new PrintWriter(outputTxt);
		outputFile.printf(game.strForPrintWriter);
		outputFile.close();
	}
	
}