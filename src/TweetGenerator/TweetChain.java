package TweetGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TweetChain {
	private final String NIL = "\0";
	private Map<String, Integer> table;
	private TweetDictionary dictionary;
	private ArrayList<String> knownWords;
	private final String[] startSymbols = new String[]{"#","\"","\'","@","$","(","*","%"};
	private final String[] endSymbols = new String[]{",",".","\"","\'","!",")","*","?"};
	private int max, min;
	public TweetChain(TweetDictionary dict) {
		this.dictionary = dict;
		this.knownWords = new ArrayList<String>();
		knownWords.add(NIL);
		this.table = new HashMap<String,Integer>();
		
		max = -1;
		min = Integer.MAX_VALUE;
		
		putAllTweetsInTable();
		getMax();
		getMin();
		
		
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
			int target = rand.nextInt((getMax() - getMin()) + 1) + getMin(); //Random num between min and max
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
		return result.trim();
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
	public int getMax() {
		if(max == -1) {
			System.out.println("Calculating max...with: "+ knownWords.size()+" words");
			for(String word1 : knownWords) {
				for(String word2 : knownWords) {
					int count = getCountForTransition(word1+word2);
					if(count > max) {
						max = count;
					}
				}
			}
		}
		return max;
	}
	public int getMin() {
		if(min == Integer.MAX_VALUE) {
			System.out.println("Calculating max...with: "+ knownWords.size()+" words");
			for(String word1 : knownWords) {
				for(String word2 : knownWords) {
					int count = getCountForTransition(word1+word2);
					if(count < min) {
						min = count;
					}
				}
			}
		}
		return min;
	}
	public static void main(String[] args) {
		//Nifty tweeters: DylDTM manacurves ColIegeStudent abominable_andy
		TweetDictionary dict = new TweetDictionary("DylDTM");
		TweetChain chain = new TweetChain(dict);
		System.out.println("Starting to create tweets");
		for(int i = 0; i < 150; i++)
			System.out.println(chain.writeTweet());
		
	}
	
}
