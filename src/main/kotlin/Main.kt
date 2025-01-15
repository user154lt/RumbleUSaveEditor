import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.window.application
import com.formdev.flatlaf.FlatDarkLaf
import data.PokemonRepository
import ui.EditPokemonWindow
import ui.MainWindow
import ui.model.ApplicationViewModel
import ui.model.EditPokemonViewModel
import ui.model.FileViewModel
import javax.swing.UIManager


fun main() = application {
    val applicationModel = remember{ ApplicationViewModel() }
    val applicationUIState = applicationModel.applicationUIState.collectAsState()
    val pokemonRepository = remember{ PokemonRepository.getInstance() }
    UIManager.setLookAndFeel(FlatDarkLaf())
    MainWindow(
        onCloseRequest = ::exitApplication,
        fileViewModel =  FileViewModel(
            pokemonRepository = pokemonRepository,
            selectForEditing = applicationModel::openEditWindowWith
        )
    )
    if(applicationUIState.value.isEditWindowShowing){
        EditPokemonWindow(
            onCloseRequest = applicationModel::closeEditWindow,
            editPokemonViewModel =
                EditPokemonViewModel(
                    pokemonRepository = pokemonRepository,
                    closeWindow =  applicationModel::closeEditWindow,
                    selected = applicationUIState.value.pokemonForEditing
                )
        )

    }
}