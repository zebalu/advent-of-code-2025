void main() {
    var input = readInput();
    List<Shape> shapes = new ArrayList<>();
    List<Area> areas = new ArrayList<>();
    boolean isInShape = false;
    Set<Coord> currentShape = null;
    int y = 0;
    for (String line : input) {
        if (line.matches("^\\d+x\\d+: .*")) {
            isInShape = false;
        } else if (line.matches("^\\d+:$")) {
            isInShape = true;
        } else {
            isInShape = true;
        }
        if (isInShape) {
            if (line.matches("\\d+:")) {
                if (currentShape != null) {
                    shapes.add(new Shape(currentShape));
                    currentShape = null;
                    y = 0;
                }
            } else {
                if (currentShape == null) {
                    currentShape = new LinkedHashSet<>();
                }
                for (int x = 0; x < line.length(); ++x) {
                    if (line.charAt(x) == '#') {
                        currentShape.add(new Coord(x, y));
                    }
                }
                ++y;
            }
        } else {
            if (currentShape != null) {
                shapes.add(new Shape(currentShape));
                currentShape = null;
            }
            areas.add(Area.fromLine(line));
        }
    }
    var vars = shapes.stream().map(ShapeVariations::fromShape).toList();
    IO.println(areas.stream().parallel()
            .filter(a -> a.testFit(vars)).count());
}

record Coord(int x, int y) {
    Coord turnRight() {
        return new Coord(2 - y, x);
    }

    Coord flipHorizontal() {
        return new Coord(2 - x, y);
    }
}

record Shape(Set<Coord> coords) {
    int occupiedSpace() {
        return coords.size();
    }

    Shape turnRight() {
        return new Shape(coords.stream().map(Coord::turnRight).collect(Collectors.toSet()));
    }

    Shape flipHorizontal() {
        return new Shape(coords.stream().map(Coord::flipHorizontal).collect(Collectors.toSet()));
    }

    List<Coord> getTranslated(int dx, int dy) {
        return coords.stream().map(c -> new Coord(c.x + dx, c.y + dy)).toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 3; ++x) {
                Coord c = new Coord(x, y);
                if (coords.contains(c)) {
                    sb.append("#");
                } else {
                    sb.append(".");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

record ShapeVariations(SequencedSet<Shape> shapes) {
    int occupiedSpace() {
        return shapes.getFirst().occupiedSpace();
    }

    static ShapeVariations fromShape(Shape shape) {
        Shape current = shape;
        SequencedSet<Shape> shapes = new LinkedHashSet<>();
        shapes.add(current);
        for (int i = 0; i < 4; ++i) {
            current = current.turnRight();
            shapes.add(current);
            current = current.flipHorizontal();
            shapes.add(current);
        }
        return new ShapeVariations(shapes);
    }
}

record Area(int width, int length, List<Integer> presents) {
    static Area fromLine(String line) {
        String[] defParts = line.split(": ");
        String[] dims = defParts[0].split("x");
        String[] presents = defParts[1].split(" ");
        return new Area(Integer.parseInt(dims[0]), Integer.parseInt(dims[1]), Arrays.stream(presents).mapToInt(Integer::parseInt).boxed().toList());
    }

    int availableSpace() {
        return width * length;
    }

    boolean testFit(List<ShapeVariations> shapeVariations) {
        int minRequired = IntStream.range(0, presents.size()).map(i -> presents.get(i) * shapeVariations.get(i).occupiedSpace()).sum();
        if (availableSpace() < minRequired) {
            return false;
        }
        int gridCount = (width / 3) * (length / 3);
        int presentCount = presents.stream().mapToInt(Integer::intValue).sum();
        if (presentCount <= gridCount) {
            return true;
        }
        Deque<ShapeVariations> presentQueue = new ArrayDeque<>();
        IntStream.range(0, presents.size()).mapToObj(s -> IntStream.range(0, presents.get(s)).mapToObj(ss -> shapeVariations.get(s))).flatMap(s -> s).forEach(presentQueue::offer);
        Set<Coord> occupied = new HashSet<>();
        return testFit(occupied, presentQueue);
    }

    boolean testFit(Set<Coord> occupied, Deque<ShapeVariations> presentQueue) {
        if (presentQueue.isEmpty()) {
            return true;
        } else if (occupied.size() == availableSpace()) {
            return false;
        }
        ShapeVariations shapeVariations = presentQueue.pollFirst();
        for (Shape shape : shapeVariations.shapes) {
            for (int y = 0; y < length - 2; ++y) {
                for (int x = 0; x < width - 2; ++x) {
                    List<Coord> translated = shape.getTranslated(x, y);
                    if (translated.stream().noneMatch(occupied::contains)) {
                        occupied.addAll(translated);
                        if (testFit(occupied, presentQueue)) {
                            return true;
                        } else {
                            occupied.removeAll(translated);
                        }
                    }
                }
            }
        }
        presentQueue.addFirst(shapeVariations);
        return false;
    }
}

List<String> readInput() {
    try {
        return Files.readAllLines(Path.of("input_12.txt"), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}