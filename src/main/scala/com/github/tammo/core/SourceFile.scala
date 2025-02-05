package com.github.tammo.core

import java.util

case class SourceFile(id: String, content: String) {

  private lazy val lineOffsets: Array[Int] = (
    0 +: content.indices.filter(i => content.charAt(i) == '\n').map(_ + 1)
  ).toArray

  def toLineColumn(offset: Int): (Int, Int) = {
    val lineIndex = util.Arrays.binarySearch(lineOffsets, offset)
    val insertionPoint =
      if lineIndex < 0 then -lineIndex - 2 else lineIndex

    val lineStartOffset = lineOffsets(Math.max(0, insertionPoint))
    val lineNumber = insertionPoint + 1 // 1-based line
    val columnNumber = offset - lineStartOffset + 1 // 1-based column

    (lineNumber, columnNumber)
  }

}
