void main() {
    var input = readInput();
    IO.println(input.stream().mapToInt(Machine::countFewestPresses).sum());
    IO.println(input.stream().mapToInt(Machine::countFewestJoltagePressesByGaussJordan).sum());
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
        Fraction[][] columns = new Fraction[buttons.size()][joltage.size()];
        for(int i=0; i<columns.length; ++i) {
            for(int j=0; j<columns[i].length; ++j) {
                columns[i][j] = Fraction.ZERO;
            }
        }
        for(int col = 0; col < buttons.size(); col++) {
            int colNum = col;
            buttons.get(col).forEach(rownum -> {
                columns[colNum][rownum] = Fraction.whole(1);
            });
        }
        Matrix matrix = new Matrix(columns);
        Fraction[] solution = joltage.stream().map(Fraction::whole).toArray(Fraction[]::new);
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

record Fraction (long top, long bottom) implements Comparable<Fraction> {

    Fraction add(Fraction other) {
        long newTop = this.top * other.bottom + other.top * this.bottom;
        long newBottom = this.bottom * other.bottom;
        return Fraction.of(newTop, newBottom);
    }

    Fraction subtract(Fraction other) {
        long newTop = this.top * other.bottom - other.top * this.bottom;
        long newBottom = this.bottom * other.bottom;
        return Fraction.of(newTop, newBottom);
    }

    Fraction multiply(Fraction other) {
        long newTop = this.top * other.top;
        long newBottom = this.bottom * other.bottom;
        return Fraction.of(newTop, newBottom);
    }

    Fraction divide(Fraction other) {
        long newTop = this.top * other.bottom;
        long newBottom = this.bottom * other.top;
        return Fraction.of(newTop, newBottom);
    }

    boolean isWhole() {
        return bottom == 1;
    }

    @Override
    public String toString() {
        return top + "/" + bottom;
    }

    @Override
    public int compareTo(Fraction o) {
        return COMPARATOR.compare(this, o);
    }

    public static final Fraction ZERO = new Fraction(0,1);
    private static final Comparator<Fraction> COMPARATOR = Comparator.comparingDouble(f -> f.top / (double) f.bottom);

    static Fraction of(long top, long bottom) {
        long gcd = gcd(Math.abs(top), Math.abs(bottom));
        long a = top;
        long b = bottom;
        if(bottom < 0) {
            a = -a;
            b = -b;
        }
        return new Fraction(a / gcd, b / gcd);
    }

    static Fraction whole(long whole) {
        return new Fraction(whole, 1);
    }

    private static long gcd(long a, long b) {
        long max = Math.max(a, b);
        long min = Math.min(a, b);
        while (min != 0) {
            long temp = min;
            min = max % min;
            max = temp;
        }
        return max;
    }
}

static class Matrix {
    Fraction[][] elements;

    Matrix(int rows, int columns) {
        elements = new Fraction[rows][columns];
    }

    Matrix(Fraction[][] columns) {
        for(int i=0; i<columns.length-1; ++i) {
            if(columns[i].length != columns[i+1].length) {
                throw new IllegalArgumentException("Columns must have the same length");
            }
        }
        elements = new Fraction[columns[0].length][columns.length];
        for(int i=0; i<elements.length; ++i) {
            for(int j=0; j<elements[i].length; ++j) {
                elements[i][j] = columns[j][i];
            }
        }
    }

    void switchRows(int row1, int row2) {
        Fraction[] temp = elements[row1];
        elements[row1] = elements[row2];
        elements[row2] = temp;
    }

    int rowIndexWithNonZeroInColAfter(int startRow, int checkColumn) {
        for(int i=startRow; i<elements.length; ++i) {
            if(!elements[i][checkColumn].equals(Fraction.ZERO)) {
                return i;
            }
        }
        return -1;
    }

    void gaussEliminate(Fraction[] solution) {
        eliminate(solution, false);
    }

    void eliminate(Fraction[] solution, boolean bothWays) {
        if(elements.length != solution.length) {
            throw new IllegalArgumentException("Matrix and solution array must have the same number of rows");
        }
        int col = 0;
        for(int row = 0; row<elements.length && col<elements[0].length; ++row) {
            int indexOfFirstPossiblePivot = -1;
            do {
                indexOfFirstPossiblePivot = rowIndexWithNonZeroInColAfter(row,col);
                if(indexOfFirstPossiblePivot<0) {
                    ++col;
                }
            } while(indexOfFirstPossiblePivot<0&&col<elements[0].length);
            if(indexOfFirstPossiblePivot!=-1) {
                if (indexOfFirstPossiblePivot != row) {
                    switchRows(row, indexOfFirstPossiblePivot);
                    swap(solution, row, indexOfFirstPossiblePivot);
                }
                Fraction divider = elements[row][col];
                if (!divider.equals(Fraction.ZERO)) {
                    for (int i = col; i < elements[row].length; ++i) {
                        elements[row][i] = elements[row][i].divide(divider);
                    }
                    solution[row] = solution[row].divide(divider);
                    for (int nextRow = bothWays ? 0 : row + 1; nextRow < elements.length; ++nextRow) {
                        if (nextRow != row) {
                            Fraction multiplier = elements[nextRow][col];
                            if (!elements[nextRow][col].equals(Fraction.ZERO)) {
                                for (int i = col; i < elements[row].length; ++i) {
                                    elements[nextRow][i] = elements[nextRow][i].subtract(elements[row][i].multiply(multiplier));
                                }
                                solution[nextRow] = solution[nextRow].subtract(solution[row].multiply(multiplier));
                            }
                        }
                    }
                }
                ++col;
            }
        }
    }

    int[] gaussJordenEliminate(Fraction[] solution) {
        long maxJoltage = 0;
        for (Fraction f : solution) {
            maxJoltage = Math.max(maxJoltage, f.top());
        }

        eliminate(solution, true);
        int [] pivotRowForVariables = new int[elements[0].length];
        Fraction[] finalSolution = new Fraction[elements[0].length];
        SequencedSet<Integer> freeVariables = new LinkedHashSet<>();
        for(int i=0; i<pivotRowForVariables.length; ++i) {
            pivotRowForVariables[i] = -1;
            freeVariables.add(i);
        }
        for(int row = 0 ; row<elements.length; ++row) {
            boolean pivotFound = false;
            for(int col=0; col<elements[row].length && !pivotFound; ++col) {
                if(!elements[row][col].equals(Fraction.ZERO)) {
                    pivotRowForVariables[col] = row;
                    freeVariables.remove(col);
                    pivotFound = true;
                }
            }
        }
        if(freeVariables.isEmpty()) {
            for(int i=0; i<finalSolution.length; ++i) {
                finalSolution[i] = solution[pivotRowForVariables[i]];
            }
            return toIntArray(finalSolution);
        }
        Queue<SequencedMap<Integer, Fraction>> queue = new ArrayDeque<>(freeVariables.size()*10);
        Set<SequencedMap<Integer, Fraction>> seen = new HashSet<>();
        SequencedMap<Integer, Fraction> startGuess = new LinkedHashMap<>();
        freeVariables.forEach(i->startGuess.put(i, Fraction.ZERO));
        queue.add(startGuess);
        seen.add(startGuess);

        Fraction[] bestSolution = null;
        int minSum = Integer.MAX_VALUE;

        while (!queue.isEmpty()) {
            SequencedMap<Integer, Fraction> guess = queue.poll();
            Fraction[] fullSolution = calculateSolution(pivotRowForVariables, solution, guess);
            if(checkGuess(solution, fullSolution)) {
                int sum = 0;
                for (Fraction f : fullSolution) {
                    sum += (int) f.top();
                }
                if (sum < minSum) {
                    minSum = sum;
                    bestSolution = fullSolution;
                }
            }

            long guessSum = guess.values().stream().mapToLong(Fraction::top).sum();
            if (guessSum < minSum && !canPrune(fullSolution, pivotRowForVariables, freeVariables)) {
                for (int i : freeVariables) {
                    if (guess.get(i).top() < maxJoltage) {
                        SequencedMap<Integer, Fraction> inc = new LinkedHashMap<>(guess);
                        inc.put(i, inc.get(i).add(Fraction.whole(1)));
                        if (seen.add(inc)) {
                            queue.add(inc);
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

    private boolean canPrune(Fraction[] fullSolution, int[] pivotRowForVariables, SequencedSet<Integer> freeVariables) {
        for (int col = 0; col < fullSolution.length; ++col) {
            int row = pivotRowForVariables[col];
            if (row != -1 && fullSolution[col].compareTo(Fraction.ZERO) < 0) {
                boolean canBeIncreased = freeVariables.stream()
                        .anyMatch(freeCol -> elements[row][freeCol].compareTo(Fraction.ZERO) < 0);
                if (!canBeIncreased) {
                    return true;
                }
            }
        }
        return false;
    }

    Fraction[] calculateSolution(int[] pivotPositions, Fraction[] solution, SequencedMap<Integer, Fraction> guess) {
        Fraction[] fullSolution = new Fraction[elements[0].length];
        for(int i=0; i<fullSolution.length; ++i) {
            int row = pivotPositions[i];
            if(pivotPositions[i]!=-1) {
                fullSolution[i] = solution[row].subtract(sumGuesses(row, guess));
            } else {
                fullSolution[i] = guess.get(i);
            }
        }
        return fullSolution;
    }

    Fraction sumGuesses(int row, SequencedMap<Integer, Fraction> guesses) {
        Fraction sum = Fraction.ZERO;
        for(int i:guesses.keySet()) {
            sum = sum.add(elements[row][i].multiply(guesses.get(i)));
        }
        return sum;
    }

    int[] toIntArray(Fraction[] fractions) {
        int[] ints = new int[fractions.length];
        for(int i=0; i<ints.length; ++i) {
            if(fractions[i].isWhole()) {
                ints[i] = (int)fractions[i].top();
            } else {
                throw new IllegalArgumentException("Fraction is not an integer");
            }
        }
        return ints;
    }

    boolean checkGuess(Fraction[] solution, Fraction[] variables) {
        for (Fraction f : variables) {
            if (!f.isWhole() || f.compareTo(Fraction.ZERO) < 0) {
                return false;
            }
        }
        for (int row = 0; row < elements.length; ++row) {
            Fraction sum = Fraction.ZERO;
            for (int col = 0; col < elements[row].length; ++col) {
                sum = sum.add(elements[row][col].multiply(variables[col]));
            }
            if (!sum.equals(solution[row])) {
                return false;
            }
        }
        return true;
    }
    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner("\n", "[\n", "\n]");
        for (Fraction[] element : elements) {
            sj.add(Arrays.toString(element));
        }
        return sj.toString();
    }

    private static<T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
