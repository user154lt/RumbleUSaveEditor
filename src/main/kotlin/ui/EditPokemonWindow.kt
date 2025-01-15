package ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import data.Move
import data.MoveButton
import data.MoveType
import data.Pokemon
import ui.components.DefaultCard
import ui.components.EditorList
import ui.model.EditCallbacks
import ui.model.EditFilterCallbacks
import ui.model.EditFilters
import ui.model.EditPokemonViewModel


enum class VisibleControl {
    SPECIES,
    POWER,
    MOVE,
}

@Composable
fun EditPokemonWindow(
    onCloseRequest: () -> Unit,
    editPokemonViewModel: EditPokemonViewModel,
) {
    val viewModel = remember { editPokemonViewModel }
    val uiState = viewModel.uiState.collectAsState()

    Window(
        onCloseRequest = onCloseRequest,
        state = WindowState(
            size = DpSize(1200.dp, 800.dp)
        ),
        title = "Edit Pokémon",
        icon = painterResource("icon.png")
    ) {
        MaterialTheme(
            colors = defaultColors
        ) {
            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top
                ) {
                    PokemonToEditInfo(
                        modifier = Modifier.padding(6.dp),
                        setVisibleControl = viewModel::setVisibleControl,
                        setMoveControlVisible = viewModel::setMoveControlVisible,
                        pokemon = uiState.value.selected,
                    )
                    Divider(modifier = Modifier.height(6.dp))
                    EditControls(
                        modifier = Modifier.weight(1f),
                        visibleControl = uiState.value.visibleControl,
                        editCallbacks = viewModel.editCallbacks,
                        filterCallbacks = viewModel.filterCallbacks,
                        filters = uiState.value.filters,
                        powerModifier = uiState.value.selected.powerModifier,
                        pokemonList = uiState.value.pokemonList,
                        moveList = uiState.value.moves
                    )
                    ConfirmDiscardButtons(
                        confirm = viewModel::confirmEdits,
                        discard = viewModel::discardEdits,
                    )
                }
            }
        }
    }
}


@Composable
fun PokemonToEditInfo(
    setVisibleControl: (VisibleControl) -> Unit,
    setMoveControlVisible: (MoveButton) -> Unit,
    pokemon: Pokemon,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(12.dp),
    ) {
        SpeciesRow(
            setVisibleControl = setVisibleControl,
            pokemon = pokemon,
        )
        PowerAndMoveRow(
            setVisibleControl = setVisibleControl,
            setMoveControlVisible = setMoveControlVisible,
            pokemon = pokemon,
        )
    }
}

@Composable
fun SpeciesRow(
    setVisibleControl: (VisibleControl) -> Unit,
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SpeciesInfo(
            editSpecies = {
                setVisibleControl(VisibleControl.SPECIES)
            },
            pokemon = pokemon
        )
    }
}

@Composable
fun SpeciesInfo(
    editSpecies: () -> Unit,
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    DefaultCard(
        modifier = modifier,
        onClick = editSpecies,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(pokemon.spriteResource),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = pokemon.speciesName,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.h4.fontSize
                )
            )
        }
    }

}

@Composable
fun PowerAndMoveRow(
    setVisibleControl: (VisibleControl) -> Unit,
    setMoveControlVisible: (MoveButton) -> Unit,
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PowerInfo(
            editPower = {
                setVisibleControl(VisibleControl.POWER)
            },
            powerModifier = pokemon.powerModifier
        )
        MoveInfo(
            modifier = modifier,
            editMove = {
                setMoveControlVisible(MoveButton.A_BTN)
            },
            move = pokemon.aMove,
            button = MoveButton.A_BTN
        )
        MoveInfo(
            modifier = modifier,
            editMove = {
                setMoveControlVisible(MoveButton.B_BTN)
            },
            move = pokemon.bMove,
            button = MoveButton.B_BTN
        )
    }

}


@Composable
fun PowerInfo(
    editPower: () -> Unit,
    powerModifier: Int,
    modifier: Modifier = Modifier,
) {
    DefaultCard(
        modifier = modifier,
        onClick = editPower
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Text(
                text = "Power modifier: $powerModifier"
            )
        }
    }
}


@Composable
fun MoveInfo(
    editMove: () -> Unit,
    move: Move,
    button: MoveButton,
    modifier: Modifier = Modifier,
) {
    DefaultCard(
        modifier = modifier,
        onClick = editMove
    ) {
        Column(
            modifier = Modifier.wrapContentSize()
                .padding(12.dp),
        ) {
            Row(
            ) {
                Image(
                    painter = painterResource(button.iconResource()),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = move.name
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
            ) {
                Text(
                    text = "Rating: ${move.rating}"
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Type: ${move.type.typeName}"
                )
            }
        }
    }
}

@Composable
fun EditControls(
    visibleControl: VisibleControl,
    editCallbacks: EditCallbacks, filterCallbacks: EditFilterCallbacks,
    filters: EditFilters,
    powerModifier: Int,
    pokemonList: List<Pokemon>,
    moveList: List<Move>,
    modifier: Modifier = Modifier,
) {
    when (visibleControl) {
        VisibleControl.SPECIES -> SelectSpeciesControl(
            modifier = modifier,
            selectSpecies = editCallbacks.editSpecies,
            updateNameFilter = filterCallbacks.updatePokemonNameFilter,
            nameFilter = filters.pokemonName,
            pokemonList = pokemonList,
        )

        VisibleControl.POWER -> SelectPowerControl(
            modifier = modifier,
            selectPowerString = editCallbacks.editPowerString,
            selectPowerFloat = editCallbacks.editPowerFloat,
            power = powerModifier,
        )

        VisibleControl.MOVE -> SelectMoveControl(
            modifier = modifier,
            filterCallbacks = filterCallbacks,
            filters = filters,
            selectMove = editCallbacks.editMove,
            moveList = moveList,
        )
    }
}


@Composable
fun SelectSpeciesControl(
    selectSpecies: (Pokemon) -> Unit,
    updateNameFilter: (String) -> Unit,
    nameFilter: String,
    pokemonList: List<Pokemon>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        NameFilterRow(
            updateNameFilter = updateNameFilter,
            nameFilter = nameFilter,
        )
        SpeciesList(
            selectSpecies = selectSpecies,
            pokemonList = pokemonList
        )
    }
}

@Composable
fun NameFilterRow(
    updateNameFilter: (String) -> Unit,
    nameFilter: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(6.dp)
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = nameFilter,
            onValueChange = { updateNameFilter(it) },
            label = {
                Text(
                    text = "Filter by name"
                )
            }
        )
    }
}

@Composable
fun SpeciesList(
    selectSpecies: (Pokemon) -> Unit,
    pokemonList: List<Pokemon>,
    modifier: Modifier = Modifier
) {
    EditorList(
        modifier = modifier
    ) {
        items(pokemonList) {
            SpeciesItem(
                selectPokemon = { selectSpecies(it) },
                pokemon = it
            )
        }
    }
}

@Composable
fun SpeciesItem(
    selectPokemon: () -> Unit,
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    DefaultCard(
        modifier = modifier,
        onClick = selectPokemon
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(pokemon.spriteResource),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = pokemon.speciesName
            )
        }
    }
}

@Composable
fun SelectPowerControl(
    selectPowerString: (String) -> Unit,
    selectPowerFloat: (Float) -> Unit,
    power: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
            .padding(6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = "$power",
            onValueChange = selectPowerString,
            label = {
                Text(
                    text = "Power modifier"
                )
            }
        )
        Row(
            modifier = modifier.wrapContentSize()
        ) {
            Text(
                text = "1"
            )
            Slider(
                modifier = Modifier.weight(1f),
                value = power / 65535f,
                onValueChange = selectPowerFloat
            )
            Text(
                text = "65535"
            )
        }
    }
}


@Composable
fun SelectMoveControl(
    filterCallbacks: EditFilterCallbacks,
    filters: EditFilters,
    selectMove: (Move) -> Unit,
    moveList: List<Move>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        MoveFilterRow(
            filterCallbacks = filterCallbacks,
            filters = filters,
        )
        MoveList(
            selectMove = selectMove,
            moveList = moveList
        )
    }
}

@Composable
fun MoveFilterRow(
    filterCallbacks: EditFilterCallbacks,
    filters: EditFilters,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        NameFilterRow(
            updateNameFilter = filterCallbacks.updateMoveNameFilter,
            nameFilter = filters.moveName
        )
        AdditionalMoveFilters(
            filterCallbacks = filterCallbacks,
            filters = filters,
        )
    }
}

@Composable
fun AdditionalMoveFilters(
    filterCallbacks: EditFilterCallbacks,
    filters: EditFilters,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        AdditionalFilterRow(
            toggleFilterVisible = filterCallbacks.toggleRatingVisible,
            isFilterVisible = filters.isRatingVisible,
            label = "Rating"
        ) {
            RatingFilters(
                filterList = filters.ratings,
                toggleRatingFilter = filterCallbacks.toggleRatingFilter
            )
        }
        AdditionalFilterRow(
            toggleFilterVisible = filterCallbacks.toggleTypeVisible,
            isFilterVisible = filters.isTypesVisible,
            label = "Types"
        ) {
            TypeFilters(
                toggleTypeFilter = filterCallbacks.toggleTypeFilter,
                filterMap = filters.types
            )

        }
    }
}


@Composable
fun AdditionalFilterRow(
    toggleFilterVisible: () -> Unit,
    isFilterVisible: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.animateContentSize(),
    ) {
        AdditionalFilterLabel(
            toggleFilterVisible = toggleFilterVisible,
            isFilterVisible = isFilterVisible,
            label = label
        )
        if (isFilterVisible) content()
    }
}

@Composable
fun AdditionalFilterLabel(
    toggleFilterVisible: () -> Unit,
    isFilterVisible: Boolean,
    label: String,
    modifier: Modifier = Modifier
) {
    val icon = remember(isFilterVisible) {
        if (isFilterVisible) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = toggleFilterVisible
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.caption
        )
    }
}


@Composable
fun RatingFilters(
    filterList: List<Boolean>,
    toggleRatingFilter: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 0..5) {
            Text(
                text = "$i:"
            )
            Checkbox(
                checked = filterList[i],
                onCheckedChange = {
                    toggleRatingFilter(i)
                }
            )
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TypeFilters(
    toggleTypeFilter: (MoveType) -> Unit,
    filterMap: Map<MoveType, Boolean>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth()
            .padding(6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
    ) {
        MoveType.entries.forEach {
            TypeCheckbox(
                name = it.typeName,
                checked = filterMap[it]!!,
                onCheckedChange = { toggleTypeFilter(it) }
            )
        }
    }
}

@Composable
fun TypeCheckbox(
    name: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$name:")
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange() }
        )
    }
}

@Composable
fun MoveList(
    selectMove: (Move) -> Unit,
    moveList: List<Move>,
    modifier: Modifier = Modifier
) {
    EditorList(
        modifier = modifier,
    ) {
        items(moveList) {
            MoveItem(
                selectMove = { selectMove(it) },
                move = it,
            )
        }
    }
}

@Composable
fun MoveItem(
    selectMove: () -> Unit,
    move: Move,
    modifier: Modifier = Modifier
) {
    DefaultCard(
        modifier = modifier,
        onClick = selectMove,
    ) {
        Column(
            modifier.fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = move.name,
            )
            Text(
                text = "Rating: ${move.rating}"
            )
            Text(
                text = "Type: ${move.type.typeName}"
            )
        }
    }
}

@Composable
fun ConfirmDiscardButtons(
    confirm: () -> Unit,
    discard: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = discard
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = null,
            )
            Text(text = "Discard edits")
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = confirm
        ) {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = null,
            )
            Text(text = "Confirm edits")
        }
    }
}