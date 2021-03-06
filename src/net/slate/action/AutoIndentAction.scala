/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on: 20th September 2011
 */
package net.slate.action;

import javax.swing.{ JTextPane, AbstractAction }
import java.awt.event.ActionEvent

import net.slate.Launch._

class AutoIndentAction extends AbstractAction with IndentText with LineParser {
  import net.slate.gui._

  def actionPerformed(e: ActionEvent) = {
    val indent = !checkPopups
    if (indent) {
      val textPane = currentScript.text.peer

      val doc = textPane.getDocument;
      val caret = textPane.getCaretPosition;
      val l = line(textPane, caret)

      val indentation = indentLine(l, true)
      doc.insertString(caret, indentation._1, null)

      if (l.trim.endsWith("{")) {
        val newCaret = textPane.getCaretPosition
        textPane.setCaretPosition(textPane.getCaretPosition - indentation._2)
      }
    }
  }

  private def checkPopups = {
    val open = CodeCompletionPopupMenu.isOpen || CodeSuggestionPopupMenu.isOpen || WordCompletionPopupMenu.isOpen
    if (CodeCompletionPopupMenu.isOpen) CodeCompletionPopupMenu.processor ! "execute"
    if (CodeSuggestionPopupMenu.isOpen) CodeSuggestionPopupMenu.processor ! "execute"
    if (WordCompletionPopupMenu.isOpen) WordCompletionPopupMenu.processor ! "execute"

    open
  }
}