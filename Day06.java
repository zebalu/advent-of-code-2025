void main() {
    var input = readInput();
    IO.println(transpose(input).stream().mapToLong(this::compute1).sum());
    IO.println(transposeByOperators(input).stream().mapToLong(this::compute2).sum());
}

long compute1(List<String> column) {
    return execute(column.stream().limit(column.size() - 1).mapToLong(Long::parseLong), column.getLast());
}

long compute2(List<String> column) {
    return execute(readNumbersByColum(column.subList(0, column.size() - 1)).stream().mapToLong(Long::longValue), column.getLast().trim());
}

long execute(LongStream nums, String method) {
    return switch (method) {
        case "*" -> nums.reduce(1L, (a, b) -> a * b);
        case "+" -> nums.sum();
        default -> throw new IllegalStateException("Unknown method: " + method);
    };
}

private List<Integer> findColumStarts(String operators) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < operators.length(); ++i) {
        if (operators.charAt(i) != ' ') {
            result.add(i);
        }
    }
    return result;
}

List<Long> readNumbersByColum(List<String> column) {
    int numberCount = column.getFirst().length();
    List<Long> result = new ArrayList<>(numberCount);
    for (int i = 0; i < numberCount; ++i) {
        result.add(0L);
    }
    for (int i = 0; i < numberCount; ++i) {
        for (String s : column) {
            if (s.charAt(i) != ' ') {
                result.set(i, result.get(i) * 10L + Long.parseLong("" + s.charAt(i)));
            }
        }
    }
    return result.reversed(); // just to match how example
}

List<List<String>> transpose(List<String> input) {
    List<List<String>> result = new ArrayList<>();
    for (var line : input) {
        var parts = line.split("\\s+");
        if (result.isEmpty()) {
            for (int i = 0; i < parts.length; ++i) {
                result.add(new ArrayList<>());
            }
        }
        for (int j = 0; j < parts.length; ++j) {
            result.get(j).add(parts[j]);
        }
    }
    return result;
}

List<List<String>> transposeByOperators(List<String> input) {
    var columStarts = findColumStarts(input.getLast());
    List<List<String>> result = new ArrayList<>();
    for (int i = 0; i < columStarts.size(); ++i) {
        result.add(new ArrayList<>());
    }
    for (var line : input) {
        for (int j = 0; j < columStarts.size(); ++j) {
            if (j < columStarts.size() - 1) {
                result.get(j).add(line.substring(columStarts.get(j), columStarts.get(j + 1) - 1));
            } else {
                result.get(j).add(line.substring(columStarts.get(j)));
            }
        }
    }
    return result;
}

List<String> readInput() {
    try {
        return Files.readAllLines(Path.of("input_06.txt"), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}