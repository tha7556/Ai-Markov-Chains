package TweetGenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class TweetChain {
	private final static String NIL = "\\0";
	private Map<String, Map<String, Integer>> table;
	private Map<String,Integer> startWords;
	private TweetDictionary dictionary;
	private final static String[] startSymbols = new String[]{"#","\"","\'","@","("};
	private final static String[] endSymbols = new String[]{",",".","\"","\'","!",")","*","?"};
	private int max, min, startMax, startMin;
	public TweetChain(TweetDictionary dict) {
		this.dictionary = dict;
		this.table = new HashMap<String,Map<String,Integer>>();
		this.startWords = new HashMap<String,Integer>();
		max = -1;
		min = Integer.MAX_VALUE;
		startMax = -1;
		startMin = Integer.MAX_VALUE;
		System.out.println("Putting tweets into the Table...");
		putAllTweetsInTable();		
		System.out.println("Now ready to write tweets in the style of: "+dict.getTwitterHandle()+"\n");
	}
	public String writeTweet() {
		ArrayList<String> tweet = new ArrayList<String>();
		tweet.add(NIL);
		int size = 0;
		String lastWord = getStartWord();
		String lastTransition = NIL + lastWord;
		size += lastWord.length();
		tweet.add(lastWord);
		
		while((size <= 1 ||  !tweet.get(tweet.size()-1).equals(NIL)) && size < 280) {
			ArrayList<String> words = new ArrayList<String>();
			ArrayList<Integer> values = new ArrayList<Integer>();
			Set<String> keys = getKeysForTransition(lastTransition);
			for(String word : keys) {
					words.add(word);
					values.add(getCountForWordAtTransition(word, lastTransition));
			}
			Random rand = new Random();
			int target = rand.nextInt((max - min) + 1) + min; //Random num between min and max
			int index = 0, value = 0;
			String nextWord = NIL;
			if(!words.get(0).equals(NIL)) {
				for(; value < target; index++) {
					if(index >= words.size())
						index = 0;
					value += values.get(index);
				}
				index--;
				if(index < 0)
					index = words.size() - 1;
				nextWord = words.get(index);
			}
			tweet.add(nextWord);
			size += nextWord.length();
			lastTransition = lastWord + nextWord;
			lastWord = nextWord;
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
	private String getStartWord() {
		Random rand = new Random();
		int target = rand.nextInt((max - min) + 1) + min; //Random num between min and max
		int index = 0, value = 0;
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<Integer> values = new ArrayList<Integer>();
		for(String word : startWords.keySet()) {
				words.add(word);
				values.add(startWords.get(word));
		}
		
		for(; value < target; index++) {
			if(index >= words.size())
				index = 0;
			value += values.get(index);
		}
		index--;
		if(index < 0)
			index = words.size() - 1;
		return words.get(index);
	}
	public void putTweetInTable(String tweet) {
		ArrayList<String> words = dictionary.getWordsFromTweet(tweet);
		String lastTransition = NIL;
		for(int i = 0; i < words.size()+1; i++) {
			
			if(lastTransition.equals(NIL)) { //first word
				lastTransition = NIL+words.get(i);
				int value = 1;
				if(startWords.containsKey(words.get(i))) 
					value = startWords.get(words.get(i))+1;
				startWords.put(words.get(i), value);
				
				if(startWords.get(words.get(i)) > startMax)
					startMax = startWords.get(words.get(i));
				if(startWords.get(words.get(i)) < startMin)
					startMin = startWords.get(words.get(i));
			}
			else if(i == words.size()) { //last word
				put(lastTransition,NIL);
			}
			else { //middle words
				put(lastTransition,words.get(i));
				lastTransition = words.get(i-1)+words.get(i);
			}
				
		}
		
	}
	private Set<String> getKeysForTransition(String transition) {
		if(table.containsKey(transition))
			return table.get(transition).keySet();
		Set<String> newSet = new HashSet<String>();
		newSet.add(NIL);
		return newSet;
	}
	private void put(String transition, String word) {
		int value = 1;
		if(table.containsKey(transition)) { //transition already in table
			if(table.get(transition).containsKey(word)) { //word already associated with transition
				value = table.get(transition).get(word) + 1;
				table.get(transition).put(word, value);
			}
			else { //transition not associated with word
				table.get(transition).put(word, value);
			}
		}
		else { //transition not in table
			Map<String, Integer> map = new HashMap<String,Integer>();
			map.put(word, value);
			table.put(transition, map);
		}
		if(value > max) 
			max = value;
		if(value < min) 
			min = value;
	}
	private void putAllTweetsInTable() {
		for(String tweet : dictionary.getTweets()) {
			putTweetInTable(tweet);
		}
	}
	public int getCountForWordAtTransition(String word, String transition) {
		if(table.containsKey(transition) && table.get(transition).containsKey(word))
			return table.get(transition).get(word);
		else 
			return 0;
	}
	public Map<String,Map<String,Integer>> getTable() {
		return table;
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
		
		
		for(String from : chain.getTable().keySet()) {
			String f = from.trim();
			if(from.equals(NIL))
				f = "NIL";
			pWriter.print(" ");
			for(String to : chain.getTable().get(from).keySet()) {
				String t = to.trim();
				if(to.equals(NIL))
					t = "NIL";
				pWriter.print(t+" ");
			}
			pWriter.println();
			
			pWriter.print(f+" ");
			for(String to : chain.getTable().get(from).keySet()) {
				pWriter.print(chain.getCountForWordAtTransition(to,from)+" ");
			}
			pWriter.println("\n");
		}
		try {
			fWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pWriter.close();
	}
	public static void main(String[] args) {
		//Nifty tweeters: DylDTM manacurves ColIegeStudent abominable_andy
		TweetDictionary dict = new TweetDictionary("UNCWilmington");
		TweetChain chain = new TweetChain(dict);
		for(int i = 0; i < 1000; i++) {
			System.out.println(chain.writeTweet() +"\n");
		}
		System.out.println("\nDone Writing Tweets!");
		TweetChain.printTable(chain);
	}
	
}
