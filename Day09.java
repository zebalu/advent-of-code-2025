void main() {
    var input = readInput();
    var lines = IntStream.range(0, input.size()).mapToObj(i -> Rectangle.fromCoords(input.get(i), input.get((i + 1) % input.size()))).toList();
    long part1 = Long.MIN_VALUE;
    long part2 = Long.MIN_VALUE;
    for (int i = 0; i < input.size() - 1; ++i) {
        for (int j = i + 1; j < input.size(); ++j) {
            Rectangle rect = Rectangle.fromCoords(input.get(i), input.get(j));
            long area = rect.area();
            if (area > part1) {
                part1 = area;
            }
            if (area > part2 && lines.stream().noneMatch(rect::isOverlap)) {
                part2 = area;
            }
        }
    }
    IO.println(part1);
    IO.println(part2);
}

record Coord(long x, long y) {
    static Coord fromStr(String str) {
        var parts = str.split(",");
        return new Coord(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }
}

record Rectangle(Coord min, Coord max) {
    boolean isOverlap(Rectangle other) {
        return min.x < other.max.x && max.x > other.min.x && min.y < other.max.y && max.y > other.min.y;
    }

    long area() {
        return (max.x - min.x + 1) * (max.y - min.y + 1);
    }

    static Rectangle fromCoords(Coord a, Coord b) {
        long minX = Math.min(a.x, b.x);
        long minY = Math.min(a.y, b.y);
        long maxX = Math.max(a.x, b.x);
        long maxY = Math.max(a.y, b.y);
        return new Rectangle(new Coord(minX, minY), new Coord(maxX, maxY));
    }
}

List<Coord> readInput() {
    try (var stream = Files.lines(Path.of("input_09.txt"), StandardCharsets.UTF_8)) {
        return stream.map(Coord::fromStr).toList();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}