void main() {
    var input = readInput();
    IO.println(input.stream().mapToInt(Machine::countFewestPresses).sum());
    IO.println(input.stream().parallel().mapToInt(Machine::countFewestJoltagePresses).sum());
}

record Machine(List<Boolean> lights, List<SequencedSet<Integer>> buttons, List<Integer> joltage) {
    int countFewestPresses() {
        record MachineCount(List<Boolean> lights, int count) {
        }
        Set<List<Boolean>> seen = new HashSet<>();
        Queue<MachineCount> queue = new ArrayDeque<>();
        List<Boolean> initial = new ArrayList<>();
        for (int i = 0; i < lights.size(); i++) {
            initial.add(false);
        }
        seen.add(initial);
        queue.add(new MachineCount(initial, 0));
        while (!queue.isEmpty()) {
            MachineCount next = queue.poll();
            if (next.lights.equals(lights)) {
                return next.count;
            }
            for (SequencedSet<Integer> button : buttons) {
                List<Boolean> switched = new ArrayList<>(next.lights);
                for (int b : button) {
                    switched.set(b, !switched.get(b));
                }
                if (switched.equals(lights)) {
                    return next.count + 1;
                } else if (seen.add(switched)) {
                    queue.add(new MachineCount(switched, next.count + 1));
                }
            }
        }
        throw new IllegalStateException("Could not switch all lights on!");
    }
    int countFewestJoltagePresses() {
        IntSolver solver = new IntSolver();
        solver.findSOlution(joltage, 0, new HashSet<>());
        if(solver.bestFound == Integer.MAX_VALUE) {
            throw new IllegalStateException("Cheapest joltage not found");
        }
        return solver.bestFound;
    }
    class IntSolver {
        int bestFound = Integer.MAX_VALUE;
        void findSOlution(List<Integer> target, int steps, Set<SequencedSet<Integer>> pressedButtons) {
            int min = target.stream().min(Comparator.naturalOrder()).orElseThrow();
            if (min < 0) {
                return;
            }
            int max = target.stream().max(Comparator.naturalOrder()).orElseThrow();
            if (steps + max >= bestFound) {
                return;
            }
            if (max == 0) {
                if (steps < bestFound) {
                    bestFound = steps;
                }
                return;
            }
            Set<SequencedSet<Integer>> validContinuations = new HashSet<>(buttons);
            validContinuations.removeAll(pressedButtons);
            for (int i = 0; i < target.size(); ++i) {
                int indexOfSmall = i;
                for (int j = 0; j < target.size(); ++j) {
                    int indexOfBig = j;
                    if (target.get(indexOfSmall) < target.get(indexOfBig)) {
                        var validButtons = validContinuations.stream().filter(b -> b.contains(indexOfBig) && !b.contains(indexOfSmall)).toList();
                        if (validButtons.isEmpty()) {
                            return;
                        } else if (validButtons.size() == 1) {
                            SequencedSet<Integer> button = validButtons.getFirst();
                            List<Integer> newTarget = new ArrayList<>(target);
                            for (Integer idx : button) {
                                newTarget.set(idx, newTarget.get(idx) - 1);
                            }
                            findSOlution(newTarget, steps + 1, pressedButtons);
                            return;
                        }
                    }
                }
            }
            int minNon0Index = IntStream.range(0, target.size()).filter(i -> target.get(i) != 0).boxed().min(Comparator.comparingInt(target::get)).orElse(-1);
            Set<Integer> nonChangeable = IntStream.range(0, target.size()).filter(i -> target.get(i) == 0).boxed().collect(Collectors.toSet());
            List<SequencedSet<Integer>> selected = validContinuations.stream().filter(b -> b.contains(minNon0Index) && !pressedButtons.contains(b) && b.stream().noneMatch(nonChangeable::contains)).toList();
            Set<SequencedSet<Integer>> newPressedButtons = new HashSet<>(pressedButtons);
            for (SequencedSet<Integer> button : selected) {
                List<Integer> newTarget = new ArrayList<>(target);
                for (Integer idx : button) {
                    newTarget.set(idx, newTarget.get(idx) - 1);
                }
                findSOlution(newTarget, steps + 1, newPressedButtons);
                newPressedButtons.add(button);
            }
        }
    }
    static Machine fromString(String line) {
        String[] spaces = line.split(" ");
        List<Boolean> lights = new ArrayList<>();
        List<SequencedSet<Integer>> buttons = new ArrayList<>();
        List<Integer> joltage = new ArrayList<>();
        for (int i = 1; i < spaces[0].length() - 1; ++i) {
            lights.add(spaces[0].charAt(i) == '#');
        }
        for (int i = 1; i < spaces.length - 1; ++i) {
            String[] wireing = spaces[i].substring(1, spaces[i].length() - 1).split(",");
            buttons.add(new LinkedHashSet<>(Arrays.stream(wireing).map(Integer::parseInt).toList()));
        }
        String[] joltageStrings = spaces[spaces.length - 1].substring(1, spaces[spaces.length - 1].length() - 1).split(",");
        Arrays.stream(joltageStrings).map(Integer::parseInt).forEach(joltage::add);
        return new Machine(lights, buttons, joltage);
    }
}

List<Machine> readInput() {
    try (var lines = Files.lines(Path.of("input_10.txt"), StandardCharsets.UTF_8)) {
        return lines.map(Machine::fromString).toList();
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}