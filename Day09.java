import java.awt.geom.Line2D;

void main() {
    var input = readInput();
    long max = -1L;
    for (int i = 0; i < input.size() - 1; ++i) {
        Coord a = input.get(i);
        for (int j = i + 1; j < input.size(); ++j) {
            Coord b = input.get(j);
            long area = a.area(b);
            if (area > max) {
                max = area;
            }
        }
    }
    IO.println(max);
    var lines = IntStream.range(0, input.size()).mapToObj(i -> new Line(input.get(i), input.get((i + 1) % input.size())).ordered()).toList();
    max = -1L;
    for (int i = 0; i < input.size() - 1; ++i) {
        Coord a = input.get(i);
        for (int j = i + 1; j < input.size(); ++j) {
            Coord b = input.get(j);
            Rectangel rect = Rectangel.fromCoords(a,b);
            Coord center = rect.center();
            Line verticalLine = new Line(new Coord(0, center.y), center);
            Line horizonalLine = new Line(new Coord(center.x, 0), center);
            long area = a.area(b);
            if(area > max && lines.stream().noneMatch(line-> rect.topLeft.x < line.end.x && rect.bottomRight.x > line.start.x && rect.topLeft.y < line.end.y && rect.bottomRight.y > line.start.y)) {
                max = area;
            }/*
            if (area > max && input.stream().noneMatch(rect::contains) && lines.stream().mapToInt(l->{
                if(l.isCrossing(verticalLine) || l.isCrossing(horizonalLine)) {
                    return 1;
                }
                return 0;
            }).sum()%2==0) {
                max = area;
            }*/
        }
    }
    IO.println(max);
}

record Coord(long x, long y) {
    long area(Coord other) {
        return (Math.abs(x - other.x) + 1) * (Math.abs(y - other.y) + 1);
    }

    static Coord fromStr(String str) {
        var parts = str.split(",");
        return new Coord(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }
}

record Line(Coord start, Coord end) {
    long length() {
        return Math.abs(end.x - start.x) + Math.abs(end.y - start.y) + 1;
    }
    boolean isCrossing(Line line) {
        long minx1 = Math.min(start.x, end.x);
        long minx2 = Math.min(line.start.x, line.end.x);
        long miny1 = Math.min(start.y, end.y);
        long miny2 = Math.min(line.start.y, line.end.y);

        long maxx1 = Math.max(start.x, end.x);
        long maxx2 = Math.max(line.start.x, line.end.x);
        long maxy1 = Math.max(start.y, end.y);
        long maxy2 = Math.max(line.start.y, line.end.y);
        /*
        if(isVertical() && line.isVertical()) {
            return minx1 == minx2 && miny1 <= miny2 && maxy1<=maxy2;
        } else if(isVertical() && !line.isVertical()) {
            return minx1 <= minx2 && maxx2 <= maxx1 && miny1 <= miny2;
        } else if(!isVertical() && line.isVertical()) {
            return minx2 <= minx1 && maxx1 <= maxx2 && miny2 <= miny1 && maxy1 <= maxy2;
        }
*/
        //return Line2D.linesIntersect(minx1, miny1,  maxx1, maxy1, minx2, miny2, maxx2, maxy2);
        boolean xOverlap = (minx1 < maxx2) && (maxx1 > minx2);
        boolean yOverlap = (miny1 < maxy2) && (maxy1 >=miny2);
        return xOverlap && yOverlap;
    }
    Line ordered() {
        return new Line(new Coord(Math.min(start.x, end.x), Math.min(start.y, end.y)), new Coord(Math.max(start.x, end.x), Math.max(start.y, end.y)));
    }
    boolean isVertical() {
        return start.y == end.y;
    }
}

record Rectangel(Coord topLeft, Coord bottomRight) {
    boolean contains(Coord c) {
        return topLeft.x < c.x && c.x < bottomRight.x && topLeft.y < c.y && c.y < bottomRight.y;
    }
    Coord center()  {
        return new Coord(topLeft.x+((bottomRight.x-topLeft.x)/2), topLeft.y+((bottomRight.y-topLeft.y)/2));
    }
    static Rectangel fromCoords(Coord a, Coord b) {
        long minX = Math.min(a.x, b.x);
        long minY = Math.min(a.y, b.y);
        long maxX = Math.max(a.x, b.x);
        long maxY = Math.max(a.y, b.y);
        return new Rectangel(new  Coord(minX, minY), new Coord(maxX, maxY));
    }
}

List<Coord> readInput() {
    try (var stream = Files.lines(Path.of("input_09.txt"), StandardCharsets.UTF_8)) {
        return stream.map(Coord::fromStr).toList();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}