package com.diamondchess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val dark = Color(0xFF171A20)
private val green = Color(0xFF7FA650)
private val light = Color(0xFFEEEED2)
private val darkSquare = Color(0xFF769656)
private val glyphs = mapOf('K' to "♔", 'Q' to "♕", 'R' to "♖", 'B' to "♗", 'N' to "♘", 'P' to "♙", 'k' to "♚", 'q' to "♛", 'r' to "♜", 'b' to "♝", 'n' to "♞", 'p' to "♟")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { DiamondApp() } }
}

@Composable
fun DiamondApp() {
    var game by remember { mutableStateOf(Chess()) }
    var revision by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var mode by remember { mutableStateOf("Бот") }
    var level by remember { mutableFloatStateOf(4f) }
    var diamonds by remember { mutableStateOf("После партии") }
    var review by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val moves = remember(revision) { game.legalMoves() }
    val result = remember(revision) { game.result() }
    fun refresh() { revision++; selected = -1 }
    fun reset() { game = Chess(); review = false; refresh() }
    fun play(from: Int, to: Int) {
        if (game.play(Move(from, to))) {
            refresh()
            if (mode == "Бот" && game.result() == null) { game.botMove(level.toInt())?.let { game.play(it) }; refresh() }
        } else selected = -1
    }
    MaterialTheme(colorScheme = darkColorScheme(primary = green, background = dark, surface = Color(0xFF242932))) {
        Column(Modifier.fillMaxSize().background(dark).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("◆ DIAMOND CHESS", color = Color.White, fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = { showSettings = !showSettings }) { Text("⚙ Настройки") }
            }
            if (showSettings) {
                Card { Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Показ бриллиантовых ходов", fontWeight = FontWeight.Bold)
                    listOf("Во время партии", "После партии", "Выключено").forEach { choice ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { diamonds = choice }) {
                            RadioButton(selected = diamonds == choice, onClick = { diamonds = choice }); Text(choice)
                        }
                    }
                    Text("Анализ бриллиантов будет подключён со Stockfish в следующей версии.", fontSize = 12.sp)
                } }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Бот", "На двоих").forEach { item -> FilterChip(selected = mode == item, onClick = { mode = item; reset() }, label = { Text(item) }) }
            }
            if (mode == "Бот") {
                Text("Уровень компьютера: ${level.toInt()} / 10 (экспериментальный)")
                Slider(value = level, onValueChange = { level = it }, valueRange = 1f..10f, steps = 8)
            }
            Text(if (game.whiteTurn) "Ход белых" else "Ход чёрных", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Column(Modifier.fillMaxWidth().aspectRatio(1f).border(2.dp, Color(0xFF414B37))) {
                for (r in 0..7) Row(Modifier.weight(1f)) {
                    for (c in 0..7) {
                        val i = r * 8 + c
                        val possible = selected >= 0 && moves.any { it.from == selected && it.to == i }
                        val background = when { i == selected -> Color(0xFFF2D15B); possible -> Color(0xFFBACA77); (r + c) % 2 == 0 -> light; else -> darkSquare }
                        Box(Modifier.weight(1f).fillMaxHeight().background(background).clickable {
                            if (review || result != null) return@clickable
                            if (selected >= 0 && possible) play(selected, i)
                            else if (Chess.side(game.board[i], game.whiteTurn) && (mode == "На двоих" || game.whiteTurn)) selected = i
                            else selected = -1
                        }, contentAlignment = Alignment.Center) {
                            Text(glyphs[game.board[i]] ?: "", color = if (Chess.white(game.board[i])) Color.White else Color(0xFF101010), fontSize = 32.sp, lineHeight = 36.sp)
                            if (c == 0) Text("${8 - r}", fontSize = 9.sp, color = if (r % 2 == 0) darkSquare else light, modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp))
                            if (r == 7) Text("${'a' + c}", fontSize = 9.sp, color = if (c % 2 == 0) light else darkSquare, modifier = Modifier.align(Alignment.BottomEnd).padding(end = 2.dp))
                        }
                    }
                }
            }
            if (result != null) Text(result, color = Color(0xFFF2D15B), fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { reset() }) { Text("Новая партия") }
                OutlinedButton(onClick = { if (game.undo()) { if (mode == "Бот") game.undo(); refresh() } }) { Text("↶ Назад") }
            }
            if (game.history.isNotEmpty()) {
                Text("Ходы", fontWeight = FontWeight.Bold)
                Text(game.history.chunked(2).mapIndexed { i, pair -> "${i + 1}. ${pair.joinToString("  ")}" }.joinToString("    "), fontSize = 13.sp)
                Button(onClick = { review = !review }) { Text(if (review) "Закрыть просмотр" else "Разбор партии (предпросмотр)") }
                if (review) Text("История ходов доступна выше. Точность, оценки движка, график преимущества и классификация ходов требуют интеграции Stockfish. Сейчас они не вычисляются.", fontSize = 13.sp)
            }
            Text("Версия 0.1 • офлайн • локальная игра", color = Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}
