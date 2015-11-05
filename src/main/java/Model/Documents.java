package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas ini menyimpan data raw document (belum dipreprocess)
 */
public class Documents {
    private List<String> documents;

    /**
     * Constructor
     * @param documentPath
     */
    public Documents(String documentPath){
        documents = new ArrayList();
        load((documentPath));
    }

    /**
     * load documents (raw) from path
     * @param documentPath
     */
    public void load(String documentPath) {
        documents = new ArrayList();
        String currentString = ""; // state untuk tipe input yang sedang dibaca
        String currentDocument = "";
        String currentDocumentTitle = "";
        String title = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(documentPath));
            String currentLine = br.readLine();

            while (currentLine != null) {
                title = currentLine;
                currentLine = currentLine.toLowerCase();
                if (currentLine.startsWith(".i") ||
                        currentLine.startsWith(".t") ||
                        currentLine.startsWith(".a") ||
                        currentLine.startsWith(".w") ||
                        currentLine.startsWith(".x")) {
                    currentString = currentLine;
                }

                if (currentString.startsWith(".i")) {
                    if (!currentDocument.equalsIgnoreCase("")) {
                        documents.add(currentDocument.substring(0, currentDocument.length() - 1));
                        //title tidak diperlukan menurut pak rila
                        currentDocument = "";
                        currentDocumentTitle = "";
                    }
                }
                if (currentString.startsWith(".t") && !currentLine.startsWith(".t")) {
                    currentDocument += currentLine + " ";
                    currentDocumentTitle += title + " ";
                }
                if (currentString.startsWith(".a") && !currentLine.startsWith(".a")) {
                    currentDocument += currentLine + " ";
                }
                if (currentString.startsWith(".w") && !currentLine.startsWith(".w")) {
                    for (String word : currentLine.split(" ")) {
                        if (!word.equalsIgnoreCase("")) {
                            currentDocument += word + " ";
                        }
                    }
                }

                currentLine = br.readLine();
            }

            if (!currentDocument.equalsIgnoreCase("")) {
                documents.add(currentDocument);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print documents
     */
    public void print()
    {
        for(int i=0; i<documents.size(); i++)
        {
            System.out.printf("Document[%d]: %s\n", i, documents.get(i).toString());
        }
    }

    /**
     * Get all tokenized documents
     * @return documents
     */
    public List<String> getDocuments(){
        return this.documents;
    }

    /**
     * Get document at a specified index
     * @param index
     * @return document
     */
    public String getDocument(int index){
        return this.documents.get(index);
    }

    /**
     *
     * @return documents size
     */
    public int size()
    {
        return this.documents.size();
    }

    /**
     * Clear memory
     */
    public void clear(){
        this.documents.clear();
    }
}
