package nlp.dipftest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class NGramExtractor {

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		
		if (args.length<3){
			System.out.println("Command line Arguments: <corpus_path> <database_path> <language_name>");
		}
		else{
		
			String inputFilePath=args[0];
			String dbPath=args[1];
			String language=args[2];
			
			Hashtable<String,Integer> ngramsFreqMap=new Hashtable<String, Integer>(10000);
			
			BufferedReader br=new BufferedReader(new FileReader(new File(inputFilePath)));
			
			String line=null;
			
			new ProgressThread(ngramsFreqMap).start();
			
			int lcount=0;
			
			while ((line=br.readLine())!=null)
			{
				StringTokenizer st=new StringTokenizer(line," ,.:?*#");
				lcount++;
				if (lcount%100000==0){
					System.out.println("Line:"+lcount);
				}
				
				while (st.hasMoreTokens()){
					String token=st.nextToken();
					
					if (token.length()>=3 && !isANumber(token)){
						List<String> ngrams=generateNGrams(token, 3);
						for (String ng:ngrams){
							if (ngramsFreqMap.containsKey(ng)){
								ngramsFreqMap.put(ng, ngramsFreqMap.get(ng)+1);
							}
							else{
								ngramsFreqMap.put(ng, 1);	
							}
							
						}
						
					}
					
				}
			}
			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection(dbPath);		
			
			for (String key:ngramsFreqMap.keySet()){
				addNgram(conn, language, key, ngramsFreqMap.get(key));
			}
	
			conn.close();

		}
	}
	
	
		
	
	private static List<String> generateNGrams(String token, int n){
		List<String> out=new LinkedList<String>();
		
		out.add("_"+token.substring(0,n-1));
		
		for (int i=0;i<token.length()-(n-1);i++){
			out.add(token.substring(i,i+n));
		}
		
		out.add(token.substring(token.length()-2,token.length())+"_");
		
		return out;
	}
	
	private static boolean isANumber(String t)
	{
	  return t.matches("-?\\d+(\\.\\d+)?"); 
	}
	
	
	public static void addNgram(Connection conn, String lang,String w, int freq){
		//tablengramsngramsCREATE TABLE ngrams(language VARCHAR(32), ngram VARCHAR(32), count INTEGER)
		try {
			PreparedStatement ps=conn.prepareStatement("insert into ngrams values(?,?,?)");
			ps.setString(1, lang);
			ps.setString(2, w);
			ps.setInt(3, freq);
			
			ps.execute();						
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	/**
	 
	 Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:C:\\sqlite\\libreria.sqlite"); 
	 
	 */
	
}


class ProgressThread extends Thread{
	
	Hashtable<String, Integer> table;

	public ProgressThread(Hashtable<String, Integer> table) {
		super();
		this.table = table;
	}
	
	public void run(){
		boolean done=false;
		int lastSize=-1;
		while (!done){
			System.out.println(table.size());
			if (table.size()==lastSize){
				done=true;
			}
			else{
				lastSize=table.size();
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
}
