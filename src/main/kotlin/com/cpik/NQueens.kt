package com.cpik

import com.google.ortools.Loader
import com.google.ortools.sat.*

class NQueensSolver(private val boardSize: Int) {
    
    fun solve(): List<Int>? {
        Loader.loadNativeLibraries()
        
        val model = CpModel()
        
        // Create variables: queens[i] represents the row of the queen in column i
        val queens = (0 until boardSize).map { col ->
            model.newIntVar(0L, (boardSize - 1).toLong(), "queen_$col")
        }
        
        // Add constraints
        addConstraints(model, queens)
        
        // Create solver
        val solver = CpSolver()
        
        // Solve for first solution
        val status = solver.solve(model)
        
        return if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
            queens.map { queen -> solver.value(queen).toInt() }
        } else {
            null
        }
    }
    
    private fun addConstraints(model: CpModel, queens: List<IntVar>) {
        // All queens must be in different rows
        model.addAllDifferent(queens.toTypedArray())
        
        // Create diagonal constraint variables
        val diag1 = queens.mapIndexed { i, queen ->
            LinearExpr.newBuilder().add(queen).add(i.toLong()).build()
        }
        val diag2 = queens.mapIndexed { i, queen ->
            LinearExpr.newBuilder().add(queen).add(-i.toLong()).build() 
        }
        
        // All diagonals must be different
        model.addAllDifferent(diag1.toTypedArray())
        model.addAllDifferent(diag2.toTypedArray())
    }
}

fun printBoard(solution: List<Int>) {
    val size = solution.size
    println("\nSolution:")
    for (row in 0 until size) {
        for (col in 0 until size) {
            if (solution[col] == row) {
                print("Q ")
            } else {
                print(". ")
            }
        }
        println()
    }
}

fun main() {
    val boardSize = 8
    val solver = NQueensSolver(boardSize)
    
    println("Solving $boardSize-Queens problem...")
    val solution = solver.solve()
    
    if (solution != null) {
        println("Found solution!")
        printBoard(solution)
    } else {
        println("No solution found")
    }
}