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
     * Melakukan satu query ke collection, dengan pembobotan query
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
     * Melakukan satu query ke collection, dengan pembobotan query
     *
     * @param query
     */
    public List<DocumentRank> queryTaskWithoutWeighting(Query query) {
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
                if (docRank.getSC()>0) { //dianggap relevan klo SC tidak 0
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

    /**
     * Melakukan banyak query ke collection, menggunakan query testing
     */
    public void secondRetrieval() {
        /* Untuk output CSV */
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

            result = queryTaskWithoutWeighting(q);
            int d = 0;
            for (DocumentRank docRank : result) {
                if (docRank.getSC()>0) { //dianggap relevan klo SC tidak 0
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

    /**
     * Melakukan banyak query ke collection, menggunakan query testing
     */
    public void experimentRelevanceFeedback() {

        List<DocumentRank> result;
        int retrievedSize = 0;
        StringBuffer toStringOutput = new StringBuffer(""); //mas string itu immutable

        // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
        Collection<Query> qex = queries.getQueries().values();
        int qAt = 0;
        for (Query q : qex) {
            Collection<Integer> relevance = relevanceJudgement.get(qAt);
            retrievedSize = 0;
            List<Integer> relevantDocsIdx = new ArrayList<Integer>();
            List<Integer> nonRelevantDocsIdx = new ArrayList<Integer>();

            result = queryTask(q);
            int d = 0;
            for (DocumentRank docRank : result) {
                if (retrievedSize<option.topN) { //ambil topN aja
                    if (relevance.contains(docRank.getDocNum())) {
                        relevantDocsIdx.add(docRank.getDocNum());
                        if (option.secondRetrievalDocs==1) { //jika retrieval kedua menggunakan dokumen baru
                            relevanceJudgement.instances().get(qAt).remove(docRank.getDocNum());
                        }
                    }
                    else {
                        nonRelevantDocsIdx.add(docRank.getDocNum());
                    }
                    retrievedSize++;
                }
                else break;
            }

            System.out.println("First retrieval "+qAt);

            //perhitungan query weighting baru, iterate per term dari query, tanpa query expansion
            if (option.isQueryExpansion) {
                /* Dari slide-query expansion: query awal ditambahkan sejumlah kata yang
                berasal dari dokumen-dokumen yang relevan */

                int querySize = q.getQuery().size();
                Set<String> expansionTerms = new TreeSet<String>(); //karena hal ini, jadinya gk terururt
                int relevantDocs = relevantDocsIdx.size();
                for (int doc=0; doc<relevantDocs; doc++) {
                    expansionTerms.addAll(tokenizedDocuments.get(doc).getText());
                }

                q.addQueryTerms(expansionTerms); //lgsg ditambahin weight juga sebanyak expansion terms dengan nilai 0.0
                queries.setQuery(qAt, q);
            }
            int N = q.getQuery().size();
            int[] queryTermIdx = new int[N];
            System.out.println("Term Size "+N);
            for (int i=0; i<N; i++) {
                queryTermIdx[i] = vsm.indexOfTerms(q.getQuery().get(i));
            }
            for (int term = 0; term < N; term++) {
                double sum = 0.0;
                double sumIrre = 0.0;
                for (int doc=0; doc<relevantDocsIdx.size(); doc++) {
                    sum += vsm.weight(term, doc);
                }
                if (option.relevanceFeedbackAlgo!=2) { //klo ide dec hi, tidak perlu dihitung
                    for (int doc = 0; doc < nonRelevantDocsIdx.size(); doc++) {
                        sumIrre += vsm.weight(term, doc);
                    }
                }
                double newWeight = 0.0;
                if (option.isRelevanceFeedback) {
                    if (option.relevanceFeedbackAlgo == 0) { //rocchio
                        //alfa, beta, gama dianggap 1
                        newWeight = q.getWeightAt(term) + (sum / (double) relevantDocsIdx.size()) - (sumIrre / (double) nonRelevantDocsIdx.size());
                    } else if (option.relevanceFeedbackAlgo == 1) { //ide reguler
                        newWeight = q.getWeightAt(term) + sum - sumIrre;
                    } else { //ide dec hi
                        newWeight = q.getWeightAt(term) + sum - vsm.weight(term, nonRelevantDocsIdx.get(0));
                    }
                }
                else { //pseudo relevance feedback
                    if (option.relevanceFeedbackAlgo == 0) { //rocchio
                        //alfa, beta, gama dianggap 1
                        newWeight = q.getWeightAt(term) + (sum / (double) relevantDocsIdx.size()) + (sumIrre / (double) nonRelevantDocsIdx.size());
                    } else if (option.relevanceFeedbackAlgo == 1) { //ide reguler
                        newWeight = q.getWeightAt(term) + sum + sumIrre;
                    } else { //ide dec hi
                        newWeight = q.getWeightAt(term) + sum + sumIrre;
                    }
                }
                queries.setWeight(qAt, term, newWeight);
            }
            System.out.println("First modification "+qAt);
            qAt++;
        }

        //query ke-2, pake fungsi retrieval yang lama
        secondRetrieval();
    }
}
