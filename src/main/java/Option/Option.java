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
    public int relevanceFeedback; //0 rocchio, 1 ide reguler, 2 ide dec-hi, -1 kalau experimen biasa
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

        this.relevanceFeedback = 0;
        this.secondRetrievalDocs = 0;

        this.isExperiment = true;
        this.isNormalInteractive = false;
        this.isFeedbackInteractive = false;
        this.documentPath = "test_collections/adi/adi.all";
        this.queryPath = "test_collections/adi/query.text";
        this.relevanceJudgmentPath = "test_collections/adi/qrels/text";
        this.queryInput = "information retrieval";
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
                this.stopwordsRemoval = Boolean.valueOf(split[1]);
                stopwordsPath = String.valueOf(split[19]);

                this.documentStem = Boolean.valueOf(split[2]);
                this.documentNormalization = Boolean.valueOf(split[3]);
                this.documentUseIDF = Boolean.valueOf(split[4]);
                this.documentTFOption = Integer.valueOf(split[5]);

                this.queryStem = Boolean.valueOf(split[6]);
                this.queryNormalization = Boolean.valueOf(split[7]);
                this.queryUseIDF = Boolean.valueOf(split[8]);
                this.queryTFOption = Integer.valueOf(split[9]);

                this.relevanceFeedback = Integer.valueOf(split[10]);
                this.secondRetrievalDocs = Integer.valueOf(split[11]);

                isExperiment = Boolean.valueOf(split[12]);
                isNormalInteractive = Boolean.valueOf(split[13]);
                isFeedbackInteractive = Boolean.valueOf(split[14]);
                documentPath = String.valueOf(split[15]);
                queryPath = String.valueOf(split[16]);
                relevanceJudgmentPath = String.valueOf(split[17]);
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
                    +" " +this.queryUseIDF +" "+this.queryTFOption +" "+this.relevanceFeedback +" "+this.secondRetrievalDocs
                    +" "+isExperiment +" "+isNormalInteractive +" "+isFeedbackInteractive
                    +" "+documentPath +" "+queryPath +" "+relevanceJudgmentPath;
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
                +" " +this.queryUseIDF +" "+this.queryTFOption +" "+this.relevanceFeedback +" "+this.secondRetrievalDocs
                +" "+isExperiment +" "+isNormalInteractive +" "+isFeedbackInteractive
                +" "+documentPath +" "+queryPath +" "+relevanceJudgmentPath;
        System.out.println("Option ["+out+"]");
        System.out.println("Query ["+queryInput+"]");
    }

    /**
     * Read Option for TF, IDF, Normalization, Stemming, and Experiment / Interactive from GUI, special for experiment
     */
    private void readOption() {
        isExperiment = true;
        String currentLine;
        Scanner scanner = new Scanner(System.in);
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("experiment"))
            isExperiment = true;
        else
            isExperiment = false;

        if (currentLine.equalsIgnoreCase("normalInteractive"))
            isNormalInteractive = true;
        else isNormalInteractive = false;

        if (currentLine.equalsIgnoreCase("feedbackInteractive"))
            isFeedbackInteractive = true;
        else isFeedbackInteractive = false;

        this.documentPath = scanner.nextLine();
        this.queryPath = scanner.nextLine();
        this.relevanceJudgmentPath = scanner.nextLine();
        this.stopwordsPath = scanner.nextLine();

        //Document TF
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("notf"))
            this.documentTFOption = 0;
        else if (currentLine.equalsIgnoreCase("rawtf"))
            this.documentTFOption = 1;
        else if (currentLine.equalsIgnoreCase("binarytf"))
            this.documentTFOption = 2;
        else if (currentLine.equalsIgnoreCase("augmentedtf"))
            this.documentTFOption = 3;
        else if (currentLine.equalsIgnoreCase("logarithmictf"))
            this.documentTFOption = 4;

        //Document IDF
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("noidf"))
            this.documentUseIDF = false;
        else if (currentLine.equalsIgnoreCase("usingidf"))
            this.documentUseIDF = true;

        //Document normalize
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("nonormalization"))
            this.documentNormalization = false;
        else if (currentLine.equalsIgnoreCase("usingnormalization"))
            this.documentNormalization = true;

        //Document Stemming
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("nostemming"))
            this.documentStem = false;
        else if (currentLine.equalsIgnoreCase("usingstemming"))
            this.documentStem = true;

        //Query TF
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("notf"))
            this.queryTFOption = 0;
        else if (currentLine.equalsIgnoreCase("rawtf"))
            this.queryTFOption = 1;
        else if (currentLine.equalsIgnoreCase("binarytf"))
            this.queryTFOption = 2;
        else if (currentLine.equalsIgnoreCase("augmentedtf"))
            this.queryTFOption = 3;
        else if (currentLine.equalsIgnoreCase("logarithmictf"))
            this.queryTFOption = 4;

        //Query IDF
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("noidf"))
            this.queryUseIDF = false;
        else if (currentLine.equalsIgnoreCase("usingidf"))
            this.queryUseIDF = true;

        //Query normalize
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("nonormalization"))
            this.queryNormalization = false;
        else if (currentLine.equalsIgnoreCase("usingnormalization"))
            this.queryNormalization = true;

        //Query Stemming
        currentLine = scanner.nextLine();
        if (currentLine.equalsIgnoreCase("nostemming"))
            this.queryStem = false;
        else if (currentLine.equalsIgnoreCase("usingstemming"))
            this.queryStem = true;
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
