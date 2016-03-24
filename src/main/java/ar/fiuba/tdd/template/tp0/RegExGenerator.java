package ar.fiuba.tdd.template.tp0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RegExGenerator {
    private static final Random RANDOM = new Random();
    private static final int START_ASCII = 33;
    private static final int END_ASCII = 127;
    private int maxLength;

    public RegExGenerator(int maxLength) {
        this.maxLength = maxLength;
    }

    private Stream<String> identity(String str) {
        return Stream.of(str);
    }

    private Stream<String> appendRandom(String str) {
        return RANDOM
                .ints(START_ASCII, END_ASCII)
                .mapToObj(i -> str + (char) i)
                .limit(maxLength);
    }

    private String notEmptyApply(String str, Function<String, String> fn) {
        if (str.isEmpty()) {
            return str;
        } else {
            return fn.apply(str);
        }
    }

    private Stream<String> repeatLast(String astr) {
        return Stream.of(notEmptyApply(astr, str -> str + str.charAt(str.length() - 1)));

    }

    private Stream<String> removeLast(String astr) {
        return Stream.of(notEmptyApply(astr, str -> str.substring(0, str.length() - 1)));
    }

    private Function<String, Stream<String>> all(Function<String, Stream<String>>... fns) {
        return str -> Stream.of(fns).flatMap(f -> f.apply(str));
    }


    public List<String> generate(String regEx, int numberOfResults) {
        Iterator<Character> iterator = regEx.chars()
                .mapToObj(i -> Character.valueOf((char) i))
                .iterator();

        // for each char do the matching operations
        Stream<String> result = Stream.of("");
        while (iterator.hasNext()) {
            char chr = iterator.next();
            switch (chr) {
                case '.':
                    result = result.flatMap(this::appendRandom);
                    break;
                case '+':
                    result = result.flatMap(this::repeatLast);
                    break;
                case '?':
                    result = result.flatMap(all(this::removeLast, this::identity));
                    break;
                case '*':
                    result = result.flatMap(all(this::removeLast, this::identity, this::repeatLast));
                    break;
                case '[':
                    result = result.flatMap(this.appendAlternatives(this.readAlternation(iterator)));
                    break;
                case '\\':
                    if (!iterator.hasNext()) {
                        break;
                    }
                    chr = iterator.next();
                    result = result.map(this.appendChar(chr));
                    break;
                default:
                    result = result.map(this.appendChar(chr));
            }
        }
        return result.limit(numberOfResults).collect(Collectors.toList());
    }

    private Function<String, String> appendChar(char chr) {
        return str -> str + chr;
    }

    private Function<String, Stream<? extends String>> appendAlternatives(List<Character> alternatives) {
        return str -> alternatives.stream().map(chr -> str + chr);
    }

    /*
     * Read all char til a closing bracket,
     * returns the characters collected.
     */
    private List<Character> readAlternation(Iterator<Character> iterator) {
        List<Character> alternatives = new ArrayList<>();
        while (iterator.hasNext()) {
            char chr = iterator.next();
            if (chr == ']') {
                break;
            }
            alternatives.add(chr);
        }
        return alternatives;
    }
}