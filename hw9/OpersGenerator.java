import java.util.Formatter;
import java.io.*;

public class OpersGenerator{
	String[] valuesPool;
	Formatter formatter;
	PrintWriter outputfile;
	
	public OpersGenerator() throws IOException{
		valuesPool = new String[]{"a", "b", "c", "d", "table"};
		StringBuilder sb = new StringBuilder();
		formatter = new Formatter(sb);
		
		formatter.format("# OPER pickup%n");
		for (int i = 0; i < valuesPool.length;i++){
			String v1 = valuesPool[i];
			if (v1.equals("table"))
				continue;
			for (int j = 0; j < valuesPool.length;j++){
				String v2 = valuesPool[j];
				if (v2.equals(v1))
					continue;
				pickup(v1, v2);
			}
		}
		
		formatter.format("# OPER puton%n");
		for (int i = 0; i < valuesPool.length;i++){
			String v1 = valuesPool[i];
			if (v1.equals("table"))
				continue;
			for (int j = 0; j < valuesPool.length;j++){
				String v2 = valuesPool[j];
				if (v2.equals(v1))
					continue;
				puton(v1, v2);
			}
		}
		
		formatter.close();
		outputfile = new PrintWriter("blocksworld.opers");
		outputfile.print(sb);
		outputfile.close();
	}
	
	private void pickup(String v1, String v2) throws IOException{
		formatter.format("OPER pickup(%s,%s)%n", v1, v2);
		formatter.format("precond: clear(%s) on(%s,%s) gripper_empty()%n", v1, v1, v2);
		formatter.format("addlist: holding(%s)", v1);
		if (!v2.equals("table"))
			formatter.format(" clear(%s)", v2);
		formatter.format("%n");
		formatter.format("dellist: clear(%s) on(%s,%s) gripper_empty()%n", v1, v1, v2);
		formatter.format("conflict:");
		for (int i = 0; i < valuesPool.length;i++){
			String partner = valuesPool[i];
			if (partner.equals(v1))
				continue;
			if (!partner.equals(v2))
				formatter.format(" on(%s,%s)", v1, partner);
			if (!partner.equals("table"))
				formatter.format(" on(%s,%s)", partner, v1);
		}
		formatter.format(" +%n");
		for (int i = 0; i < valuesPool.length;i++){
			String partner = valuesPool[i];
			if (!partner.equals(v1) && !partner.equals("table"))
				formatter.format(" holding(%s)", partner);
		}
		formatter.format("%n");
		formatter.format("END%n%n");
	}
	
	private void puton(String v1, String v2) throws IOException{
		formatter.format("OPER puton(%s,%s)%n", v1, v2);
		formatter.format("precond: holding(%s)",  v1);
		if (!v2.equals("table"))
			formatter.format(" clear(%s)",v2);
		formatter.format("%n");
		formatter.format("addlist: clear(%s) on(%s,%s) gripper_empty()%n", v1, v1, v2);
		formatter.format("dellist: holding(%s)", v1);
		if (!v2.equals("table"))
			formatter.format(" clear(%s)", v2);
		formatter.format("%n");
		formatter.format("conflict:");
		for (int i = 0; i < valuesPool.length;i++){
			String partner = valuesPool[i];
			if (partner.equals(v1))
				continue;
			if (!partner.equals(v2))
				formatter.format(" on(%s,%s)", v1, partner);
			if (!partner.equals("table"))
				formatter.format(" on(%s,%s)", partner, v1);
		}
		formatter.format(" +%n");
		for (int i = 0; i < valuesPool.length;i++){
			String partner = valuesPool[i];
			if (!partner.equals(v1) && !partner.equals("table"))
				formatter.format(" holding(%s)", partner);
		}
		formatter.format("%n");
		formatter.format("END%n%n");
	}
	
	public static void main(String[] args) throws IOException{
		OpersGenerator object = new OpersGenerator();
	}
}