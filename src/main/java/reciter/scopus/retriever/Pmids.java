package reciter.scopus.retriever;

import java.util.List;

public class Pmids {
    private List<Long> pmids;

    public Pmids() {}

    public Pmids(List<Long> pmids) {
        this.pmids = pmids;
    }
    public List<Long> getPmids() {
        return pmids;
    }
}