package DocumentRanker;

import java.util.Comparator;

/**
 * Created by wiragotama on 11/5/15.
 * Kelas ini bermaksud sama seperti yang dibuat oleh timothai
 */
public class DocumentRank {
    public int docNum;
    public double SC;

    public DocumentRank()
    {
        this.docNum = 0;
        this.SC = 0;
    }

    public DocumentRank(int docNum, double SC)
    {
        this.docNum = docNum;
        this.SC = SC;
    }

    public int getDocNum()
    {
        return docNum;
    }

    public double getSC()
    {
        return SC;
    }

    public static Comparator<DocumentRank> comparator = new Comparator<DocumentRank>() {
        public int compare(DocumentRank o1, DocumentRank o2) {
            return Double.compare(o2.SC, o1.SC);
        }
    };

    public boolean equals(Object other)
    {
        if (!(other instanceof DocumentRank))
            return false;
        DocumentRank n = (DocumentRank) other;
        return Double.compare(n.SC, this.SC)==0;
    }
}
