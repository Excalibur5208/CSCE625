import java.util.LinkedList;
import java.util.Deque;
import java.util.Scanner;
import java.io.*;
import java.util.Iterator;

public class Search{
	int departure;
	int destination;
	int maxID = -1;
	int maxEdgeNumber = -1;
	int[][] matrix;
	Vertices[] array;
	Deque<Integer> deque; 
	int iter;
	int maxFrontierSize;
	int verticesVisited;
	int pathLength;
	double dist2goal;
	PrintWriter outputFile;
	int searchType;
	String searchTypeName;
	
	public Search(int searchType, String dataFileName, int departureX, int departureY, int destinationX, int destinationY) throws IOException{
		
		this.searchType = searchType;
		switch (searchType){
			case 1:
				searchTypeName = "BFS";
				break;
			case 2:
				searchTypeName = "DFS";
				break;
			case 3:
				searchTypeName = "GBFS";
				break;
		}
		
		FileWriter outputFileTxt = new FileWriter(searchTypeName + "_Search.txt", true);
		outputFile = new PrintWriter(outputFileTxt);
		
		// Find maximum ID of the vertices.
		File dataTxt = new File(dataFileName);
		Scanner dataScanner = new Scanner(dataTxt);
		int flagFinished = 0;	// 0 stands for non-int meets 0 time, 1 stands for 1 time, 2 for 2 times
		while (dataScanner.hasNext() && flagFinished <= 1){
			String readLine = dataScanner.nextLine();
			if (readLine.charAt(0) >= 48 && readLine.charAt(0) <= 57)
				maxID++;
			else
				flagFinished++;
		}
		
		// Put the information of edges into Matrix.	
		matrix = new int[maxID + 1][maxID + 1];	
		flagFinished = 0;	// Indicate if vertices scanning finishes.
		while (dataScanner.hasNext() && flagFinished <= 1){
			String readLine = dataScanner.nextLine();
			if (readLine.charAt(0) >= 48 && readLine.charAt(0) <= 57){
				Scanner readLineScanner = new Scanner(readLine);
				maxEdgeNumber = readLineScanner.nextInt();	
				int ID1 = readLineScanner.nextInt();			
				int ID2 = readLineScanner.nextInt();
				matrix[ID1][ID2] = 1;
				matrix[ID2][ID1] = 1;
				readLineScanner.close();
			}
			else
				flagFinished++;
		}
		
		dataScanner.close();
		
		System.out.printf("vertices=%d, edges=%d\n", maxID + 1, maxEdgeNumber + 1);
		outputFile.printf("vertices=%d, edges=%d", maxID + 1, maxEdgeNumber + 1);
		outputFile.println("");
		
		// Construct an array to hold all the attributes of vertices.
		array = new Vertices[maxID + 1];
		for (int i = 0; i <= maxID; i++){
			array[i] = new Vertices();
		}
		dataScanner = new Scanner(dataTxt);
		
		flagFinished = 0;	// Indicate if vertices scanning finishes.
		while (dataScanner.hasNext() && flagFinished <= 1){
			String readLine = dataScanner.nextLine();
			if (readLine.charAt(0) >= 48 && readLine.charAt(0) <= 57){
				Scanner readLineScanner = new Scanner(readLine);
				int ID = readLineScanner.nextInt();
				int X = readLineScanner.nextInt();
				int Y = readLineScanner.nextInt();
				array[ID].ID = ID;	
				array[ID].X = X;
				array[ID].Y = Y;
				array[ID].dist2goal = Math.hypot(Math.abs(X - destinationX), 
												Math.abs(Y - destinationY));
				if (X == departureX && Y == departureY)		// Find departure's ID.
					departure = ID;
				if (X == destinationX && Y == destinationY)	// Find destination's ID.
					destination = ID;
				readLineScanner.close();
			}	
			else
				flagFinished++;
		}
		
		dataScanner.close();
		
		System.out.printf("start=(%d,%d), goal=(%d,%d), vertices: %d and %d", 
							array[departure].X, array[departure].Y, 
							array[destination].X, array[destination].Y,
							departure, destination);
		System.out.println("");
		outputFile.printf("start=(%d,%d), goal=(%d,%d), vertices: %d and %d", 
							array[departure].X, array[departure].Y, 
							array[destination].X, array[destination].Y,
							departure, destination);
		outputFile.println("");
		
		// Initialize deque and departure.
		deque = new LinkedList<Integer>();
		array[departure].discovered = true;	
		array[departure].distance = 0;
		deque.add(departure);
		verticesVisited++;
	}
	
	public void searchProcess(){
		while (!deque.isEmpty() && !array[destination].discovered){
			iter++;
			int current = -1;	// The vertex popped up and being operated.
			double dist2goalMin = Double.MAX_VALUE;	// Find minimum dist2goal in frontier deque.
			switch (searchType){
				case 1:
					current = deque.removeFirst();
					break;
				case 2:
					current = deque.removeLast();
					break;
				case 3:
					Iterator itr = deque.iterator();
					while (itr.hasNext()){
						int candidate = ((Integer)itr.next()).intValue();
						if (array[candidate].dist2goal < dist2goalMin){
							current = candidate;
							dist2goalMin = array[candidate].dist2goal;
						}
					}
					deque.remove(current);
					dist2goalMin = Double.MAX_VALUE;
					break;
			}
			
			if (maxFrontierSize < deque.size())
				maxFrontierSize = deque.size();
			System.out.printf("iter=%d, frontier=%d, popped=%d (%d,%d), depth=%d, dist2goal=%.1f\n", 
								iter, deque.size(), current, array[current].X, array[current].Y, 
								array[current].distance, array[current].dist2goal);
			outputFile.printf("iter=%d, frontier=%d, popped=%d (%d,%d), depth=%d, dist2goal=%.1f\n", 
								iter, deque.size(), current, array[current].X, array[current].Y, 
								array[current].distance, array[current].dist2goal);
			outputFile.println("");
			for (int j = 0; j <= maxID; j++){
				if (matrix[current][j] == 1 && !array[j].discovered){
					array[j].discovered = true;
					array[j].parent = current;
					array[j].distance = array[current].distance + 1;
					deque.add(j);
					verticesVisited++;
					System.out.printf(" push %d (%d,%d)\n", j, array[j].X, array[j].Y);
					outputFile.printf(" push %d (%d,%d)\n", j, array[j].X, array[j].Y);
					outputFile.println("");
				}
			}
		}
	}
	
	// Generate and print put the solution path.
	public void trackback(){
		deque.clear();
		int current = destination;
		deque.add(current);
		while (array[current].parent != -1){
			current = array[current].parent;
			deque.add(current);
		}
		pathLength = deque.size() - 1;
		while (!deque.isEmpty()){
			int pathVertex = deque.removeLast();
			System.out.printf(" vertex %d (%d,%d)\n", 
								pathVertex, array[pathVertex].X, array[pathVertex].Y);
			outputFile.printf(" vertex %d (%d,%d)\n", 
								pathVertex, array[pathVertex].X, array[pathVertex].Y);
			outputFile.println("");
		}
		System.out.println("");
		outputFile.println("");
	}
	
	public void searchAndTrackback(){
		searchProcess();
		
		if (!array[destination].discovered){
			System.out.println("Failure.");
			outputFile.println("Failure.");
		}
		else{
			System.out.println("-----------------");
			outputFile.println("-----------------");
			trackback();
			System.out.println("search algorithm = " + searchTypeName);
			outputFile.println("search algorithm = " + searchTypeName);
			System.out.println("total iterations = " + iter);
			outputFile.println("total iterations = " + iter);
			System.out.println("max frontier size= " + (maxFrontierSize + 1));
			outputFile.println("max frontier size= " + (maxFrontierSize + 1));
			System.out.printf("vertices visited = %d/%d\n", verticesVisited, maxID + 1);
			outputFile.printf("vertices visited = %d/%d\n", verticesVisited, maxID + 1);
			outputFile.println("");
			System.out.println("path length      = " + pathLength);
			outputFile.println("path length      = " + pathLength);
		}
			
		outputFile.println("-------------------------------------------------------------------------");
		outputFile.println("-------------------------------------------------------------------------");
		outputFile.close();
	}
	
	public static void main(String[] args) throws IOException{
		
		Scanner keyboard = new Scanner(System.in);
		
		System.out.print("Select search type. To run BFS, enter 1," + 
						 "\n                    to run DFS, enter 2," + 
						 "\n                    to run GBFS, enter 3. ");
		int searchType = keyboard.nextInt();
		String searchTypeName_ = "";
		switch (searchType){
			case 1:
				searchTypeName_ = "BFS";
				break;
			case 2:
				searchTypeName_ = "DFS";
				break;
			case 3:
				searchTypeName_ = "GBFS";
				break;
			default:
				System.out.println("Error search type. Please run again.");
				System.exit(0);
		}
			
		String dataFileName;
		System.out.print("Select input data file. To use the default name \"data.txt\", enter 1," + 
						 "\n                        to type in your data file name, enter 2. ");
		int dataFileChoice = keyboard.nextInt();
		
		if (dataFileChoice == 2){
			System.out.print("Type in your data file name. ");
			keyboard.nextLine();
			dataFileName = keyboard.nextLine();
		}
		else
			dataFileName = "data.txt";
		
		System.out.println("Enter the coordinates of the departure and the destination.");
		System.out.print(searchTypeName_ + "_nav ATM.graph ");
		int departureX = keyboard.nextInt();
		int departureY = keyboard.nextInt();
		int destinationX = keyboard.nextInt();
		int destinationY = keyboard.nextInt();
		
		Search search = new Search(searchType, dataFileName, departureX, departureY, destinationX, destinationY);	// initialize BFS_Search
		search.searchAndTrackback();
	}
}

class Vertices{
	int ID;
	int X;
	int Y;
	boolean discovered = false;
	int parent = -1;	// -1 stands for has no parent
	int distance = Integer.MAX_VALUE;	// Integer.MAX_VALUE stands for can not be reached from departure
	double dist2goal;
}