import java.util.HashMap;
import java.util.Arrays;
import java.util.Formatter;

public class Board{
	private char[][] state;
	private int size;
	private char maxColor;
	private char minColor;
	public int utilValue;
	
	public Board(int size, char maxColor){
		this.size = size;
		this.maxColor = maxColor;
		minColor = maxColor == 'B' ? 'W' : 'B';
		
		state = new char[size][size];
		for (int i = 0; i < size; i++)
			Arrays.fill(state[i], '.');
	}
		
	public Board(Board b){
		size = b.size;
		maxColor = b.maxColor;
		minColor = maxColor == 'B' ? 'W' : 'B';
		
		state = new char[size][size];
		for (int i = 0; i < size; i++){
			for (int j = 0; j < size; j++)
				state[i][j] = b.state[i][j];
		}
	}
	
	public void init(){
		state[(size - 1) / 2][(size - 1) / 2] = minColor;
		state[(size - 1) / 2][(size - 1) / 2 + 1] = maxColor;
		state[(size - 1) / 2 + 1][(size - 1) / 2] = maxColor;
		state[(size - 1) / 2 + 1][(size - 1) / 2 + 1] = minColor;
	}
	
	public void clear(){
		for (int i = 0; i < size; i++)
			Arrays.fill(state[i], '.');
	}
	
	public HashMap<Integer, Board> legalMoves(char color){
		HashMap<Integer, Board> ans = new HashMap<Integer, Board>();
		for (int i = 0; i < size; i++){
			for (int j = 0; j < size; j++){
				if (state[i][j] != '.')
					continue;
				Board tmp = new Board(this);
				tmp.makeMoves(color, i, j);
				int utilTmp = tmp.utility();
				if ((color == maxColor && utilTmp - 1 != utility()) || (color == minColor && utilTmp + 1 != utility()))
					ans.put(i * size + j, tmp);
			}
		}
		return ans;
	}
	
	public void makeMoves(char color, int x, int y){	
		char opponent = color == 'B' ? 'W' : 'B';
		state[x][y] = color;
		for (int i = 0; i < 8; i++)
			flip(color, opponent, x, y, i);
	}
	
	private void flip(char color, char opponent, int x, int y, int caseNo){
		int flag = 0;
		int p, q;
		for (int i = 1; i < state.length - 1; i++){
			switch (caseNo){
				case 0:
					p = x; q = y - i; break;
				case 1:
					p = x; q = y + i; break;
				case 2:
					p = x - i; q = y; break;
				case 3:
					p = x + i; q = y; break;
				case 4:
					p = x - i; q = y - i; break;
				case 5:
					p = x - i; q = y + i; break;
				case 6:
					p = x + i; q = y + i; break;
				default:
					p = x + i; q = y - i; break;
					}
			if (p < 0 || p >= size || q < 0 || q >= size)
				break;
			if (state[p][q] == opponent)
				flag = 1;
			else if (state[p][q] == color && flag == 1){
				flag = 2;
				break;
			}
			else
				break;
		}
		if (flag == 2){
			for (int i = 1; i < state.length - 1; i++){
				switch (caseNo){
				case 0:
					p = x; q = y - i; break;
				case 1:
					p = x; q = y + i; break;
				case 2:
					p = x - i; q = y; break;
				case 3:
					p = x + i; q = y; break;
				case 4:
					p = x - i; q = y - i; break;
				case 5:
					p = x - i; q = y + i; break;
				case 6:
					p = x + i; q = y + i; break;
				default:
					p = x + i; q = y - i; break;
					}
				if (state[p][q] == opponent)
					state[p][q] = color;
				else
					break;
			}
		}
	}
	
	public int utility(){
		int score = 0;
		for (int i = 0; i < size; i++){
			for (int j = 0; j < size; j++){
				if (state[i][j] == maxColor)
					score++;
				else if (state[i][j] == minColor)
					score--;
			}
		}
		return score;
	}
	
	public boolean isFull(){
		for (int i = 0; i < size; i++){
			for (int j = 0; j < size; j++){
				if (state[i][j] == '.')
					return false;
			}
		}
		return true;
	}

	public String toString(){
		StringBuilder builder = new StringBuilder();
		Formatter formatter = new Formatter(builder);
		
		for (int i = 0; i < size; i++){
			formatter.format("# ");
			for (int j = 0; j < size; j++)
				formatter.format("%c ", state[i][j]);
			formatter.format("%n");
		}
		formatter.close();
		return builder.toString();
	}
}