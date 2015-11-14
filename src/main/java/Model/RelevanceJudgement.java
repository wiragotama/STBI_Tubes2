package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas ini menyimpan seluruh data relevance judgements
 */
public class RelevanceJudgement {

    private List<Set<Integer>> relevanceJudgements;

    /**
     * Default Constructor
     * @param judgementPath
     * @param querySize
     */
    public RelevanceJudgement(String judgementPath, int querySize)
    {
        relevanceJudgements = new ArrayList();
        load(judgementPath, querySize);
    }

    public RelevanceJudgement()
    {
        relevanceJudgements = new ArrayList<Set<Integer>>();
    }

    /**
     * load from file
     * @param judgementPath
     * @param querySize
     */
    public void load(String judgementPath, int querySize)
    {
        relevanceJudgements = new ArrayList();

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(judgementPath));
            String currentLine = br.readLine();
            int maxIndex = querySize;

            br = new BufferedReader(new FileReader(judgementPath));
            currentLine = br.readLine();

            for(int i=0; i<maxIndex; i++)
            {
                relevanceJudgements.add(new HashSet<Integer>());
            }

            while(currentLine != null)
            {
                String words[] = currentLine.split(" ");
                boolean indexFound = false;
                boolean relevantDocumentFound = false;
                int index = 0;
                int relevantDocument = 0;
                for(String word : words)
                {
                    if(!word.equalsIgnoreCase("") && Integer.valueOf(word) > 0 && !indexFound && !relevantDocumentFound)
                    {
                        index = Integer.valueOf(word) - 1;
                        indexFound = true;
                    }
                    else if (!word.equalsIgnoreCase("") && Integer.valueOf(word) > 0 && !relevantDocumentFound && indexFound)
                    {
                        relevantDocument = Integer.valueOf(word) - 1;
                        break;
                    }
                }

                relevanceJudgements.get(index).add(relevantDocument);
                currentLine = br.readLine();
            }

            for(int i=0; i<relevanceJudgements.size(); i++)
            {
                if(relevanceJudgements.get(i).size() == 0)
                {
                    relevanceJudgements.get(i).add(-1);
                }
                relevanceJudgements.set(i, new TreeSet<Integer>(relevanceJudgements.get(i)));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Output all queries
     */
    public void print()
    {
        for(int i=0; i<relevanceJudgements.size(); i++)
        {
            System.out.printf("Relevance Judgement [%d]: %s\n", i, relevanceJudgements.get(i).toString());
        }
    }

    /**
     * Get all tokenized documents
     * @return documents
     */
    public List<Set<Integer>> instances(){
        return this.relevanceJudgements;
    }

    /**
     * Get relevance judgement for certain query
     * @param idx
     * @return relevance judgement of query at idx
     */
    public Set<Integer> get(int idx)
    {
        return this.relevanceJudgements.get(idx);
    }

    /**
     *
     * @return documents size
     */
    public int size()
    {
        return relevanceJudgements.size();
    }

    /**
     * Clear memory
     */
    public void clear(){
        relevanceJudgements.clear();
    }

    /**
     * Delete data
     * @param docNumber
     */
    public void deleteData(int queryNumber, int docNumber)
    {
        int simpan = -1;
        relevanceJudgements.get(queryNumber).remove(docNumber);
    }
}
