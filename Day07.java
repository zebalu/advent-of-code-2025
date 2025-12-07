void main() {
    var input = readInput();
    int startPos = input.getFirst().indexOf("S");
    int countSpliterHit = part1(startPos, input);
    IO.println(countSpliterHit);
    Map<Coord, Long> beamWays = new HashMap<>();
    List<Integer> beams = new ArrayList<>(List.of(startPos));
    beamWays.put(new Coord(startPos, 0), 1L);
    for(int row = 1; row < input.size(); ++row) {
        String level =  input.get(row);
        SequencedSet<Integer> newBeams = new LinkedHashSet<>();
        for(int beam: beams) {
            Coord beamPos = new Coord(beam, row);
            Coord beamPrev = new Coord(beam, row-1);
            if(level.charAt(beam) == '.') {
                newBeams.add(beam);
                long prev = beamWays.get(beamPrev);
                beamWays.compute(beamPos, (k,v)->{
                    if(v==null) {
                        return prev;
                    } else {
                        return v+prev;
                    }
                });
            } else if(level.charAt(beam) == '^') {
                int left = beam-1;
                int right = beam+1;
                long ways = beamWays.get(beamPrev);
                if(left >=0) {
                    newBeams.add(left);
                    beamWays.compute(new Coord(left, row), (k,v)->{
                        if(v==null) {
                            return ways;
                        } else {
                            return v+ways;
                        }
                    });
                }
                if(right < level.length()) {
                    newBeams.add(right);
                    beamWays.compute(new Coord(right, row), (k,v)->{
                        if(v==null) {
                            return ways;
                        } else {
                            return v+ways;
                        }
                    });
                }
                ++countSpliterHit;
            }
        }
        beams = new ArrayList<>(newBeams);
    }
    long[] l = beamWays.entrySet().stream().filter(e->e.getKey().y==input.size()-1).sorted(Comparator.comparing(Map.Entry::getKey)).mapToLong(e->e.getValue()).toArray();
    long ways = beamWays.entrySet().stream().filter(e->e.getKey().y==input.size()-1).mapToLong(Map.Entry::getValue).sum();
    IO.println(ways);
    IO.println(Arrays.toString(l));
}

private static int part1(int startPos, List<String> input) {
    List<Integer> beams = new ArrayList<>(List.of(startPos));
    int countSpliterHit = 0;
    for(int row = 1; row < input.size(); ++row) {
        String level =  input.get(row);
        SequencedSet<Integer> newBeams = new LinkedHashSet<>();
        for(int beam: beams) {
            if(level.charAt(beam) == '.') {
                newBeams.add(beam);
            } else if(level.charAt(beam) == '^') {
                int left = beam-1;
                int right = beam+1;
                if(left >=0) {
                    newBeams.add(left);
                }
                if(right < level.length()) {
                    newBeams.add(right);
                }
                ++countSpliterHit;
            }
        }
        beams = new ArrayList<>(newBeams);
    }
    return countSpliterHit;
}

List<String> readInput() {
    try {
        return Files.readAllLines(Path.of("input_07.txt"), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}

record Coord(int x, int y) implements Comparable<Coord> {
    @Override
    public int compareTo(Coord o) {
        return Comparator.comparingInt(Coord::x).thenComparingInt(Coord::y).compare(this, o);
    }
}