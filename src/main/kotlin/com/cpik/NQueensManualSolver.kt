package com.cpik

class NQueensManualSolver(private val boardSize: Int) {
    
    data class SolverStep(
        val stepNumber: Int,
        val column: Int,
        val row: Int,
        val action: String, // TRY, PLACE, REJECT, BACKTRACK
        val reason: String,
        val board: Array<Int?>,
        val conflicts: List<String> = emptyList()
    ) {
        override fun toString(): String {
            return "Step $stepNumber: $action at ($column, $row) - $reason"
        }
    }
    
    private val steps = mutableListOf<SolverStep>()
    private var stepCounter = 0
    
    fun solve(): List<SolverStep> {
        steps.clear()
        stepCounter = 0
        val board = Array<Int?>(boardSize) { null }
        
        addStep(-1, -1, "START", "Begin solving ${boardSize}x${boardSize} N-Queens problem", board)
        
        if (solveRecursive(board, 0)) {
            addStep(-1, -1, "COMPLETE", "Solution found!", board)
        } else {
            addStep(-1, -1, "NO_SOLUTION", "No solution exists", board)
        }
        
        return steps.toList()
    }
    
    private fun solveRecursive(board: Array<Int?>, col: Int): Boolean {
        if (col == boardSize) {
            return true // All queens placed
        }
        
        for (row in 0 until boardSize) {
            addStep(col, row, "TRY", "Trying to place queen at column $col, row $row", board)
            
            val conflicts = getConflicts(board, col, row)
            
            if (conflicts.isEmpty()) {
                board[col] = row
                addStep(col, row, "PLACE", "No conflicts - placing queen", board)
                
                if (solveRecursive(board, col + 1)) {
                    return true
                }
                
                // Backtrack
                board[col] = null
                addStep(col, row, "BACKTRACK", "Dead end reached - removing queen and trying next position", board)
            } else {
                addStep(col, row, "REJECT", "Conflicts found: ${conflicts.joinToString(", ")}", board, conflicts)
            }
        }
        
        return false
    }
    
    private fun getConflicts(board: Array<Int?>, col: Int, row: Int): List<String> {
        val conflicts = mutableListOf<String>()
        
        for (c in 0 until col) {
            val r = board[c] ?: continue
            
            // Row conflict
            if (r == row) {
                conflicts.add("Row conflict with queen at ($c, $r)")
            }
            
            // Diagonal conflicts
            if (kotlin.math.abs(r - row) == kotlin.math.abs(c - col)) {
                conflicts.add("Diagonal conflict with queen at ($c, $r)")
            }
        }
        
        return conflicts
    }
    
    private fun addStep(col: Int, row: Int, action: String, reason: String, board: Array<Int?>, conflicts: List<String> = emptyList()) {
        steps.add(SolverStep(
            stepNumber = ++stepCounter,
            column = col,
            row = row,
            action = action,
            reason = reason,
            board = board.copyOf(),
            conflicts = conflicts
        ))
    }
    
    fun printSteps() {
        println("N-Queens Manual Solver Trace (${boardSize}x${boardSize})")
        println("=".repeat(60))
        
        for (step in steps) {
            println("\nStep ${step.stepNumber}: ${step.action}")
            
            when (step.action) {
                "START", "COMPLETE", "NO_SOLUTION" -> {
                    println("  ${step.reason}")
                }
                "TRY" -> {
                    println("  ${step.reason}")
                }
                "PLACE" -> {
                    println("  ✅ Column ${step.column}, Row ${step.row}: ${step.reason}")
                    printBoard(step.board)
                }
                "REJECT" -> {
                    println("  ❌ Column ${step.column}, Row ${step.row}: ${step.reason}")
                }
                "BACKTRACK" -> {
                    println("  ⬅️ Column ${step.column}, Row ${step.row}: ${step.reason}")
                    printBoard(step.board)
                }
            }
        }
        
        println("\n" + "=".repeat(60))
        println("Summary:")
        println("- Total steps: ${steps.size}")
        println("- Attempts: ${steps.count { it.action == "TRY" }}")
        println("- Successful placements: ${steps.count { it.action == "PLACE" }}")
        println("- Rejections: ${steps.count { it.action == "REJECT" }}")
        println("- Backtracks: ${steps.count { it.action == "BACKTRACK" }}")
    }
    
    private fun printBoard(board: Array<Int?>) {
        println("    Board state:")
        for (row in 0 until boardSize) {
            print("    ")
            for (col in 0 until boardSize) {
                if (board[col] == row) {
                    print("Q ")
                } else {
                    print(". ")
                }
            }
            println()
        }
    }
}

fun main() {
    println("N-Queens Manual Solver - See How Backtracking Really Works!")
    println()
    
    // Solve 4x4 first (shows backtracking nicely)
    val solver4 = NQueensManualSolver(4)
    val steps4 = solver4.solve()
    solver4.printSteps()
    
    println("\n\n" + "=".repeat(80))
    println("Now let's try 6x6 (more complex):")
    println("=".repeat(80))
    
    val solver6 = NQueensManualSolver(6)
    val steps6 = solver6.solve()
    solver6.printSteps()
}