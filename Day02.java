void main() {
    var ranges = Arrays.stream(readInput().getFirst().split(",")).map(Range::fromString).toList();
    var part1 = ranges.stream().flatMap(r -> r.collectBy(this::isPalindrom).stream()).mapToLong(Long::longValue).sum();
    IO.println(part1);
    var part2 = ranges.stream().flatMap(r -> r.collectBy(this::isInvalid).stream()).mapToLong(Long::longValue).sum();
    IO.println(part2);
}

record Range(long start, long end) {
    List<Long> collectBy(Predicate<Long> predicate) {
        List<Long> result = new ArrayList<>();
        for (long L = start; L <= end; L++) {
            if (predicate.test(L)) {
                result.add(L);
            }
        }
        return result;
    }

    static Range fromString(String rng) {
        String[] split = rng.split("-");
        return new Range(Long.parseLong(split[0]), Long.parseLong(split[1]));
    }
}

boolean isPalindrom(long num) {
    String str = Long.toString(num);
    return str.length() > 1 && isRepetitionsOnly(str.substring(0, str.length() / 2), str);
}

boolean isInvalid(long num) {
    String str = Long.toString(num);
    for (int i = 1; i <= str.length() / 2; ++i) {
        if (str.length() % i == 0 && isRepetitionsOnly(str.substring(0, i), str)) {
            return true;
        }
    }
    return false;
}

boolean isRepetitionsOnly(String prefix, String whole) {
    String current = whole;
    while (!current.isEmpty()) {
        if (!current.startsWith(prefix)) {
            return false;
        }
        current = current.substring(prefix.length());
    }
    return true;
}

List<String> readInput() {
    try {
        return Files.readAllLines(Path.of("input_02.txt"), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}