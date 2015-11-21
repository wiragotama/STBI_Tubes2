package DocumentRanker;

import Model.*;
import VSM.*;
import Option.*;
import Preprocessor.*;

import java.io.*;
import java.util.*;

/**
 * Created by wiragotama on 11/5/15.
 * Kelas untuk melakukan query ke collections, prekondisi: vsm, tokenized documents, relevance judgement, queries lengkap
 */
public class DocumentRanker {

    private VSM vsm;
    private TokenizedDocuments tokenizedDocuments;
    private Documents docs;
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
                          Queries queries, Option option, Documents docs) //all copy reference
    {
        this.vsm = vsm;
        this.tokenizedDocuments = tokenizedDocuments;
        this.relevanceJudgement = relevanceJudgement;
        this.queries = queries;
        this.option = option;
        this.queriesResult = new ArrayList();
        this.queryResult = new ArrayList();
        this.docs = docs;
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
        int qsize = query.getUniqueTerms().size();
        List<DocumentRank> rank = new ArrayList();
        for (int i=0; i<collectionSize; i++) {
            DocumentRank docRank = new DocumentRank(i, 0.0);
            rank.add(docRank);
        }

        //Okelah, pembobotan query sudah selesai...
        int it=0;
        for (String term: query.getUniqueTerms())
        {
            int idx = vsm.indexOfTerms(term);
            if (idx!=-1) {
                double queryWeight = query.getWeightAt(it);
                for (int doc = 0; doc < collectionSize; doc++) {
                    rank.get(doc).SC += (queryWeight * vsm.weight(idx, doc));
                }
            }
            it++;
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
        int qsize = query.getUniqueTerms().size();
        List<DocumentRank> rank = new ArrayList();
        for (int i=0; i<collectionSize; i++) {
            DocumentRank docRank = new DocumentRank(i, 0.0);
            rank.add(docRank);
        }

        //Okelah, pembobotan query sudah selesai...
        int it=0;
        for (String term: query.getUniqueTerms())
        {
            int idx = vsm.indexOfTerms(term);
            if (idx!=-1) {
                double queryWeight = query.getWeightAt(it);
                for (int doc = 0; doc < collectionSize; doc++) {
                    rank.get(doc).SC += (queryWeight * vsm.weight(idx, doc));
                }
            }
            it++;
        }
        //urutkan
        Collections.sort(rank, DocumentRank.comparator);
        return rank;
    }

    /**
     * Melakukan banyak query ke collection, menggunakan query testing
     */
    public void queriesTask() {

        try {
            PrintWriter writer = new PrintWriter("outputInteractive.txt");

            /* Untuk output CSV */
            List<DocumentRank> result;
            int retrievedSize = 0;
            int relevanceSize = 0;

            // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
            Collection<Query> qex = queries.getQueries().values();
            int qAt = 0;
            for (Query q : qex) {
                List<Integer> retrievedDocNum = new ArrayList<Integer>();
                List<Double> SCdocs = new ArrayList<Double>();
                retrievedSize = 0;
                relevanceSize = 0;

                result = queryTask(q);
                int d = 0;
                for (DocumentRank docRank : result) {
                    if (docRank.getSC()>0 && retrievedSize<option.showN) { //dianggap relevan klo SC tidak 0
                        retrievedSize++;
                        relevanceSize++;
                        retrievedDocNum.add(docRank.getDocNum());
                        SCdocs.add(docRank.getSC());
                    }
                }

                writer.println(retrievedSize);
                for (int i=0; i<retrievedSize; i++) {
                    writer.println(retrievedDocNum.get(i)+1);
                    writer.println(SCdocs.get(i));
                    writer.println(docs.getDocument(retrievedDocNum.get(i)));
                }
                qAt++;
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Melakukan banyak query ke collection, menggunakan query testing
     */
    public void secondRetrieval() {
        try {
            PrintWriter writer = new PrintWriter("outputExperiment.txt");

            /* Untuk output CSV */
            List<DocumentRank> result;
            int retrievedSize = 0;
            int relevanceSize = 0;
            double precision = 0;
            double recall = 0;
            double nonInterpolatedAveragePrecision = 0;

            // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
            Collection<Query> qex = queries.getQueries().values();
            int qAt = 0;
            double avgPrecision = 0.0;
            double avgRecall = 0.0;
            double avgNIP = 0.0;
            for (Query q : qex) {
                Collection<Integer> relevance = relevanceJudgement.get(qAt);
                List<Integer> retrievedDocNum = new ArrayList<Integer>();
                List<Double> SCdocs = new ArrayList<Double>();
                retrievedSize = 0;
                relevanceSize = 0;
                nonInterpolatedAveragePrecision = 0;
                precision = 0;
                recall = 0;

                result = queryTaskWithoutWeighting(q);

                int d = 0;
                for (DocumentRank docRank : result) {
                    if (docRank.getSC()>0 && retrievedSize<option.showN) { //dianggap relevan klo SC tidak 0
                        retrievedSize++;
                        if (option.isExperiment)
                            if (relevance.contains(docRank.getDocNum())) {
                                relevanceSize++;
                                nonInterpolatedAveragePrecision = nonInterpolatedAveragePrecision + ((double) relevanceSize / (double) retrievedSize);
                            }
                        retrievedDocNum.add(docRank.getDocNum());
                        SCdocs.add(docRank.getSC());
                    }
                }

                if (option.isExperiment) {
                    if (retrievedSize > 0 && relevanceSize>0) {
                        precision = (double) relevanceSize / (double) retrievedSize;
                        recall = (double) relevanceSize / (double) relevance.size();
                        nonInterpolatedAveragePrecision = nonInterpolatedAveragePrecision / (double) relevance.size();
                    } else {
                        precision = 0;
                        recall = 0;
                        nonInterpolatedAveragePrecision = 0;
                    }
                    writer.println(retrievedSize);
                    writer.println(precision);
                    writer.println(recall);
                    writer.println(nonInterpolatedAveragePrecision);
                    for (int i=0; i<retrievedSize; i++) {
                        writer.println(retrievedDocNum.get(i)+1);
                        writer.println(SCdocs.get(i));
                        writer.println(docs.getDocument(retrievedDocNum.get(i)));
                    }
                    System.out.println((qAt+1)+", "+precision+", "+recall+", "+nonInterpolatedAveragePrecision);
                    avgPrecision+=precision; avgRecall+=recall; avgNIP+=nonInterpolatedAveragePrecision;
                }
                qAt++;
            }
            System.out.println("average, "+avgPrecision/qAt+", "+avgRecall/qAt+", "+avgNIP/qAt);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
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
                if (docRank.getSC()>0 && retrievedSize<option.topN) { //ambil topN aja
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

            //perhitungan query weighting baru, iterate per term dari query, tanpa query expansion
            if (option.isQueryExpansion) {
                /* Dari slide-query expansion: query awal ditambahkan sejumlah kata yang
                berasal dari dokumen-dokumen yang relevan */

                int querySize = q.getUniqueTerms().size();
                Set<String> expansionTerms = new TreeSet<String>(); //karena hal ini, jadinya gk terururt
                int relevantDocs = relevantDocsIdx.size();
                for (int doc=0; doc<relevantDocs; doc++) {
                    expansionTerms.addAll(tokenizedDocuments.get(doc).getText());
                }

                q.addQueryTerms(expansionTerms); //lgsg ditambahin weight juga sebanyak expansion terms dengan nilai 0.0
                queries.setQuery(qAt, q);
            }
            int N = q.getUniqueTerms().size();
            int[] queryTermIdx = new int[N];

            int it = 0;
            for (String term : q.getUniqueTerms()) {
                queryTermIdx[it] = vsm.indexOfTerms(term);
                it++;
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
                    } if (option.relevanceFeedbackAlgo == 1) { //ide reguler
                        newWeight = q.getWeightAt(term) + sum + sumIrre;
                    } else if (option.relevanceFeedbackAlgo == 2){ //ide dec hi
                        newWeight = q.getWeightAt(term) + sum + sumIrre;
                    }
                    else { //rocchio
                        //alfa, beta, gama dianggap 1
                        newWeight = q.getWeightAt(term) + (sum / (double) relevantDocsIdx.size()) + (sumIrre / (double) nonRelevantDocsIdx.size());
                    }
                queries.setWeight(qAt, term, newWeight);
            }
            qAt++;
        }

        //query ke-2, pake fungsi retrieval yang lama
        secondRetrieval();
    }

    /**
     * Melakukan banyak query ke collection, menggunakan query input
     */
    public void pseudoInteractive() {

        try {
            PrintWriter writer = new PrintWriter("queryLamaBaru.txt");
            List<DocumentRank> result;
            int retrievedSize = 0;
            StringBuffer toStringOutput = new StringBuffer(""); //mas string itu immutable

            // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
            Collection<Query> qex = queries.getQueries().values();
            int qAt = 0;
            for (Query q : qex) {
                retrievedSize = 0;
                List<Integer> relevantDocsIdx = new ArrayList<Integer>();
                List<Integer> nonRelevantDocsIdx = new ArrayList<Integer>();

                result = queryTask(q);
                writer.println(q.getUniqueTerms());
                writer.println(q.getWeight());
                System.out.println(q.getUniqueTerms());
                System.out.println(q.getWeight());
                int d = 0;
                for (DocumentRank docRank : result) {
                    if (retrievedSize<option.topN) { //ambil topN aja
                        relevantDocsIdx.add(docRank.getDocNum());
                        retrievedSize++;
                    }
                    else break;
                }

                //perhitungan query weighting baru, iterate per term dari query, tanpa query expansion
                if (option.isQueryExpansion) {
                    /* Dari slide-query expansion: query awal ditambahkan sejumlah kata yang
                    berasal dari dokumen-dokumen yang relevan */

                    int querySize = q.getUniqueTerms().size();
                    Set<String> expansionTerms = new TreeSet<String>(); //karena hal ini, jadinya gk terururt
                    int relevantDocs = relevantDocsIdx.size();
                    for (int doc=0; doc<relevantDocs; doc++) {
                        expansionTerms.addAll(tokenizedDocuments.get(doc).getText());
                    }

                    q.addQueryTerms(expansionTerms); //lgsg ditambahin weight juga sebanyak expansion terms dengan nilai 0.0
                    queries.setQuery(qAt, q);
                }
                int N = q.getUniqueTerms().size();
                int[] queryTermIdx = new int[N];

                int it = 0;
                for (String term : q.getUniqueTerms()) {
                    queryTermIdx[it] = vsm.indexOfTerms(term);
                    it++;
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
                writer.println(q.getUniqueTerms());
                writer.println(q.getWeight());

                System.out.println(q.getUniqueTerms());
                System.out.println(q.getWeight());
                qAt++;
            }
            writer.close();

            //query ke-2, pake fungsi retrieval yang lama
            secondRetrievalV2();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Melakukan banyak query ke collection, menggunakan query testing, versi interaktif
     */
    public void secondRetrievalV2() {
        try {
            PrintWriter writer = new PrintWriter("outputInteractive.txt");

            /* Untuk output CSV */
            List<DocumentRank> result;
            int retrievedSize = 0;

            // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
            Collection<Query> qex = queries.getQueries().values();
            int qAt = 0;
            for (Query q : qex) {
                List<Integer> retrievedDocNum = new ArrayList<Integer>();
                List<Double> SCdocs = new ArrayList<Double>();
                retrievedSize = 0;

                result = queryTaskWithoutWeighting(q);

                int d = 0;
                for (DocumentRank docRank : result) {
                    if (docRank.getSC()>0 && retrievedSize<option.showN) { //dianggap relevan klo SC tidak 0
                        retrievedSize++;
                        retrievedDocNum.add(docRank.getDocNum());
                        SCdocs.add(docRank.getSC());
                    }
                }

                writer.println(retrievedSize);
                for (int i=0; i<retrievedSize; i++) {
                    writer.println(retrievedDocNum.get(i)+1);
                    writer.println(SCdocs.get(i));
                    writer.println(docs.getDocument(retrievedDocNum.get(i)));
                }
                qAt++;
            }
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void interactiveCentang()
    {
        try {
            //relevant judgement document dari user
            List<Integer> relevanceJudgementUser = new ArrayList<Integer>();
            BufferedReader br = new BufferedReader(new FileReader("savedFiles/nomorRelevant.txt"));
            String relNumber = "";
            relNumber = br.readLine();
            if (relNumber!="") {
                relevanceJudgementUser.add(Integer.valueOf(relNumber));
            }
            while (relNumber!=null) {
                relNumber = br.readLine();
                if (relNumber!=null && relNumber!="") {
                    relevanceJudgementUser.add(Integer.valueOf(relNumber));
                }
            }

            PrintWriter writer = new PrintWriter("queryLamaBaru.txt");
            List<DocumentRank> result;
            int retrievedSize = 0;
            StringBuffer toStringOutput = new StringBuffer(""); //mas string itu immutable

            // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
            Collection<Query> qex = queries.getQueries().values();
            int qAt = 0;
            for (Query q : qex) {
                retrievedSize = 0;
                List<Integer> relevantDocsIdx = new ArrayList<Integer>();
                List<Integer> nonRelevantDocsIdx = new ArrayList<Integer>();

                result = queryTask(q);
                writer.println(q.getUniqueTerms());
                writer.println(q.getWeight());
                System.out.println(q.getUniqueTerms());
                System.out.println(q.getWeight());
                int d = 0;
                for (DocumentRank docRank : result) {
                    if (retrievedSize<option.topN) { //ambil topN aja
                        if (relevanceJudgementUser.contains(docRank.getDocNum()))
                            relevantDocsIdx.add(docRank.getDocNum());
                        else nonRelevantDocsIdx.add(docRank.getDocNum());
                        retrievedSize++;
                    }
                    else break;
                }

                //perhitungan query weighting baru, iterate per term dari query, tanpa query expansion
                if (option.isQueryExpansion) {
                    /* Dari slide-query expansion: query awal ditambahkan sejumlah kata yang
                    berasal dari dokumen-dokumen yang relevan */

                    int querySize = q.getUniqueTerms().size();
                    Set<String> expansionTerms = new TreeSet<String>(); //karena hal ini, jadinya gk terururt
                    int relevantDocs = relevantDocsIdx.size();
                    for (int doc=0; doc<relevantDocs; doc++) {
                        expansionTerms.addAll(tokenizedDocuments.get(doc).getText());
                    }

                    q.addQueryTerms(expansionTerms); //lgsg ditambahin weight juga sebanyak expansion terms dengan nilai 0.0
                    queries.setQuery(qAt, q);
                }
                int N = q.getUniqueTerms().size();
                int[] queryTermIdx = new int[N];

                int it = 0;
                for (String term : q.getUniqueTerms()) {
                    queryTermIdx[it] = vsm.indexOfTerms(term);
                    it++;
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
                writer.println(q.getUniqueTerms());
                writer.println(q.getWeight());

                System.out.println(q.getUniqueTerms());
                System.out.println(q.getWeight());
                qAt++;
            }
            writer.close();

            //query ke-2, pake fungsi retrieval yang lama
            secondRetrievalV2();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Melakukan banyak query ke collection, menggunakan query testing
     */
    public void experimentLaporan() {

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
                if (docRank.getSC()>0 && retrievedSize<option.topN) { //ambil topN aja
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

            //perhitungan query weighting baru, iterate per term dari query, tanpa query expansion
            if (option.isQueryExpansion) {
                /* Dari slide-query expansion: query awal ditambahkan sejumlah kata yang
                berasal dari dokumen-dokumen yang relevan */

                int querySize = q.getUniqueTerms().size();
                Set<String> expansionTerms = new TreeSet<String>(); //karena hal ini, jadinya gk terururt
                int relevantDocs = relevantDocsIdx.size();
                for (int doc=0; doc<relevantDocs; doc++) {
                    expansionTerms.addAll(tokenizedDocuments.get(doc).getText());
                }

                q.addQueryTerms(expansionTerms); //lgsg ditambahin weight juga sebanyak expansion terms dengan nilai 0.0
                queries.setQuery(qAt, q);
            }
            int N = q.getUniqueTerms().size();
            int[] queryTermIdx = new int[N];

            int it = 0;
            for (String term : q.getUniqueTerms()) {
                queryTermIdx[it] = vsm.indexOfTerms(term);
                it++;
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
                } if (option.relevanceFeedbackAlgo == 1) { //ide reguler
                    newWeight = q.getWeightAt(term) + sum + sumIrre;
                } else if (option.relevanceFeedbackAlgo == 2){ //ide dec hi
                    newWeight = q.getWeightAt(term) + sum + sumIrre;
                }
                else { //rocchio
                    //alfa, beta, gama dianggap 1
                    newWeight = q.getWeightAt(term) + (sum / (double) relevantDocsIdx.size()) + (sumIrre / (double) nonRelevantDocsIdx.size());
                }
                queries.setWeight(qAt, term, newWeight);
            }
            qAt++;
        }

        //query ke-2, pake fungsi retrieval yang lama
        secondRetrievalLaporan();
    }

    /**
     * Melakukan banyak query ke collection, menggunakan query testing
     */
    public void secondRetrievalLaporan() {
        try {
            String algo = "";
            if (option.relevanceFeedbackAlgo==0) {
                algo = "Rocchio";
            }
            else if (option.relevanceFeedbackAlgo==1) {
                algo = "IdeReguler";
            }
            else if (option.relevanceFeedbackAlgo==2) {
                algo = "IdeDecHi";
            }
            else algo = "Pseudo";
            PrintWriter writer = new PrintWriter("laporan/"+option.documentPath.substring(17,20)+"_"+algo+".csv");
            writer.println(option.documentPath);
            writer.println(algo);
            writer.println("Query, Precision, Recall, NonInterpolatedAvgPrecision");

            /* Untuk output CSV */
            List<DocumentRank> result;
            int retrievedSize = 0;
            int relevanceSize = 0;
            double precision = 0;
            double recall = 0;
            double nonInterpolatedAveragePrecision = 0;

            // Evaluasi setiap document yang diretrieve dengan relevance judgment pada setiap query
            Collection<Query> qex = queries.getQueries().values();
            int qAt = 0;
            double avgPrecision = 0.0;
            double avgRecall = 0.0;
            double avgNIP = 0.0;
            for (Query q : qex) {
                Collection<Integer> relevance = relevanceJudgement.get(qAt);
                List<Integer> retrievedDocNum = new ArrayList<Integer>();
                List<Double> SCdocs = new ArrayList<Double>();
                retrievedSize = 0;
                relevanceSize = 0;
                nonInterpolatedAveragePrecision = 0;
                precision = 0;
                recall = 0;

                result = queryTaskWithoutWeighting(q);

                int d = 0;
                for (DocumentRank docRank : result) {
                    if (docRank.getSC()>0 && retrievedSize<option.showN) { //dianggap relevan klo SC tidak 0
                        retrievedSize++;
                        if (option.isExperiment)
                            if (relevance.contains(docRank.getDocNum())) {
                                relevanceSize++;
                                nonInterpolatedAveragePrecision = nonInterpolatedAveragePrecision + ((double) relevanceSize / (double) retrievedSize);
                            }
                        retrievedDocNum.add(docRank.getDocNum());
                        SCdocs.add(docRank.getSC());
                    }
                }

                if (option.isExperiment) {
                    if (retrievedSize > 0 && relevanceSize>0) {
                        precision = (double) relevanceSize / (double) retrievedSize;
                        recall = (double) relevanceSize / (double) relevance.size();
                        nonInterpolatedAveragePrecision = nonInterpolatedAveragePrecision / (double) relevance.size();
                    } else {
                        precision = 0;
                        recall = 0;
                        nonInterpolatedAveragePrecision = 0;
                    }
                    /*writer.println(retrievedSize);
                    writer.println(precision);
                    writer.println(recall);
                    writer.println(nonInterpolatedAveragePrecision);
                    for (int i=0; i<retrievedSize; i++) {
                        writer.println(retrievedDocNum.get(i)+1);
                        writer.println(SCdocs.get(i));
                        writer.println(docs.getDocument(retrievedDocNum.get(i)));
                    }*/
                    System.out.println((qAt+1)+", "+precision+", "+recall+", "+nonInterpolatedAveragePrecision);
                    writer.println((qAt+1)+", "+precision+", "+recall+", "+nonInterpolatedAveragePrecision);
                    avgPrecision+=precision; avgRecall+=recall; avgNIP+=nonInterpolatedAveragePrecision;
                }
                qAt++;
            }
            writer.println("average, "+avgPrecision/qAt+", "+avgRecall/qAt+", "+avgNIP/qAt);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
