# Constraint Programming Best Practices in Kotlin

This project demonstrates best practices for using Google OR-Tools constraint programming from Kotlin through multiple examples:

1. **Rota Scheduler** - A practical scheduling system with worker shift constraints
2. **N-Queens Solver** - Classic constraint satisfaction problem with multiple solution approaches
3. **Educational Tools** - Step-by-step solving visualization and web-based interfaces

The implementations showcase:
- **Encapsulated** constraint and objective definitions
- **Testable** modular architecture
- **Multiple solving strategies** (single solution, multiple solutions, step-by-step)
- **Interactive visualization** through web UI generation
- **Educational value** with manual solver tracing

## Examples

### 1. Rota Scheduler (`Main.kt`)
Demonstrates workforce scheduling with constraints:
- Minimum time between shifts for workers
- Single worker per shift requirement
- Objective to fill all available shifts

### 2. N-Queens Solver (`NQueens.kt`)
Classic constraint satisfaction problem:
- Place N queens on NxN chessboard without conflicts
- Demonstrates constraint modeling for combinatorial problems

### 3. N-Queens Web UI (`NQueensWebUI.kt`)
Enhanced solver with visualization:
- Multiple solution finding
- HTML/CSS chessboard generation
- Interactive solution navigation
- Performance metrics display

### 4. Manual Step-by-Step Solver (`NQueensManualSolver.kt`)
Educational backtracking demonstration:
- Detailed step-by-step trace of solving process
- Shows how constraint propagation and backtracking work
- Perfect for understanding algorithm internals

## Getting Started

**Requirements:**
- OpenJDK 21
- Gradle (wrapper included)

**Running Examples:**

```shell
# Default rota scheduler
./gradlew run

# N-Queens basic solver
./gradlew run --args="nqueens"

# N-Queens with web UI generation
./gradlew run --args="nqueens-web"

# Manual step-by-step solver (educational)
./gradlew run --args="nqueens-manual"
```

**Testing:**

```shell
./gradlew test
```

**Web UI:**
After running the web UI generator, open `web/nqueens_solution.html` in your browser to see the interactive chessboard visualization.