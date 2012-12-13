package nlp.dipftest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class TextLanguageIdentificationTool {

	
	public static final int NOT_IN_CORPUS_PROFILE_PENALTY=600;
	
	
	public static void main(String[] args) throws LangIdentificationException {
		
		//System.out.println(getLanguageNGramsRanks("ENGLISH","/Users/hcadavid/temp/test1/ThreeGramLanguageIdentifier/db/ngramsBd.sqlite"));
		//System.out.println(getAvailableLanguages("/Users/hcadavid/temp/test1/ThreeGramLanguageIdentifier/db/ngramsBd.sqlite"));
		System.out.println(generateDocumentNGramRanks("/Users/hcadavid/temp/open_source_license.txt"));
		
		
		
		
	}

	
	
	public String indentifyDocumentLanguage(String docPath, String dbPath) throws LangIdentificationException{
		
		List<String> docNgrams=generateDocumentNGramRanks(docPath);
		
		List<String> availableLangs=getAvailableLanguages(dbPath);
		
		for (String lang:availableLangs){
			
		}
	
		return "";
	}
	
	public int compareRankingPrifles(List<String> corpusNgrams,List<String> docNgrams){
		
		int distance=0;
		
		for (int i=0;i<docNgrams.size();i++){
			
			String iethDocNgram=docNgrams.get(i);
			
			if (corpusNgrams.contains(iethDocNgram)){
				distance+=Math.abs(i-corpusNgrams.indexOf(iethDocNgram));
			}
			else{
				distance+=NOT_IN_CORPUS_PROFILE_PENALTY;
			}
		}
		
		return distance;
	}
	
	
	
	
	private static List<String> generateDocumentNGramRanks(String docPath) throws LangIdentificationException{
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(new File(docPath)));
			Hashtable<String,Integer> docNgramsFreqMap=new Hashtable<String, Integer>();
			
			NGramExtractor.genFreqMap(docNgramsFreqMap, br, 3);
			NGramExtractor.genFreqMap(docNgramsFreqMap, br, 4);
			
			TreeSet<NGram> ts=new TreeSet<NGram>();
			
			Enumeration<String> mkeys=docNgramsFreqMap.keys();
			
			
			while (mkeys.hasMoreElements()){
				String key=mkeys.nextElement();
				ts.add(new NGram(key, docNgramsFreqMap.get(key)));
			}
			
			Iterator<NGram> it=ts.iterator();
			
			List<String> res=new LinkedList<String>();
			
			int ngcount=0;
			
			while (it.hasNext() && ngcount<=300){	
				NGram ng=it.next();
				System.out.println(ng.freq);
				res.add(ng.ngram);
				ngcount++;
			}
			
			if (ngcount<300){
				throw new LangIdentificationException("Document is not long enough to determite its language. Only "+ngcount+" extracted.");
			}			
			
			return res;
			
		} catch (FileNotFoundException e) {
			throw new LangIdentificationException("",e);
		} catch (IOException e) {
			throw new LangIdentificationException("",e);
		}
		
	}
	
	
	public static void main2(String[] args) throws IOException, ClassNotFoundException, SQLException {
		
		if (args.length<3){
			System.out.println("Command line Arguments: <document_path> <database_path>");
		}
	
	}
	
	public static List<String> getAvailableLanguages(String dbPath) throws LangIdentificationException{
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
			PreparedStatement stm=conn.prepareStatement("select distinct language from ngrams;");
			
			ResultSet rs=stm.executeQuery();
			
			List<String> res=new LinkedList<String>();
			
			while (rs.next()){
				String lang=rs.getString(1);	
				res.add(lang);
			}
			
			return res;
			
		} catch (ClassNotFoundException e) {
			throw new LangIdentificationException("",e);
		}		
		catch (SQLException e) {
			throw new LangIdentificationException("",e);
		}
		
	}
	
	public static List<String> getLanguageNGramsRanks(String lang, String dbPath) throws LangIdentificationException{
		
		try {
			Class.forName("org.sqlite.JDBC");
			Connection conn = DriverManager.getConnection("jdbc:sqlite:"+dbPath);
			
			PreparedStatement stm=conn.prepareStatement("select ngram from ngrams where language=? order by count desc limit 300");
			stm.setString(1, lang);			
			
			ResultSet rs=stm.executeQuery();
			
			List<String> res=new LinkedList<String>();
			
			while (rs.next()){
				String ngram=rs.getString(1);	
				res.add(ngram);
			}
			
			return res;
			
		} catch (ClassNotFoundException e) {
			throw new LangIdentificationException("",e);			
		} catch (SQLException e) {
			throw new LangIdentificationException("",e);
		}
				

		
	}
	
	
}

class NGram implements Comparable<NGram>{
	String ngram;
	int freq;
	
	public NGram(String ngram, int freq) {
		super();
		this.ngram = ngram;
		this.freq = freq;
	}

	@Override
	public int compareTo(NGram o) {
		return o.freq-this.freq;
	}
}
