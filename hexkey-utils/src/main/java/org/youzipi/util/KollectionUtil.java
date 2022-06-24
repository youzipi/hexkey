package org.youzipi.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wuqiantai
 */
public class KollectionUtil {


    public static int size(final Collection<?> coll) {
        return isEmpty(coll)
                ? 0
                : coll.size();
    }

    public static boolean isEmpty(final Collection<?> coll) {
        return CollectionUtils.isEmpty(coll);
    }

    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }


    public static boolean isEmpty(final Map<?, ?> map) {
        return MapUtils.isEmpty(map);
    }


    /**
     * 以 m2 为基准
     *
     * @param m1
     * @param m2
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> concat(Map<K, V> m1, Map<K, V> m2) {
        if (isEmpty(m1)) {
            if (isEmpty(m2)) {
                return Collections.emptyMap();
            } else {
                // todo map copy
                return m2;
            }
        } else if (isEmpty(m2)) {
            return Collections.emptyMap();
        } else {
            // todo 支持多个(2+) map
//            Arrays.asList(m1.entrySet(),m2.entrySet(),m3.entrySet);
            Map<K, V> m3 = Stream.concat(
                            m1.entrySet().stream(),
                            m2.entrySet().stream()
                    )
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, e2) -> e2
                    ));
            return m3;
        }
    }

    public static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    /**
     * jdk9-likely mapOf
     *
     * @param entries
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> mapOfEntries(Map.Entry... entries) {
        HashMap<K, V> map = new HashMap<>(entries.length);
        for (Map.Entry<K, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * a - b
     *
     * @param a
     * @param b
     * @param getter 依赖的字段
     * @param <T>
     * @return
     */
    public static <T, D> List<T> diff(List<T> a, List<T> b, Function<T, D> getter) {
        if (isEmpty(a)) {
            return Collections.emptyList();
        } else if (isEmpty(b)) {
            return a;
        }
        final Map<D, List<T>> aMap = a.stream().collect(
                Collectors.groupingBy(getter)
        );
        final Map<D, List<T>> bMap = b.stream().collect(
                Collectors.groupingBy(getter)
        );
        final Set<D> bKeys = bMap.keySet();
        final List<T> result = aMap.entrySet()
                .stream()
                .filter(entry -> !bKeys.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return result;
    }

    /**
     * stream enhance
     */

    /**
     * @param keyExtractor
     * @param <T>
     * @param <E>
     * @return
     * @see https://www.baeldung.com/java-streams-distinct-by#1-using-a-stateful-filter
     */
    public static <T, E> Predicate<T> distinctBy(Function<T, E> keyExtractor) {
        Map<E, Boolean> seen = new ConcurrentHashMap<>();
        Predicate<T> predicate = t -> {
            Boolean prevVal = seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE);
            /**
             * 第一次见，就是 true，
             * 之前出现过，就是 false
             */
            return prevVal == null;
        };
        return predicate;
    }

}
