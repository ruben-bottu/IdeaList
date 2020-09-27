package domain;

import java.util.Comparator;

public class TripletComparator<A extends Comparable<A>, B extends Comparable<B>, C extends Comparable<C>> implements Comparator<Triplet<A, B, C>> {

    @Override
    public int compare(Triplet<A, B, C> o1, Triplet<A, B, C> o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        int res = o1.first.compareTo(o2.first);
        if (res != 0) return res;
        res = o1.second.compareTo(o2.second);
        if (res != 0) return res;
        return o1.third.compareTo(o2.third);
    }
}
