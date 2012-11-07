package prolog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import com.declarativa.interprolog.PrologEngine;
import com.declarativa.interprolog.PrologOutputListener;
import com.declarativa.interprolog.SWISubprocessEngine;
import com.declarativa.interprolog.TermModel;
import com.declarativa.interprolog.XSBSubprocessEngine;
import java.util.StringTokenizer;


public class MineSnifferPrologInterface {
	private static int N, M;
	private static char board[][];
	private static String xsbPath;
	private static Process xsbEngine;
	private static InputStream xsbRecvStream;
	private static OutputStream xsbSendStream;
	private static BufferedReader xsbOutputReceiver;
	private static BufferedWriter xsbCommandWriter;
	private static String prologFileName ="";
	private static PrologOutputListener prologOutput;
	
	public static void main(String[] args) throws Exception {
			if(args.length!=3)  {
		      System.err.println("usage: MineSniffer <filePath>  <prolog-file-path> <xsbPath>");
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
			prologFileName = args[1];
			xsbPath = args[2];
			
			//displayBoard();
			long startTime = System.currentTimeMillis();
			PrologEngine engine = new XSBSubprocessEngine(xsbPath);
			
			//prologOutput = new XSBOutputListener();
			
			boolean consulted = engine.consultAbsolute(new File(prologFileName));
			System.out.println("Consulted = " + consulted);
			for(int i = 1 ; i <= N ; i ++){
				for(int j = 1 ; j <= M ; j ++){
					String command = "assert( board(("+i+","+j+"),"+board[i-1][j-1]+"))" ;
					boolean added = engine.deterministicGoal(command);
					System.out.println(command );
					command = "assert( boardCopy(("+i+","+j+"),"+board[i-1][j-1]+"))" ;
					System.out.println(command );
					//System.out.print("integerField(("+i+","+j+")) ");/*
					
					//engine.command(command);
				}
				
			}
			
			/*for(int i = 1 ; i <= N ; i ++){
				for(int j = 1 ; j <= M ; j ++){
					
					boolean result = engine.deterministicGoal("integerField(("+i+","+j+"))");
				    if(result == true){
						System.out.print("Int ");
					}
					result = engine.deterministicGoal("unknownField(("+i+","+j+"))");
					if(result == true){
						System.out.print("? ");
					}
					result = engine.deterministicGoal("nohintField(("+i+","+j+"))");
					if(result == true){
						System.out.print("NH ");
					}
				}
				System.out.println();
			}
			*/
		/*	Object bindings[] = engine.deterministicGoal("board((2,2), X)", "[string(X)]");
			if(bindings!=null){
				String X = (String) bindings[0];
				System.out.print("X = " + X);
			}*/
			long endTime = System.currentTimeMillis();
			

	}

}
