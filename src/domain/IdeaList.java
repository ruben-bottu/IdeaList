package domain;

import java.util.*;
import java.util.function.*;

// acc == accumulator, cur == currentValue, idx == index
public class IdeaList<E> implements Iterable<E> {
    private final List<E> innerList;

    // Constructors and factory methods ===========================================================
    private IdeaList(Iterable<? extends E> elements) {
        List<E> result = new ArrayList<>();
        elements.forEach(result::add);
        innerList = Collections.unmodifiableList(result);
    }

    public static <E> IdeaList<E> of(Iterable<? extends E> elements) {
        return new IdeaList<>(elements);
    }

    @SafeVarargs
    public static <E> IdeaList<E> of(E... elements) {
        return IdeaList.of(Arrays.asList(elements));
    }

    public static <E> IdeaList<E> empty() {
        return IdeaList.of();
    }

    // Added these methods to avoid redundant copy of calculation result lists
    private IdeaList(List<? extends E> elements) {
        innerList = Collections.unmodifiableList(elements);
    }

    private static <E> IdeaList<E> readOnlyList(List<? extends E> elements) {
        return new IdeaList<>(elements);
    }


    // Getters ====================================================================================
    public E get(int index) {
        return innerList.get(index);
    }

    public E first() {
        return get(0);
    }

    public E single() {
        if (size() != 1) throw new IllegalStateException("List must contain exactly one element");
        return first();
    }

    public E last() {
        return get(lastIndex());
    }

    public E random() {
        return get(new Random().nextInt(size()));
    }

    public E find(Predicate<E> predicate) {
        return loopUntil(predicate, (cur, idx) -> cur, null);
    }

    public int indexOf(E element) {
        return innerList.indexOf(element);
    }

    public int indexOfFirst(Predicate<E> predicate) {
        return loopUntil(predicate, (cur, idx) -> idx, -1);
    }

    public int lastIndex() {
        return size() - 1;
    }

    public IdeaList<Integer> indices() {
        return rangeInclusive(0, lastIndex());
    }

    public int size() {
        return innerList.size();
    }

    public List<E> toList() {
        return innerList;
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerList);
    }

    @Override
    public String toString() {
        return innerList.toString();
    }

    @Override
    public Iterator<E> iterator() {
        return innerList.iterator();
    }


    // Checks =========================================================================================
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdeaList<?> ideaList = (IdeaList<?>) o;
        return Objects.equals(innerList, ideaList.innerList);
    }

    public boolean contains(E element) {
        return innerList.contains(element);
    }

    public boolean containsAll(Iterable<? extends E> elements) {
        for (E element : elements) {
            if (!contains(element)) return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return innerList.isEmpty();
    }

    public boolean isNested() {
        return get(0) instanceof IdeaList;
    }

    public boolean all(Predicate<E> predicate) {
        return loopUntil(cur -> !predicate.test(cur), (cur, idx) -> false, true);
    }

    public boolean any() {
        return size() > 0;
    }

    public boolean any(Predicate<E> predicate) {
        return loopUntil(predicate, (cur, idx) -> true, false);
    }

    public boolean none() {
        return !any();
    }

    public boolean none(Predicate<E> predicate) {
        return !any(predicate);
    }


    // Modifiers =======================================================================================
    private IdeaList<E> create(Consumer<List<E>> transform) {
        List<E> result = new ArrayList<>(innerList);
        transform.accept(result);
        return IdeaList.readOnlyList(result);
    }

    public IdeaList<E> insert(int index, Iterable<? extends E> elements) {
        List<E> result = new ArrayList<>(innerList);
        for (E element : elements) {
            result.add(index, element);
            index++;
        }
        return IdeaList.readOnlyList(result);
    }

    @SafeVarargs
    public final IdeaList<E> insert(int index, E... elements) {
        return insert(index, Arrays.asList(elements));
    }

    public IdeaList<E> concat(Iterable<? extends E> elements) {
        return insert(lastIndex() + 1, elements);
    }

    @SafeVarargs
    public final IdeaList<E> add(E... elements) {
        return concat(Arrays.asList(elements));
    }

    public IdeaList<E> remove(E element) {
        return filter(cur -> !cur.equals(element));
    }

    public IdeaList<E> removeAt(int index) {
        return filterIndexed((cur, idx) -> idx != index);
    }

    public void forEachIndexed(ObjIntConsumer<? super E> action) {
        for (int i = 0; i < size(); i++) {
            action.accept(get(i), i);
        }
    }

    public void forEach(Consumer<? super E> action) {
        forEachIndexed((cur, idx) -> action.accept(cur));
    }


    // Functional programming methods ====================================================================
    private <A> A reduceHelper(TriFunction<A, E, Integer, A> operation, A initialValue, int startIndex) {
        A accumulator = initialValue;
        for (int i = startIndex; i < size(); i++) {
            accumulator = operation.apply(accumulator, get(i), i);
        }
        return accumulator;
    }

    public <A> A reduceIndexed(TriFunction<A, E, Integer, A> operation, A initialValue) {
        return reduceHelper(operation, initialValue, 0);
    }

    public <A> A reduce(BiFunction<A, E, A> operation, A initialValue) {
        return reduceIndexed((acc, cur, idx) -> operation.apply(acc, cur), initialValue);
    }

    public E reduceIndexed(TriFunction<E, E, Integer, E> operation) {
        return reduceHelper(operation, first(), 1);
    }

    public E reduce(BinaryOperator<E> operation) {
        return reduceIndexed((acc, cur, idx) -> operation.apply(acc, cur));
    }

    public <R> IdeaList<R> mapIndexed(BiFunction<E, Integer, R> transform) {
        return reduceIndexed((acc, cur, idx) -> acc.add(transform.apply(cur, idx)), IdeaList.empty());
    }

    public <R> IdeaList<R> map(Function<E, R> transform) {
        return mapIndexed((cur, idx) -> transform.apply(cur));
    }

    public <R> IdeaList<R> selectIndexed(BiFunction<E, Integer, R> transform) {
        return mapIndexed(transform);
    }

    public <R> IdeaList<R> select(Function<E, R> transform) {
        return map(transform);
    }

    public IdeaList<E> filterIndexed(BiPredicate<E, Integer> predicate) {
        return reduceIndexed((acc, cur, idx) -> predicate.test(cur, idx) ? acc.add(cur) : acc, IdeaList.empty());
    }

    public IdeaList<E> filter(Predicate<E> predicate) {
        return filterIndexed((cur, idx) -> predicate.test(cur));
    }

    public IdeaList<E> whereIndexed(BiPredicate<E, Integer> predicate) {
        return filterIndexed(predicate);
    }

    public IdeaList<E> where(Predicate<E> predicate) {
        return filter(predicate);
    }

    public IdeaList<E> flatten() {
        return isNested() ? ((IdeaList<IdeaList<E>>) this).reduce(IdeaList::concat, IdeaList.empty()) : this;
    }

    private <A, B> boolean allHaveNext(Triplet<Iterator<E>, Iterator<A>, Iterator<B>> iterators) {
        return iterators.first.hasNext() && iterators.second.hasNext() && iterators.third.hasNext();
    }

    private <A, B> Triplet<E, A, B> giveNextElements(Triplet<Iterator<E>, Iterator<A>, Iterator<B>> iterators) {
        return Triplet.of(iterators.first.next(), iterators.second.next(), iterators.third.next());
    }

    public <A, B> IdeaList<Triplet<E, A, B>> zipWith(Iterable<A> other, Iterable<B> other2) {
        List<Triplet<E, A, B>> result = new ArrayList<>();
        Triplet<Iterator<E>, Iterator<A>, Iterator<B>> its = Triplet.of(iterator(), other.iterator(), other2.iterator());
        while (allHaveNext(its)) {
            result.add(giveNextElements(its));
        }
        return IdeaList.readOnlyList(result);
    }

    private <A> boolean allHaveNext(Pair<Iterator<E>, Iterator<A>> iterators) {
        return iterators.first.hasNext() && iterators.second.hasNext();
    }

    private <A> Pair<E, A> giveNextElements(Pair<Iterator<E>, Iterator<A>> iterators) {
        return Pair.of(iterators.first.next(), iterators.second.next());
    }

    public <A> IdeaList<Pair<E, A>> zipWith(Iterable<A> other) {
        List<Pair<E, A>> result = new ArrayList<>();
        Pair<Iterator<E>, Iterator<A>> its = Pair.of(iterator(), other.iterator());
        while (allHaveNext(its)) {
            result.add(giveNextElements(its));
        }
        return IdeaList.readOnlyList(result);
    }

    public int count(Predicate<E> predicate) {
        return reduce((acc, cur) -> predicate.test(cur) ? acc + 1 : acc, 0);
    }

    public int sumOf(ToIntFunction<E> selector) {
        return reduce((acc, cur) -> acc + selector.applyAsInt(cur), 0);
    }

    public double sumOfDouble(ToDoubleFunction<E> selector) {
        return reduce((acc, cur) -> acc + selector.applyAsDouble(cur), 0.0d);
    }

    private <R extends Comparable<R>> E extremes(Function<E, R> selector, IntPredicate predicate) {
        Pair<R, E> result = map(selector).zipWith(this)
                .reduce((acc, cur) -> predicate.test(acc.first.compareTo(cur.first)) ? acc : cur);
        return result.second;
    }

    public <R extends Comparable<R>> E maxBy(Function<E, R> selector) {
        return extremes(selector, compareToResult -> compareToResult > 0);
    }

    public <R extends Comparable<R>> E minBy(Function<E, R> selector) {
        return extremes(selector, compareToResult -> compareToResult < 0);
    }

    public IdeaList<E> reversed() {
        return create(Collections::reverse);
    }

    public <R extends Comparable<R>> IdeaList<E> sortBy(Function<E, R> selector) {
        List<Pair<R, E>> result = new ArrayList<>(map(selector).zipWith(this).toList());
        result.sort(new KeyValueComparator<>());
        return IdeaList.readOnlyList(result).map(cur -> cur.second);
    }

    public IdeaList<E> unique() {
        return filterIndexed((cur, idx) -> !drop(idx + 1).contains(cur));
    }

    public String joinWith(String prefix, String separator, String postfix) {
        return prefix + map(Object::toString).reduce((acc, cur) -> acc + separator + cur) + postfix;
    }

    public String joinWith(String separator) {
        return joinWith("", separator, "");
    }

    // Loop that stops and returns a value when the predicate is satisfied. If no element satisfies the predicate, allFailedValue is returned.
    private <R> R loopUntil(Predicate<E> predicate, BiFunction<E, Integer, R> givePassedValue, R allFailedValue) {
        for (int i = 0; i < size(); i++) {
            if (predicate.test(get(i))) return givePassedValue.apply(get(i), i);
        }
        return allFailedValue;
    }


    // Ranges =========================================================================================
    public static IdeaList<Integer> rangeInclusive(int from, int to) {
        List<Integer> result = new ArrayList<>(-from + to + 1);
        for (int i = from; i <= to; i++) result.add(i);
        return IdeaList.readOnlyList(result);
    }

    public static IdeaList<Integer> rangeExclusive(int from, int upTo) {
        return rangeInclusive(from, upTo - 1);
    }

    public static IdeaList<Integer> rangeLength(int from, int length) {
        return rangeExclusive(from, from + length);
    }

    public IdeaList<E> subListInclusive(int fromIndex, int toIndex) {
        return filterIndexed((cur, idx) -> (idx >= fromIndex && idx <= toIndex));
    }

    public IdeaList<E> subListExclusive(int fromIndex, int upToIndex) {
        return subListInclusive(fromIndex, upToIndex - 1);
    }

    public IdeaList<E> subListLength(int fromIndex, int length) {
        return subListExclusive(fromIndex, fromIndex + length);
    }

    public IdeaList<E> take(int n) {
        return subListExclusive(0, n);
    }

    public IdeaList<E> takeLast(int n) {
        return subListInclusive(lastIndex() - n + 1, lastIndex());
    }

    public IdeaList<E> drop(int n) {
        return subListInclusive(n, lastIndex());
    }

    public IdeaList<E> dropLast(int n) {
        return subListInclusive(0, lastIndex() - n);
    }
}