void main() {
    var input = readInput();
    IO.println(input.stream().mapToInt(Machine::countFewestPresses).sum());
    IO.println(input.stream().parallel().mapToInt(Machine::countFewestJoltagePressesByGaussJordan).sum());
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

    int countFewestJoltagePressesByGaussJordan() {
        double[][] columns = new double[buttons.size()][joltage.size()];
        for (int col = 0; col < buttons.size(); col++) {
            int colNum = col;
            buttons.get(col).forEach(rownum -> {
                columns[colNum][rownum] = 1.0;
            });
        }
        Matrix matrix = new Matrix(columns);
        double[] solution = joltage.stream().mapToDouble(Integer::doubleValue).toArray();
        int[] result = matrix.gaussJordenEliminate(solution);
        return Arrays.stream(result).sum();
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

static class Matrix {
    static final double EPS = 1e-7;
    double[][] elements;

    Matrix(int rows, int columns) {
        elements = new double[rows][columns];
    }

    Matrix(double[][] columns) {
        for (int i = 0; i < columns.length - 1; ++i) {
            if (columns[i].length != columns[i + 1].length) {
                throw new IllegalArgumentException("Columns must have the same length");
            }
        }
        elements = new double[columns[0].length][columns.length];
        for (int i = 0; i < elements.length; ++i) {
            for (int j = 0; j < elements[i].length; ++j) {
                elements[i][j] = columns[j][i];
            }
        }
    }

    void switchRows(int row1, int row2) {
        double[] temp = elements[row1];
        elements[row1] = elements[row2];
        elements[row2] = temp;
    }

    int rowIndexWithNonZeroInColAfter(int startRow, int checkColumn) {
        for (int i = startRow; i < elements.length; ++i) {
            if (Math.abs(elements[i][checkColumn]) > EPS) {
                return i;
            }
        }
        return -1;
    }

    void gaussEliminate(double[] solution) {
        eliminate(solution, false);
    }

    void eliminate(double[] solution, boolean bothWays) {
        if (elements.length != solution.length) {
            throw new IllegalArgumentException("Matrix and solution array must have the same number of rows");
        }
        int col = 0;
        for (int row = 0; row < elements.length && col < elements[0].length; ++row) {
            int indexOfFirstPossiblePivot = -1;
            do {
                indexOfFirstPossiblePivot = rowIndexWithNonZeroInColAfter(row, col);
                if (indexOfFirstPossiblePivot < 0) {
                    ++col;
                }
            } while (indexOfFirstPossiblePivot < 0 && col < elements[0].length);
            if (indexOfFirstPossiblePivot != -1) {
                if (indexOfFirstPossiblePivot != row) {
                    switchRows(row, indexOfFirstPossiblePivot);
                    double temp = solution[row];
                    solution[row] = solution[indexOfFirstPossiblePivot];
                    solution[indexOfFirstPossiblePivot] = temp;
                }
                double divider = elements[row][col];
                if (Math.abs(divider) > EPS) {
                    for (int i = col; i < elements[row].length; ++i) {
                        elements[row][i] /= divider;
                    }
                    solution[row] /= divider;
                    for (int nextRow = bothWays ? 0 : row + 1; nextRow < elements.length; ++nextRow) {
                        if (nextRow != row) {
                            double multiplier = elements[nextRow][col];
                            if (Math.abs(multiplier) > EPS) {
                                for (int i = col; i < elements[row].length; ++i) {
                                    elements[nextRow][i] -= elements[row][i] * multiplier;
                                }
                                solution[nextRow] -= solution[row] * multiplier;
                            }
                        }
                    }
                }
                ++col;
            }
        }
    }

    record Guess(int[] values, int sum) {
        @Override
        public boolean equals(Object o) {
            return o instanceof Guess g && Arrays.equals(values, g.values);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(values);
        }
    }

    int[] gaussJordenEliminate(double[] solution) {
        long maxJoltage = 0;
        for (double d : solution) {
            maxJoltage = Math.max(maxJoltage, (long) Math.ceil(d));
        }

        eliminate(solution, true);
        int[] pivotRowForVariables = new int[elements[0].length];
        double[] finalSolution = new double[elements[0].length];
        SequencedSet<Integer> freeVariables = new LinkedHashSet<>();
        for (int i = 0; i < pivotRowForVariables.length; ++i) {
            pivotRowForVariables[i] = -1;
            freeVariables.add(i);
        }
        for (int row = 0; row < elements.length; ++row) {
            boolean pivotFound = false;
            for (int col = 0; col < elements[row].length && !pivotFound; ++col) {
                if (Math.abs(elements[row][col]) > EPS) {
                    pivotRowForVariables[col] = row;
                    freeVariables.remove(col);
                    pivotFound = true;
                }
            }
        }
        if (freeVariables.isEmpty()) {
            for (int i = 0; i < finalSolution.length; ++i) {
                finalSolution[i] = solution[pivotRowForVariables[i]];
            }
            return toIntArray(finalSolution);
        }

        int[] freeVarArray = freeVariables.stream().mapToInt(Integer::intValue).toArray();

        boolean[] rowCanBeIncreased = new boolean[elements.length];
        for (int row = 0; row < elements.length; ++row) {
            int r = row;
            rowCanBeIncreased[row] = freeVariables.stream()
                    .anyMatch(freeCol -> elements[r][freeCol] < -EPS);
        }

        Deque<Guess> queue = new ArrayDeque<>(freeVarArray.length * 10);
        Set<Guess> seen = new HashSet<>();
        Guess startGuess = new Guess(new int[freeVarArray.length], 0);
        queue.add(startGuess);
        seen.add(startGuess);

        double[] bestSolution = null;
        int minSum = Integer.MAX_VALUE;

        while (!queue.isEmpty()) {
            Guess guess = queue.poll();
            double[] fullSolution = calculateSolution(pivotRowForVariables, solution, freeVarArray, guess.values);
            if (checkGuess(fullSolution)) {
                int sum = 0;
                for (double v : fullSolution) {
                    sum += (int) Math.round(v);
                }
                if (sum < minSum) {
                    minSum = sum;
                    bestSolution = fullSolution;
                }
            }

            if (guess.sum < minSum && !canPrune(fullSolution, pivotRowForVariables, rowCanBeIncreased)) {
                for (int i = 0; i < freeVarArray.length; ++i) {
                    if (guess.values[i] < maxJoltage) {
                        int[] nextVals = guess.values.clone();
                        nextVals[i]++;
                        Guess next = new Guess(nextVals, guess.sum + 1);
                        if (seen.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }
        if (bestSolution != null) {
            return toIntArray(bestSolution);
        }
        throw new IllegalStateException("No solution found");
    }

    private boolean canPrune(double[] fullSolution, int[] pivotRowForVariables, boolean[] rowCanBeIncreased) {
        for (int col = 0; col < fullSolution.length; ++col) {
            int row = pivotRowForVariables[col];
            if (row != -1 && fullSolution[col] < -EPS && !rowCanBeIncreased[row]) {
                return true;
            }
        }
        return false;
    }

    double[] calculateSolution(int[] pivotPositions, double[] solution, int[] freeVarArray, int[] guessValues) {
        double[] fullSolution = new double[elements[0].length];
        for (int i = 0; i < freeVarArray.length; ++i) {
            fullSolution[freeVarArray[i]] = guessValues[i];
        }
        for (int i = 0; i < fullSolution.length; ++i) {
            int row = pivotPositions[i];
            if (row != -1) {
                double sum = solution[row];
                for (int j = 0; j < freeVarArray.length; ++j) {
                    int freeCol = freeVarArray[j];
                    if (Math.abs(elements[row][freeCol]) > EPS) {
                        sum -= elements[row][freeCol] * guessValues[j];
                    }
                }
                fullSolution[i] = sum;
            }
        }
        return fullSolution;
    }

    int[] toIntArray(double[] doubles) {
        int[] ints = new int[doubles.length];
        for (int i = 0; i < ints.length; ++i) {
            if (Math.abs(doubles[i] - Math.round(doubles[i])) < EPS) {
                ints[i] = (int) Math.round(doubles[i]);
            } else {
                throw new IllegalArgumentException("Value is not an integer: " + doubles[i]);
            }
        }
        return ints;
    }

    boolean checkGuess(double[] variables) {
        for (double v : variables) {
            if (v < -EPS || Math.abs(v - Math.round(v)) > EPS) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n", "[\n", "\n]");
        for (double[] element : elements) {
            sj.add(Arrays.toString(element));
        }
        return sj.toString();
    }
}
