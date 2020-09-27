package domain;

import java.util.Comparator;

public class KeyValueComparator<K extends Comparable<K>, V> implements Comparator<Pair<K, V>> {

    @Override
    public int compare(Pair<K, V> o1, Pair<K, V> o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        return o1.first.compareTo(o2.first);
    }
}
