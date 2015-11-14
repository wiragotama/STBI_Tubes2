package Option;

import Preprocessor.Preprocessor;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas ini menyimpan semua option pada sistem, dibuat singleton agar gampang gaperlu besar simpan sini simpan sana
 */
public class Option {
    public boolean stopwordsRemoval;
    public List<String> stopwords; //tidak disimpan dalam file external

    //document
    public boolean documentNormalization;
    public boolean documentUseIDF;
    public int documentTFOption; //0:no TF, 1:Raw TF, 2:Binary TF, 3:Augmented TF, 4:Logarithmic TF
    public boolean documentStem;

    //Query
    public boolean queryNormalization;
    public boolean queryUseIDF;
    public int queryTFOption; //0:no TF, 1:Raw TF, 2:Binary TF, 3:Augmented TF, 4:Logarithmic TF
    public boolean queryStem;

    //tambahan spek baru
    public int relevanceFeedbackAlgo; //0 rocchio, 1 ide reguler, 2 ide dec-hi, -1 kalau experimen biasa
    public boolean isRelevanceFeedback; //false if pseudo
    public boolean isQueryExpansion; //expand query when using relevance feedback
    public int secondRetrievalDocs; //0 same as first, 1 different from the first (collections - yang sudah muncul)

    //berkaitan dengan runtime
    public boolean isExperiment;
    public boolean isNormalInteractive; //mode query normal
    public boolean isFeedbackInteractive; //more query dengan feedback
    public String documentPath;
    public String queryPath;
    public String relevanceJudgmentPath;
    public String stopwordsPath;
    //add when needed
    public static final String filePath = "savedFiles/option";
    public String queryInput; //klo gk experiment, berarti ada query
    public int topN;
    public int showN;
    public boolean isSecondRetrieval;

    /**
     * Default Constructor
     */
    public Option() {
        this.stopwordsRemoval = true;
        stopwordsPath = "stopwords/custom.stopword";
        this.stopwords = new ArrayList<String>();
        this.stopwords = Preprocessor.loadStopWords(stopwordsPath);

        this.documentStem = true;
        this.documentNormalization = false;
        this.documentUseIDF = false;
        this.documentTFOption = 1;

        this.queryStem = true;
        this.queryNormalization = false;
        this.queryUseIDF = false;
        this.queryTFOption = 1;

        this.relevanceFeedbackAlgo = -1;
        this.isRelevanceFeedback = false;
        this.isQueryExpansion = false;
        this.secondRetrievalDocs = 0;

        this.isExperiment = true;
        this.isNormalInteractive = false;
        this.isFeedbackInteractive = false;
        this.documentPath = "test_collections/adi/adi.all";
        this.queryPath = "test_collections/adi/query.text";
        this.relevanceJudgmentPath = "test_collections/adi/qrels.text";
        this.queryInput = "information retrieval";
        this.topN = 0;
        this.showN = 10;
        isSecondRetrieval = false;
    }

    /**
     * load from file
     */
    public void load()
    {
        System.out.println("Loading Option");

        File configFile = new File(filePath);
        File queryInputFile = new File(filePath+".query");
        BufferedReader reader = null;
        BufferedReader readerQ = null;
        String line = null;

        try {
            reader = new BufferedReader(new FileReader(configFile));
            readerQ = new BufferedReader(new FileReader(queryInputFile));
        } catch (FileNotFoundException e) {
            System.out.println(filePath + " is not found");
            System.out.println(filePath+".query" + " is not found");
        }

        try {
            while((line = reader.readLine()) != null){
                String split[] = line.split(" ");
                this.stopwordsRemoval = Boolean.valueOf(split[0]);
                stopwordsPath = String.valueOf(split[1]);

                this.documentStem = Boolean.valueOf(split[2]);
                this.documentNormalization = Boolean.valueOf(split[3]);
                this.documentUseIDF = Boolean.valueOf(split[4]);
                this.documentTFOption = Integer.valueOf(split[5]);

                this.queryStem = Boolean.valueOf(split[6]);
                this.queryNormalization = Boolean.valueOf(split[7]);
                this.queryUseIDF = Boolean.valueOf(split[8]);
                this.queryTFOption = Integer.valueOf(split[9]);

                this.relevanceFeedbackAlgo = Integer.valueOf(split[10]);
                this.isRelevanceFeedback = Boolean.valueOf(split[11]);
                this.isQueryExpansion = Boolean.valueOf(split[12]);
                this.secondRetrievalDocs = Integer.valueOf(split[13]);

                isExperiment = Boolean.valueOf(split[14]);
                isNormalInteractive = Boolean.valueOf(split[15]);
                isFeedbackInteractive = Boolean.valueOf(split[16]);
                documentPath = String.valueOf(split[17]);
                queryPath = String.valueOf(split[18]);
                relevanceJudgmentPath = String.valueOf(split[19]);
                this.topN = Integer.valueOf(split[20]);
                this.showN = Integer.valueOf(split[21]);
                isSecondRetrieval = Boolean.valueOf(split[22]);
            }

            while ((line = readerQ.readLine())!=null) {
                this.queryInput = line.toString();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            reader.close();
            readerQ.close();
        } catch (IOException ex) {
           ex.printStackTrace();
        }
    }

    /**
     * Saving configuration options in making VSM
     */
    public void save()
    {
        System.out.println("Saving Option");
        try {
            PrintWriter writer = null;
            writer = new PrintWriter(filePath, "UTF-8");
            String out = this.stopwordsRemoval + " " + stopwordsPath + " " + this.documentStem + " "+this.documentNormalization
                    + " " + this.documentUseIDF +" "+ this.documentTFOption + " " + this.queryStem + " "+this.queryNormalization
                    +" " +this.queryUseIDF +" "+this.queryTFOption +" "+this.relevanceFeedbackAlgo +" "+this.isRelevanceFeedback
                    +" "+this.isQueryExpansion+" "+this.secondRetrievalDocs
                    +" "+isExperiment +" "+isNormalInteractive +" "+isFeedbackInteractive
                    +" "+documentPath +" "+queryPath +" "+relevanceJudgmentPath+" "+topN+" "+showN+" "+isSecondRetrieval;
            writer.println(out);
            writer.close();

            writer = null;
            writer = new PrintWriter(filePath+".query", "UTF-8");
            out = queryInput;
            writer.println(out);
            writer.close();
        }
        catch (Exception e)
        {
            System.out.println("save option failed");
        }
    }

    /**
     * output to stdout
     */
    public void print()
    {
        String out = this.stopwordsRemoval + " " + stopwordsPath + " " + this.documentStem + " "+this.documentNormalization
                + " " + this.documentUseIDF +" "+ this.documentTFOption + " " + this.queryStem + " "+this.queryNormalization
                +" " +this.queryUseIDF +" "+this.queryTFOption +" "+this.relevanceFeedbackAlgo +" "+this.isRelevanceFeedback
                +" "+this.isQueryExpansion+" "+this.secondRetrievalDocs
                +" "+isExperiment +" "+isNormalInteractive +" "+isFeedbackInteractive
                +" "+documentPath +" "+queryPath +" "+relevanceJudgmentPath+" "+topN+" "+showN+" "+isSecondRetrieval;
        System.out.println("Option ["+out+"]");
        System.out.println("Query ["+queryInput+"]");
    }

    /**
     * Read Query from File
     */
    public void readQueryInput()
    {
        System.out.println("Loading Query");
        File queryInputFile = new File(filePath+".query");
        BufferedReader readerQ = null;
        String line = null;

        try {
            readerQ = new BufferedReader(new FileReader(queryInputFile));
        } catch (FileNotFoundException e) {
            System.out.println("option"+".query" + " is not found");
        }

        try {
            while ((line = readerQ.readLine())!=null) {
                this.queryInput = line.toString();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            readerQ.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
