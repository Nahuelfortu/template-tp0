package ar.fiuba.tdd.template.tp0;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RegExGenerator {
    private Generator generator;

    public RegExGenerator(Generator generator) {
        this.generator = generator;
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