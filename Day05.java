void main() {
    var input = readInput();
    int cut = input.indexOf("");
    List<Range> ranges = input.subList(0, cut).stream().map(Range::fromString).toList();
    List<Long> ids = input.subList(cut + 1, input.size()).stream().map(Long::parseLong).toList();
    IO.println(part1(ids, ranges));
    IO.println(part2(ranges));
}

long part1(List<Long> ids, List<Range> ranges) {
    return ids.stream().filter(id -> ranges.stream().anyMatch(r -> r.contains(id))).count();
}

long part2(List<Range> ranges) {
    Set<Range> cleaned = distinct(ranges);
    return cleaned.stream().mapToLong(Range::size).sum();
}

Set<Range> distinct(List<Range> ranges) {
    Set<Range> toClean = new HashSet<>(ranges);
    boolean changed;
    do {
        changed = false;
        Iterator<Range> it1 = toClean.iterator();
        while (!changed && it1.hasNext()) {
            Range range = it1.next();
            Iterator<Range> it2 = toClean.iterator();
            while (!changed && it2.hasNext()) {
                Range toCheck = it2.next();
                if (range != toCheck && range.hasIntersection(toCheck)) {
                    changed = true;
                    toClean.remove(toCheck);
                    toClean.remove(range);
                    toClean.add(range.merge(toCheck));
                }
            }
        }
    } while (changed);
    return toClean;
}

record Range(long from, long to) {
    long size() {
        return to - from + 1;
    }

    boolean hasIntersection(Range other) {
        if (from <= other.from) {
            return other.from <= to;
        } else {
            return from <= other.to;
        }
    }

    Range merge(Range other) {
        if (hasIntersection(other)) {
            return new Range(Math.min(from, other.from), Math.max(to, other.to));
        } else {
            throw new IllegalArgumentException("Could not merge ranges without intersection: "+this+" + "+other);
        }
    }

    boolean contains(long value) {
        return from <= value && value <= to;
    }

    static Range fromString(String desc) {
        var parts = desc.split("-");
        return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
    }
}

List<String> readInput() {
    try {
        return Files.readAllLines(Path.of("input_05.txt"), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
