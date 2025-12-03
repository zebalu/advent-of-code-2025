void main() {
    var jolts = readInput();
    var part1 = jolts.stream().mapToLong(jolt -> findLargestJoltage(jolt, 2)).sum();
    IO.println(part1);
    var part2 = jolts.stream().mapToLong(jolt -> findLargestJoltage(jolt, 12)).sum();
    IO.println(part2);
}

record MaxIndex(int max, int index) { }

long findLargestJoltage(List<Integer> jolt, int digits) {
    int prevStart = 0;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < digits; ++i) {
        var mi = findMaxWitIndex(jolt, prevStart, jolt.size() - (digits - i - 1));
        prevStart = mi.index() + 1;
        sb.append(mi.max());
    }
    return Long.parseLong(sb.toString());
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