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
	
	public static void displayBoard(){
		for(int i = 0 ; i < N ; i ++){
			for(int j = 0 ; j < M ; j ++){
				System.out.print(board[i][j]+" ");
			}
			System.out.println();
		}
	}
	
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
	
	public static void addNeighborKnowledge(){
		for(int i = 0 ; i < N ; i ++){
			for(int j = 0; j < M ; j ++){
				if(board[i][j] > 0){
					ArrayList<String> neigh = getXNeighbors(i,j);
					String clauses = getClausalKnowledge(neigh, board[i][j]);
					kb.tell(clauses);
					//System.out.println(clauses);
				}
			}
		}
	}
	
	public static void findMines(){
		boolean newRuleAdded = false;
		do{
			newRuleAdded = false;
			while(!process.isEmpty()){
				String rowmajor = process.peek();
				String query = "M"+rowmajor;
				process.remove();
				//System.out.print("Resolution started found");
				//boolean foundMine = plr.plResolution(kb, query);// Is there a mine at i,j
				//System.out.print("Resolution ended found");
				boolean foundMine = kb.askWithDpll(query);// Is there a mine at i,j
				if(foundMine){
					mines.add(rowmajor);
					if(!kb.contains(query)){
						kb.tell(query); // Hey KB, I found a mine. Please add
						while(!processedAndUnknown.isEmpty()){
							process.add(processedAndUnknown.peek()); //Check the new status of mines, given new addition
							processedAndUnknown.remove();
						}
						//System.out.print(" added " + query);
					}
					newRuleAdded = true;
					continue;
				}
				
				query = "( NOT M" + rowmajor+" )";// Is a mine absent at i,j
				//System.out.print("Resolution started absent");
				
				boolean mineAbsent = kb.askWithDpll(query);// Is there no mine at i,j
				//System.out.print("Resolution ended absent");
				
				if(mineAbsent){
					noMines.add(rowmajor);
					kb.tell(query); // Hey KB, there is no mine at this position
					newRuleAdded = true;
					//System.out.print(" added " + query);
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
			//System.out.println(clauses.get(i));
			if(result.length() == 0){
				result+=" ( " + clauses.get(i);
			}
			else{
				result="( " + result;
				result+=" OR " + clauses.get(i) + ") ";
			}
		}
		result+=" )";
		//System.out.println(result);
		return result;
	}
	
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

			//displayBoard();
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
