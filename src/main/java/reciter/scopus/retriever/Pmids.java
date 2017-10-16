package reciter.scopus.retriever;

import java.util.List;

public class Pmids {
    private List<Object> pmids;
    private String type;

    public Pmids() {}

    public Pmids(List<Object> pmids, String type) {
        this.pmids = pmids;
        this.type = type;
    }
    public List<Object> getPmids() {
        return pmids;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}