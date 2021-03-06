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
package net.slate.gui

import java.io.File
import javax.swing.{ ImageIcon, JFileChooser, JOptionPane, JToolBar, KeyStroke }
import scala.swing._
import net.slate.Launch

class ToolBar(title: String) extends Component with SequentialContainer.Wrapper {

  override lazy val peer: JToolBar = new JToolBar(title)

  def add(action: Action) { peer.add(action.peer) }

  def add(component: Component) { peer.add(component.peer) }
}

class NavigationToolBar extends ToolBar("Navigation") {
  import net.slate.Launch._

  val component = this

  abstract class ToolAction(title: String, iconPath: String, keys: String = null) extends Action(title) {
    icon = new ImageIcon(iconPath)
    tooltip = title
    peer.putValue(javax.swing.Action.SHORT_DESCRIPTION, title)
    if (keys != null) accelerator = getAccelerator(keys)
  }

  add(new ToolAction("New Project", "images/newjprj_wiz.gif", "control shift N") {
    lazy val newPrjDialog = new NewProjectDialog(top)

    def apply() {
      newPrjDialog.display
    }
  })

  add(new ToolAction("Open Project", "images/open.gif", "control shift P") {

    def apply() {
      val chooser = ProjectOpener.chooser
      val returnVal = chooser.showOpenDialog(component)
      if (null != chooser.selectedFile) top.fileExplorer.openProject(chooser.selectedFile)
    }
  })

  add(new ToolAction("Clear Console", "images/clear.gif", "control E") {

    def apply() {
      outputPane.pane.text = ""
      top.outputFrame.outputPane.pane.text = ""
    }
  })

  add(new ToolAction("Run", "images/run.gif", "control F11") {

    def apply() {
      runDialog.display
    }
  })

  add(new ToolAction("Stop", "images/stop.gif") {
    import net.slate.ExecutionContext._

    def apply() {
      if (runningProcess != null) {
        stop
        outputPane.pane.text += "Stopped running process"
      }
    }
  })

  add(new ToolAction("Add Task", "images/Add.png") {
    import net.slate.ExecutionContext._

    def apply() {
      val summary = JOptionPane.showInputDialog(
        top.peer,
        "Please enter a summary of the task.",
        "New Task",
        JOptionPane.PLAIN_MESSAGE,
        null,
        null,
        null) match { case x: String => x case _ => null }

      if (summary != null && summary.trim != "") {
        bottomTabPane.tasks.add(summary, summary, currentProjectName)
        bottomTabPane.selection.index = 3
      }
    }
  })

  add(new ToolAction("Delete Task", "images/completed.png") {
    def apply() {
      bottomTabPane.tasks.delete
    }
  })

  private def getAccelerator(acce: String) = {
    import scala.util.Properties._

    val shortcut = if (isMac && acce.contains("control"))
      acce.replace("control", "meta")
    else acce

    Some(KeyStroke.getKeyStroke(shortcut))
  }
}

object ProjectOpener {
  val chooser = new FileChooser(new File("."))
  chooser.peer.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
}