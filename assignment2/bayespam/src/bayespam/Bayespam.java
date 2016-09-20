package bayespam;

import java.io.*;
import java.util.*;

public class Bayespam
{
    // This defines the two types of messages we have.
    static enum MessageType
    {
        NORMAL, SPAM
    }

    // This a class with two counters (for regular and for spam)
    static class Multiple_Counter
    {
        int counter_spam    = 0;
        int counter_regular = 0;

        // Increase one of the counters by one
        public void incrementCounter(MessageType type)
        {
            if ( type == MessageType.NORMAL ){
                ++counter_regular;
            } else {
                ++counter_spam;
            }
        }
    }

    // Listings of the two subdirectories (regular/ and spam/)
    private static File[] listing_regular = new File[0];
    private static File[] listing_spam = new File[0];

    // A hash table for the vocabulary (word searching is very fast in a hash table)
    private static Hashtable <String, Multiple_Counter> vocab = new Hashtable <String, Multiple_Counter> ();

    
    // Add a word to the vocabulary
    private static void addWord(String word, MessageType type)
    {
        Multiple_Counter counter = new Multiple_Counter();

        if ( vocab.containsKey(word) ){                  // if word exists already in the vocabulary..
            counter = vocab.get(word);                  // get the counter from the hashtable
        }
        counter.incrementCounter(type);                 // increase the counter appropriately

        vocab.put(word, counter);                       // put the word with its counter into the hashtable
    }


    // List the regular and spam messages
    private static void listDirs(File dir_location)
    {
        // List all files in the directory passed
        File[] dir_listing = dir_location.listFiles();

        // Check that there are 2 subdirectories
        if ( dir_listing.length != 2 )
        {
            System.out.println( "- Error: specified directory does not contain two subdirectories.\n" );
            Runtime.getRuntime().exit(0);
        }

        listing_regular = dir_listing[0].listFiles();
        listing_spam    = dir_listing[1].listFiles();
    }

    
    // Print the current content of the vocabulary
    private static void printVocab()
    {
        Multiple_Counter counter = new Multiple_Counter();

        for (Enumeration<String> e = vocab.keys() ; e.hasMoreElements() ;)
        {   
            String word;
            
            word = e.nextElement();
            counter  = vocab.get(word);
            
            System.out.println( word + " | in regular: " + counter.counter_regular + 
                                " in spam: "    + counter.counter_spam);
        }
    }


    // Read the words from messages and add them to your vocabulary. The boolean type determines whether the messages are regular or not  
    private static void readMessages(MessageType type)
    throws IOException
    {
        File[] messages = new File[0];

        if (type == MessageType.NORMAL){
            messages = listing_regular;
        } else {
            messages = listing_spam;
        }
        
        for (int i = 0; i < messages.length; ++i)
        {
            FileInputStream i_s = new FileInputStream( messages[i] );
            BufferedReader in = new BufferedReader(new InputStreamReader(i_s));
            String line;
            String word;
            
            while ((line = in.readLine()) != null)                      // read a line
            {
                StringTokenizer st = new StringTokenizer(line);         // parse it into words
        
                while (st.hasMoreTokens())                  // while there are stille words left..
                {
                    addWord(st.nextToken(), type);                  // add them to the vocabulary
                }
            }

            in.close();
        }
    }
   
    public static void main(String[] args)
    throws IOException
    {
        // Location of the directory (the path) taken from the cmd line (first arg)
        File dir_location = new File( args[0] );
        
        // Check if the cmd line arg is a directory
        if ( !dir_location.isDirectory() )
        {
            System.out.println( "- Error: cmd line arg not a directory.\n" );
            Runtime.getRuntime().exit(0);
        }

        // Initialize the regular and spam lists
        listDirs(dir_location);

        // Read the e-mail messages
        readMessages(MessageType.NORMAL);
        readMessages(MessageType.SPAM);

        // Print out the hash table
        //printVocab();
        
        /// Is counter_spam the right variable?
        /*double aPrioriSpam, aPrioriNormal;
        aPrioriSpam = vocab.counter_spam/ (vocab.counter_spam + vocab.counter_regular);
        aPrioriNormal = vocab.counter_regular / (vocab.counter_spam + vocab.counter_regular);
        */
        
        
        
        // Now all students must continue from here:
        //
        // 1) A priori class probabilities must be computed from the number of regular and spam messages
        
        double nMessagesRegular = listing_regular.length;
        double nMessagesSpam = listing_spam.length;
        double nMessagesTotal = listing_regular.length + listing_spam.length;
        
        double aPrioriSpam    = nMessagesSpam / nMessagesTotal;
        double aPrioriRegular = nMessagesRegular / nMessagesTotal;
        
        System.out.println("a priori spam:    " + aPrioriSpam);
        System.out.println("a priori regular: " + aPrioriRegular);
        
        
        // 2) The vocabulary must be clean: punctuation and digits must be removed, case insensitive
        
        Enumeration<String> enumKey = vocab.keys();
        while (enumKey.hasMoreElements())
        {
        	String key = enumKey.nextElement();
        	Multiple_Counter counter = vocab.get(key);
        	
        	String newKey = key.replaceAll("[^a-zA-Z]", "");
        	newKey = newKey.toLowerCase();
        	
        	vocab.remove(key);
        	
        	if (newKey.length() > 4)
        	{
        		vocab.put(newKey, counter);
        	}
        }
        
        // 3) Conditional probabilities must be computed for every word
        
        double nWordsRegular 	= 0;
        double nWordsSpam 		= 0;
        enumKey = vocab.keys();
        
        /// calculate the the total amount of spam and regular words
        while (enumKey.hasMoreElements())
        {
        	String key = enumKey.nextElement();
        	nWordsRegular += vocab.get(key).counter_regular;
        	nWordsSpam += vocab.get(key).counter_spam;
        }
        
        System.out.println("number of regular words : " + nWordsRegular);
        System.out.println("number of spam words    : " + nWordsSpam);
        
        
        // 4) A priori probabilities must be computed for every word
        // 5) Zero probabilities must be replaced by a small estimated value
        // 6) Bayes rule must be applied on new messages, followed by argmax classification
        // 7) Errors must be computed on the test set (FAR = false accept rate (misses), FRR = false reject rate (false alarms))
        // 8) Improve the code and the performance (speed, accuracy)
        //
        // Use the same steps to create a class BigramBayespam which implements a classifier using a vocabulary consisting of bigrams
    }
}