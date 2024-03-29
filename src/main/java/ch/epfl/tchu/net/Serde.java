package ch.epfl.tchu.net;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Representation of a Serde (serialized or deserialized object).
 *
 * @param <T> type of the serde
 * @author Luca Mouchel (324748)
 * @author Hugues Devimeux (327282)
 */
public interface Serde<T> {

    /**
     * Serialization of object of type T into a String.
     *
     * @param object of type {@code T} to serialize
     * @return serialized string
     */
    String serialize(T object);

    /**
     * Deserialization of a string into an object of type T.
     *
     * @param s of type String to deserialize into type {@code T}
     * @return deserialized string
     */
    T deserialize(String s);

    /**
     * Creates a simple serde given the serializer and the deserializer.
     *
     * @param serializer Function to serialize an object of type {@code T}
     * @param deserializer Function to deserialize a String into an object of type {@code T}
     * @param <T> type of the object to (de)serialize
     * @return Serde of type {@code T}
     */
    static <T> Serde<T> of(Function<T, String> serializer, Function<String, T> deserializer) {
        return new Serde<>() {
            /**
             * Redefinition of {@code serialize(T object)
             *
             * @param object to serialize
             * @return serialized string
             */
            @Override
            public String serialize(T object) {
                return serializer.apply(object);
            }

            /**
             * Redefinition of {@code deserialize(String s)}
             *
             * @param s of type {@code String} to deserialize into type {@code T}
             * @return deserialized string
             */
            @Override
            public T deserialize(String s) {
                return deserializer.apply(s);
            }
        };
    }

    /**
     * Given a list, returns the serializer of its elements.
     *
     * @param objList to (de)serialize its elements
     * @param <T> type of the object to (de)serialize
     * @return Serde corresponding to a list
     * @throws IllegalArgumentException if the list in argument is empty
     */
    static <T> Serde<T> oneOf(List<T> objList) {
        Preconditions.checkArgument(!objList.isEmpty());
        Function<T, String> serialize = (T t) -> {
        	if (t == null) return NetConstants.Serdes.DEFAULT_VALUE_EMPTINESS;
        	return String.valueOf(objList.indexOf(t));
		};
        Function<String, T> deserialize = (String s) -> {
        	if (s.equals(NetConstants.Serdes.DEFAULT_VALUE_EMPTINESS)) return null;
        	return objList.get(Integer.parseInt(s));
		};
        return Serde.of(serialize, deserialize);
    }

    /**
     * Returns a serde capable of (de)serializing lists of (de)serialized values given by parameter
     * {@code serde}
     *
     * @param serde to use to (de)serialize
     * @param separator separating character between each element
     * @param <T> type of the object to (de)serialize
     * @return a serde capable of (de)serializing lists of (de)serialized values
     * @throws NullPointerException if the separator is null
     */
    static <T> Serde<List<T>> listOf(Serde<T> serde, String separator) {
        Objects.requireNonNull(separator);
        return new Serde<>() {
            /**
             * Returns a Serialized string where each element of the parameter is separated by
             * {@code separator}.
             *
             * @param listToSerialize each element is serialized and joined into a string
             * @return a serialized string
             */
            @Override

            public String serialize(List<T> listToSerialize) {
            	if (listToSerialize.isEmpty()) return NetConstants.Serdes.DEFAULT_VALUE_EMPTINESS;
                return Serde.serializeList(serde, listToSerialize, separator);
            }

            /**
             * Returns a deserialized string in the form of a list.
             *
             * @param s of type {@code String to deserialize into type {@code List<T>
             * @return a list that's been deserialized from the string s
             */
            @Override
            public List<T> deserialize(String s) {
            	if (s.equals(NetConstants.Serdes.DEFAULT_VALUE_EMPTINESS)) return Collections.emptyList();
                return Arrays.stream(s.split(Pattern.quote(separator), -1))
                        .map(serde::deserialize)
                        .collect(Collectors.toList());
            }
        };
    }

    /**
     * Returns a serde capable of (de)serializing Bags of (de)serialized values given by parameter
     * {@code serde}.
     *
     * @param serde to use to (de)serialize
     * @param separator separating character between each element
     * @param <T> type of the object to (de)serialize
     * @return a serde capable of (de)serializing bags of (de)serialized values
     */
    static <T extends Comparable<T>> Serde<SortedBag<T>> bagOf(Serde<T> serde, String separator) {
        return new Serde<>() {
            /**
             * Returns a Serialized string where each element of the parameter is separated by
             * {@code separator}.
             *
             * @param bagToSerialize each element is serialized and joined into a string
             * @return a serialized string
             */
            @Override
            public String serialize(SortedBag<T> bagToSerialize) {
				if (bagToSerialize.isEmpty()) return NetConstants.Serdes.DEFAULT_VALUE_EMPTINESS;
				return Serde.serializeList(serde, bagToSerialize.toList(), separator);
            }

            /**
             * Returns a deserialized string in the form of a Sorted Bag.
             *
             * @param s of type {@code String} to deserialize into type SortedBag<T>
             * @return a bag that's been deserialized from the string s
             */
            @Override
            public SortedBag<T> deserialize(String s) {
            	if (s.equals(NetConstants.Serdes.DEFAULT_VALUE_EMPTINESS)) return SortedBag.of();
            	List<String> splitString = Arrays.asList(s.split(Pattern.quote(separator), -1));
                return SortedBag.of(
                        splitString.stream().map(serde::deserialize).collect(Collectors.toList()));
            }
        };
    }

    /**
     * Private method in charge of serializing lists.
	 * If the list is empty, returns an empty string.
     *
     * @param serde to use to (de)serialize
     * @param listToSerialize serialize each element and join them in a string
     * @param separator character to separate each element of the list
     * @param <T> type of the object to (de)serialize
     * @return a String that's been serialized from the list
     */
    private static <T> String serializeList(
            Serde<T> serde, List<T> listToSerialize, String separator) {
		// If the list is empty, this will return an empty string.
        return listToSerialize.stream()
                .map(serde::serialize)
                .collect(Collectors.joining(separator));
    }
}
