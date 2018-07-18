package TweetGenerator;
import java.util.ArrayList;

import twitter4j.*;
import twitter4j.conf.*;
public class TweetDictionary {
	private String twitterHandle;
	private ArrayList<String> tweets;
	public TweetDictionary(String twitterHandle) {
		tweets = TweetDictionary.getAllTweetsFrom(twitterHandle);
		this.twitterHandle = twitterHandle;
	}
	public ArrayList<String> getTweets() {
		return tweets;
	}
	public String getTwitterHandle() {
		return twitterHandle;
	}
	public ArrayList<String> getWordsFromTweet(String tweet) {
		ArrayList<String> result = new ArrayList<String>();
		String[] words = tweet.split(" ");
		for(String s : words) {
			ArrayList<String> temp = new ArrayList<String>();
			int start = 0, current = 0;
			for(; current < s.length(); current++) {
				if(!Character.isLetter(s.charAt(current)) && !Character.isDigit(s.charAt(current)) && !(s.charAt(current) == '\'')) { //current point is a symbol
					if(start != current) {
						temp.add(s.substring(start,current).toLowerCase().trim());
						start = current;
					}
					else {
						temp.add(s.substring(start, current+1).toLowerCase().trim());
						start++;
					}
				}
			}
			temp.add(s.substring(start,current).toLowerCase());
			result.addAll(temp);
		}
		return result;
}
	public static String formatTweetToString(String status) { //remove newlines, extra space, and urls
		String text = status;
		if(text.indexOf("http") != -1) {
			String temp = text.substring(text.indexOf("http"));
			text = text.replace(temp, "");
		}
		while(text.indexOf("\n") != -1) {
			text = text.substring(0,text.indexOf("\n"))+text.substring(text.indexOf("\n")+1);
		}
		while(text.indexOf("\r") != -1) {
			text = text.substring(0,text.indexOf("\r"))+text.substring(text.indexOf("\r")+1);
		}
		return text.trim();
	}
	public static ArrayList<String> getAllTweetsFrom(String twitterHandle) {
		System.out.println("Gathering tweets from: "+twitterHandle+"...");
		ArrayList<String> result = new ArrayList<String>();
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("NkHd3jqeKSSf4zYXvlCmxSvQh");
	    cb.setOAuthConsumerSecret("bflKHYWEHVxYz0kUtIcLT10o2CfsEMbeP1Scqv9A4A2s0PeHTb");
	    cb.setOAuthAccessToken("928369473295437824-Yre5mtZWf73r0wGdWIzrpDdEX310GJU");
	    cb.setOAuthAccessTokenSecret("LK4sf8uvPY2NCKYTMTyySglVOqdRyY5HHCShMhp3X4Q7V");
	    cb.setIncludeMyRetweetEnabled(false);
	    
	    Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		int pageno = 1;		
		while(true) {
			try {
				int size = result.size();
				Paging page = new Paging(pageno++, 100);
				for(Status status : twitter.getUserTimeline(twitterHandle, page)) {
					if(!status.isRetweet() && !status.isTruncated()) {
						String tweet = formatTweetToString(status.getText());
						if(tweet.length() > 0)
							result.add(tweet);
					}
				}
				if(result.size() == size)
					break;
			}
			catch(TwitterException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Gathered: "+result.size()+" tweets");
		return result;
	}
	public static void main(String[] args) {
		//TweetDictionary dict = new TweetDictionary("ColIegeStudent");
		TweetDictionary dict = new TweetDictionary("DylDTM");
		//dict.getWordsFromTweet(1);
		for(String tweet : dict.getTweets()) {
			System.out.println(tweet);
		}
	}
}
