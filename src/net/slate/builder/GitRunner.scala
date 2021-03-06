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
 *  Created on: 11th October 2011
 */
package net.slate.builder

import java.io.File

import edu.nyu.cs.javagit.api._
import edu.nyu.cs.javagit.api.commands._

/**
 * Enables execution of git related commands.
 *
 * @author Aishwarya Singhal
 */
class GitRunner(path: String) extends Runner {
  // path to the working tree
  private val workingTreePath = new File(path)
  private val wt = instance.getWorkingTree

  /**
   * Git init at the current path.
   */
  def init(remote: String) = {
    instance.init
    val readmePath = path + File.separator + "README"
    val readme = new File(readmePath)
    if (!readme.exists) readme.createNewFile
    println("created " + readmePath)
    createIgnoreFile
    wt.add
    println("added files")
    wt.commit("first commit")
    println("first commit done")
    addRemote(remote)
    println("Git init complete")
  }

  /**
   * Checks if there are any modified files at the current path.
   */
  def ? = {
    val gitStatus = new GitStatus
    val status = gitStatus.status(workingTreePath)

    (status.getModifiedFilesToCommitSize + status.getUntrackedFilesSize + status.getDeletedFilesToCommitSize) > 0
  }

  /**
   * Git add at the current path.
   */
  def + = {
    val gitStatus = new GitStatus
    val status = gitStatus.status(workingTreePath)
    val numberOfUntrackedFiles = status.getUntrackedFilesSize
    if (numberOfUntrackedFiles > 0) wt.add
  }

  /**
   * Git commit at the current path.
   */
  def commit(message: String) = wt.commit(message)

  /**
   * Git checkout at the current path.
   */
  def checkout = wt.checkout(wt.getCurrentBranch)

  /**
   * Git push in the project containing the current path.
   */
  def push = run("push")

  /**
   * Git pull in the project containing the current path.
   */
  def pull = run("pull")

  /**
   * execute a git command externally.
   *
   * @param command
   */
  private def run(command: String) {
    import net.slate.ExecutionContext
    val project = ExecutionContext.currentProjectName(path)
    executeCommand(List(GitRunner.git, command), project, "git", false)
  }

  /**
   * add git remote repository and push to master.
   *
   * @param remote
   */
  private def addRemote(remote: String) {
    import net.slate.ExecutionContext
    val project = ExecutionContext.currentProjectName(path)
    println("adding remote origin")
    println(project)
    executeCommand(List(GitRunner.git, "remote", "add", "origin", remote), project, "git", false)
    val p = ExecutionContext.runningProcess
    if (p != null) p.waitFor
    println("pushing origin to master")
    executeCommand(List(GitRunner.git, "push", "origin", "master"), project, "git", false)
  }

  /**
   * creates the .gitignore file to indicate what must not be added to git.
   */
  private def createIgnoreFile {
    import java.io.{ BufferedWriter, FileWriter }
    import net.slate.ExecutionContext
    val project = ExecutionContext.currentProjectName(path)
    val writer = new BufferedWriter(new FileWriter(path + File.separator + ".gitignore"))
    if (project == path) {
      writer.write(".slate\n")
      writer.write(settings(project)._2 + "\n")
    }
    writer.close()
  }

  //get the instance of the dotGit Object
  private def instance = DotGit.getInstance(path)
}

object GitRunner extends Runner {
  private val gitPath = configuration("git")

  // Make sure you have JavaGit added as an external jar in your project
  JavaGitConfiguration.setGitPath(gitPath)

  def git = {
    gitPath + File.separator + getGitExecutable
  }

  /**
   * gets the executable based on the operating system.
   *
   * @return
   */
  private def getGitExecutable = {
    if (System.getProperty("os.name").startsWith("Windows")) "git.exe" else "git"
  }
}