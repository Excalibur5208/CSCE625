import java.util.ArrayList;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.math.BigDecimal;

public class SA{
	private final double TEMPERATURE;
	private final int ITERATION;
	
	private ArrayList<Cities> cityInformation;
	private int cityNumber;
	private int[] initialState;
	private PrintWriter tracingInformation;
	private int iteration;
	private double[] tourDistanceForMatlab; 
	
	private double currentTemperature;
	private int[] currentState;
	private int[] nextState;
	private double currentTourLength;
	private double nextTourLength;
	
	public SA() throws IOException{
		TEMPERATURE = 60;
		ITERATION = 100000;
		
		tracingInformation = new PrintWriter("SA_tracingInformation.txt");
		cityInformation = new ArrayList<Cities>(53);
		loadInputData();
		cityNumber = cityInformation.size();
		tourDistanceForMatlab = new double[ITERATION];
		initialState = new int[cityNumber];
		generateInitialState();
		simulatedAnnealing();

		
		tracingInformation.close();
		print();
	}
	
	/**
		Load input data file into cityInformation.
	*/
	
	public void loadInputData() throws IOException{
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Enter the input file name.");
		String fileName = keyboard.nextLine();
		
		File inputDataTxt = new File(fileName);
		if (!inputDataTxt.exists())
			System.out.println("No such file found. Run again.");
		
		Scanner inputDataScanner = new Scanner(inputDataTxt);
		while (inputDataScanner.hasNext()){
			String str = inputDataScanner.nextLine();
			int nameEndIndex = 0;
			while(str.charAt(nameEndIndex) != ' ')
				nameEndIndex++;
			String name = str.substring(0, nameEndIndex);
			String remainingStr = str.substring(nameEndIndex);
			
			Scanner remainingStrScanner = new Scanner(remainingStr);
			double latitude = remainingStrScanner.nextDouble();
			double longitude = remainingStrScanner.nextDouble();
			remainingStrScanner.close();
			
			Cities city = new Cities(cityInformation.size(), name, latitude, longitude);
			cityInformation.add(city);
		}
		inputDataScanner.close();
	}
	
	/**
		Generate random initial state.
	*/
	
	public void generateInitialState() throws IOException{
		Random rand = new Random();
		ArrayList<Integer> indexArrayList = new ArrayList<Integer>(cityNumber);
		for (int i = 0; i < cityNumber; i++)
			indexArrayList.add(i);
		
		for (int i = 0; i < cityNumber; i++){
			int index = rand.nextInt(indexArrayList.size());
			initialState[i] = indexArrayList.get(index);
			indexArrayList.remove(index);
		}
		
		tracingInformation.printf("TSP %d %.0f%n", ITERATION, TEMPERATURE);
		System.out.printf("TSP %d %.0f%n", ITERATION, TEMPERATURE);
		tracingInformation.printf("initial state, tour length=%.1f miles%n", calculateTourLength(initialState));
		System.out.printf("initial state, tour length=%.1f miles%n", calculateTourLength(initialState));
		for (int i = 0; i < cityNumber; i++){
			tracingInformation.printf("%s ", cityInformation.get(initialState[i]).getName());
			System.out.printf("%s ", cityInformation.get(initialState[i]).getName());
		}
			
		tracingInformation.printf("%n%n");
		System.out.printf("%n%n");
	}
	
	/**
		Randomly generate a nextState.
	*/
	
	public void generateNextState(){
		Random rand = new Random();
		int index1 = rand.nextInt(cityNumber);
		int index2;
		do {
			index2 = rand.nextInt(cityNumber);
		} while (index2 == index1);
		
		nextState = new int [cityNumber];
		for (int i = 0; i < cityNumber; i++)
			nextState[i] = currentState[i];
		int tmp = nextState[index1];
		nextState[index1] = nextState[index2];
		nextState[index2] = tmp;
		
		nextTourLength = calculateTourLength(nextState);
	}
	
	/**
		Calculate the total length of a tour.
		@param tour A tour.
	*/
	
	public double calculateTourLength(int[] tour){
		double tourLength = 0;
		for (int i = 0; i < tour.length; i++)
			tourLength += Cities.distance(cityInformation.get(tour[i]), cityInformation.get(tour[(i + 1) % tour.length]));
		return tourLength;
	}
	
	public void simulatedAnnealing(){
		currentState = initialState;
		currentTourLength = calculateTourLength(currentState);
		currentTemperature = TEMPERATURE;
		
		while (currentTemperature > 0){
			tourDistanceForMatlab[iteration] = currentTourLength;
			
			generateNextState();
			
			double delta = nextTourLength - currentTourLength;
			double probability = Math.exp(-delta / currentTemperature);
			
			tracingInformation.printf("iter=%d len=%.1f newlen=%.1f delta=%.1f temp=%.4f p<%.4f%n", 
									  iteration, currentTourLength, nextTourLength, delta, currentTemperature, probability);
			
			if (delta < 0){
				currentState = nextState;
				currentTourLength = nextTourLength;
				
				tracingInformation.printf("update! len=%.1f%n", currentTourLength);
				for (int i = 0; i < currentState.length; i++)
					tracingInformation.printf("%s ", cityInformation.get(currentState[i]).getName());
				tracingInformation.printf("%n%n");
				
			}
			else{
				 Random rand = new Random();
				 if (rand.nextInt((int)(1 / probability)) == 0){
					currentState = nextState;
					currentTourLength = nextTourLength;
				 }
			}
			
			
			
			BigDecimal currentTemperatureBD = new BigDecimal(Double.toString(currentTemperature));
			BigDecimal TEMPERATUREBD = new BigDecimal(Double.toString(TEMPERATURE));
			BigDecimal ITERATIONBD = new BigDecimal(Double.toString(ITERATION));
			BigDecimal intervalBD = TEMPERATUREBD.divide(ITERATIONBD);
			currentTemperature = currentTemperatureBD.subtract(intervalBD).doubleValue();
			iteration++;
		}
	}
	
	
	public void print() throws IOException{
		System.out.printf("final state, tour length=%.1f miles%n", currentTourLength);
		for (int i = 0; i < cityNumber; i++)
			System.out.printf("%s ", cityInformation.get(currentState[i]).getName());
		System.out.printf("%n%n");
		
/*
		PrintWriter tourDistanceForMatlabPW = new PrintWriter("tourDistanceForMatlab.txt");
		for (int i = 0; i < tourDistanceForMatlab.length; i++)
			tourDistanceForMatlabPW.printf("%.1f ", tourDistanceForMatlab[i]);
		tourDistanceForMatlabPW.printf("%n");
		tourDistanceForMatlabPW.close();
*/
	}
	
	public static void main(String[] args) throws IOException{
		SA object = new SA();
	}
}

class Cities{
	private static final double R = 3961;
	
	private int id;
	private String name;
	private double latitude;
	private double longitude;
	
	public Cities(int id, String name, double latitude, double longitude){
		this.id = id;
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public static double degreesToRadians(double degree){
		return degree * 3.14159 / 180.0;
	}
	
	public static double distance(Cities city1, Cities city2){
		double dlon = city2.longitude - city1.longitude;
		double dlat = city2.latitude - city1.latitude;
		double a = Math.pow(Math.sin(degreesToRadians(dlat) / 2), 2.0) + 
				   Math.cos(degreesToRadians(city1.latitude)) *
				   Math.cos(degreesToRadians(city2.latitude)) * 
				   Math.pow(Math.sin(degreesToRadians(dlon) / 2), 2.0);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return R * c;
	}
	
	/**
		Get the city's name.
	*/
	
	public String getName(){
		return name;
	}
}