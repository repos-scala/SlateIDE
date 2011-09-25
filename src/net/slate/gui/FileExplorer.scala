package net.slate.gui

import java.io.File
import javax.swing.{ BorderFactory, ImageIcon, JTree }
import javax.swing.event.{ TreeSelectionEvent, TreeSelectionListener }
import javax.swing.tree.{ DefaultMutableTreeNode, DefaultTreeCellRenderer, DefaultTreeModel, TreeCellRenderer }
import scala.swing._
import scala.swing.event._

import net.slate.{ ExecutionContext, Launch }
import net.slate.builder.ProjectConfigurator

class FileExplorer(dir: File) extends ScrollPane {
  import Launch._
  import net.slate.editor.tools.TypeIndexer
  import net.slate.util.FileUtils

  val customLeafIcon = new ImageIcon("images/file.png")

  val top = new DefaultMutableTreeNode(new File("."))

  val tree = new JTree(addNodes(top, dir))

  val projectTreeMenu = new ProjectTreeMenu

  // Add a listener
  tree.addMouseListener(new java.awt.event.MouseAdapter {
    override def mousePressed(e: java.awt.event.MouseEvent) {
      val treePath = tree.getPathForLocation(e.getX(), e.getY())
      if (treePath != null) {
        if (e.getButton == java.awt.event.MouseEvent.BUTTON1 && e.getClickCount() == 2) {
          val node = treePath.getLastPathComponent().asInstanceOf[DefaultMutableTreeNode]

          node.getUserObject match {
            case fileNode: FileNode =>
              if (!fileNode.isDirectory && FileUtils.open(fileNode.name, fileNode.path)) {
              }
            case _ =>
          }
        } else if (e.getButton == java.awt.event.MouseEvent.BUTTON3) {

          val node = tree.getPathForLocation(e.getX(), e.getY()).getLastPathComponent().asInstanceOf[DefaultMutableTreeNode]
          val row = tree.getRowForLocation(e.getX(), e.getY())

          node.getUserObject match {
            case fileNode: FileNode =>
              if (fileNode.isDirectory) {
                projectTreeMenu.path = fileNode.path
                projectTreeMenu.nodeRow = row
                projectTreeMenu.show(Launch.top.fileExplorer, e.getX(), e.getY())
              }
            case _ =>
          }
        }
      }
    }
  })

  tree.setRootVisible(false)

  val renderer = new ProjectTreeCellRenderer();
  renderer.setLeafIcon(customLeafIcon);

  tree.setCellRenderer(renderer)
  tree.setBorder(BorderFactory.createEmptyBorder(10, 10, 220, 420))
  peer.getViewport().add(tree)

  openProjects

  preferredSize = new Dimension(200, 400)

  def addNodes(curTop: DefaultMutableTreeNode, dir: File): DefaultMutableTreeNode = {
    if (dir != null) {
      val curPath = dir.getPath()
      var pathname = curPath
      if (curPath.contains(File.separator)) {
        pathname = pathname.substring(pathname.lastIndexOf(File.separator) + 1)
      }
      val curDir = new DefaultMutableTreeNode(new FileNode(pathname, curPath, true))
      if (curTop != null) { // should only be null at root
        curTop.add(curDir);
      }

      val ol = List(dir.list(): _*).sort(_.toUpperCase < _.toUpperCase)

      var files = List[FileNode]()
      // Make two passes, one for Dirs and one for Files. This is #1.
      ol foreach { file =>
        if (file != ".slate") {
          val newPath = if (curPath.equals(".")) file else (curPath + File.separator + file)
          val f = new File(newPath)

          if (f.isDirectory()) addNodes(curDir, f) else files :::= List(new FileNode(file, newPath))
        }
      }

      // Pass two: for files.
      files.sortWith(_.compareTo(_) < 0).foreach { file => curDir.add(new DefaultMutableTreeNode(file)) }

      if (curTop != null) curTop else curDir
    } else {
      curTop
    }
  }

  def addNewNode(targetNodeRow: Int, name: String, path: String, isDirectory: Boolean) {
    val node = new DefaultMutableTreeNode(new FileNode(name, path, isDirectory))
    val parent = tree.getPathForRow(targetNodeRow).getLastPathComponent().asInstanceOf[DefaultMutableTreeNode]
    tree.getModel.asInstanceOf[DefaultTreeModel].insertNodeInto(node, parent, parent.getChildCount())
  }

  def openProject(project: File, persist: Boolean = true) = {
    if (project != null && project.exists) {
      tree.setModel(new DefaultTreeModel(addNodes(top, project)))
      ProjectConfigurator.init(project)
      new TypeIndexer(project.getAbsolutePath).index

      ExecutionContext.loadedProjects :::= List(project.getPath)

      if (persist)
        ProjectDetailsSerializer.write(ProjectDetailsSerializer.read ::: List(new ProjectDetails(project.getPath, true)))
    } else {
      ProjectDetailsSerializer.write(ProjectDetailsSerializer.read.remove(p => p.path == project.getPath))
    }
  }

  def openProjects = {
    ProjectDetailsSerializer.read.foreach { details =>
      if (details.open) openProject(new File(details.path), false)
    }
  }

  case class FileNode(val name: String, val path: String, val isDirectory: Boolean = false) {

    override def toString = { name }

    def compareTo(other: FileNode) = { name.toUpperCase.compareTo(other.name.toUpperCase) }
  }

  class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {

    override def getTreeCellRendererComponent(tree: JTree, value: AnyRef, selected: Boolean,
      expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) = {
      var isLeaf = leaf

      value match {
        case v: DefaultMutableTreeNode =>
          v.getUserObject match {
            case file: FileNode => if (new File(file.path).isDirectory) { isLeaf = false; }
            case file: File => if (file.isDirectory) { isLeaf = false; }
            case _ =>
          }
      }
      super.getTreeCellRendererComponent(tree, value, selected, expanded, isLeaf, row, hasFocus)
    }

  }
}

@scala.serializable
class ProjectDetails(val path: String, val open: Boolean) {
  val serialVersionUID = 1L
}

object ProjectDetailsSerializer {
  import java.io._

  val storeName = "projectDetails.ser"

  def write(data: List[ProjectDetails]) {
    val file = new File(storeName)
    if (file.exists) { file.delete }
    val store = new ObjectOutputStream(new FileOutputStream(storeName))
    store.writeObject(data)
    store.close
  }

  def read = {
    if (new File(storeName).exists) {
      val in = new ObjectInputStream(new FileInputStream(storeName))
      val details = in.readObject().asInstanceOf[List[ProjectDetails]]
      in.close()
      details
    } else {
      List[ProjectDetails]()
    }
  }
}
