package VSM;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.Query;
import Model.TokenizedDocument;
import Model.TokenizedDocuments;
import Option.*;
import Preprocessor.Preprocessor;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas vector space model
 */
public class VSM {
    private TreeSet<String> terms; //cepetan pake set
    private Double[][] weightMatrix; //[N_terms][N_documents]
    /**
     *          doc1 doc2 doc3 ...
     * term1
     * term2
     * term3
     * ...
     */
    private int collectionSize;
    private ArrayList<Double> termsIDF;
    private Option option; //reference to Option object

    /**
     * Default Constructor
     */
    public VSM(Option option)
    {
        this.weightMatrix = new Double[0][0];
        this.terms = new TreeSet<String>();
        this.termsIDF = new ArrayList();
        this.option = option; //reference
    }

    /**
     *
     * @return collectionSize
     */
    public int getCollectionSize()
    {
        return this.collectionSize;
    }

    /**
     *
     * @return terms
     */
    public Set<String> getTerms()
    {
        return this.terms;
    }

    /**
     * @param str
     * @return index of term in set, -1 if not found
     */
    public int indexOfTerms(String str)
    {
        return this.terms.contains(str)? this.terms.headSet(str).size(): -1;
    }
    /**
     *
     * @return termsIDF
     */
    public ArrayList<Double> getTermsIDF()
    {
        return this.termsIDF;
    }

    /**
     *
     * @return weightMatrix
     */
    public Double[][] getWeightMatrix()
    {
        return this.weightMatrix;
    }

    /**
     * @param term
     * @param doc
     * @return weight of weightMatrix[term][doc]
     */
    public double weight(int term, int doc)
    {
        return this.weightMatrix[term][doc];
    }

    /*
    * Making union of all term vectors from papersCollection abstract
    * Producing ArrayList<String> terms
    */
    public void listAllTerms(TokenizedDocuments collection)
    {
        System.out.println("Listing all unique terms");
        this.terms.clear();

        Set<String> temp = new HashSet<String>();
        this.collectionSize = collection.size();
        for (int i=0; i<this.collectionSize; i++)
        {
            final int M = collection.get(i).getText().size();
            for (int j=0; j<M; j++)
            {
                temp.add(collection.get(i).getText().get(j));
            }
        }
        this.terms = new TreeSet(temp); //sortedset
    }

    /**
     * TF weight of term to the doc
     * @param option 0:no TF, 1:Raw TF, 2:Binary TF, 3:Augmented TF, 4:Logarithmic TF
     * @param term
     * @param doc
     * @return TF weight
     */
    public double TFWeight(int option, String term, List<String> doc)
    {
        if (option==0)
            return 0;
        else
        {
            double TF =  (double) Collections.frequency(doc, term);
            if (option==2)
            {
                if (TF>0) TF=1;
                //else, means TF is 0
            }
            else if (option==4)
            {
                if (TF!=0)
                    TF = 1+ Math.log(TF);
                //else, means TF is 0
            }
            return TF;
        }
    }

    /**
     * @param term
     * @param collection Documents collection
     * @return IDF weight of term in the collection
     */
    public double IDFWeight(String term, TokenizedDocuments collection)
    {
        int N = this.collectionSize;
        int dft=0; //prevent infinity, ganti jadi 1, tapi gk mungkin infinity sih
        Collection<TokenizedDocument> arrCol = collection.instances().values();
        for (TokenizedDocument tokDoc: arrCol)
        {
            if (tokDoc.getText().contains(term)) {
                dft++;
            }
        }
        if (dft>N) dft = N;
        return Math.log((double)N / (double)dft);
    }

    /**
     * Generate Terms IDF vector
     */
    private void generateTermsIDF(TokenizedDocuments collection)
    {
        System.out.println("Generate terms IDF");
        this.termsIDF.clear();
        int N = terms.size();
        for(String str : this.terms) {
            double idfTerm = IDFWeight(str, collection);
            this.termsIDF.add(idfTerm);
        }
    }

    /**
     * Make TF-IDF Matrix
     * @param collection documents collection
     */
    public void makeTFIDFWeightMatrix(TokenizedDocuments collection)
    {
        System.out.println("Making VSM");
        listAllTerms(collection);
        if (option.documentUseIDF)
            generateTermsIDF(collection);
        System.out.println("Making terms-weighting matrix");
        int N = this.terms.size();
        this.weightMatrix = new Double[N][collection.size()];

        double[] maxTF = new double[this.collectionSize]; //max TF for each terms in all documents
        Collection<TokenizedDocument> arrayCol = collection.instances().values();

        for (int j=0; j<this.collectionSize; j++)
            maxTF[j] = Integer.MIN_VALUE;

        //making VSM
        int docAt=0;
        for (TokenizedDocument tokDoc: arrayCol)
        {
            List<String> text = tokDoc.getText();
            int termAt = 0;
            for (String term: this.terms)
            {
                double weight = 0.0;
                //TF Part
                double TF = TFWeight(option.documentTFOption, term, text);
                if (option.documentTFOption!=0) //useTF
                    weight = TF;
                //else weight is 0

                if (TF>maxTF[docAt])
                    maxTF[docAt] = TF;
                this.weightMatrix[termAt][docAt] = weight;

                //IDF Part
                if (option.documentTFOption!=0) { //use TF and IDF
                    if (option.documentUseIDF)
                        this.weightMatrix[termAt][docAt] = this.weightMatrix[termAt][docAt] * termsIDF.get(termAt);
                    //else TF Only
                }
                else if (option.documentUseIDF) //IDF only
                    this.weightMatrix[termAt][docAt] = termsIDF.get(termAt);

                termAt++;
            }
            docAt++;
        }

        if (option.documentTFOption ==3) //augmented TF Case, devide by biggest TF in documents
        {
            /* 0.5 + 0.5*TF(T, D) / Max TF(T, Di) for Di is all documents */
            for (int i=0; i<N; i++)
                for (int j=0; j<this.collectionSize; j++)
                    if (this.weightMatrix[i][j]>0)
                        this.weightMatrix[i][j] = 0.5+0.5*this.weightMatrix[i][j]/maxTF[j];
        }

        if (option.documentNormalization) //normalize is counted to the terms vector
        {
            //foreach doc, count |doc weight|, for each element of doc devide by |doc weight|
            for (int j=0; j<this.collectionSize; j++) {
                double cosineLength = 0.0;
                for (int i=0; i<N; i++)
                {
                    cosineLength += Math.pow(this.weightMatrix[i][j], 2.0);
                }

                cosineLength = Math.sqrt(cosineLength);
                for (int i=0; i<N; i++)
                {
                    this.weightMatrix[i][j] = this.weightMatrix[i][j]/cosineLength;
                }
            }
        }
    }

    /**
     * Pembobotan query, anggap tiap query sudah di pre-proses
     * @param query
     * @return array of bobot
     */
    public double[] queryWeighting(Query query)
    {
        //kita bobotin query
        double maxTF = 0.0;
        int qsize = query.getUniqueTerms().size();
        query.resetWeight();
        double[] weightQ = new double[qsize];

        int i = 0;
        for(String term : query.getUniqueTerms())
        {
            //TF Part
            double weight = 0.0;
            double TF = TFWeight(option.queryTFOption, term, query.getQuery());
            if (option.queryTFOption != 0) { //use TF
                weight = TF;
            } else //not use TF, weight is 0

                if (TF > maxTF) //for augmented TF
                    maxTF = TF;
            weightQ[i] = weight;

            //IDF Part
            if (option.queryTFOption != 0) { //use TF and IDF
                if (option.queryUseIDF) {
                    int idx = getTermsIDF().indexOf(term);
                    if (idx!=-1) {
                        weightQ[i] = weightQ[i] * getTermsIDF().get(idx);
                    }
                    //else leave to TF only
                }
                //else TF only
            } else if (option.queryUseIDF) { //IDF only
                int idx = getTermsIDF().indexOf(term);
                if (idx != -1) {
                    weightQ[i] = weightQ[i] * getTermsIDF().get(idx);
                }
                //else just leave it alone
            }
            i++;
        }

        if (option.queryTFOption ==3) //augmented TF Case, devide by biggest TF in documents
        {
            /* 0.5 + 0.5*TF(T, D) / Max TF(T, Di) for Di is all documents */
            for (i=0; i<qsize; i++)
                if (weightQ[i]>0)
                    weightQ[i] = 0.5+0.5*query.getWeightAt(i);
        }

        if (option.queryNormalization) //normalize is counted to the terms vector
        {
            //foreach doc, count |doc weight|, for each element of doc devide by |doc weight|
            double cosineLength = 0.0;
            for (int j=0; j<qsize; j++) {
                cosineLength += Math.pow(query.getWeightAt(j), 2.0);
            }
            if (cosineLength==0)
                cosineLength = 1;
            else cosineLength = Math.sqrt(cosineLength);
            for (int j=0; j<qsize; j++) {
                weightQ[j] = weightQ[j]/cosineLength;
            }
        }

        return weightQ;
    }

    /**
     * Print Options
     */
    public void printOptions()
    {
        option.print();
    }

    /**
     * Output weight matrix to screen
     */
    public void printWeightMatrix()
    {
        int N = this.terms.size();
        int M = this.weightMatrix[0].length;

        for (int i=0; i<N; i++)
        {
            System.out.print("[ ");
            for (int j=0; j<M; j++)
                System.out.printf("%.2f ",this.weightMatrix[i][j]);
            System.out.println("]");
        }
    }

    /**
     * save inverted file, terms-IDF and configuration
     * @param folderPath without extension
     */
    public void save(String folderPath)
    {
        saveConfig(folderPath+"/vsm.config");
        saveIDF(folderPath+"/vsm.idf");
        saveToInvertedFile(folderPath+"/vsm.invertedFile");
    }

    /**
     * load inverted file, terms-IDF and configuration
     * @param folderPath without extension
     */
    public void load(String folderPath)
    {
        loadConfig(folderPath+"/vsm.config");
        loadIDF(folderPath+"/vsm.idf");
        loadFromInvertedFile(folderPath+"/vsm.invertedFile");
    }

    /**
     * Save to invertedFile
     * format (separated by space)
     * term docNo Weight
     * @param filePath
     */
    private void saveToInvertedFile(String filePath)
    {
        System.out.println("Saving Inverted File");
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            int i=0;
            for(String term : this.terms)
            {
                for (int j=0; j<this.collectionSize; j++)
                {
                    if (Double.compare(this.weightMatrix[i][j],0.0)!=0)
                    {
                        writer.println(term+" "+Integer.toString(j)+" "+this.weightMatrix[i][j]);
                    }
                }
                i++;
            }
            writer.close();
        }
        catch (Exception ex)
        {
            System.out.println("save inverted file failed");
        }
    }

    /**
     * load Data from inverted file
     * @param filePath
     */
    private void loadFromInvertedFile(String filePath)
    {
        System.out.println("Loading Inverted File");
        BufferedReader reader = null;
        String line = null;

        int colSize = this.collectionSize;
        int N = this.terms.size();
        this.weightMatrix = new Double[N][colSize];

        File invertedFile = new File(filePath);

        for (int i=0; i<N; i++) {
            ArrayList<Double> temp = new ArrayList();
            for (int j=0; j<colSize; j++) {
                this.weightMatrix[i][j] = 0.0;
            }
        }

        try
        {
            reader = new BufferedReader(new FileReader(invertedFile));
        }
        catch (FileNotFoundException ex)
        {
            System.out.println(filePath+" is not found");
        }

        try {
            Set<String> temp = new HashSet();
            while ((line = reader.readLine()) != null)
            {
                String split[] = line.split(" ");
                //split[0] term pada file eksternal sudah sorted, harusnya aman
                int idxDoc = Integer.valueOf(split[1]);
                double val = Double.valueOf(split[2]);
                this.weightMatrix[indexOfTerms(split[0])][idxDoc] = val;
            }
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }


        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(VSM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saving Terms IDF
     * @param filePath
     */
    private void saveIDF(String filePath)
    {
        System.out.println("Saving Terms IDF");
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            int i = 0;
            for(String term : this.terms)
            {
                if (option.documentUseIDF)
                    writer.println(term+" "+this.termsIDF.get(i).toString());
                else
                    writer.println(term);
                ++i;
            }
            writer.close();
        }
        catch (Exception ex)
        {
            System.out.println("save terms-IDF failed");
        }
    }

    /**
     * Load Terms IDF
     * @param filePath
     */
    private void loadIDF(String filePath)
    {
        System.out.println("Loading Terms IDF File");
        BufferedReader reader = null;
        String line = null;
        this.terms = new TreeSet<String>();
        this.termsIDF = new ArrayList();

        File idfFile = new File(filePath);

        try
        {
            reader = new BufferedReader(new FileReader(idfFile));
        }
        catch (FileNotFoundException ex)
        {
            System.out.println(filePath+" is not found");
        }

        Set<String> temp = new HashSet();

        try {
            while ((line = reader.readLine()) != null)
            {
                String split[] = line.split(" ");
                //this.terms.add(split[0]);
                temp.add(split[0]);
                if (option.documentUseIDF)
                    this.termsIDF.add(Double.valueOf(split[1]));
            }
            this.terms = new TreeSet<String>(temp);
        } catch (Exception ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }

        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(VSM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saving configuration options in making VSM
     * @param filePath
     */
    private void saveConfig(String filePath)
    {
        System.out.println("Saving Configuration");
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            writer.println(collectionSize);
            writer.close();
        }
        catch (Exception ex)
        {
            System.out.println("save config failed");
        }
    }

    /**
     * load configuration options from file
     * @param filePath
     */
    private void loadConfig(String filePath)
    {
        System.out.println("Loading Config File");
        this.terms = new TreeSet<String>();
        this.termsIDF = new ArrayList();

        File configFile = new File(filePath);
        BufferedReader reader = null;
        String line = null;

        try {
            reader = new BufferedReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            System.out.println(filePath + " is not found");
        }

        try {
            while((line = reader.readLine()) != null){
                String split[] = line.split(" ");

                this.collectionSize = Integer.valueOf(split[0]);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(VSM.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * getLine from inputStream separated by separator
     * @param inputStream
     * @param separator character
     * @return ArrayList<String>
     */
    public static ArrayList<String> getLine(FileInputStream inputStream, char separator)
    {
        try {
            StringBuffer str = new StringBuffer();
            ArrayList<String> line = new ArrayList();

            char character = (char) inputStream.read();
            while (character != '\n' && character != 65535)
            {
                if (character == separator)
                {
                    line.add(str.toString());
                    str.delete(0, str.length());
                }
                else
                    str.append(character);
                character = (char) inputStream.read();
            } //character == '\n'
            if (!str.toString().isEmpty())
                line.add(str.toString());
            return line;
        }
        catch (IOException ex) {
            return null;
        }
    }
}
