void main() {
    var jolts = readInput();
    IO.println(jolts.stream().mapToLong(jolt -> findLargestJoltage(jolt, 2)).sum());
    IO.println(jolts.stream().mapToLong(jolt -> findLargestJoltage(jolt, 12)).sum());
}

record MaxIndex(int max, int index) { }

long findLargestJoltage(List<Integer> jolt, int digits) {
    int from = 0;
    long maxJoltage = 0L;
    for (int i = 0; i < digits; ++i) {
        var mi = findMaxWitIndex(jolt, from, jolt.size() - (digits - i - 1));
        from = mi.index() + 1;
        maxJoltage = maxJoltage * 10 + mi.max();
    }
    return maxJoltage;
}

MaxIndex findMaxWitIndex(List<Integer> list, int from, int to) {
    return IntStream.range(from, to).mapToObj(i -> new MaxIndex(list.get(i), i)).max(Comparator.comparingInt(MaxIndex::max)).orElseThrow();
}

List<List<Integer>> readInput() {
    try (Stream<String> lines = Files.lines(Path.of("input_03.txt"))) {
        return lines.map(s -> s.chars().map(Character::getNumericValue).boxed().collect(Collectors.toList())).toList();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}