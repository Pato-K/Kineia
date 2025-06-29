package org.ycc.sordos

import android.graphics.drawable.shapes.Shape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.ycc.sordos.ui.theme.BabyLilac

@Composable
fun PantallaMenu(
    onClickCamara: () -> Unit,
    onClickModelo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onClickCamara() },
            modifier = Modifier.size(width = 300.dp, height = 150.dp),
            colors = ButtonColors(
                containerColor = BabyLilac,
                contentColor = Color.White,
                disabledContainerColor = Color.Unspecified,
                disabledContentColor = Color.Unspecified
            ),
            border = BorderStroke(8.dp, color = Color.White),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Text(
                text = "Leer señas",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Button(
            onClick = { onClickModelo() },
            modifier = Modifier.size(width = 300.dp, height = 150.dp),
            colors = ButtonColors(
                containerColor = BabyLilac,
                contentColor = Color.White,
                disabledContainerColor = Color.Unspecified,
                disabledContentColor = Color.Unspecified
            ),
            border = BorderStroke(8.dp, color = Color.White),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Text(
                text = "Hablar",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
