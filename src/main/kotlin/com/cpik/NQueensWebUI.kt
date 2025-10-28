package com.cpik

import com.google.ortools.Loader
import com.google.ortools.sat.*
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis

class NQueensWebUI(private val boardSize: Int) {
    
    data class SolutionResult(
        val solutions: List<List<Int>>,
        val solveTimeMs: Long,
        val status: CpSolverStatus,
        val selectedSolution: Int = 0
    )
    
    fun solveAndGenerateWeb(findMultiple: Boolean = true, maxSolutions: Int = 10): SolutionResult {
        Loader.loadNativeLibraries()
        
        val model = CpModel()
        
        // Create variables
        val queens = (0 until boardSize).map { col ->
            model.newIntVar(0L, (boardSize - 1).toLong(), "queen_$col")
        }
        
        // Add constraints
        addConstraints(model, queens)
        
        // Solve with timing
        val solver = CpSolver()
        var status: CpSolverStatus = CpSolverStatus.UNKNOWN
        val solutions = mutableListOf<List<Int>>()
        
        val solveTime = measureTimeMillis {
            if (findMultiple) {
                // Find multiple solutions
                val solutionCallback = object : CpSolverSolutionCallback() {
                    override fun onSolutionCallback() {
                        if (solutions.size < maxSolutions) {
                            val solution = queens.map { queen -> value(queen).toInt() }
                            solutions.add(solution)
                        }
                        if (solutions.size >= maxSolutions) {
                            stopSearch()
                        }
                    }
                }
                status = solver.solve(model, solutionCallback)
            } else {
                // Find single solution
                status = solver.solve(model)
                if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
                    solutions.add(queens.map { queen -> solver.value(queen).toInt() })
                }
            }
        }
        
        // Randomize the selected solution
        val selectedSolution = if (solutions.isNotEmpty()) {
            (0 until solutions.size).random()
        } else 0
        
        val result = SolutionResult(solutions, solveTime, status, selectedSolution)
        
        // Generate web UI
        generateHtmlFile(result)
        
        return result
    }
    
    private fun addConstraints(model: CpModel, queens: List<IntVar>) {
        // All queens in different rows
        model.addAllDifferent(queens.toTypedArray())
        
        // Diagonal constraints
        val diag1 = queens.mapIndexed { col, queen ->
            LinearExpr.newBuilder().add(queen).add(col.toLong()).build()
        }
        val diag2 = queens.mapIndexed { col, queen ->
            LinearExpr.newBuilder().add(queen).add(-col.toLong()).build()
        }
        
        model.addAllDifferent(diag1.toTypedArray())
        model.addAllDifferent(diag2.toTypedArray())
    }
    
    private fun generateHtmlFile(result: SolutionResult) {
        val templatePath = File("web/template.html")
        if (!templatePath.exists()) {
            println("Template file not found at: ${templatePath.absolutePath}")
            return
        }
        
        val template = templatePath.readText()
        
        val currentSolution = if (result.solutions.isNotEmpty()) result.solutions[result.selectedSolution] else null
        
        val html = template
            .replace("{{BOARD_SIZE}}", boardSize.toString())
            .replace("{{SOLUTION_STATUS}}", getSolutionStatus(result))
            .replace("{{CHESSBOARD}}", generateChessboard(currentSolution))
            .replace("{{ROW_LABELS}}", generateRowLabels())
            .replace("{{COL_LABELS}}", generateColLabels())
            .replace("{{QUEEN_COUNT}}", (currentSolution?.size ?: 0).toString())
            .replace("{{CONSTRAINT_COUNT}}", calculateConstraintCount().toString())
            .replace("{{SOLVE_TIME}}", "${result.solveTimeMs}ms")
            .replace("{{QUEEN_POSITIONS}}", generateQueenPositions(currentSolution))
            .replace("{{QUEEN_POSITIONS_ARRAY}}", generateQueenPositionsArray(currentSolution))
            .replace("{{ALL_SOLUTIONS_ARRAY}}", generateAllSolutionsArray(result.solutions))
            .replace("{{SOLUTION_COUNT}}", result.solutions.size.toString())
            .replace("{{SELECTED_SOLUTION}}", (result.selectedSolution + 1).toString())
            .replace("{{BEST_SOLUTION_INDEX}}", findBestSolutionIndex(result.solutions).toString())
        
        val outputFile = File("web/nqueens_solution.html")
        outputFile.writeText(html)
        
        println("Web UI generated: ${outputFile.absolutePath}")
        println("Open this file in your browser to see the visualization!")
    }
    
    private fun getSolutionStatus(result: SolutionResult): String {
        return when {
            result.solutions.isEmpty() -> "❌ No solution exists for ${boardSize}x${boardSize} board"
            result.solutions.size == 1 -> "✅ Solution found in ${result.solveTimeMs}ms"
            else -> "✅ Found ${result.solutions.size} solutions in ${result.solveTimeMs}ms (showing solution ${result.selectedSolution + 1})"
        }
    }
    
    private fun generateChessboard(solution: List<Int>?): String {
        val sb = StringBuilder()
        
        for (row in 0 until boardSize) {
            sb.append("<div class=\"chess-row\">\n")
            
            for (col in 0 until boardSize) {
                val isLight = (row + col) % 2 == 0
                val squareClass = if (isLight) "light" else "dark"
                val hasQueen = solution?.get(col) == row
                val queenClass = if (hasQueen) " queen" else ""
                
                sb.append("                <div class=\"chess-square $squareClass$queenClass\">")
                
                if (hasQueen) {
                    sb.append("<span class=\"queen-symbol\">♛</span>")
                }
                
                sb.append("</div>\n")
            }
            
            sb.append("            </div>\n")
        }
        
        return sb.toString()
    }
    
    private fun generateRowLabels(): String {
        val sb = StringBuilder()
        for (row in 0 until boardSize) {
            sb.append("<div class=\"row-label\">${boardSize - row}</div>\n")
        }
        return sb.toString()
    }
    
    private fun generateColLabels(): String {
        val sb = StringBuilder()
        for (col in 0 until boardSize) {
            val label = ('A' + col).toString()
            sb.append("<div class=\"col-label\">$label</div>\n")
        }
        return sb.toString()
    }
    
    private fun calculateConstraintCount(): Int {
        // Row constraints: 1 (all different)
        // Diagonal constraints: 2 (both diagonal sets)
        // Total constraint expressions: row + 2 diagonals
        return 3
    }
    
    private fun generateQueenPositions(solution: List<Int>?): String {
        if (solution == null) return "No solution found"
        
        return solution.mapIndexed { col, row ->
            val colLabel = ('A' + col).toString()
            val rowLabel = (boardSize - row).toString()
            "Column $col ($colLabel): Row $row ($rowLabel)"
        }.joinToString("<br>")
    }
    
    private fun generateQueenPositionsArray(solution: List<Int>?): String {
        if (solution == null) return "[]"
        return "[${solution.joinToString(", ")}]"
    }
    
    private fun generateAllSolutionsArray(solutions: List<List<Int>>): String {
        return "[${solutions.joinToString(", ") { "[${it.joinToString(", ")}]" }}]"
    }
    
    private fun findBestSolutionIndex(solutions: List<List<Int>>): Int {
        // For demonstration, let's say the "best" solution has queens more spread out
        // We'll use the one with maximum distance between consecutive queens
        if (solutions.isEmpty()) return 0
        
        return solutions.mapIndexed { index, solution ->
            val spreadScore = calculateSpreadScore(solution)
            index to spreadScore
        }.maxByOrNull { it.second }?.first ?: 0
    }
    
    private fun calculateSpreadScore(solution: List<Int>): Int {
        // Calculate how "spread out" the queens are
        var totalDistance = 0
        for (i in 0 until solution.size - 1) {
            totalDistance += kotlin.math.abs(solution[i] - solution[i + 1])
        }
        return totalDistance
    }
}

fun main() {
    println("🏰 N-Queens Web UI Generator")
    println("=".repeat(40))
    
    // Try different board sizes
    val boardSizes = listOf(8)
    
    for (size in boardSizes) {
        println("\n📋 Solving ${size}x${size} board...")
        val webUI = NQueensWebUI(size)
        val result = webUI.solveAndGenerateWeb()
        
        if (result.solutions.isNotEmpty()) {
            println("✅ Solution found! Check web/nqueens_solution.html")
            break
        } else {
            println("❌ No solution for ${size}x${size}")
        }
    }
}