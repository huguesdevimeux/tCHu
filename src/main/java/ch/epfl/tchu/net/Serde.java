package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public interface Serde<T> {

    String serialize(T t);

    T deserialize(String s);

    static <T> Serde<T> of(Function<T, String> serializer, Function<String, T> deserializer) {
        return new Serde<>() {
            @Override
            public String serialize(T obj) {
                return serializer.apply(obj);
            }

            @Override
            public T deserialize(String s) {
                return deserializer.apply(s);
            }
        };
    }

    static <T> Serde<T> oneOf(List<T> objList) {
        Function<T, String> serialize = (T t) -> String.valueOf(objList.indexOf(t));
        Function<String, T> deserialize = (String s) -> objList.get(Integer.parseInt(s));
        return Serde.of(serialize, deserialize);
    }

    static <T> Serde<List<T>> listOf(Serde<T> obj, String separator) {
        return new Serde<>() {
            @Override
            public String serialize(List<T> t) {
                List<String> stringList =
                        t.stream().map(obj::serialize).collect(Collectors.toList());
                return String.join(separator, stringList);
            }

            @Override
            public List<T> deserialize(String s) {
                return Arrays.stream(s.split(Pattern.quote(separator), -1))
                        .map(obj::deserialize)
                        .collect(Collectors.toList());
            }
        };
    }

    static <T extends Comparable<T>> Serde<SortedBag<T>> bagOf(Serde<T> obj, String separator) {
        return new Serde<>() {
            @Override
            public String serialize(SortedBag<T> ts) {
                List<String> stringList =
                        ts.toList().stream().map(obj::serialize).collect(Collectors.toList());
                return String.join(separator, stringList);
            }

            @Override
            public SortedBag<T> deserialize(String s) {
                List<String> a = Arrays.asList(s.split(Pattern.quote(separator), -1));
                return SortedBag.of(a.stream().map(obj::deserialize).collect(Collectors.toList()));
            }
        };
    }
}
