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
    int at = from;
    int count0 = steps / 100;
    steps = steps % 100;
    if (isLeft) {
        if(at==0) {
            at = 100;
        }
        if(at <= steps) {
            at += (100-steps);
            at %= 100;
            ++count0;
        } else {
            at -= steps;
        }
    } else {
        if(at+steps>=100) {
            ++count0;
            steps -= 100;
        }
        at += steps;
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
