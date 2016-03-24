package ar.fiuba.tdd.template.tp0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RegExGenerator {
    private Generator generator;

    public RegExGenerator(int maxLength) {
        this.generator = new Generator(maxLength);
    }

    public static class Generator {
        private static final Random RANDOM = new Random();
        private static final int START_ASCII = 33;
        private static final int END_ASCII = 127;
        private int maxLength;

        public Generator(int maxLength) {
            this.maxLength = maxLength;
        }

        public Stream<String> identity(String str) {
            return Stream.of(str);
        }

        public Stream<String> appendRandom(String str) {
            return RANDOM
                    .ints(START_ASCII, END_ASCII)
                    .mapToObj(i -> str + (char) i)
                    .limit(maxLength);
        }

        public String notEmptyApply(String str, Function<String, String> fn) {
            if (str.isEmpty()) {
                return str;
            } else {
                return fn.apply(str);
            }
        }

        public Stream<String> repeatLast(String astr) {
            return Stream.of(notEmptyApply(astr, str -> str + str.charAt(str.length() - 1)));

        }

        public Stream<String> removeLast(String astr) {
            return Stream.of(notEmptyApply(astr, str -> str.substring(0, str.length() - 1)));
        }

        public Function<String, Stream<String>> seq(Function<String, Stream<String>>... fns) {
            return str -> Stream.of(fns).flatMap(f -> f.apply(str));
        }

        public Function<String, Stream<String>> appendChar(char chr) {
            return str -> Stream.of(str + chr);
        }

        public Function<String, Stream<String>> appendAlternatives(List<Character> alternatives) {
            return str -> alternatives.stream().map(chr -> str + chr);
        }
    }

    public List<String> generate(String regEx, int numberOfResults) {
        Iterator<Character> iterator = regEx.chars()
                .mapToObj(i -> Character.valueOf((char) i))
                .iterator();

        Stream<String> result = Stream.of("");
        while (iterator.hasNext()) {
            Function<String, Stream<String>> nextOp
                    = generateNext(iterator, result);
            result = result.flatMap(nextOp);
        }

        return result.limit(numberOfResults).collect(Collectors.toList());
    }

    private Function<String, Stream<String>> generateNext(Iterator<Character> iterator, Stream<String> result) {
        char chr = iterator.next();
        switch (chr) {
            case '.':
                return generator::appendRandom;
            case '+':
                return generator::repeatLast;
            case '?':
                return generator.seq(generator::removeLast, generator::identity);
            case '*':
                return generator.seq(generator::removeLast, generator::identity, generator::repeatLast);
            case '[':
                return generator.appendAlternatives(this.readAlternation(iterator));
            case '\\':
                if (!iterator.hasNext()) {
                    return generator::identity;
                }
                chr = iterator.next();
                return generator.appendChar(chr);
            default:
                return generator.appendChar(chr);
        }
    }


    /*
     * Read seq char til a closing bracket,
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