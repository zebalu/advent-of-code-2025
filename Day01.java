void main() {
    List<String> turns = readInput();
    IO.println(part1(turns));
    IO.println(part2(turns));
}

int part1(List<String> turns) {
    return count0s(turns, tr -> tr.pos() == 0 ? 1 : 0);
}

int part2(List<String> turns) {
   return count0s(turns, TurnResult::click0);
}

int count0s(List<String> turns, Function<TurnResult, Integer> f) {
    TurnResult tr = new TurnResult(50, 0);
    int count0 = 0;
    for (String task : turns) {
        tr = turn(tr.pos(), task);
        count0 += f.apply(tr);
    }
    return count0;
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
