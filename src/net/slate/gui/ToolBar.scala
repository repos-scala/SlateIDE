package net.slate.gui

import java.io.File
import javax.swing.{ JFileChooser, JToolBar, KeyStroke }
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

  add(new Action("New Project") {
    lazy val newPrjDialog = new NewProjectDialog(top)
    icon = new javax.swing.ImageIcon("images/newjprj_wiz.gif")
    tooltip = title
    accelerator = getAccelerator("control shift N")

    def apply() {
      newPrjDialog.display
    }
  })

  add(new Action("Open Project") {
    icon = new javax.swing.ImageIcon("images/open.gif")
    tooltip = title
    accelerator = getAccelerator("control shift O")

    def apply() {
      val chooser = ProjectOpener.chooser
      val returnVal = chooser.showOpenDialog(component)
      if (null != chooser.selectedFile) top.fileExplorer.openProject(chooser.selectedFile)
    }
  })

  add(new Action("Clear Console") {
    icon = new javax.swing.ImageIcon("images/clear.gif")
    tooltip = title
    accelerator = getAccelerator("control E")

    def apply() {
      outputPane.pane.text = ""
    }
  })

  add(new Action("Run") {
    icon = new javax.swing.ImageIcon("images/run.gif")
    tooltip = title
    accelerator = getAccelerator("control F11")

    def apply() {
      runDialog.display
    }
  })

  add(new Action("Stop") {
    import net.slate.ExecutionContext._

    icon = new javax.swing.ImageIcon("images/stop.gif")
    tooltip = title

    def apply() {
      if (runningProcess != null) {
        stop
        outputPane.pane.text += "Stopped running process"
      }
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