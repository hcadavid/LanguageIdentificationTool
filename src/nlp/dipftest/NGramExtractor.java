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
		main2(new String[]{"/Users/hcadavid/corpuses/corpusespanol.txt","/Users/hcadavid/temp/test1/ThreeGramLanguageIdentifier/db/ngramsBd.sqlite","SPANISH"});
		main2(new String[]{"/Users/hcadavid/corpuses/corpusingles.txt","/Users/hcadavid/temp/test1/ThreeGramLanguageIdentifier/db/ngramsBd.sqlite","ENGLISH"});		
	}
	
	
	public static void main2(String[] args) throws IOException, ClassNotFoundException, SQLException {
		
		if (args.length<3){
			System.out.println("Command line Arguments: <corpus_path> <database_path> <language_name>");
		}
		else{
		
			String inputFilePath=args[0];
			String dbPath=args[1];
			String language=args[2];
			
			Hashtable<String,Integer> ngramsFreqMap=new Hashtable<String, Integer>(10000);
			
			BufferedReader br=new BufferedReader(new FileReader(new File(inputFilePath)));						
			
			new ProgressThread(ngramsFreqMap).start();
			
			genFreqMap(ngramsFreqMap, br, 3);
			genFreqMap(ngramsFreqMap, br, 4);
			
			
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+dbPath);		
			
			System.out.println("Inserting "+ngramsFreqMap.size()+" records...");
			int ircount=0;
			for (String key:ngramsFreqMap.keySet()){
				ircount++;
				if (ircount%1000==0){
					System.out.println(ircount+" of "+ngramsFreqMap.size()+".");
				}
				int freq=ngramsFreqMap.get(key);
				if (freq>1000) addNgram(conn, language, key, freq);
			}
	
			conn.close();

		}
	}
	
	
	public static void genFreqMap(Hashtable<String,Integer> ngramsFreqMap, BufferedReader br, int n) throws IOException{

		String line;
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
				
				if (token.length()>=n && !isANumber(token)){
					List<String> ngrams=generateNGrams(token, n);
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
		
	}
	
	public static List<String> generateNGrams(String token, int n){
		List<String> out=new LinkedList<String>();
		
		out.add("_"+token.substring(0,n-1));
		
		for (int i=0;i<token.length()-(n-1);i++){
			out.add(token.substring(i,i+n));
		}
		
		out.add(token.substring(token.length()-(n-1),token.length())+"_");
		
		return out;
	}
	
	private static boolean isANumber(String t)
	{
	  return t.matches("-?\\d+(\\.\\d+)?"); 
	}
	
	
	public static void addNgram(Connection conn, String lang,String w, int freq){
		//tablengramsngrams CREATE TABLE ngrams(language VARCHAR(32), ngram VARCHAR(32), count INTEGER)
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
