package Preprocessor;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.StopTokenizerFactory;
import com.aliasi.tokenizer.Tokenization;
import com.aliasi.tokenizer.TokenizerFactory;
import com.google.common.collect.ImmutableSet;

/**
 * Created by wiragotama on 11/4/15.
 * Seperti namanya, untuk preprocess
 */
public class Preprocessor {

    /**
     * Tokenize String, including lowercasing token and stopwords removal
     * @param input
     * @param stopWords
     * @return Array of tokenized String
     */
    public static List<String> tokenize(String input, List<String> stopWords, boolean stopwordsremoval){
        TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory lowercasetokenizer = new LowerCaseTokenizerFactory(TOKENIZER_FACTORY);

        String result[];
        if (stopwordsremoval) {
            Set<String> stopwords = ImmutableSet.copyOf(stopWords);
            StopTokenizerFactory fstop = new StopTokenizerFactory(lowercasetokenizer, stopwords);
            TokenizerFactory tokenizer = new EnglishStopTokenizerFactory(fstop);


            Tokenization tk = new Tokenization(input, tokenizer);
            result = tk.tokens();
        }
        else {
            Tokenization tk = new Tokenization(input, lowercasetokenizer);
            result = tk.tokens();
        }

        return Arrays.asList(result);
    }

    /**
     * Load stop-words list from external file
     * @param filePath
     * @return List of stopwords
     */
    public static List<String> loadStopWords(String filePath){
        File stopWords = new File(filePath);
        String line = null;
        List<String> stopwords = new ArrayList();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(stopWords));
        } catch (FileNotFoundException e) {
            System.out.println(filePath + " is not found");
        }

        try {
            while((line = reader.readLine()) != null){
                String split[] = line.split(" ");
                stopwords.add(split[0]);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(Preprocessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return stopwords;
    }

    /**
     * Stem String using Porter Stemmer
     */
    public static void stem(List<String> documents) {
        TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory porter = new PorterStemmerTokenizerFactory(f1);

        for (int i = 0; i < documents.size(); i++) {
            Tokenization stem = new Tokenization(documents.get(i), porter);
            documents.set(i, stem.token(0));
        }
    }

    /**
     * Stem String using Porter Stemmer
     */
    public static List<String> funcStem(List<String> documents) {
        List<String> result = new ArrayList<String>();
        TokenizerFactory f1 = IndoEuropeanTokenizerFactory.INSTANCE;
        TokenizerFactory porter = new PorterStemmerTokenizerFactory(f1);

        for (int i = 0; i < documents.size(); i++) {
            Tokenization stem = new Tokenization(documents.get(i), porter);
            result.add(stem.token(0));
        }
        return result;
    }
}
