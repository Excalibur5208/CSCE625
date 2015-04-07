import java.util.Scanner;
import java.io.*;

public class KBGenerator{
	PrintWriter outputFile;
	
	public KBGenerator() throws IOException{
		outputFile = new PrintWriter("KB.txt");
		facts();
		for (int i = 0; i < 7; i++)
			printith(i, "KBSeed.txt");
		goal();
		outputFile.close();
	}
	
	public void facts(){
		outputFile.println("T0_FaL");
		outputFile.println("T0_FxL");
		outputFile.println("T0_ChL");
		outputFile.println("T0_GrL");
		outputFile.println();
	}
	
	public void printith(int i, String name) throws IOException{
		File kbSeedFile = new File(name);
		Scanner kbScanner = new Scanner(kbSeedFile);
		while (kbScanner.hasNext()){
			String str = kbScanner.nextLine();
			str = str.trim();
			if (str.length() == 0 || str.charAt(0) == '#')
				continue;
			str = str.replace('1', (char)(i + '0' + 1)).replace('0', (char)(i + '0'));
			outputFile.println(str);
		}
		outputFile.println();
	}
	
	public void goal() throws IOException{
		outputFile.println("T7_FaR");
		outputFile.println("T7_FxR");
		outputFile.println("T7_ChR");
		outputFile.println("T7_GrR");
		outputFile.println();
	}
	
	public static void main(String[] args) throws IOException{
		KBGenerator object = new KBGenerator();
	}
}