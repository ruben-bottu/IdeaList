package domain;

import java.util.Comparator;

public class IdeaListComparator<E extends Comparable<E>> implements Comparator<IdeaList<E>> {

    @Override
    public int compare(IdeaList<E> o1, IdeaList<E> o2) {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        int res = o1.size() - o2.size();
        if (res != 0) return res;
        for (Pair<E, E> pair : o1.zip(o2)) {
            res = pair.first.compareTo(pair.second);
            if (res != 0) return res;
        }
        return 0;
    }
}