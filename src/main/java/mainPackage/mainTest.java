package mainPackage;

import DocumentRanker.*;
import Model.*;
import Option.*;
import VSM.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas ini berisi contoh2 penggunaan
 */
public class mainTest {
    public static void test() {
        //singleton option variable, refer to option to change the default configuration
        Option ops = new Option();
        ops.print();
        ops.save();

        //klo read dr file
        ops.relevanceFeedbackAlgo = 1;
        ops.isRelevanceFeedback = false;
        ops.isQueryExpansion = true;
        ops.secondRetrievalDocs = 0;
        ops.topN = 20;

        //cara load document, bisa juga pathnya di-supply dari options
        Documents docs = new Documents("test_collections/cisi/cisi.all");
        //docs.print();
        TokenizedDocuments tokDocs = new TokenizedDocuments();
        tokDocs.preprocessRawDocuments(docs, ops);
        //tokDocs.print();

        //cara load daftar query, bisa juga pathnya di-supply dari options
        Queries q = new Queries("test_collections/cisi/query.text");
        //q.print();
        q.preprocess(ops);
        //q.print();

        //cara load relevance judgement
        RelevanceJudgement r = new RelevanceJudgement("test_collections/cisi/qrels.text", q.size());
        //r.print();

        //pake VSM
        VSM vsm = new VSM(ops);
        vsm.makeTFIDFWeightMatrix(tokDocs);
        //System.out.println(vsm.getTerms().toString());
        //vsm.printOptions();
        //vsm.printWeightMatrix();
        vsm.save("savedFiles");
        //load dari file
        //vsm.load("savedFiles"); //inget ops harus di-load duluan
        //vsm.printWeightMatrix();*/

        //Query ke sistem
        System.out.println("Performing query");
        DocumentRanker dr = new DocumentRanker(vsm, tokDocs, r, q, ops, docs);

        //1 query aja
        /*Query qu = new Query(Arrays.asList(ops.queryInput)); //query input dr user
        qu.preprocess(ops);
        List<DocumentRank> result = dr.queryTask(qu);
        for (int i=0; i<5; i++)
        {
            System.out.println(result.get(i).getDocNum()+1+" "+result.get(i).getSC());
            System.out.println(docs.getDocument(result.get(i).getDocNum()));
        }*/

        //semua query (experiment)
        //dr.queriesTask();

        //relevance feedback
        dr.experimentRelevanceFeedback();
    }

    /**
     * Report Mode
     * @param ops
     */
    public static void generateReport(Option ops) {

    }

    /**
     * Experiment mode
     * @param ops
     */
    public static void experiment(Option ops) {
        Documents docs = new Documents(ops.documentPath);
        TokenizedDocuments tokDocs = new TokenizedDocuments();
        tokDocs.preprocessRawDocuments(docs, ops);

        Queries q = new Queries(ops.queryPath);
        q.preprocess(ops);

        RelevanceJudgement r = new RelevanceJudgement(ops.relevanceJudgmentPath, q.size());
        VSM vsm = new VSM(ops);
        vsm.makeTFIDFWeightMatrix(tokDocs);
        vsm.save("savedFiles");

        System.out.println("Performing query");
        DocumentRanker dr = new DocumentRanker(vsm, tokDocs, r, q, ops, docs);

        dr.experimentRelevanceFeedback();
    }

    /**
     * Experiment mode
     * @param ops
     */
    public static void pseudoRelevance(Option ops) {
        Documents docs = new Documents(ops.documentPath);
        TokenizedDocuments tokDocs = new TokenizedDocuments();
        tokDocs.preprocessRawDocuments(docs, ops);

        Query query = new Query(ops.queryInput);
        Queries q = new Queries();
        q.setQuery(0, query);
        q.preprocess(ops);

        RelevanceJudgement r = new RelevanceJudgement();
        VSM vsm = new VSM(ops);
        vsm.load("savedFiles");

        System.out.println("Performing query");
        DocumentRanker dr = new DocumentRanker(vsm, tokDocs, r, q, ops, docs);

        dr.pseudoInteractive();
    }

    /**
     * First retrieval yang centang2
     * @param ops
     */
    public static void firstRetrieval(Option ops) {
        Documents docs = new Documents(ops.documentPath);
        TokenizedDocuments tokDocs = new TokenizedDocuments();
        tokDocs.preprocessRawDocuments(docs, ops);

        Query query = new Query(ops.queryInput);
        Queries q = new Queries();
        q.setQuery(0, query);
        q.preprocess(ops);

        RelevanceJudgement r = new RelevanceJudgement();
        VSM vsm = new VSM(ops);
        vsm.load("savedFiles");

        System.out.println("Performing query");
        DocumentRanker dr = new DocumentRanker(vsm, tokDocs, r, q, ops, docs);

        dr.queriesTask();
    }

    /**
     * First retrieval yang centang2
     * @param ops
     */
    public static void secondRetrieval(Option ops) {
        Documents docs = new Documents(ops.documentPath);
        TokenizedDocuments tokDocs = new TokenizedDocuments();
        tokDocs.preprocessRawDocuments(docs, ops);

        Query query = new Query(ops.queryInput);
        Queries q = new Queries();
        q.setQuery(0, query);
        q.preprocess(ops);

        RelevanceJudgement r = new RelevanceJudgement();
        VSM vsm = new VSM(ops);
        vsm.load("savedFiles");

        System.out.println("Performing query");
        DocumentRanker dr = new DocumentRanker(vsm, tokDocs, r, q, ops, docs);

        dr.interactiveCentang();
    }

    public static void main(String[] args)
    {
        //singleton option variable, refer to option to change the default configuration
        Option ops = new Option();
        ops.load();
        ops.print();

        /*ops.isExperiment = false;
        ops.relevanceFeedbackAlgo = 1;
        ops.isSecondRetrieval = false;*/

        if (ops.isExperiment) {
            experiment(ops);
        }
        else { //bukan experiment, pasti interactive
            if (ops.isSecondRetrieval) { //ini pake yg relevance feedback
                System.out.println("second retrieval");
                secondRetrieval(ops);
            }
            else {
                if (ops.relevanceFeedbackAlgo == -1) { //pseudo relevance feedback
                    pseudoRelevance(ops);
                }
                else { //relevance feedback pertama
                    System.out.println("first retrieval");
                    firstRetrieval(ops);
                }
            }
        }
    }
}
