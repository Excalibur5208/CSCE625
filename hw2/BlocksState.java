import java.util.ArrayList;
import java.util.Random; 

	/**
		This class stores data about a BlocksState.
	*/	

public class BlocksState{
	private int stacksNo;			// Total number of stacks
	private int blocksNo;			// Total number of blocks
	private ArrayList<ArrayList<Integer>> stacks;	// Blocks are in stacks
	private int heuristic;			// A heuristic
	private int depth;				// The path length from the initial state to the current state
	private BlocksState previous;	// Parent, shallow copy
	private boolean h0;				// Indicate if choose h0 as heuristic
	
	/**
		Generate a empty BlocksState.
		@param stacksNo The number of stacks.
		@param blocksNo The number of blocks.
	*/
	
	public BlocksState(int stacksNo, int blocksNo, boolean h0){
		this.stacksNo = stacksNo;
		this.blocksNo = blocksNo;
		this.h0 = h0;
		stacks = new ArrayList<ArrayList<Integer>>(stacksNo);
		for (int i = 0; i < stacksNo; i++){
			stacks.add(new ArrayList<Integer>(blocksNo));
		}
	}
	
	/**
		Deep copy a random BlocksState.
		@param model The model to be to copy from.
	*/	
	
	public BlocksState(BlocksState model){
		stacksNo = model.stacksNo;
		blocksNo = model.blocksNo;
		h0 = model.h0;
		depth = model.depth;
		heuristic = model.heuristic;
		previous = model.previous;
		stacks = new ArrayList<ArrayList<Integer>>(stacksNo);
		for (int i = 0; i < stacksNo; i++){
			ArrayList<Integer> currentToCopy = new ArrayList<Integer>(blocksNo);
			for (Integer integer : model.stacks.get(i))
				currentToCopy.add(integer);
			stacks.add(currentToCopy);
		}
	}
	
	/**
		Randomly allocate blocks into empty stacks.
	*/	
	
	public void allocateBlocks(){
		Random rand = new Random();
		ArrayList<Integer> remainingBlocks = new ArrayList<Integer>(blocksNo);
		for (int i = 0; i < blocksNo; i++)
			remainingBlocks.add(i);

		for (int i = 0; i < blocksNo; i++){
			int currentBlockOrdinal = rand.nextInt(remainingBlocks.size());
			Integer currentBlock = remainingBlocks.get(currentBlockOrdinal);
			int currentStackOrdinal = rand.nextInt(stacksNo);
			ArrayList<Integer> currentStack = stacks.get(currentStackOrdinal);
			currentStack.add(currentBlock);
			stacks.set(currentStackOrdinal, currentStack);
			remainingBlocks.remove(currentBlockOrdinal);
		}
		calculateHeuristic();
	}
	
	/**
		Move one block from a given stack to another given stack. 
		Keep the old BlocksState, and return a new BlocksState. 
		@param stackFromNo The stack ordinal to move a block from.
		@param stackToNo The stack ordinal to move a block to.
		@return The new BlocksState after moved.
	*/	
	
	public BlocksState moveBlock(int stackFromNo, int stackToNo){
		BlocksState nextState = new BlocksState(this);		// Deep copy the BlocksState
		nextState.previous = this;
		ArrayList<Integer> stackFrom = nextState.stacks.get(stackFromNo);
		ArrayList<Integer> stackTo = nextState.stacks.get(stackToNo);
		Integer currentBlock = stackFrom.remove(stackFrom.size() - 1);
		stackTo.add(currentBlock);
		nextState.stacks.set(stackFromNo, stackFrom);
		nextState.stacks.set(stackToNo, stackTo);
		nextState.depth++;
		nextState.calculateHeuristic();
		return nextState;
	}
	
	/**
		Calculate heuristic.
	*/
	
	public void calculateHeuristic(){
		heuristic = 0;				// Clear heuristic
		int firstIncorrect = -1;	// The first miss placed block
		
		// If the first miss placed block is already in the first stack, find it,
		// and add heuristic by the number of blocks need to be removed to clear this place
		for (int i = 0; i < stacks.get(0).size() && firstIncorrect == -1; i++){
			if (stacks.get(0).get(i) != i){
				heuristic += stacks.get(0).size() - i;
				firstIncorrect = i;
			}
		}
		
		// If the first stack has no miss placed block, set firstIncorrect to stacks.get(0).size()
		if (firstIncorrect == -1)
			firstIncorrect = stacks.get(0).size();
		
		// If choose h0 as heuristic, set heuristic with number of blocks out of place
		if (h0){
			heuristic = blocksNo - firstIncorrect;
			return;
		}
		
		// Add heuristic by the number of miss placed blocks
		heuristic += blocksNo - firstIncorrect;
		
		// Find the place of firstIncorrect, if it is not in the first stack,
		// add heuristic by the number of it's upper blocks
		
		for (int i = 0; i < stacksNo; i++){
			for (int j = 0; j < stacks.get(i).size(); j++){
				if (stacks.get(i).get(j) == firstIncorrect){
					if (i != 0)
						heuristic += stacks.get(i).size() - j - 1;
				}
			}
		}
	
	}
	
	/**
		Get a BlocksState's deep copy stacks.
		@return Stacks.
	*/

	public ArrayList<ArrayList<Integer>> getStacks(){
		ArrayList<ArrayList<Integer>> stacksCopy = new ArrayList<ArrayList<Integer>>(stacksNo);
		for (int i = 0; i < stacksNo; i++){
			ArrayList<Integer> currentToCopy = new ArrayList<Integer>(blocksNo);
			for (Integer integer : stacks.get(i))
				currentToCopy.add(integer);
			stacksCopy.add(currentToCopy);
		}
		return stacksCopy;
	}
	
	/**
		Get a BlocksState's heuristic.
		@return Heuristic.
	*/
	
	public int getHeuristic(){
		return heuristic;
	}
	
	/**
		Get a BlocksState's depth.
		@return Heuristic.
	*/
	
	public int getDepth(){
		return depth;
	}
	
	/**
		Get a BlocksState's previous.
		@return Previous.
	*/
	
	public BlocksState getPrevious(){
		return previous;
	}
	
	/**
		Convert BlocksState into a string.
		@return The string representing the BlocksState.
	*/	
	
	public String toString(){
		String str = "";
		for (int i = 0; i < stacksNo; i++){
			str += (i + 1) + " | ";
			for (int j = 0; j < stacks.get(i).size(); j++)
				str += (char)(stacks.get(i).get(j) + 'A') + " ";
			str += "\n";
		}
		str += "depth: " + depth + ", heuristic: " + heuristic + "\n";
		return str;
	}
	
	/**
		Convert BlocksState into a string, the format is suitable for PrintWriter.
		@return The string representing the BlocksState.
	*/	
	
	public String toStringPrintWriter(){
		String str = "";
		for (int i = 0; i < stacksNo; i++){
			str += (i + 1) + " | ";
			for (int j = 0; j < stacks.get(i).size(); j++)
				str += (char)(stacks.get(i).get(j) + 'A') + " ";
			str += "%n";
		}
		str += "depth: " + depth + ", heuristic: " + heuristic + "%n%n";
		return str;
	}
	
	/**
		Compare if two BlocksState objects are equal.
		@param object2 The BlocksState object to be compared with.
		@return The results if two BlocksState objects are equal.
	*/
	
	public boolean equals(BlocksState object2){
		if (stacksNo != object2.stacksNo || blocksNo != object2.blocksNo)
			return false;
		if (stacks.size() != object2.stacks.size())
			return false;
		for (int i = 0; i < stacks.size(); i++){
			if (stacks.get(i).size() != object2.stacks.get(i).size())
				return false;
			for (int j = 0; j < stacks.get(i).size(); j++){
				if (stacks.get(i).get(j) != object2.stacks.get(i).get(j))
					return false;
			}
		}
		return true;
	}
	
	/**
		Generate the goal BlocksState.
		@return The goal BlocksState.
	*/
	
	public BlocksState goalState(){
		BlocksState goalState = new BlocksState(stacksNo, blocksNo, h0);
		ArrayList<Integer> goalStateArray = new ArrayList<Integer>(blocksNo);
		for (int i = 0; i < blocksNo; i++)
			goalStateArray.add(i);
		goalState.stacks.set(0, goalStateArray);
		return goalState;
	}

}
