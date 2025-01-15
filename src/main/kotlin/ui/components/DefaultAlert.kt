package ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.DialogWindow


@Composable
fun DefaultAlert(
    onCloseRequest: () -> Unit,
    message: String,
    modifier: Modifier = Modifier,
) {
    DialogWindow(
        onCloseRequest = onCloseRequest,
        state = DialogState(
            size = DpSize(400.dp, 200.dp)
        ),
        title = "Information",
    ) {
        Surface(
            modifier = modifier,
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(6.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onCloseRequest,
                ){
                    Text(text = "Ok!")
                }
            }
        }
    }
}