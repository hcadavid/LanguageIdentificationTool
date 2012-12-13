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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TextLanguageIdentificationTool {

	
	public static final int NOT_IN_CORPUS_PROFILE_PENALTY=1000;
	public static final int NI=3;
	public static final int NF=4;
	
	public static int nGramProfileSize=1000;
	
	
	public static void main(String[] args) throws LangIdentificationException {
		
		if (args.length<3){
			System.out.println("Command line arguments required: <Document_Path> <NGramsDatabasePath> <NGramProfilesSize>");
		}
		else{
			String documentPath=args[0];
			String dbPath=args[1];
			nGramProfileSize=Integer.parseInt(args[2]);			
			System.out.println("Identified Language:"+identifyDocumentLanguage(documentPath, dbPath));			
		}
		
	}

	
	
	public static String identifyDocumentLanguage(String docPath, String dbPath) throws LangIdentificationException{
		
		List<String> docNgrams=generateDocumentNGramRanks(docPath,NI,NF);
		
		List<String> availableLangs=getAvailableLanguages(dbPath);
		
		int maxDistance=Integer.MAX_VALUE;
		
		String bestFittedLanguage="UNKNOWN";
		
		for (String lang:availableLangs){
			int dist=ngramProfilesDistance(getLanguageNGramsRanks(lang, dbPath), docNgrams);
			if (dist<maxDistance){
				maxDistance=dist;
				bestFittedLanguage=lang;
			}
			System.out.println("Distance to "+lang+" profile:"+dist);
		}
	
		return bestFittedLanguage;
	}
	
	
	public static int ngramProfilesDistance(List<String> corpusNgrams,List<String> docNgrams){
		
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
	
	
	
	
	private static List<String> generateDocumentNGramRanks(String docPath, int ni, int nf) throws LangIdentificationException{
		
		try {
			
			Hashtable<String,Integer> docNgramsFreqMap=new Hashtable<String, Integer>();
			
			for (int n=ni; n<=nf;n++){
				BufferedReader br=new BufferedReader(new FileReader(new File(docPath)));
				NGramExtractor.genFreqMap(docNgramsFreqMap, br, n);
				br.close();	
			}
									
				
			ArrayList<NGram> ngl=new ArrayList<NGram>();
			
						
			Enumeration<String> mkeys=docNgramsFreqMap.keys();
			
			int i=0;
			while (mkeys.hasMoreElements()){
								
				String key=mkeys.nextElement();
				ngl.add(new NGram(key, docNgramsFreqMap.get(key)));
				
			}
			
			
			Iterator<NGram> it=ngl.iterator();
			
			List<String> res=new LinkedList<String>();
			
			int ngcount=0;
			
			while (it.hasNext() && ngcount<=nGramProfileSize){	
				NGram ng=it.next();
				res.add(ng.ngram);
				ngcount++;
			}
			
			if (ngcount<nGramProfileSize){
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
			
			PreparedStatement stm=conn.prepareStatement("select ngram from ngrams where language=? order by count desc limit ?");
			stm.setString(1, lang);									
			stm.setInt(2, nGramProfileSize);
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
