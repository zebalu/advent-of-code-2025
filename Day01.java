void main() {
    List<TurnResult> turnResults = readInput().stream().gather(Gatherers.scan(()->new TurnResult(50, 0), (tr, task) -> turn(tr.pos(), task))).toList();
    IO.println(part1(turnResults));
    IO.println(part2(turnResults));
}

int part1(List<TurnResult> turns) {
    return (int)turns.stream().filter(tr->tr.pos()==0).count();
}

int part2(List<TurnResult> turns) {
   return turns.stream().mapToInt(TurnResult::click0).sum();
}

record TurnResult(int pos, int click0) { }

TurnResult turn(int from, String task) {
    boolean isLeft = task.startsWith("L");
    int steps = Integer.parseInt(task.substring(1));
    int sum = from + (isLeft ? -steps : steps);
    int count0 = Math.abs(Math.floorDiv(sum, 100));
    int at = Math.floorMod(sum, 100);
    if (isLeft) {
        if (at == 0) {
            ++count0;
        }
        if (from == 0) {
            --count0;
        }
    }
    return new TurnResult(at, count0);
}

List<String> readInput() {
    try {
        return Files.readAllLines(Paths.get("input_01.txt"));
    } catch (IOException e) {
        throw new IllegalStateException(e);
    }
}
