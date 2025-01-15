package ui.model

import data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ui.VisibleControl

class EditPokemonViewModel(
    private val pokemonRepository: PokemonRepository,
    private val closeWindow: () -> Unit,
    selected: Pokemon = Pokemon()
) {

    private val _uiState = MutableStateFlow(
        EditPokemonUIState(
            selected = selected,
            pokemonList = pokemonRepository.fullPokemonList(),
            moves = pokemonRepository.fullMoveList(),
        )
    )
    val uiState = _uiState.asStateFlow()

    private val mainScope by lazy {
        CoroutineScope(Dispatchers.Main)
    }


    val editCallbacks = EditCallbacks(
        editSpecies = ::editSpecies,
        editPowerFloat = ::editPowerFloat,
        editPowerString = ::editPowerString,
    )

    val filterCallbacks = EditFilterCallbacks(
        updatePokemonNameFilter = ::updatePokemonNameFilter,
        updateMoveNameFilter = ::updateMoveNameFilter,
        toggleRatingVisible = ::toggleRatingFilterVisible,
        toggleRatingFilter = ::toggleRatingFilterInt,
        toggleTypeVisible = ::toggleTypeFilterVisible,
        toggleTypeFilter = ::toggleTypeFilter,
    )


    fun setVisibleControl(
        visibleControl: VisibleControl,
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            _uiState.update {
                it.copy(
                    visibleControl = visibleControl,
                    filters = EditFilters(),
                    pokemonList = pokemonRepository.fullPokemonList()
                )
            }
        }
    }

    fun setMoveControlVisible(button: MoveButton) {
        CoroutineScope(Dispatchers.Main).launch {
            editCallbacks.editMove = { editMove(it, button) }
            _uiState.update {
                it.copy(
                    moveToEdit = button,
                    visibleControl = VisibleControl.MOVE,
                    moves = pokemonRepository.fullMoveList(),
                    filters = EditFilters(),
                )
            }
        }
    }


    fun confirmEdits() =
        CoroutineScope(Dispatchers.Default).launch {
            pokemonRepository.updatePokemon(uiState.value.selected)
            closeWindow()
        }

    fun discardEdits() {
        closeWindow()
    }

    private fun editSpecies(pokemon: Pokemon) =
        mainScope.launch {
            val newPokemon = uiState.value.selected.copy(
                speciesName = pokemon.speciesName,
                spriteResource = pokemon.spriteResource,
                species = pokemon.species
            )
            updateSelected(newPokemon)
        }

    private fun editPowerFloat(power: Float) =
        mainScope.launch {
            val newPokemon = uiState.value.selected.copy(
                powerModifier = (power * 65535)
                    .toInt()
                    .coerceIn(1..65535)
            )
            updateSelected(newPokemon)
        }


    private fun editPowerString(power: String) =
        mainScope.launch {
            power.toIntOrNull()?.let {
                val newPokemon = uiState.value.selected.copy(
                    powerModifier = it.coerceIn(1..65535)
                )
                updateSelected(newPokemon)
            }
        }


    private fun editMove(move: Move, button: MoveButton) =
        mainScope.launch {
            val newPokemon =
                when (button) {
                    MoveButton.A_BTN -> uiState.value.selected.copy(
                        aMove = move
                    )

                    MoveButton.B_BTN -> uiState.value.selected.copy(
                        bMove = move
                    )
                }
            updateSelected(newPokemon)
        }

    private fun updateSelected(pokemon: Pokemon) =
        _uiState.update {
            it.copy(
                selected = pokemon
            )
        }

    private fun updatePokemonNameFilter(name: String) =
        mainScope.launch {
            val newFilters = uiState.value.filters.copy(
                pokemonName = name
            )
            updatePokemonFilters(newFilters)
        }

    private fun updatePokemonFilters(filters: EditFilters) {
        _uiState.update {
            it.copy(
                filters = filters
            )
        }
        filterPokemon()
    }

    private fun filterPokemon() {
        var pokemon = pokemonRepository.fullPokemonList()
        val filter = uiState.value.filters.pokemonName
        if (filter != "") {
            pokemon = pokemon.filter { it.speciesName.contains(filter, true) }
        }
        updatePokemonList(pokemon)
    }

    private fun updatePokemonList(list: List<Pokemon>) =
        _uiState.update {
            it.copy(
                pokemonList = list
            )
        }

    private fun updateMoveNameFilter(name: String) =
        mainScope.launch {
            val newFilters = uiState.value.filters.copy(
                moveName = name
            )
            updateMoveFilters(newFilters)
        }

    private fun toggleRatingFilterVisible() =
        mainScope.launch {
            val newFilters = uiState.value.filters.copy(
                isRatingVisible = !uiState.value.filters.isRatingVisible
            )
            updateMoveFilters(newFilters)
        }

    private fun toggleRatingFilterInt(rating: Int) =
        mainScope.launch {
            val newRatings = uiState.value.filters.ratings.toMutableList().apply {
                this[rating] = !this[rating]
            }
            val newFilters = uiState.value.filters.copy(
                ratings = newRatings
            )
            updateMoveFilters(newFilters)
        }

    private fun toggleTypeFilterVisible() =
        mainScope.launch {
            val newFilters = uiState.value.filters.copy(
                isTypesVisible = !uiState.value.filters.isTypesVisible
            )
            updateMoveFilters(newFilters)
        }

    private fun toggleTypeFilter(type: MoveType) =
        mainScope.launch {
            val newTypes = uiState.value.filters.types.toMutableMap().apply {
                this[type] = !this[type]!!
            }
            val newFilters = uiState.value.filters.copy(
                types = newTypes
            )
            updateMoveFilters(newFilters)
        }

    private fun updateMoveFilters(filters: EditFilters) {
        _uiState.update{
            it.copy(
                filters = filters
            )
        }
        filterMoves()
    }


    private fun filterMoves() {
        var moves = pokemonRepository.fullMoveList()
        val filters = uiState.value.filters
        if (filters.moveName != "") moves = moves.filter { it.name.contains(filters.moveName, true) }
        if (!filters.ratings.all { !it }) moves = moves.filter { filters.ratings[it.rating] }
        if (!filters.types.all { !it.value }) moves = moves.filter { filters.types[it.type]!! }
        updateMovesList(moves)
    }

    private fun updateMovesList(list: List<Move>) =
        _uiState.update {
            it.copy(
                moves = list
            )
        }
}