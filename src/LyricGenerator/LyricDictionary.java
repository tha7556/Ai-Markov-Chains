package LyricGenerator;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LyricDictionary {
	private ArrayList<String> lyrics;
	public LyricDictionary(ArrayList<String> lyrics) {
		this.lyrics = lyrics;
	}
	public ArrayList<String> getLyrics() {
		return lyrics;
	}
	
	public ArrayList<String> getWordsFromLyric(String lyric) {
		ArrayList<String> result = new ArrayList<String>();
		String[] words = lyric.split(" ");
		for(String s : words) {
			ArrayList<String> temp = new ArrayList<String>();
			int start = 0, current = 0;
			for(; current < s.length(); current++) {
				if(!Character.isLetter(s.charAt(current)) && !Character.isDigit(s.charAt(current)) && !(s.charAt(current) == '\'') && !(s.charAt(current) == '’')) { //current point is a symbol
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
	public static String formatLyricToString(String lyric) { //remove newlines, extra space, and urls
		String text = lyric;
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
	public static ArrayList<String> getLyricsFrom(String folder) {
		File fold = new File(folder);
		ArrayList<String> result = new ArrayList<String>();
		if(fold.isDirectory()) {
			File[] fileNames = fold.listFiles();
			for(File file : fileNames) {
				result.addAll(getLyricsFrom(file.getAbsolutePath()));
			}
		}
		else if(fold.isFile()) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fold.getPath()),"UTF-8"));
				while(reader.ready()) {
					String line = reader.readLine().trim().toLowerCase();
					if(line.length() > 0 && !line.contains("[") && !result.contains(line))
						result.add(line);
				}
				reader.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	public static void main(String[] args) {
		ArrayList<String> lyrics = LyricDictionary.getLyricsFrom("artists");
		LyricDictionary dict = new LyricDictionary(lyrics);
		MarkovChain chain = new MarkovChain(dict);
		for(int i = 0; i < 15; i++) {
			System.out.println(chain.write());
		}
	}
}
