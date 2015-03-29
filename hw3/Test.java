import java.io.*;
import java.util.Scanner;
import java.math.BigDecimal;

public class Test{
	public static void main(String[] args){
		double TEMPERATURE = 1;
		double ITERATION = 100;
		double currentTemperature = TEMPERATURE;
		
		while (currentTemperature > 0){
			
			BigDecimal currentTemperatureBD = new BigDecimal(Double.toString(currentTemperature));
			BigDecimal TEMPERATUREBD = new BigDecimal(Double.toString(TEMPERATURE));
			BigDecimal ITERATIONBD = new BigDecimal(Double.toString(ITERATION));
			BigDecimal intervalBD = TEMPERATUREBD.divide(ITERATIONBD);
			currentTemperature = currentTemperatureBD.subtract(intervalBD).doubleValue();
		}
		System.out.println(currentTemperature);
//		System.out.println(intervalBD);
	}
}