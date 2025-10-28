package com.cpik

import com.google.ortools.Loader
import com.google.ortools.sat.*

class NQueensExplained(private val boardSize: Int = 4) {
    
    fun explainSolving() {
        Loader.loadNativeLibraries()
        
        println("=== N-Queens Problem Explained (${boardSize}x${boardSize} board) ===\n")
        
        val model = CpModel()
        
        // Step 1: Create Variables
        println("STEP 1: Creating Variables")
        println("We need $boardSize queens, one per column")
        println("Each queen's position is represented by which ROW it's in")
        
        val queens = (0 until boardSize).map { col ->
            val queen = model.newIntVar(0L, (boardSize - 1).toLong(), "queen_col_$col")
            println("  Column $col: queen can be in rows 0 to ${boardSize-1}")
            queen
        }
        
        println("\nSTEP 2: Adding Constraints")
        
        // Constraint 1: All different rows
        println("\nConstraint 1: No two queens in same ROW")
        println("  queens = [queen_col_0, queen_col_1, queen_col_2, queen_col_3]")
        println("  All must have different values (different rows)")
        model.addAllDifferent(queens.toTypedArray())
        
        // Constraint 2: Diagonals
        println("\nConstraint 2: No two queens on same DIAGONAL")
        println("  Diagonal math: If queen at (col_i, row_i) and (col_j, row_j)")
        println("  Same diagonal means: |col_i - col_j| = |row_i - row_j|")
        println("  We prevent this by ensuring diagonal sums are all different")
        
        val diag1 = queens.mapIndexed { col, queen ->
            println("  Diagonal 1 for col $col: queen_row + $col")
            LinearExpr.newBuilder().add(queen).add(col.toLong()).build()
        }
        val diag2 = queens.mapIndexed { col, queen ->
            println("  Diagonal 2 for col $col: queen_row - $col") 
            LinearExpr.newBuilder().add(queen).add(-col.toLong()).build()
        }
        
        model.addAllDifferent(diag1.toTypedArray())
        model.addAllDifferent(diag2.toTypedArray())
        
        println("\nSTEP 3: Solving")
        println("The solver now tries different combinations...")
        
        val solver = CpSolver()
        val status = solver.solve(model)
        
        if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            val solution = queens.map { queen -> solver.value(queen).toInt() }
            
            println("\nSTEP 4: Solution Found!")
            println("Queen positions (column -> row): $solution")
            
            println("\nLet's verify this solution:")
            for (i in solution.indices) {
                for (j in i + 1 until solution.size) {
                    val rowDiff = kotlin.math.abs(solution[i] - solution[j])
                    val colDiff = kotlin.math.abs(i - j)
                    println("  Queens at cols $i,$j (rows ${solution[i]},${solution[j]}): " +
                           "row_diff=$rowDiff, col_diff=$colDiff, same_diagonal=${rowDiff == colDiff}")
                }
            }
            
            println("\nVisual representation:")
            printBoardWithExplanation(solution)
            
        } else {
            println("No solution found!")
        }
    }
    
    private fun printBoardWithExplanation(solution: List<Int>) {
        println("\nBoard layout:")
        for (row in 0 until boardSize) {
            print("Row $row: ")
            for (col in 0 until boardSize) {
                if (solution[col] == row) {
                    print("Q ")
                } else {
                    print(". ")
                }
            }
            
            // Show which column has the queen in this row
            val queenCol = solution.indexOf(row)
            if (queenCol != -1) {
                print(" <- Queen from column $queenCol")
            }
            println()
        }
        
        println("\nColumn analysis:")
        solution.forEachIndexed { col, row ->
            println("Column $col: Queen at row $row")
        }
    }
}

fun main() {
    println("Let's solve a smaller 4-Queens problem to understand the process:\n")
    val explainer = NQueensExplained(4)
    explainer.explainSolving()
    
    println("\n" + "=".repeat(60))
    println("Now let's try 8-Queens:")
    val explainer8 = NQueensExplained(8) 
    explainer8.explainSolving()
}