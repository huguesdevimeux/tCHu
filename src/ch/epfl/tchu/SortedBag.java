package ch.epfl.tchu;

import java.util.*;
import java.util.stream.Stream;

/**
 * Multiensemble trié et immuable.
 *
 * @param <E> le type des éléments du multiensemble.
 */
public final class SortedBag<E extends Comparable<E>> implements Iterable<E> {
    // Table associant sa multiplicité à chaque élément de l'ensemble.
    // Invariant : toutes les multiplicités sont strictement positives (> 0).
    private final SortedMap<E, Integer> elements;

    /**
     * Crée un multiensemble vide.
     * @param <E> le type des éléments du multiensemble.
     * @return un multiensemble vide.
     */
    public static <E extends Comparable<E>> SortedBag<E> of() {
        return new SortedBag<E>(new TreeMap<>());
    }

    /**
     * Crée un multiensemble contenant un seul élément.
     * @param e1 l'unique élément du multiensemble
     * @param <E> le type de l'élément du multiensemble
     * @return un multiensemble ne contenant que l'élément <code>e1</code>
     */
    public static <E extends Comparable<E>> SortedBag<E> of(E e1) {
        return of(1, e1);
    }

    /**
     * Crée un multiensemble contenant un élément avec une multiplicité donnée.
     * @param n la multiplicité de l'élément <code>e</code> (>= 0)
     * @param e l'élément du multiensemble
     * @param <E> le type de l'élément du multiensemble
     * @return un multiensemble contenant <code>n</code> occurrences de <code>e</code>
     * @throws IllegalArgumentException si <code>n</code> est négatif
     */
    public static <E extends Comparable<E>> SortedBag<E> of(int n, E e) {
        Preconditions.checkArgument(0 <= n);
        return n == 0 ? of() : new SortedBag<>(new TreeMap<>(Map.of(e, n)));
    }

    /**
     * Crée un multiensemble contenant deux éléments, chacun avec une multiplicité donnée.
     * @param n1 la multiplicité de l'élément <code>e1</code> (>= 0)
     * @param e1 le premier élément du multiensemble
     * @param n2 la multiplicité de l'élément <code>e2</code> (>= 0)
     * @param e2 le second élément du multiensemble
     * @param <E> le type des éléments du multiensemble
     * @return un multiensemble contenant <code>n1</code> occurrences de <code>e1</code>,
     * et <code>n2</code> de <code>e2</code>
     * @throws IllegalArgumentException si <code>n1</code> ou <code>n2</code> est négatif
     */
    public static <E extends Comparable<E>> SortedBag<E> of(int n1, E e1, int n2, E e2) {
        Preconditions.checkArgument(0 <= n1 && 0 <= n2);
        var elements = new TreeMap<E, Integer>();
        if (n1 > 0) elements.put(e1, n1);
        if (n2 > 0) elements.put(e2, n2);
        return new SortedBag<>(elements);
    }

    /**
     * Crée un multiensemble contenant les éléments d'un itérable (p.ex. une liste)
     * @param iterable l'itérable fournissant les éléments du multiensemble
     * @param <E> le type des éléments du multiensemble
     * @return un multiensemble contenant exactement les mêmes éléments que <code>iterable</code>
     */
    public static <E extends Comparable<E>> SortedBag<E> of(Iterable<E> iterable) {
        var builder = new Builder<E>();
        iterable.forEach(builder::add);
        return builder.build();
    }

    // Construit un multiensemble avec la table des multiplicités donnée.
    // Les multiplicités doivent toutes être strictement positives.
    // Attention: la table n'est pas copiée et ne doit donc jamais être modifiée !
    private SortedBag(SortedMap<E, Integer> elements) {
        assert elements.values().stream().allMatch(n -> n > 0);
        this.elements = Collections.unmodifiableSortedMap(elements);
    }

    /**
     * Retourne vrai ssi le multiensemble est vide.
     * @return vrai ssi le multiensemble est vide.
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Retourne le nombre d'éléments du multiensemble.
     * @return le nombre d'éléments dans le multiensemble.
     */
    public int size() {
        return elements.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    /**
     * Retourne la multiplicité de l'élément donné.
     * @param element l'élément dont la multiplicité doit être retournée
     * @return la multiplicité de <code>element</code>, 0 s'il n'appartient pas au multiensemble
     */
    public int countOf(E element) {
        return elements.getOrDefault(element, 0);
    }

    /**
     * Retourne vrai ssi l'élément donné appartient au multiensemble (au moins une fois).
     * @param element l'élément dont la présence doit être testée
     * @return vrai ssi <code>element</code> appartient au multiensemble
     */
    public boolean contains(E element) {
        return elements.containsKey(element);
    }

    /**
     * Retourne vrai ssi le multiensemble donné est un sous-ensemble de celui-ci.
     * @param that le multiensemble dont on doit déterminer s'il est un sous-ensemble
     * @return vrai ssi <code>that</code> est un sous-ensemble de <code>this</code>
     */
    public boolean contains(SortedBag<E> that) {
        return that.elements.entrySet().stream()
                .allMatch(e -> e.getValue() <= countOf(e.getKey()));
    }

    /**
     * Retourne l'élément du sous-ensemble d'index donné.
     * @param index l'index de l'élément à retourner
     * @return l'élément d'index donné
     * @throws IndexOutOfBoundsException si l'index est invalide
     */
    public E get(int index) {
        Objects.checkIndex(index, size());
        for (var elementsAndCount : elements.entrySet()) {
            var count = elementsAndCount.getValue();
            if (index < count)
                return elementsAndCount.getKey();
            index -= count;
        }
        throw new Error(); // ne devrait jamais se produire
    }

    /**
     * Retourne l'union du multiensemble récepteur et du multiensemble donné.
     * @param that le multiensemble à combiner avec le récepteur
     * @return l'union de <code>this</code> et de <code>that</code>
     */
    public SortedBag<E> union(SortedBag<E> that) {
        var newElements = new TreeMap<>(elements);
        that.elements.forEach((e, n) -> newElements.merge(e, n, Integer::sum));
        return new SortedBag<>(newElements);
    }

    /**
     * Retourne la différence entre le multiensemble récepteur et le multiensemble donné.
     * @param that le multiensemble à combiner avec le récepteur
     * @return la différence entre <code>this</code> et <code>that</code>
     */
    public SortedBag<E> difference(SortedBag<E> that) {
        var newElements = new TreeMap<>(elements);
        that.elements.forEach((eR, nR) ->
                newElements.compute(eR, (e, n) -> n != null && n > nR ? n - nR : null));
        return new SortedBag<>(newElements);
    }

    /**
     * Retourne tous les sous-ensembles du multiensemble ayant une taille donnée.
     * @param size la taille des sous-ensembles à retourner
     * @return l'ensemble des sous-ensembles de <code>this</code> de taille <code>size</code>
     * @throws IllegalArgumentException si <code>size</code> n'est pas comprise entre 0 et
     * la taille du multiensemble
     */
    public Set<SortedBag<E>> subsetsOfSize(int size) {
        Preconditions.checkArgument(0 <= size && size <= size());
        if (size == 0)
            return Set.of(SortedBag.of());

        var result = new HashSet<SortedBag<E>>();
        for (var e1 : elements.keySet()) {
            var s1 = SortedBag.of(e1);
            this.difference(s1)
                    .subsetsOfSize(size - 1)
                    .forEach(e -> result.add(s1.union(e)));
        }
        return result;
    }

    /**
     * Retourne une liste contenant les éléments du multiensemble, dans l'ordre.
     * @return une liste contenant les éléments de <code>this</code>
     */
    public List<E> toList() {
        var list = new ArrayList<E>(size());
        elements.forEach((v, n) -> list.addAll(Collections.nCopies(n, v)));
        return list;
    }

    /**
     * Retourne un flot des éléments du multiensemble, dans l'ordre.
     * @return un flot des éléments de <code>this</code>
     */
    public Stream<E> stream() {
        var builder = Stream.<E>builder();
        elements.forEach((v, n) -> { for (var i = 0; i < n; i++) builder.add(v); });
        return builder.build();
    }

    /**
     * Retourne un itérateur sur les éléments du multiensemble.
     * @return un itérateur sur les éléments de <code>this</code>
     */
    @Override
    public Iterator<E> iterator() {
        return stream().iterator();
    }

    /**
     * Retourne une table (immuable) associant sa multiplicité à chaque élément du multiensemble.
     * @return une table associant sa multiplicité à chaque élément de <code>this</code>
     */
    public Map<E, Integer> toMap() {
        return elements;
    }

    /**
     * Retourne l'ensemble des éléments du multiensemble.
     * Attention : les éléments apparaissant plus d'une fois dans le multiensemble
     * n'apparaissent qu'une seule fois dans l'ensemble retourné !
     * @return l'ensemble des éléments du multiensemble.
     */
    public Set<E> toSet() {
        return elements.keySet();
    }

    /**
     * Retourne la valeur de hachage du multiensemble.
     * @return la valeur de hachage du multiensemble.
     */
    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    /**
     * Retourne vrai ssi ce multiensemble est égal à l'objet donné (comparaison structurelle).
     * @param that l'objet avec lequel comparer <code>this</code>
     * @return vrai ssi <code>that</code> est un multiensemble égal à <code>this</code>
     */
    @Override
    public boolean equals(Object that) {
        return (that instanceof SortedBag<?>)
                && (elements.equals(((SortedBag<?>) that).elements));
    }

    /**
     * Retourne la représentation textuelle du multiensemble.
     * @return la représentation textuelle de <code>this</code>
     */
    @Override
    public String toString() {
        var j = new StringJoiner(", ", "{", "}");
        elements.forEach((e, n) -> j.add((n > 1 ? n + "×" : "") + e));
        return j.toString();
    }

    /**
     * Bâtisseur de multiensemble.
     * @param <E> le type des éléments du multiensemble à bâtir.
     */
    public static final class Builder<E extends Comparable<E>> {
        private final SortedMap<E, Integer> elements = new TreeMap<>();

        /**
         * Ajoute un nombre donné d'occurrences d'un élément au bâtisseur.
         * @param count le nombre d'occurrences de l'élément à ajouter
         * @param element l'élément à ajouter
         * @return le bâtisseur (<code>this</code>)
         * @throws IllegalArgumentException si <code>count</code> est négatif
         */
        public Builder<E> add(int count, E element) {
            Preconditions.checkArgument(0 <= count);
            if (count > 0) elements.merge(element, count, Integer::sum);
            return this;
        }

        /**
         * Ajoute une occurrence de l'élément au bâtisseur.
         * @param e l'élément à ajouter
         * @return le bâtisseur (<code>this</code>)
         */
        public Builder<E> add(E e) {
            return add(1, e);
        }

        /**
         * Ajoute tous les éléments du multiensemble donné au bâtisseur.
         * @param that le multiensemble dont les éléments sont à ajouter
         * @return le bâtisseur (<code>this</code>)
         */
        public Builder<E> add(SortedBag<E> that) {
            that.elements.forEach((e, c) -> elements.merge(e, c, Integer::sum));
            return this;
        }

        /**
         * Retourne vrai ssi le bâtisseur est actuellement vide.
         * @return vrai ssi le bâtisseur est actuellement vide.
         */
        public boolean isEmpty() {
            return elements.isEmpty();
        }

        /**
         * Retourne la taille actuelle du bâtisseur.
         * @return le nombre d'éléments ajoutés au bâtisseur jusqu'à présent
         */
        public int size() {
            return elements.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        /**
         * Retourne un multiensemble contenant les éléments ajoutés jusqu'à présent au bâtisseur.
         * @return un multiensemble contenant les éléments ajoutés à <code>this</code>
         */
        public SortedBag<E> build() {
            return new SortedBag<>(new TreeMap<>(elements));
        }
    }
}
