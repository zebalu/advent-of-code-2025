void main() {
    List<String> turns = readInput();
    IO.println(part1(turns));
    IO.println(part2(turns));
}

int part1(List<String> turns) {
    TurnResult tr = new TurnResult(50, 0);
    int count0 = 0;
    for (String task : turns) {
        tr = turn2(tr.pos(), task);
        if(tr.pos()==0) {
            ++count0;
        }
    }
    return count0;
}

int part2(List<String> turns) {
    TurnResult tr = new TurnResult(50, 0);
    int count0 = 0;
    for (String task : turns) {
        tr = turn2(tr.pos(), task);
        count0 += tr.click0();
    }
    return count0;
}

record TurnResult(int pos, int click0) {
}

TurnResult turn2(int from, String task) {
    boolean isLeft = task.startsWith("L");
    int steps = Integer.parseInt(task.substring(1, task.length()));
    int at = from;
    int count0 = 0;
    if (isLeft) {
        if (at == 0) {
            at = 100;
        }
        while (at < steps) {
            at += 100;
            count0++;
        }
        at = (at - steps) % 100;
        if (at == 0) {
            ++count0;
        }
    } else {
        while (at + steps >= 100) {
            steps -= 100;
            count0++;
        }
        at += steps;
        if (at < 0) {
            at += 100;
        }
        at = at % 100;
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