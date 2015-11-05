package Model;

import java.util.ArrayList;
import java.util.List;
import Preprocessor.*;

/**
 * Created by wiragotama on 11/4/15.
 * Kelas ini menyimpan definisi 1 query
 */
public class Query {
    protected List<String> query;
    protected List<Double> weight;

    /**
     * Another Constructor
     */
    public Query(String query)
    {
        this.query = new ArrayList();
        this.weight = new ArrayList();
        this.query.add(query);
    }

    /**
     * Another Constructor
     */
    public Query(List<String> query)
    {
        this.query = new ArrayList();
        this.weight = new ArrayList();
        for (int i=0; i<query.size(); i++) {
            this.query.add(query.get(i));
            weight.add(-1.0); //initialize
        }
    }

    /**
     * Copy constructor
     * @param q
     */
    public Query(Query q)
    {
        this.query = new ArrayList();
        this.weight = new ArrayList();
        for (int i=0; i<q.query.size(); i++)
            this.query.add(q.query.get(i));
        for (int i=0; i<q.weight.size(); i++)
            this.weight.add(q.weight.get(i));
    }

    /**
     * Set weight at idx with value
     * @param idx
     * @param value
     */
    public void setWeight(int idx, double value)
    {
        this.weight.set(idx, value);
    }

    /**
     * @return weight
     */
    public List<Double> getWeight()
    {
        return this.weight;
    }

    /**
     * @param idx
     * @return weight at specific index
     */
    public double getWeightAt(int idx)
    {
        return this.weight.get(idx);
    }

    public void weightLength(int n)
    {
        for (int i=0; i<n; i++)
            this.weight.add(0.0);
    }

    /**
     * @return query
     */
    public List<String> getQuery()
    {
        return this.query;
    }

    /**
     * @param idx
     * @return queryTerm at idx
     */
    public String queryTerm(int idx)
    {
        return this.query.get(idx);
    }

    /**
     * Set Query Text
     * @param queryText
     */
    public void setQuery(List<String> queryText)
    {
        this.query = new ArrayList();
        this.weight = new ArrayList();
        for (int i=0; i<queryText.size(); i++) {
            query.add(queryText.get(i));
        }
    }

    /**
     * clear memory
     */
    public void clear()
    {
        this.query.clear();
        this.weight.clear();
    }
}
