void main() {
    var input = readInput();
    PriorityQueue<JBoxPair> queue = new PriorityQueue<>();
    for (int i = 0; i < input.size() - 1; ++i) {
        for (int j = i + 1; j < input.size(); ++j) {
            queue.add(new JBoxPair(input.get(i), input.get(j)));
        }
    }
    Map<Coord, Set<Coord>> circuits = new HashMap<>();
    for (Coord c : input) {
        circuits.put(c, new HashSet<>(Set.of(c)));
    }
    IO.println(part1(queue, circuits));
    IO.println(part2(queue, circuits));
}

private static int part1(PriorityQueue<JBoxPair> queue, Map<Coord, Set<Coord>> circuits) {
    for (int i = 0; i < 1000 && !queue.isEmpty(); ++i) {
        JBoxPair pair = queue.poll();
        connect(circuits, pair);
    }
    return circuits.values().stream().distinct().sorted(Comparator.<Set<?>>comparingInt(Set::size).reversed()).limit(3).mapToInt(Set::size).reduce(1, (a, b) -> a * b);
}

private static int part2(PriorityQueue<JBoxPair> queue, Map<Coord, Set<Coord>> circuits) {
    JBoxPair last = null;
    boolean allConnected = false;
    while (!allConnected && !queue.isEmpty()) {
        last = queue.poll();
        Set<Coord> circuit = connect(circuits, last);
        allConnected = circuit.size() == circuits.size();
    }
    if (last == null) {
        throw new IllegalArgumentException("There are no circuit connected");
    }
    return last.a.x * last.b.x;
}

private static Set<Coord> connect(Map<Coord, Set<Coord>> circuits, JBoxPair pair) {
    Set<Coord> circuitA = circuits.get(pair.a);
    Set<Coord> circuitB = circuits.get(pair.b);
    if (!circuitA.contains(pair.b)) {
        circuitA.addAll(circuitB);
        circuitA.forEach(a -> circuits.put(a, circuitA));
    }
    return circuitA;
}

record Coord(int x, int y, int z) {
    double distance(Coord other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2) + Math.pow(z - other.z, 2));
    }

    static Coord fromString(String s) {
        var parts = s.split(",");
        return new Coord(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}

record JBoxPair(Coord a, Coord b, double distance) implements Comparable<JBoxPair> {
    JBoxPair(Coord a, Coord b) {
        this(a, b, a.distance(b));
    }
    @Override
    public int compareTo(JBoxPair o) {
        return Comparator.comparingDouble(JBoxPair::distance).compare(this, o);
    }
}

List<Coord> readInput() {
    try (var lines = Files.lines(Path.of("input_08.txt"), StandardCharsets.UTF_8)) {
        return lines.map(Coord::fromString).toList();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
