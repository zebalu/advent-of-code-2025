void main() {
    Instant start = Instant.now();
    var jolts = readInput();
    IO.println(jolts.stream().mapToLong(jolt -> findLargestJoltage(jolt, 2)).sum());
    IO.println(jolts.stream().mapToLong(jolt -> findLargestJoltage(jolt, 12)).sum());
    System.out.println(Duration.between(start, Instant.now()).toMillis());
}

long findLargestJoltage(List<Integer> jolt, int digits) {
    int from = -1;
    long maxJoltage = 0L;
    for (int i = 0; i < digits; ++i) {
        from = IntStream.range(from+1, jolt.size() - (digits - i - 1)).boxed().max(Comparator.comparingInt(jolt::get)).orElseThrow();
        maxJoltage = maxJoltage * 10 + jolt.get(from);
    }
    return maxJoltage;
}

List<List<Integer>> readInput() {
    try (Stream<String> lines = Files.lines(Path.of("input_03.txt"))) {
        return lines.map(s -> s.chars().map(Character::getNumericValue).boxed().collect(Collectors.toList())).toList();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}