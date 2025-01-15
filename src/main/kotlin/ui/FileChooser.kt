package ui

import java.io.File
import javax.swing.JFileChooser

class FileChooser {
    fun showFileChooser() : File? {
        val fileChooser = JFileChooser("/").apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Choose 00slot00 folder"
        }
        fileChooser.showOpenDialog(null)
        fileChooser.selectedFile
        return fileChooser.selectedFile
    }
}