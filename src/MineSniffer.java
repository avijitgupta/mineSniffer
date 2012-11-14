import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;
import aima.core.logic.propositional.algorithms.KnowledgeBase;
import aima.core.logic.propositional.algorithms.PLResolution;

public class MineSniffer {
	private static int N, M;
	private static int board[][];
	private static final int FIND = -2;
	private static final int UNKNOWN = -1;
	private static KnowledgeBase kb;
	private static Queue<String> processedAndUnknown;
	private static Queue<String> process;
	private static Queue<String> mines;
	private static Queue<String> noMines;
	/**
	 * @param args
	 */
	/**
	 * Displays the board
	 */
	public static void displayBoard(){
		for(int i = 0 ; i < N ; i ++){
			for(int j = 0 ; j < M ; j ++){
				System.out.print(board[i][j]+" ");
			}
			System.out.println();
		}
	}
	
	/**
	 * Adds the knowledge of locations that dont contain any mines
	 */
	public static void addNoMineLocationKnowledge(){
		for(int i = 0 ; i < N ; i ++){
			for(int j = 0; j < M ; j ++){
				if(board[i][j] > 0 || board[i][j]==UNKNOWN){
					String fact = "( NOT M"+getRowMajorNumber(i, j)+" )";
					kb.tell(fact);
				}
			}
		}
	}
	
	/**
	 * For any number i in the board, we chose exactly i mined cells out of n Xs present in the neighboring
	 * cells. For example, if there are 3 Xs (say A, B, C) present and the number 2 is written in the cell, 
	 * we generate the following clauses
	 * 
	 * M(A) AND M(B) AND NOT M(C)
	 * M(A) AND M(C) AND NOT M(B)
	 * M(B) AND M(B) AND NOT M(A)
	 * 
	 * We add these to knowledge base
	 */
	public static void addNeighborKnowledge(){
		for(int i = 0 ; i < N ; i ++){
			for(int j = 0; j < M ; j ++){
				if(board[i][j] > 0){
					ArrayList<String> neigh = getXNeighbors(i,j);
					String clauses = getClausalKnowledge(neigh, board[i][j]);
					kb.tell(clauses);
				}
			}
		}
	}
	
	/**
	 * Main method that does the inference from knowledge base. If any new information is inferrend, such as
	 * the presence of mine, the KB is updated accordingly 
	 */
	public static void findMines(){
		boolean newRuleAdded = false;
		do{
			newRuleAdded = false;
			while(!process.isEmpty()){
				String rowmajor = process.peek();
				String query = "M"+rowmajor;
				process.remove();
				boolean foundMine = kb.askWithDpll(query);// Is there a mine at i,j
				if(foundMine){
					mines.add(rowmajor);
					kb.tell(query); // KB, I found a mine. Please add
					while(!processedAndUnknown.isEmpty()){
						process.add(processedAndUnknown.peek()); //Check the new status of mines, given new addition
						processedAndUnknown.remove();
					}
					newRuleAdded = true;
					continue;
				}
				
				query = "( NOT M" + rowmajor+" )";// Is a mine absent at i,j
				
				boolean mineAbsent = kb.askWithDpll(query);// Is there no mine at i,j
				
				if(mineAbsent){
					noMines.add(rowmajor);
					kb.tell(query); // KB, there is no mine at this position
					newRuleAdded = true;
					continue;
				}
				
				processedAndUnknown.add(rowmajor);
				
			}
			if(newRuleAdded){
				while(!processedAndUnknown.isEmpty()){
					process.add(processedAndUnknown.peek()); //Check the new status of mines, given new addition
					processedAndUnknown.remove();
				}
			}
		}while(newRuleAdded);
	}
	
	/**
	 * gets neighbors which are Xs around a cell
	 * @param r
	 * @param c
	 * @return
	 */
	public static ArrayList<String> getXNeighbors(int r, int c){
		ArrayList<String> neigh = new ArrayList<String>();
		if(r-1 >= 0){
			if(board[r-1][c]==FIND){
				neigh.add(getRowMajorNumber(r-1, c));
			}
			if(c-1 >= 0 && board[r-1][c-1]==FIND){
				neigh.add(getRowMajorNumber(r-1, c-1));	
			}
			if(c+1 <M && board[r-1][c+1]==FIND){
				neigh.add(getRowMajorNumber(r-1, c+1));
			}	
		}
		if(c-1 >= 0 && board[r][c-1]==FIND){
			neigh.add(getRowMajorNumber(r, c-1));	
		}
		if(c+1 <M && board[r][c+1]==FIND){
			neigh.add(getRowMajorNumber(r, c+1));
		}
		if(r+1 <N){
			if(board[r+1][c]==FIND){
				neigh.add(getRowMajorNumber(r+1, c));
			}
			if(c-1 >= 0 && board[r+1][c-1]==FIND){
				neigh.add(getRowMajorNumber(r+1, c-1));	
			}
			if(c+1 <M && board[r+1][c+1]==FIND){
				neigh.add(getRowMajorNumber(r+1, c+1));
			}	
		}
		
		return neigh;
	}
	
	/**
	 * Generates a clause which contains all possible combinations of n neighbours
	 * from within the list of neighbors
	 * @param neigh
	 * @param n
	 * @return
	 */
	public static String getClausalKnowledge(ArrayList<String> neigh, int n){
		ArrayList<String> clauses= new ArrayList<String>();
		String result = "";
		boolean used[] = new boolean[neigh.size()];
		for(int i = 0 ; i < n ; i ++){
			used[i]=false;
		}
		nCk(neigh.size(), n, 0, neigh,used, clauses, 0 );
		
		if(clauses.size() ==0) return "";
		
		for(int i = 0 ; i < clauses.size(); i++){
			if(result.length() == 0){
				result+=" ( " + clauses.get(i);
			}
			else{
				result="( " + result;
				result+=" OR " + clauses.get(i) + ") ";
			}
		}
		result+=" )";
		return result;
	}
	
	/**
	 * Generates neighbors choosing k out of n neighbors as positive propositions and n-k negative propositions
	 * @param n
	 * @param k
	 * @param index
	 * @param neigh
	 * @param used
	 * @param clauses
	 * @param level
	 */
	public static void nCk(int n, int k, int index, ArrayList<String>neigh, boolean used[],ArrayList<String> clauses, int level ){
		if(index >n)return;
		
		if(level == k){
			String clause = "";
			for(int i = 0 ; i < n ; i ++){
				if(clause.length()==0 && used[i]){
					clause+="( M" + neigh.get(i)+" ";
				}
				else if(clause.length()==0 && !used[i]){
					clause+="( (NOT M" + neigh.get(i)+") ";
				}
				else if(used[i]){
					clause= "( " + clause;
					clause+=" AND M" + neigh.get(i)+") ";
				}
				else{
					clause= "( " + clause;
					clause+=" AND (NOT M" + neigh.get(i)+") ) ";
				}
					
			}
			clause+=" )";
			clauses.add(clause);
			return;
		}
		for(int i = index; i < n ; i ++  ){
			if(!used[i]){
				used[i] = true;
				nCk(n, k, i+1, neigh, used, clauses, level+1);
				used[i] = false;
			}
		}
	}
	
	/**
	 * Debugging helper
	 * @param q
	 */
	public static void displayQueue(Queue<String> q){
		if(q.isEmpty()){
			System.out.print("None");
		}
		
		while(!q.isEmpty()){
			displayRowMajorToCompatible(q.peek());
			q.remove();
		}
		System.out.println();
	}
	
	public static void displayRowMajorToCompatible(String rowm){
		int rowmajor = Integer.parseInt(rowm);
		int row = rowmajor/M;
		int col = (rowmajor%M) + 1;
		int ar = N - row;
		System.out.print("("+ar+","+col+") ");
	}
	
	public static String getRowMajorNumber(int r, int c){
		int rowmajor;
		rowmajor = r*M + c;
		return ""+rowmajor;
	}
	
	/**
	 * Main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
			if(args.length!=1)  {
		      System.err.println("usage: MineSniffer <filePath> ");
		      return;
		    }		 
			FileInputStream fstream = new FileInputStream(args[0]);
			DataInputStream in = new DataInputStream(fstream);
			String info = in.readLine();
			StringTokenizer st = new StringTokenizer(info, ", ");

			N = Integer.parseInt(st.nextToken());
			M = Integer.parseInt(st.nextToken());
		
			process = new LinkedList<String>();
			board = new int[N][M];
			for(int i = 0 ; i < N ; i ++){
				info = in.readLine();
				st = new StringTokenizer(info, ", ");
				for(int j= 0 ; j < M ; j ++){
					String next = st.nextToken();
					if(next.compareTo("NH")==0){
						board[i][j] = UNKNOWN;
					}
					else if(next.compareTo("X") == 0){
						board[i][j] = FIND;
						process.add(getRowMajorNumber(i,j));
					}
					else{
						board[i][j]=Integer.parseInt(next);
					}
				}
			}
			kb = new KnowledgeBase();
			processedAndUnknown = new LinkedList<String>();
			mines = new LinkedList<String>();
			noMines = new LinkedList<String>();

			long startTime = System.currentTimeMillis();
			addNoMineLocationKnowledge();
			addNeighborKnowledge();
			findMines();
			long endTime = System.currentTimeMillis();
			System.out.print("Unknown (?):  ");
			displayQueue(processedAndUnknown);
			System.out.print("Mines (X):  ");
			displayQueue(mines);
			System.out.print("No Mines:  ");
			displayQueue(noMines);
		    System.out.println("Time: " + (endTime - startTime) + "  milliSeconds");

	}

}
