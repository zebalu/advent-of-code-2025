void main() {
    var input = readInput();
    IO.println(part1(input));
    IO.println(part2(input));
}

private int part1(List<char[]> input) {
    return getMovables(input).size();
}

private int part2(List<char[]> input) {
    List<Coord> movables;
    int moved = 0;
    do {
        movables = getMovables(input);
        moved += movables.size();
        movables.forEach(c -> setAt(input, c, 'X'));
    } while (!movables.isEmpty());
    return moved;
}

List<Coord> getMovables(List<char[]> input) {
    List<Coord> movables = new ArrayList<>();
    for (int y = 0; y < input.size(); ++y) {
        for (int x = 0; x < input.get(y).length; ++x) {
            Coord coord = new Coord(x, y);
            if (isMoveable(input, coord)) {
                movables.add(coord);
            }
        }
    }
    return movables;
}

boolean isMoveable(List<char[]> matrix, Coord coord) {
    return coord.isValid(matrix) && '@' == getAt(matrix, coord) && countNeighbors(matrix, coord) < 4;
}

int countNeighbors(List<char[]> matrix, Coord coord) {
    if (coord.isValid(matrix)) {
        return coord.neighbours().stream().filter(c -> c.isValid(matrix)).mapToInt(c -> getAt(matrix, c) == '@' ? 1 : 0).sum();
    } else {
        return 0;
    }
}

char getAt(List<char[]> matrix, Coord coord) {
    return matrix.get(coord.y)[coord.x];
}

void setAt(List<char[]> matrix, Coord coord, char c) {
    matrix.get(coord.y)[coord.x] = c;
}

record Coord(int x, int y) {
    boolean isValid(List<char[]> matrix) {
        return 0 <= x && 0 <= y && y < matrix.size() && x < matrix.get(y).length;
    }

    List<Coord> neighbours() {
        return List.of(
                new Coord(x - 1, y - 1), new Coord(x, y - 1), new Coord(x + 1, y - 1),
                new Coord(x - 1, y),                                 new Coord(x + 1, y),
                new Coord(x - 1, y + 1), new Coord(x, y + 1), new Coord(x + 1, y + 1)
        );
    }
}

List<char[]> readInput() {
    try {
        return Files.readAllLines(Path.of("input_04.txt"), StandardCharsets.UTF_8).stream().map(String::toCharArray).toList();
    } catch (IOException ioe) {
        throw new RuntimeException(ioe);
    }
}