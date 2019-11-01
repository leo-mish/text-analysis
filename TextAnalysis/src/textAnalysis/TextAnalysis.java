package textAnalysis;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.stream.*;
import java.util.Collections;


/**
 * Provides various functions for analyzing a section of English text.
 * @author Leo Mishlove
 *
 */
public class TextAnalysis {
	
	/**
	 * Counts the number of words in a piece of text. (Note: not the number of unique words.)
	 * @param sourceFile the file containing the source text
	 * @return the word count for the text
	 * @throws FileNotFoundException
	 */
	public static int countWords(File sourceFile) throws FileNotFoundException{
		Scanner sourceScanner = new Scanner(sourceFile);		//default delimiter (whitespace)
		int wordCount = 0;
		while (sourceScanner.hasNext()) {						//count each word found.
			wordCount++;
			sourceScanner.next();
		}
		sourceScanner.close();
		return wordCount;
	}
	

	/**
	 * Initializes a HashMap with the frequencies of each unique word in the text (case-insensitive). Note: hyphenated
	 * and compounded words (e.g. "e-mail", "ascending-order") are treated as a single word.
	 * @param sourceFile the file containing the source text
	 * @param wordCount the total number of words in the source text (not necessarily the number of unique words)
	 * @return a hash map containing each unique word in the text and the number of times it occurs
	 * @throws FileNotFoundException if source file is not found
	 */
	public static HashMap<String, Integer> calculateWordFrequencies(File sourceFile, int wordCount) throws FileNotFoundException {
		/* HashMap chosen b/c retrieving current count (get) and adding new count (put) are constant-time */
	
		Scanner sourceScanner = new Scanner(sourceFile);
		float loadFactor = 0.75f;
		
		/* # unique words will not exceed total word count, so set initial capacity to avoid rehashing.
		 * add extra padding for the edge case where there are few-to-none repeated words. */
		HashMap<String, Integer> wordFrequencies = new HashMap<String, Integer>((int) (wordCount / loadFactor), loadFactor);	
		
		while (sourceScanner.hasNext()) {
			String currentWord = sourceScanner.next();
			currentWord = convertWordToCompatible(currentWord);		
			
			if (!currentWord.equals("") && wordFrequencies.containsKey(currentWord)) {	
				Integer frequency = wordFrequencies.get(currentWord);	//if word has been seen before, increase its count
				wordFrequencies.put(currentWord, frequency + 1);		//note: put overwrites previous value for that key
			} else if (!currentWord.equals("")){						//if word hasn't been seen before, add it
				wordFrequencies.put(currentWord, 1);					
			} else {													//if word is the empty string, do nothing
			}													
		}
		
		sourceScanner.close();
		return wordFrequencies;
	}
	
	
	/**
	 * Given a map of words to the frequency with which they appear, finds the top [n] most frequent words, where [n]
	 * is the number of most-frequent words desired.
	 * @param wordFrequencies a hashmap containing (word, frequency) pairs, indexed by word
	 * @param wordsDesired the cutoff point for the the number of most frequent words
	 * @return the top [wordsDesired] most frequent words, in descending order with the most-frequent first
	 */
	public static List<String> findMostFrequentWords(HashMap<String, Integer> wordFrequencies, int wordsDesired) {
		List<String> mostFrequentWords = wordFrequencies.entrySet().stream()		// get set of entries, convert to stream
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))		// sort by frequency in descending order
				.limit(wordsDesired)												// limit to only [n] most-frequent words
				.map(e -> e.getKey())												// reduce to only the words themselves
				.collect(Collectors.toList());										// convert to list
		return mostFrequentWords;
	}
	
	
	/**
	 * Finds the last sentence in a text file that contains a given word
	 * @param sourceFile the file containing the source text
	 * @param word the word to search for
	 * @return the last sentence containing the word
	 * @throws FileNotFoundException if source file is not found
	 */
	public static String lastOccurrence(File sourceFile, String word) throws FileNotFoundException {
		Scanner sourceScanner = new Scanner(sourceFile);  
		sourceScanner.useDelimiter("\\.|\\?|!");			//split by ., ?, or ! (end-of-sentence punctuation)
		
		String lastMatch = "";								//most recent sentence found with the given word.
		
		while(sourceScanner.hasNext()) {					
			String currentString = sourceScanner.next();
			if (containsWord(currentString, word)) {		//if matching sentence is found
				lastMatch = currentString;					//then set lastMatch, overwriting any previous match
			}
		}
		
		sourceScanner.close();
		return lastMatch;
	}
	
	/**
	 * Determines if a sentence contains a particular word.
	 * @param sentence a string containing one or more words separated by whitespace
	 * @param searchWord the word to search for within the sentence
	 * @return true if the sentence contains the word, false otherwise
	 */
	private static boolean containsWord(String sentence, String searchWord) {
		String[] words = sentence.split("\\s"); 			//split by whitespace \s (and escape the backslash)
	
		for (String currentWord : words) {
			currentWord = convertWordToCompatible(currentWord);
			if (searchWord.equals(currentWord)) {			//if the word is found, no need to keep looking
				return true;
			}
		}
		
		return false;										//if search whole sentence and no match, word not found
	}
	
	
	/**
	 * Converts word to a search- and comparison-compatible format by removing leading and trailing punctuation 
	 * and converting all letters to lowercase. Ensures equivalence of strings such as "word," and "\"Word".
	 * @param word the word to be converted to compatible format
	 * @return the input word in compatible format (no leading/trailing punctuation, all lowercase)
	 */
	private static String convertWordToCompatible(String word) {
		word = removeLeadingPunctuation(word);
		word = removeTrailingPunctuation(word);
		word = word.toLowerCase();		//de-capitalize so that "Word" and "word" are considered equivalent
		return word;
	}
	
	
	/**
	 * Removes punctuation (non-alphanumeric characters) from the beginning of a word
	 * @param word the word to be processed
	 * @return the input word with leading non-alphanumeric characters removed
	 */
	private static String removeLeadingPunctuation (String word) {		
		int wordStart = 0;			//string index where leading punctuation ends and the word begins
		
		/* loop front-to-back to find the first index where an alphanumeric character occurs */
		while(wordStart < word.length() && !Character.isLetterOrDigit(word.charAt(wordStart))) {
			wordStart++;
		}
		
		word = word.substring(wordStart);
		return word;
	}
	
	/**
	 * Removes punctuation (non-alphanumeric characters) from the end of a word
	 * @param word the word to be processed
	 * @return the input word with trailing non-alphanumeric characters removed
	 */
	private static String removeTrailingPunctuation (String word) {
		int wordEnd = word.length() - 1; 	//string index where trailing punctuation begins and the word ends
		
		/* loop back-to-front to find the last index where an alphanumeric character occurs */
		while(wordEnd >= 0 && !Character.isLetterOrDigit(word.charAt(wordEnd))) {
			wordEnd--;
		}
		
		word = word.substring(0, wordEnd + 1);
		return word;
	}
	
	
	
	/**
	 * Given the text file passage.txt, counts the number of words in the text, gives the 10 most frequently used words
	 * in descending order with most-used first, and gives the last sentence containing the most frequently used word.
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* file path and cutoff point for number of most-used words could also be taken in as arguments */
		File sourceFile = new File("src/passage.txt"); 	
		int wordsDesired = 10;						 	 // only the top 10 most frequent words are desired
		
		try {
			int wordCount = countWords(sourceFile);
			System.out.println("Total words: " + wordCount);
		
			//generate the word/frequency pairs
			HashMap<String, Integer> wordFrequencies = calculateWordFrequencies(sourceFile, wordCount);
			
			// get the most-used words
			List<String> mostUsedWords = findMostFrequentWords(wordFrequencies, wordsDesired);
			

			System.out.println("Most used words are:");
			for (String word : mostUsedWords) {					
				System.out.println(word);
			}
		
			String mostUsedWord = mostUsedWords.get(0);	 // mostUsedWords is ordered high-low; so #1 most used is first in the list
			String lastOccurrence = lastOccurrence(sourceFile, mostUsedWord);
			System.out.println("The last sentence containing \"" + mostUsedWord + "\" is: " + lastOccurrence);
		
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		}
	}
	
	
	
	
	
	

}
