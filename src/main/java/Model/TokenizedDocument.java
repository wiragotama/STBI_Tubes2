package Model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wiragotama
 * Kelas ini menyimpan data 1 dokumen yang sudah di pre-prosess
 */
public class TokenizedDocument {
    
    private List<String> text; //text of document, could be say tokenized text, already preprocessed

    /**
     * Constructor
     * @param terms
     */
    public TokenizedDocument(List<String> terms)
    {
        this.text = new ArrayList();
        int len = terms.size();
        for (int i=0; i<len; i++)
            this.text.add(terms.get(i));
    }
    
    /**
     * Copy Constructor
     * @param d Data
     */
    public TokenizedDocument(TokenizedDocument d)
    {
        this.text = new ArrayList();
        int len = d.getText().size();
        for (int i=0; i<len; i++)
            this.text.add(d.getText().get(i));
    }
    
    /**
     * 
     * @return text
     */
    public List<String> getText()
    {
        return this.text;
    }
    
    /**
     * 
     * @param text new text
     */
    public void setText(List<String> text)
    {
        this.text = new ArrayList();
        int len = text.size();
        for (int i=0; i<len; i++)
            this.text.add(text.get(i));
    }
    
    /**
    * Get instance
     * @return this instance
    */
    public TokenizedDocument instance()
    {
        return this;
    }
    
    /**
     * 
     * @return size
     */
    public int size()
    {
        return this.text.size();
    }
    
    /**
     * Output to screen
     */
    public void print()
    {
        System.out.println("Text    = "+this.text.toString());
    }
}
