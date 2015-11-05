package DocumentRanker;

import Model.Queries;
import Model.Query;
import Model.RelevanceJudgement;
import Model.TokenizedDocuments;
import VSM.*;
import Option.*;
import Preprocessor.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by wiragotama on 11/5/15.
 * Kelas untuk melakukan query ke collections, prekondisi: vsm, tokenized documents, relevance judgement, queries lengkap
 */
public class DocumentRanker {

    private VSM vsm;
    private TokenizedDocuments tokenizedDocuments;
    private RelevanceJudgement relevanceJudgement;
    private Queries queries;
    private Option option;
    private List<List<DocumentRank>> queriesResult;
    private List<DocumentRank> queryResult;

    /**
     * Default constructor
     *
     * @param vsm
     * @param tokenizedDocuments
     * @param relevanceJudgement
     * @param queries
     * @param option
     */
    public DocumentRanker(VSM vsm, TokenizedDocuments tokenizedDocuments, RelevanceJudgement relevanceJudgement,
                          Queries queries, Option option) //all copy reference
    {
        this.vsm = vsm;
        this.tokenizedDocuments = tokenizedDocuments;
        this.relevanceJudgement = relevanceJudgement;
        this.queries = queries;
        this.option = option;
        this.queriesResult = new ArrayList();
        this.queryResult = new ArrayList();
    }

    /**
     * Melakukan satu query ke collection
     *
     * @param query
     */
    public List<DocumentRank> queryTask(Query query) {
        //anggap query sudah di pre-prosess
        double res[] = vsm.queryWeighting(query);
        for (int i = 0; i < res.length; i++)
            query.setWeight(i, res[i]);

        int collectionSize = vsm.getCollectionSize();
        int qsize = query.getQuery().size();
        List<DocumentRank> rank = new ArrayList();
        for (int i=0; i<collectionSize; i++) {
            DocumentRank docRank = new DocumentRank(i, 0.0);
            rank.add(docRank);
        }

        //Okelah, pembobotan query sudah selesai...
        for (int i=0; i<qsize; i++)
        {
            int idx = vsm.indexOfTerms(query.queryTerm(i));
            if (idx!=-1) {
                double queryWeight = query.getWeightAt(i);
                for (int doc = 0; doc < collectionSize; doc++) {
                    rank.get(doc).SC += queryWeight * vsm.weight(idx, doc);
                }
            }
        }

        //urutkan
        Collections.sort(rank, DocumentRank.comparator);
        return rank;
    }

    /**
     * Melakukan banyak query ke collection, menggunakan query testing
     */
    public void queriesTask() {
        /* Untuk output CSV */
        //try {
            /*PrintWriter printer = new PrintWriter("outputLaporan.csv", "UTF-8");
            printer.println(",document,query");
            printer.println("TF,"+this.DocumentTFOption+","+this.QueryTFOption);
            printer.println("IDF,"+this.DocumentIDFOption+","+this.QueryIDFOption);
            printer.println("Normalization,"+this.DocumentnormalizationOption+","+this.QueryNormalizationOption);
            printer.println("Stemming,"+this.DocumentstemmingOption+","+this.QueryStemmingOption);
            printer.println("Query");
            printer.println("Query,Precision,Recall,Non-Interpolated Average Precision");*/

        List<DocumentRank> result;
        int retrievedSize = 0;
        int relevanceSize = 0;
        double precision = 0;
        double recall = 0;
        double nonInterpolatedAveragePrecision = 0;
        StringBuffer toStringOutput = new StringBuffer(""); //mas string itu immutable
        double threshold = 0;

        // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
        Collection<Query> qex = queries.getQueries().values();
        int qAt = 0;
        for (Query q : qex) {
            Collection<Integer> relevance = relevanceJudgement.get(qAt);
            retrievedSize = 0;
            relevanceSize = 0;
            nonInterpolatedAveragePrecision = 0;
            precision = 0;
            recall = 0;

            result = queryTask(q);
            int d = 0;
            for (DocumentRank docRank : result) {
                if (retrievedSize<15) { //dianggap cuman 15 elemen pertama saja yang relevan
                    retrievedSize++;
                    if (option.isExperiment)
                        if (relevance.contains(docRank.getDocNum())) {
                            relevanceSize++;
                            nonInterpolatedAveragePrecision = nonInterpolatedAveragePrecision + ((double) relevanceSize / (double) retrievedSize);
                        }
                }
            }

            if (option.isExperiment) {
                if (retrievedSize > 0) {
                    int size = relevance.size();
                    precision = (double) relevanceSize / (double) retrievedSize;
                    recall = (double) relevanceSize / (double)size;
                    nonInterpolatedAveragePrecision = nonInterpolatedAveragePrecision / (double)size;
                } else {
                    precision = 0;
                    recall = 0;
                    nonInterpolatedAveragePrecision = 0;
                }
                System.out.println((qAt+1)+", "+precision+", "+recall+","+nonInterpolatedAveragePrecision);
                //relevant docs
                /*for (int z=0; z<15; z++) {
                    System.out.println(result.get(z).getDocNum()+" "+tokenizedDocuments.get(result.get(z).getDocNum()).getText().toString());
                }*/
            }
            qAt++;
        }
        System.out.println(toStringOutput);
    }
}
