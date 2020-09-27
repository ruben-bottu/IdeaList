package domain;

import java.util.Comparator;

public class PairComparator<A extends Comparable<A>, B extends Comparable<B>> implements Comparator<Pair<A, B>> {

    @Override
    public int compare(Pair<A, B> o1, Pair<A, B> o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        int res = o1.first.compareTo(o2.first);
        if (res != 0) return res;
        return o1.second.compareTo(o2.second);
    }
}