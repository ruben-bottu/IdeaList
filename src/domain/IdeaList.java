package domain;

import java.util.*;
import java.util.function.*;

// acc == accumulator, cur == currentValue, idx == index
public class IdeaList<E> {
    private final List<E> innerList;

    // Constructors and factory methods ===========================================================
    private IdeaList(Collection<? extends E> elements) {
        innerList = List.copyOf(elements);
    }

    public static <E> IdeaList<E> of(Collection<? extends E> elements) {
        return new IdeaList<>(elements);
    }

    @SafeVarargs
    public static <E> IdeaList<E> of(E... elements) {
        return IdeaList.of(Arrays.asList(elements));
    }

    public static <E> IdeaList<E> empty() {
        return IdeaList.of();
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

    /*public E find(Function<E, Boolean> predicate) {
        return filter(predicate).first();
    }*/

    /*public E find(Predicate<E> predicate) {
        return loopUntil(predicate, cur -> cur, null);
    }*/

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

    private List<E> toList() {
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

    // TODO
    public String getElementType() {
        return get(0).getClass().getSimpleName();
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

    public boolean containsAll(Collection<? extends E> elements) {
        return innerList.containsAll(elements);
    }

    public boolean containsAll(IdeaList<? extends E> elements) {
        return containsAll(elements.toList());
    }

    public boolean isEmpty() {
        return innerList.isEmpty();
    }

    public boolean isNested() {
        return get(0) instanceof IdeaList;
    }

    /*public boolean all(Function<E, Boolean> predicate) {
        return filter(predicate).size() == size();
    }*/

    /*public boolean all(Predicate<E> predicate) {
        return loopUntil(cur -> !predicate.test(cur), cur -> false, true);
    }*/

    public boolean all(Predicate<E> predicate) {
        return loopUntil(cur -> !predicate.test(cur), (cur, idx) -> false, true);
    }

    public boolean any() {
        return size() > 0;
    }

    /*public boolean any(Function<E, Boolean> predicate) {
        return filter(predicate).any();
    }*/

    /*public boolean any(Predicate<E> predicate) {
        return loopUntil(predicate, cur -> true, false);
    }*/

    public boolean any(Predicate<E> predicate) {
        return loopUntil(predicate, (cur, idx) -> true, false);
    }


    // Modifiers =======================================================================================
    private IdeaList<E> create(Consumer<List<E>> transform) {
        List<E> result = new ArrayList<>(innerList);
        transform.accept(result);
        return IdeaList.of(result);
    }

    public IdeaList<E> insert(int index, Collection<? extends E> elements) {
        return create(list -> list.addAll(index, elements));
    }

    public IdeaList<E> insert(int index, IdeaList<? extends E> elements) {
        return insert(index, elements.toList());
    }

    @SafeVarargs
    public final IdeaList<E> insert(int index, E... elements) {
        return insert(index, Arrays.asList(elements));
    }

    public IdeaList<E> concat(Collection<? extends E> elements) {
        return insert(lastIndex() + 1, elements);
    }

    public IdeaList<E> concat(IdeaList<? extends E> elements) {
        return concat(elements.toList());
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

    public IdeaList<E> filterIndexed(BiPredicate<E, Integer> predicate) {
        return reduceIndexed((acc, cur, idx) -> predicate.test(cur, idx) ? acc.add(cur) : acc, IdeaList.empty());
    }

    public IdeaList<E> filter(Predicate<E> predicate) {
        return filterIndexed((cur, idx) -> predicate.test(cur));
    }

    public IdeaList<E> flatten() {
        return isNested() ? ((IdeaList<IdeaList<E>>) this).reduce(IdeaList::concat, IdeaList.empty()) : this;
    }

    public <A, B> IdeaList<Triplet<E, A, B>> zip(IdeaList<A> other, IdeaList<B> other2) {
        int sizeOfShortest = IdeaList.of(size(), other.size(), other2.size()).min(cur -> cur);
        return rangeExclusive(0, sizeOfShortest).map(idx -> Triplet.of(get(idx), other.get(idx), other2.get(idx)));
    }

    public <A> IdeaList<Pair<E, A>> zip(IdeaList<A> other) {
        int sizeOfShortest = IdeaList.of(size(), other.size()).min(cur -> cur);
        return rangeExclusive(0, sizeOfShortest).map(idx -> Pair.of(get(idx), other.get(idx)));
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

    /*private <R extends Comparable<R>> E extremes(Function<E, R> selector, IntPredicate predicate) {
        return reduce((acc, cur) -> predicate.test(selector.apply(acc).compareTo(selector.apply(cur))) ? acc : cur, first());
    }*/

    private <R extends Comparable<R>> E extremes(Function<E, R> selector, IntPredicate predicate) {
        Pair<R, E> result = map(selector).zip(this)
                .reduce((acc, cur) -> predicate.test(acc.first.compareTo(cur.first)) ? acc : cur);
        return result.second;
    }

    public <R extends Comparable<R>> E max(Function<E, R> selector) {
        return extremes(selector, compareToResult -> compareToResult > 0);
    }

    public <R extends Comparable<R>> E min(Function<E, R> selector) {
        return extremes(selector, compareToResult -> compareToResult < 0);
    }

    public IdeaList<E> reversed() {
        return create(Collections::reverse);
    }

    // TODO create sorted list
    /*public <R extends Comparable<R>> IdeaList<E> sorted() {
        return create(list -> Collections.sort(list));
    }*/

    public IdeaList<E> unique() {
        return filterIndexed((cur, idx) -> !drop(idx + 1).contains(cur));
    }

    // TODO create join method
    /*public String joinWith(String separator) {
        return reduce((acc, cur) -> acc + separator + cur);
    }*/

    // Loop that stops and returns a value when the predicate is satisfied. If no element satisfies the predicate, allFailedValue is returned.
    /*public <R> R loopUntil(Predicate<E> predicate, Function<E, R> givePassedValue, R allFailedValue) {
        for (E element : innerList) {
            if (predicate.test(element)) return givePassedValue.apply(element);
        }
        return allFailedValue;
    }*/

    public <R> R loopUntil(Predicate<E> predicate, BiFunction<E, Integer, R> givePassedValue, R allFailedValue) {
        for (int i = 0; i < size(); i++) {
            if (predicate.test(get(i))) return givePassedValue.apply(get(i), i);
        }
        return allFailedValue;
    }


    // Ranges =========================================================================================
    public static IdeaList<Integer> rangeInclusive(int from, int to) {
        List<Integer> result = new ArrayList<>();
        for (int i = from; i <= to; i++) result.add(i);
        return IdeaList.of(result);
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