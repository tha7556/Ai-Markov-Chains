package TweetGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TweetChain {
	private final static String NIL = "\0";
	private Map<String, Integer> table;
	private TweetDictionary dictionary;
	private ArrayList<String> knownWords;
	private final static String[] startSymbols = new String[]{"#","\"","\'","@","$","(","*","%"};
	private final static String[] endSymbols = new String[]{",",".","\"","\'","!",")","*","?"};
	private int max, min;
	public TweetChain(TweetDictionary dict) {
		this.dictionary = dict;
		this.knownWords = new ArrayList<String>();
		knownWords.add(NIL);
		this.table = new HashMap<String,Integer>();
		max = -1;
		min = Integer.MAX_VALUE;
		System.out.println("Putting tweets into the Table...");
		putAllTweetsInTable();		
		System.out.println("Now ready to write tweets in the style of: "+dict.getTwitterHandle()+"\n");
	}
	public String writeTweet() {
		ArrayList<String> tweet = new ArrayList<String>();
		tweet.add(NIL);
		int size = 0;
		while((size <= 1 ||  !tweet.get(tweet.size()-1).equals(NIL)) && size < 280) {
			ArrayList<String> words = new ArrayList<String>();
			ArrayList<Integer> values = new ArrayList<Integer>();
			String lastWord = tweet.get(tweet.size()-1);
			//System.out.println("last word: "+lastWord);
			for(String word : knownWords) {
				//System.out.println("count for: "+lastWord+"+"+word+" = "+getCountForTransition(lastWord+word));
				if(getCountForTransition(lastWord+word) > 0) {
					words.add(word);
					values.add(getCountForTransition(lastWord+word));
				}
			}
			Random rand = new Random();
			int target = rand.nextInt((max - min) + 1) + min; //Random num between min and max
			int index = 0, value = 0;
			for(; value < target; index++) {
				if(index >= words.size())
					index = 0;
				value += values.get(index);
			}
			index--;
			if(index < 0)
				index = words.size() - 1;
			tweet.add(words.get(index));
			size += words.get(index).length();
		}
		String result = "";
		for(int i = 1; i < tweet.size(); i++) {
			if(i == 1 && tweet.get(i).length() > 1)
				result += tweet.get(i).substring(0,1).toUpperCase() + tweet.get(i).substring(1)+" ";
			else if(!tweet.get(i).equals(NIL))
				result += tweet.get(i) +" ";
		}
		return TweetChain.formatResult(result.trim());
	}
	public void putTweetInTable(String tweet) {
		ArrayList<String> words = dictionary.getWordsFromTweet(tweet);
		for(int i = 0; i < words.size()+1; i++) {
			if(i < words.size() && !knownWords.contains(words.get(i))) { //new word
				knownWords.add(words.get(i).toLowerCase());
			}
			String transition = "";
			if(i == 0) { //first word
				transition = NIL+words.get(i).toLowerCase();
			}
			else if(i == words.size()) { //last word
				transition = words.get(i-1).toLowerCase()+NIL;
			}
			else { //middle words
				transition = words.get(i-1).toLowerCase()+words.get(i).toLowerCase();
			}
			transition = transition.toLowerCase();
			if(getCountForTransition(transition) == 0) { //not in table
				table.put(transition, 1);
				if(1 > max)
					max = 1;
				if(1 < min)
					min = 1;
			}
			else { //already in table
				int num = table.get(transition);
				table.put(transition, num+1);
				if((num+1) > max)
					max = num+1;
				if((num+1) < min)
					min = num+1;
			}
		}
		
	}
	public void putAllTweetsInTable() {
		for(String tweet : dictionary.getTweets()) {
			putTweetInTable(tweet);
		}
	}
	public int getCountForTransition(String transition) {
		if(!table.containsKey(transition)) //transition not in table yet
			return 0;
		else {
			return table.get(transition);
		}
	}
	public Map<String,Integer> getTable() {
		return table;
	}
	public ArrayList<String> getKnownWords() {
		return knownWords;
	}
	public static String formatResult(String tweet) {
		String[] words = tweet.split(" ");
		String result = "";
		for(int i = 0; i < words.length; i++) {
			if(words[i].equals("i"))
				words[i] = "I";
			else if(words[i].indexOf("i\'") != -1)
				words[i] = "I" + words[i].substring(1);
			else if(words[i].equals("america"))
				words[i] = "America";
			
			if(isIn(words[i],startSymbols)) {
				result += words[i];
			}
			else if(i < words.length-1 && isIn(words[i+1],endSymbols)) {
				result += words[i];
			}
			else {
				result += words[i] + " ";
			}
		}
		return result;
	}
	public static boolean isIn(String string, String[] arr) {
		for(String s : arr) 
			if(string.equals(s))
				return true;
			
		return false;
	}
	public static void printTable(TweetChain chain) {
		File file = new File ("chain.csv"); 
		FileWriter fWriter = null;
		try {
			fWriter = new FileWriter (file);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		PrintWriter pWriter = new PrintWriter (fWriter);
		
		pWriter.print(",");
		for(String s : chain.getKnownWords()) {
			String s1 = s;
			if(s.equals(NIL))
				s1 = "NIL";
			s1 = "=\""+s1+"\"";
			pWriter.print(s1+",");
		}
		pWriter.println();
		for(String from : chain.getKnownWords()) {
			String f = from;
			if(from.equals(NIL))
				f = "NIL";
			f = "=\""+f+"\"";
			pWriter.print(f+",");
			for(String to : chain.getKnownWords()) {
				pWriter.print(chain.getCountForTransition(from+to)+",");
			}
			pWriter.println();
		}
		try {
			fWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pWriter.close();
	}
	public static void main(String[] args) {
		//Nifty tweeters: DylDTM manacurves ColIegeStudent abominable_andy
		TweetDictionary dict = new TweetDictionary("DylDTM");
		TweetChain chain = new TweetChain(dict);
		for(int i = 0; i < 150; i++)
			System.out.println(chain.writeTweet() +"\n");
		TweetChain.printTable(chain);
	}
	
}
