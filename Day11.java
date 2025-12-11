void main() {
    Map<String, Set<String>> connections = readConnectionMap();
    part1(connections);
    part2(connections);
}

void part1(Map<String, Set<String>> connections) {
    IO.println(countPaths(connections, new HashMap<>(), "you", "out"));
}

void part2(Map<String, Set<String>> connections) {
    long fftToDac = countPaths(connections, new HashMap<>(), "fft", "dac");
    if (fftToDac > 0L) {
        long svrToFft = countPaths(connections, new HashMap<>(), "svr", "fft");
        long dacToOut = countPaths(connections, new HashMap<>(), "dac", "out");
        IO.println(svrToFft * fftToDac * dacToOut);
    } else {
        long svrToDac = countPaths(connections, new HashMap<>(), "svr", "dac");
        long dacToFft = countPaths(connections, new HashMap<>(), "dac", "fft");
        long fftToOut = countPaths(connections, new HashMap<>(), "fft", "out");
        IO.println(svrToDac * dacToFft * fftToOut);
    }
}

Map<String, Set<String>> readConnectionMap() {
    var input = readInput();
    Map<String, Set<String>> connections = new HashMap<>();
    input.forEach(line -> {
        String[] kv = line.split(": ");
        Set<String> v = new HashSet<>(Arrays.asList(kv[1].split(" ")));
        connections.put(kv[0], v);
    });
    return connections;
}

long countPaths(Map<String, Set<String>> connections, Map<String, Long> memo, String start, String end) {
    if (memo.containsKey(start)) {
        return memo.get(start);
    }
    if (start.equals(end)) {
        return 1L;
    }
    long sum = Optional.ofNullable(connections.get(start)).map(s -> s.stream().mapToLong(c -> countPaths(connections, memo, c, end)).sum()).orElse(0L);
    memo.put(start, sum);
    return sum;
}

List<String> readInput() {
    try (var lines = Files.lines(Path.of("input_11.txt"), StandardCharsets.UTF_8)) {
        return lines.toList();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
