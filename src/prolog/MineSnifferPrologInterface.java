package prolog;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.util.StringTokenizer;

/**
 * Java program to print out prolog assertions after reading a file using JAVA.
 * There assertions are to be fed as input to the prolog engine
 * 
 */
public class MineSnifferPrologInterface {
	private static int N, M;
	private static char board[][];
	
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
		
			board = new char[N][M];
			for(int i = 0 ; i < N ; i ++){
				info = in.readLine();
				st = new StringTokenizer(info, ", ");
				for(int j= 0 ; j < M ; j ++){
					String next = st.nextToken();
					if(next.compareTo("NH")==0){
						board[i][j] = '@';
					}
					else if(next.compareTo("X") == 0){
						board[i][j] = '#';
					}
					else{
						board[i][j]=next.charAt(0); //Value is <10 always
					}
				}
			}
				
			for(int i = 1 ; i <=N ; i ++){
				for(int j = 1 ; j <= M ; j ++){
				    String command = "assert( board(("+ (N -i + 1) +","+j+"),"+board[i-1][j-1]+"))." ;
                    System.out.println(command);
					command = "assert( boardCopy(("+ (N -i + 1) +","+j+"),"+board[i-1][j-1]+"))." ;
					System.out.println(command);
				}
			}
			
	}

}
