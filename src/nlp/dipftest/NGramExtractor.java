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


/**
 * Tool for the population of the Ngram database required by the language identifier for text documents.
 * This tool receives a corpus of a certain language, extracts NGrams, calculate its frequencies, and
 * stores them on a sqlite3 database.
 * 
 * @author HŽctor Fabio Cadavid R.
 *
 * [1] W. B. Cavnar and J. M. Trenkle, "N-Gram-Based Text Categorization,
 * "Proceedings of the 1994 Sym- posium on Document Analysis and Information Retrieval
 * (Univ.of Nevada, Las Vegas, 1994), p. 161.
 *
 */
public class NGramExtractor {

	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		
		if (args.length<5){
			System.out.println("Command line Arguments: <corpus_path> <database_path> <language_name> <ni> <nf>");
			System.out.println("Where <corpus_path> is the absolute path to the corpus in plain text format," +
					"\n<database_path> is the path where the sqlite database file is located (the database file must be " +
					"\na sqlite3 data file, with a single table: ngrams(language VARCHAR(32), ngram VARCHAR(32), count INTEGER))." +
					"\nA ngram database file (ngramsBd.sqlite) with Ngrams extracted from english and spanish corpuses are already " +
					"\nprovided with this software distribution in the 'db' folder." + 
					"\n<language_name> is the name of the language in which the document is written." +
					"\n<ni> and <nf> are the range of Ngrams that will be extracted from the corpus (ni=3,ni=6) whill create" +
					"\nNGrams of size 3,4,5 and 6.");
		}
		else{
		
			String inputFilePath=args[0];
			String dbPath=args[1];
			String language=args[2];
			int ni=Integer.parseInt(args[3]);
			int nf=Integer.parseInt(args[4]);
			
			Hashtable<String,Integer> ngramsFreqMap=new Hashtable<String, Integer>(10000);
			
			
			for (int n=ni;n<=nf;n++){
				BufferedReader br=new BufferedReader(new FileReader(new File(inputFilePath)));										
				new ProgressThread(ngramsFreqMap).start();				
				genFreqMap(ngramsFreqMap, br, n);				
				br.close();
			}
			
			
			
			
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
			StringTokenizer st=new StringTokenizer(line," \"()[],.:?*#+-=\\|@%<>/;ö&");
			lcount++;
			if (lcount%100000==0){
				System.out.println("Line:"+lcount);
			}
			
			while (st.hasMoreTokens()){
				String token=st.nextToken().toLowerCase();
				
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
			System.out.println("N-Gram map size:"+ table.size());
			if (table.size()==lastSize){
				done=true;
			}
			else{
				lastSize=table.size();
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
