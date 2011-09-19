package net.slate.action

import javax.swing.{ JTextPane, AbstractAction }
import java.awt.event.ActionEvent

class CommentCodeAction(textPane: JTextPane) extends AbstractAction with LineParser {

  def actionPerformed(e: ActionEvent) = {
    val doc = textPane.getDocument;
    val selectionStart = textPane.getSelectionStart
    val end = textPane.getSelectionEnd
    var caret = selectionStart

    while (caret <= textPane.getSelectionEnd) {
      val l = line(textPane, caret)
      val start = startOfLine(textPane, caret)
      doc.remove(start, l.length)
      doc.insertString(start, "// " + l, null)
      caret += (l.length + 2)
    }
  }
}