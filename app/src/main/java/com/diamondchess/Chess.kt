package com.diamondchess

import kotlin.math.abs
import kotlin.random.Random

data class Move(val from: Int, val to: Int, val promotion: Char? = null)

class Chess {
    var board = initial()
        private set
    var whiteTurn = true
        private set
    var enPassant = -1
        private set
    var rights = "KQkq"
        private set
    val history = mutableListOf<String>()
    private val states = mutableListOf<State>()
    data class State(val board: List<Char>, val white: Boolean, val ep: Int, val castle: String)
    companion object {
        fun initial(): MutableList<Char> = ("rnbqkbnr" + "pppppppp" + ".".repeat(32) + "PPPPPPPP" + "RNBQKBNR").toMutableList()
        fun white(c: Char) = c in 'A'..'Z'
        fun side(c: Char, w: Boolean) = c != '.' && white(c) == w
        fun coord(i: Int) = "${'a' + i % 8}${8 - i / 8}"
    }
    private fun row(i: Int) = i / 8
    private fun col(i: Int) = i % 8
    private fun at(r: Int, c: Int) = if (r in 0..7 && c in 0..7) board[r * 8 + c] else '?'
    private fun attack(square: Int, byWhite: Boolean): Boolean {
        val r = row(square); val c = col(square)
        val pawn = if (byWhite) 'P' else 'p'
        val pr = r + if (byWhite) 1 else -1
        if (at(pr, c - 1) == pawn || at(pr, c + 1) == pawn) return true
        val knight = if (byWhite) 'N' else 'n'
        for ((dr, dc) in listOf(1 to 2, 2 to 1, -1 to 2, -2 to 1, 1 to -2, 2 to -1, -1 to -2, -2 to -1)) if (at(r + dr, c + dc) == knight) return true
        for (dr in -1..1) for (dc in -1..1) if (dr != 0 || dc != 0) {
            var rr = r + dr; var cc = c + dc; var steps = 1
            while (rr in 0..7 && cc in 0..7) {
                val p = at(rr, cc)
                if (p != '.') {
                    if (side(p, byWhite)) {
                        val t = p.uppercaseChar()
                        if (steps == 1 && t == 'K') return true
                        if ((dr == 0 || dc == 0) && (t == 'R' || t == 'Q')) return true
                        if (dr != 0 && dc != 0 && (t == 'B' || t == 'Q')) return true
                    }
                    break
                }
                rr += dr; cc += dc; steps++
            }
        }
        return false
    }
    fun inCheck(w: Boolean): Boolean {
        val king = board.indexOf(if (w) 'K' else 'k')
        return king < 0 || attack(king, !w)
    }
    private fun pseudo(w: Boolean): List<Move> {
        val out = mutableListOf<Move>()
        fun add(i: Int, j: Int) { if (j in 0..63 && !side(board[j], w) && board[j].uppercaseChar() != 'K') out.add(Move(i, j, if (board[i].uppercaseChar() == 'P' && row(j) == if (w) 0 else 7) 'Q' else null)) }
        for (i in 0..63) {
            val p = board[i]; if (!side(p, w)) continue
            val r = row(i); val c = col(i)
            when (p.uppercaseChar()) {
                'P' -> {
                    val d = if (w) -1 else 1; val nr = r + d
                    if (at(nr, c) == '.') {
                        add(i, nr * 8 + c)
                        if (r == if (w) 6 else 1) if (at(r + 2 * d, c) == '.') add(i, (r + 2 * d) * 8 + c)
                    }
                    for (dc in listOf(-1, 1)) if (nr in 0..7 && c + dc in 0..7) {
                        val j = nr * 8 + c + dc
                        if ((board[j] != '.' && !side(board[j], w)) || j == enPassant) add(i, j)
                    }
                }
                'N' -> for ((dr, dc) in listOf(1 to 2, 2 to 1, -1 to 2, -2 to 1, 1 to -2, 2 to -1, -1 to -2, -2 to -1)) if (r + dr in 0..7 && c + dc in 0..7) add(i, (r + dr) * 8 + c + dc)
                'K' -> {
                    for (dr in -1..1) for (dc in -1..1) if ((dr != 0 || dc != 0) && r + dr in 0..7 && c + dc in 0..7) add(i, (r + dr) * 8 + c + dc)
                    if (!inCheck(w)) {
                        val rank = if (w) 7 else 0
                        val k = if (w) 'K' else 'k'; val q = if (w) 'Q' else 'q'
                        if (i == rank * 8 + 4 && k in rights && board[rank * 8 + 7] == if (w) 'R' else 'r' && at(rank, 5) == '.' && at(rank, 6) == '.' && !attack(rank * 8 + 5, !w) && !attack(rank * 8 + 6, !w)) out.add(Move(i, rank * 8 + 6))
                        if (i == rank * 8 + 4 && q in rights && board[rank * 8] == if (w) 'R' else 'r' && at(rank, 1) == '.' && at(rank, 2) == '.' && at(rank, 3) == '.' && !attack(rank * 8 + 3, !w) && !attack(rank * 8 + 2, !w)) out.add(Move(i, rank * 8 + 2))
                    }
                }
                else -> {
                    val dirs = mutableListOf<Pair<Int, Int>>()
                    if (p.uppercaseChar() in "BRQ") dirs.addAll(listOf(1 to 1, 1 to -1, -1 to 1, -1 to -1))
                    if (p.uppercaseChar() in "RQ") dirs.addAll(listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1))
                    for ((dr, dc) in dirs) {
                        var rr = r + dr; var cc = c + dc
                        while (rr in 0..7 && cc in 0..7) {
                            val j = rr * 8 + cc
                            if (side(board[j], w)) break
                            add(i, j)
                            if (board[j] != '.') break
                            rr += dr; cc += dc
                        }
                    }
                }
            }
        }
        return out
    }
    private fun snapshot() = State(board.toList(), whiteTurn, enPassant, rights)
    private fun restore(s: State) { board = s.board.toMutableList(); whiteTurn = s.white; enPassant = s.ep; rights = s.castle }
    private fun apply(m: Move) {
        val p = board[m.from]; val captured = board[m.to]
        if (p.uppercaseChar() == 'P' && m.to == enPassant && captured == '.' && col(m.from) != col(m.to)) board[m.to + if (white(p)) 8 else -8] = '.'
        board[m.to] = if (m.promotion != null) if (white(p)) m.promotion.uppercaseChar() else m.promotion.lowercaseChar() else p
        board[m.from] = '.'
        if (p.uppercaseChar() == 'K' && abs(col(m.to) - col(m.from)) == 2) {
            val r = row(m.from); val kingside = col(m.to) == 6
            val rookFrom = r * 8 + if (kingside) 7 else 0; val rookTo = r * 8 + if (kingside) 5 else 3
            board[rookTo] = board[rookFrom]; board[rookFrom] = '.'
        }
        if (p == 'K') rights = rights.replace("K", "").replace("Q", "")
        if (p == 'k') rights = rights.replace("k", "").replace("q", "")
        for ((sq, flag) in listOf(63 to 'K', 56 to 'Q', 7 to 'k', 0 to 'q')) if (m.from == sq || m.to == sq) rights = rights.replace(flag.toString(), "")
        enPassant = if (p.uppercaseChar() == 'P' && abs(m.to - m.from) == 16) (m.to + m.from) / 2 else -1
        whiteTurn = !whiteTurn
    }
    fun legalMoves(): List<Move> {
        val w = whiteTurn; val out = mutableListOf<Move>()
        for (m in pseudo(w)) { val s = snapshot(); apply(m); if (!inCheck(w)) out.add(m); restore(s) }
        return out
    }
    fun play(m: Move): Boolean {
        val legal = legalMoves().find { it.from == m.from && it.to == m.to && (it.promotion == null || it.promotion.uppercaseChar() == (m.promotion ?: 'Q').uppercaseChar()) } ?: return false
        states.add(snapshot()); history.add(coord(legal.from) + coord(legal.to) + (legal.promotion?.lowercaseChar() ?: "")); apply(legal); return true
    }
    fun undo(): Boolean { if (states.isEmpty()) return false; restore(states.removeAt(states.lastIndex)); history.removeAt(history.lastIndex); return true }
    fun result(): String? { if (legalMoves().isEmpty()) return if (inCheck(whiteTurn)) if (whiteTurn) "Победа чёрных — мат" else "Победа белых — мат" else "Ничья — пат"; return null }
    fun botMove(level: Int): Move? {
        val moves = legalMoves(); if (moves.isEmpty()) return null
        val values = mapOf('P' to 100, 'N' to 320, 'B' to 330, 'R' to 500, 'Q' to 900, 'K' to 0)
        val ranked = moves.map { m ->
            val captured = if (board[m.to] == '.') 0 else values[board[m.to].uppercaseChar()] ?: 0
            val s = snapshot(); apply(m)
            val mate = if (legalMoves().isEmpty() && inCheck(whiteTurn)) 100000 else 0
            val safety = if (attack(m.to, whiteTurn)) -(values[board[m.to].uppercaseChar()] ?: 0) / 2 else 0
            restore(s)
            m to (captured + mate + safety + Random.nextInt(0, 45))
        }.sortedByDescending { it.second }
        val pool = when { level <= 1 -> ranked; level <= 3 -> ranked.take(12); level <= 6 -> ranked.take(5); else -> ranked.take(2) }
        return pool.random().first
    }
}
