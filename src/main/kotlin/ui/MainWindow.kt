package ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import data.Move
import data.MoveButton
import data.Pokemon
import ui.components.DefaultAlert
import ui.components.DefaultCard
import ui.model.FileViewModel


@Composable
fun MainWindow(
    onCloseRequest: () -> Unit,
    fileViewModel: FileViewModel,
) {
    val viewModel = remember{ fileViewModel }
    val uiState = viewModel.uiState.collectAsState()
    val pokemonList = viewModel.pokemonList.collectAsState()

    Window(
        onCloseRequest = onCloseRequest,
        title = "Main Window",
        icon = painterResource("icon.png")
    ) {
        MaterialTheme(
            colors = defaultColors        ) {
            if (uiState.value.message != null) {
                DefaultAlert(
                    onCloseRequest = viewModel::dismissDialog,
                    message = uiState.value.message!!
                )
            }
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                        .padding(24.dp)
                ) {
                    SelectFolder(
                        selectPath = viewModel::chooseFolder,
                        compress = viewModel::compressFile,
                        decompress = viewModel::decompressFiles,
                        path = uiState.value.folderPath,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SavedPokemonList(
                        list = pokemonList.value,
                        selectForEditing = viewModel.selectForEditing
                    )
                }
            }
        }

    }
}

@Composable
fun SelectFolder(
    selectPath: () -> Unit,
    compress: () -> Unit,
    decompress: () -> Unit,
    path: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colors.primary
                ),
                shape = RoundedCornerShape(5.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        FolderTextField(path = path)
        Spacer(modifier = Modifier.height(6.dp))
        FolderActionButtons(
            selectPath = selectPath,
            compress = compress,
            decompress = decompress,
        )
    }
}

@Composable
fun FolderTextField(
    path: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = path,
        readOnly = true,
        onValueChange = {},
        modifier = modifier,
        label = { Text(text = "00slot00 folder") }
    )
}

@Composable
fun FolderActionButtons(
    selectPath: () -> Unit,
    compress: () -> Unit,
    decompress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
    ) {
        Button(
            onClick = selectPath
        ) {
            Text(text = "Select folder")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = decompress
        ) {
            Text(text = "Decompress")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = compress
        ) {
            Text(text = "Compress")
        }
    }
}

@Composable
fun SavedPokemonList(
    list: List<Pokemon>,
    selectForEditing: (Pokemon) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState
        ) {
            items(list) {
                PokemonCard(
                    modifier = Modifier.padding(12.dp),
                    onClick = { selectForEditing(it) },
                    pokemon = it
                )
            }
        }
        VerticalScrollbar(
            modifier = modifier.align(Alignment.CenterEnd)
                .matchParentSize(),
            adapter = rememberScrollbarAdapter(
                scrollState = listState
            )
        )
    }
}

@Composable
fun PokemonCard(
    onClick: () -> Unit,
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    DefaultCard(
        modifier = modifier,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(pokemon.spriteResource),
                contentDescription = pokemon.speciesName
            )
            Spacer(modifier = modifier.width(12.dp))
            PokemonBasicData(
                pokemon = pokemon
            )
            Spacer(modifier = modifier.width(12.dp))
            PokemonMoveData(
                pokemon = pokemon
            )
        }
    }

}

@Composable
fun PokemonBasicData(
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.wrapContentSize(),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        Text(
            text = pokemon.speciesName
        )
        Text(
            text = "Power modifier: ${pokemon.powerModifier}"
        )

    }
}


@Composable
fun PokemonMoveData(
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        MoveData(
            moveButton = MoveButton.A_BTN,
            move = pokemon.aMove
        )
        MoveData(
            moveButton = MoveButton.B_BTN,
            move = pokemon.bMove
        )
    }
}

@Composable
fun MoveData(
    moveButton: MoveButton,
    move: Move,
    modifier: Modifier = Modifier,
) {
    val sprite = remember { if (moveButton == MoveButton.A_BTN) "btn_A.png" else "btn_B.png" }
    Row(
        modifier = modifier.padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(sprite),
            contentDescription = null,
            modifier = Modifier.size(25.dp)
        )
        MoveTextData(move = move)
    }
}

@Composable
fun MoveTextData(
    move: Move,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(6.dp),
    ) {
        Text(
            text = move.name
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = move.type.typeName
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Rating: ${move.rating}"
        )
    }
}

