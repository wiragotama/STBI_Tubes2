package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import Option.*;
import Preprocessor.Preprocessor;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas ini menyimpan data semua dokumen yang sudah di-preproses
 */
public class TokenizedDocuments {
    HashMap<Integer, TokenizedDocument> papersCollection;

    /**
     * Default Constructor
     */
    public TokenizedDocuments()
    {
        this.papersCollection = new HashMap<Integer, TokenizedDocument>();
    }

    /**
     * copy constructor
     * @param d DataTokenizedInstances
     */
    public TokenizedDocuments(TokenizedDocuments d)
    {
        this.papersCollection = new HashMap<Integer, TokenizedDocument>();
        int len = d.size();
        for (int i=0; i<len; i++) {
            TokenizedDocument newData = new TokenizedDocument(d.get(i));
            this.papersCollection.put(i, newData);
        }
    }

    /**
     * @param idx
     * @return an instance
     */
    public TokenizedDocument get(int idx)
    {
        return this.papersCollection.get(idx);
    }

    /**
     * @return papersCollection size
     */
    public int size()
    {
        return this.papersCollection.size();
    }

    /**
     * @return dataTokenizedInstances
     */
    public HashMap<Integer, TokenizedDocument> instances()
    {
        return this.papersCollection;
    }

    /**
     * Set instance at idx into newInstance
     * @param idx
     * @param newInstance
     */
    public void setInstance(int idx, TokenizedDocument newInstance)
    {
        this.papersCollection.get(idx).setText(newInstance.getText());
    }

    /**
     * Add new instance to collection
     * @param instance
     */
    public void add(TokenizedDocument instance)
    {
        TokenizedDocument newInstance = new TokenizedDocument(instance);
        int k = this.papersCollection.size();
        this.papersCollection.put(k, newInstance);
    }

    /**
     * Add new instances from other DataTokenizedsInstances to this collection
     * @param dataInstances
     */
    public void add(TokenizedDocuments dataInstances)
    {
        int len = dataInstances.instances().size();
        int k = this.papersCollection.size();
        for (int i=0; i<len; i++)
        {
            TokenizedDocument newInstance = new TokenizedDocument(dataInstances.get(i));
            this.papersCollection.put(k, newInstance);
            k++;
        }
    }

    /**
     * output to screen
     */
    public void print()
    {
        int len = this.papersCollection.size();
        System.out.println("Count = "+len);
        for (int i=0; i<len; i++)
        {
            this.papersCollection.get(i).print();
        }
    }

    /**
     * Clear memory
     */
    public void clear()
    {
        this.papersCollection.clear();
    }

    /**
     * Delete data
     * @param beginIndex
     * @param lastIndex
     */
    public void deleteData(int beginIndex, int lastIndex)
    {
        int N = lastIndex - beginIndex +1;
        while (N>0) {
            this.papersCollection.remove(beginIndex);
            N--;
        }
    }

    /**
     * Preprocess all raw documents
     * @param raw
     * @param option
     */
    public void preprocessRawDocuments(Documents raw, Option option)
    {
        for (int i=0; i<raw.size(); i++)
        {
            List<String> tokens = Preprocessor.tokenize(raw.getDocument(i), option.stopwords, option.stopwordsRemoval);
            if (option.documentStem)
                Preprocessor.stem(tokens);

            TokenizedDocument n = new TokenizedDocument(tokens);
            this.papersCollection.put(i, n);
        }
    }
}
