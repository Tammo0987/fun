package com.github.tammo.diagnostics

object CompilerErrorRenderer {

  // ANSI escape codes
  private val RESET = "\u001B[0m"
  private val BOLD = "\u001B[1m"
  private val UNDERLINE = "\u001B[4m"

  private val FG_BLACK = "\u001B[30m"
  private val FG_RED = "\u001B[31m"
  private val FG_GREEN = "\u001B[32m"
  private val FG_YELLOW = "\u001B[33m"
  private val FG_BLUE = "\u001B[34m"
  private val FG_MAGENTA = "\u001B[35m"
  private val FG_CYAN = "\u001B[36m"
  private val FG_WHITE = "\u001B[37m"

  def render(
      error: CompilerError,
      source: SourceFile
  ): String =
    s"[${renderLevel(error.level)}] ${error.message}$RESET" + "\n" +
      s"Source file location: ${source.id}" + "\n" +
      renderSourceCode(source, PositionSpan(source.id, 47, 120))

  private def renderSourceCode(
      source: SourceFile,
      position: PositionSpan
  ): String = {
    val lines = source.content.split("\n")
    val (startLine, startColumn) = source.toLineColumn(position.startOffset)
    val (endLine, endColumn) = source.toLineColumn(position.endOffset)

    println(s"startLine: $startLine, startColumn: $startColumn")
    println(s"endLine: $endLine, endColumn: $endColumn")

    val start =
      Math.max(startLine - 3, 0) // Lines are 1-based but array is 0-based
    val end = Math.min(endLine + 2, lines.length)
    lines
      .slice(start, end)
      .zipWithIndex
      .map { case (lineContent, lineIndex) =>
        val lineNumber = start + lineIndex + 1
        if (lineNumber == startLine) {
          f"$BOLD>$RESET $lineNumber%4d | ${lineContent.zipWithIndex.map { case (c, idx) =>
              if (idx + 1 == startColumn) {
                s"$UNDERLINE$c"
              } else if (lineNumber == endLine && idx + 1 == endColumn) {
                s"$c$RESET"
              } else {
                c
              }
            }.mkString}$RESET"
        } else if (lineNumber > startLine && lineNumber < endLine) {
          f"$BOLD>$RESET $lineNumber%4d | $UNDERLINE$lineContent$RESET"
        } else if (lineNumber == endLine) {
          f"$BOLD>$RESET $lineNumber%4d | ${lineContent.zipWithIndex.map { case (c, idx) =>
              if (idx == 0) {
                s"$UNDERLINE$c"
              } else if (idx + 1 == endColumn) {
                s"$c$RESET"
              } else {
                c
              }
            }.mkString}"
        } else {
          f"  $lineNumber%4d | $lineContent"
        }
      }
      .mkString("\n")
  }

  private def renderLevel(level: Level): String = level match {
    case Level.Fatal   => FG_RED + "FATAL" + RESET
    case Level.Error   => FG_RED + "ERROR" + RESET
    case Level.Warning => FG_YELLOW + "WARNING" + RESET
    case Level.Info    => FG_GREEN + "INFO" + RESET
  }

}
