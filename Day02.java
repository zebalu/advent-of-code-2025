void main() {
    var inputLines = readInput();
    var ranges = Arrays.stream(inputLines.getFirst().split(",")).map(Range::fromString).toList();
    var sum = ranges.stream().map(this::palindromsInRange).flatMap(List::stream).mapToLong(Long::longValue).sum();
    IO.println(sum);
    var sum2 = ranges.stream().map(this::invalidsInRange).flatMap(List::stream).mapToLong(Long::longValue).sum();
    IO.println(sum2);
}

record Range(long start, long end) {
    static Range fromString(String rng) {
        String[] split = rng.split("-");
        return new Range(Long.parseLong(split[0]), Long.parseLong(split[1]));
    }
    long size() {
        return end - start+1;
    }
}

boolean isPalindrom(long num) {
    String str = Long.toString(num);
    if(str.length()%2!=0) {
        return false;
    } else {
        return str.substring(0, str.length()/2).equals(str.substring(str.length()/2));
    }
}

boolean isInvalid(long num) {
    String str = Long.toString(num);
    for(int i=1; i<=str.length()/2; ++i) {
        if(str.length()%i==0) {
            if(isRepetitionsOnly(str.substring(0,i), str)){
                return true;
            }
        }
    }
    return false;
}

boolean isRepetitionsOnly(String prefix, String whole) {
    String current = whole;
    while(!current.isEmpty()) {
        if(!current.startsWith(prefix)) {
            return false;
        }
        current = current.substring(prefix.length());
    }
    return true;
}

List<Long> palindromsInRange(Range range) {
    List<Long> result = new ArrayList<>();
    for(long L=range.start(); L<=range.end(); ++L) {
        if(isPalindrom(L)) {
            result.add(L);
        }
    }
    return result;
}

List<Long> invalidsInRange(Range range) {
    List<Long> result = new ArrayList<>();
    for(long L=range.start(); L<=range.end(); ++L) {
        if(isInvalid(L)) {
            result.add(L);
        }
    }
    return result;
}

List<String> readInput() {
    try {
        return Files.readAllLines(Path.of("input_02.txt"), StandardCharsets.UTF_8);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}