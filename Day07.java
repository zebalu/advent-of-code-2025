void main() {
    Map<Coord, Long> beamWays = creatPathMap(readInput());
    IO.println(part1(beamWays));
    IO.println(part2(beamWays));
}

long part1(Map<Coord, Long> beamWays) {
    return beamWays.values().stream().filter(v -> v == -1L).count();
}

long part2(Map<Coord, Long> beamWays) {
    int lastRow = beamWays.keySet().stream().mapToInt(Coord::y).max().orElseThrow();
    return beamWays.entrySet().stream().filter(e -> e.getKey().y == lastRow).mapToLong(Map.Entry::getValue).sum();
}

Map<Coord, Long> creatPathMap(List<String> input) {
    int startPos = input.getFirst().indexOf("S");
    Map<Coord, Long> beamWays = new HashMap<>();
    SequencedSet<Integer> beams = new LinkedHashSet<>(List.of(startPos));
    beamWays.put(new Coord(startPos, 0), 1L);
    for (int row = 1; row < input.size(); ++row) {
        String level = input.get(row);
        SequencedSet<Integer> newBeams = new LinkedHashSet<>();
        for (int beam : beams) {
            Coord beamPos = new Coord(beam, row);
            Coord beamPrev = new Coord(beam, row - 1);
            if (level.charAt(beam) == '.') {
                newBeams.add(beam);
                long prev = beamWays.get(beamPrev);
                update(beamWays, beamPos, prev);
            } else if (level.charAt(beam) == '^') {
                int left = beam - 1;
                int right = beam + 1;
                long ways = beamWays.get(beamPrev);
                beamWays.put(beamPos, -1L);
                if (left >= 0) {
                    newBeams.add(left);
                    update(beamWays, new Coord(left, row), ways);
                }
                if (right < level.length()) {
                    newBeams.add(right);
                    update(beamWays, new Coord(right, row), ways);
                }
            }
        }
        beams = newBeams;
    }
    return beamWays;
}

void update(Map<Coord, Long> beamWays, Coord pos, long value) {
    beamWays.compute(pos, (k, v) -> {
        if (v == null) {
            return value;
        } else {
            return v + value;
        }
    });
}

List<String> readInput() {
    try {
        return Files.readAllLines(Path.of("input_07.txt"), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

record Coord(int x, int y) { }
