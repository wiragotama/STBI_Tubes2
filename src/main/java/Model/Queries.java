package Model;

import Option.Option;
import Preprocessor.Preprocessor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas ini menyimpan data seluruh query
 */
public class Queries {
    private HashMap<Integer, Query> queries;

    /**
     * Default constructor
     * @param queryPath
     */
    public Queries(String queryPath)
    {
        queries = new HashMap<Integer, Query>();
        load(queryPath);
    }

    /**
     * load queries from file (raw)
     * @param queryPath
     */
    public void load(String queryPath)
    {
        queries = new HashMap<Integer, Query>();
        String currentString = ""; // state untuk tipe input yang sedang dibaca
        String currentQuery = "";

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(queryPath));
            String currentLine = br.readLine();
            int key = 0;
            while(currentLine != null)
            {
                currentLine = currentLine.toLowerCase();
                if(currentLine.startsWith(".i") ||
                        currentLine.startsWith(".w") ||
                        currentLine.startsWith(".a") ||
                        currentLine.startsWith(".t"))
                {
                    currentString = currentLine;
                }

                if(currentString.startsWith(".i"))
                {
                    if(!currentQuery.equalsIgnoreCase(""))
                    {
                        Query temp = new Query(currentQuery.substring(0, currentQuery.length()-1));
                        queries.put(key, temp);
                        key++;
                        currentQuery = "";
                    }
                }
                if(currentString.startsWith(".w") && !currentLine.startsWith(".w"))
                {
                    for(String word : currentLine.split(" "))
                    {
                        if(!word.equalsIgnoreCase(""))
                        {
                            currentQuery += word + " ";
                        }
                    }
                }
                if(currentString.startsWith(".t") && !currentLine.startsWith(".t"))
                {
                    for(String word : currentLine.split(" "))
                    {
                        if(!word.equalsIgnoreCase(""))
                        {
                            currentQuery += word + " ";
                        }
                    }
                }
                if(currentString.startsWith(".a") && !currentLine.startsWith(".a"))
                {
                    for(String word : currentLine.split(" "))
                    {
                        if(!word.equalsIgnoreCase(""))
                        {
                            currentQuery += word + " ";
                        }
                    }
                }

                currentLine = br.readLine();
            }

            if(!currentQuery.equalsIgnoreCase(""))
            {
                Query temp = new Query(currentQuery);
                queries.put(key, temp);
                key++;
            }

        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Output all queries
     */
    public void print()
    {
        for(int i=0; i<queries.size(); i++)
        {
            System.out.printf("Query[%d]: %s\n", i, queries.get(i).getQuery().toString());
        }
    }

    /**
     * Get all tokenized documents
     * @return documents
     */
    public HashMap<Integer, Query> getQueries(){
        return queries;
    }

    /**
     * Get document at a specified index
     * @param index
     * @return document
     */
    public Query getQuery(int index){
        return queries.get(index);
    }

    /**
     *
     * @return documents size
     */
    public int size()
    {
        return queries.size();
    }

    /**
     * Clear memory
     */
    public void clear(){
        queries.clear();
    }

    /**
     * Preprocess raw queries
     * @param option
     */
    public void preprocess(Option option)
    {
        for (int i=0; i<this.queries.size(); i++)
        {
            List<String> tokens = Preprocessor.tokenize(queries.get(i).queryTerm(0), option.stopwords, option.stopwordsRemoval);
            if (option.documentStem)
                Preprocessor.stem(tokens);

            Query n = new Query(tokens);
            this.queries.get(i).setQuery(tokens);
            this.queries.get(i).weightLength(tokens.size());
            //this.queries.set(i, n);
        }
    }
}
