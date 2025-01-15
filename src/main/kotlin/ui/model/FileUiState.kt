package ui.model

import java.io.File

data class FileUiState(
    val folder: File? = null,
    val folderPath: String = "No folder selected",
    val message: String? = null,
)
