package org.ycc.sordos

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppKineia(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold { innerPading ->
        NavHost(
            navController = navController,
            startDestination = Pantallas.Login.name,
            modifier = modifier.padding(innerPading)
        ) {
            composable(route = Pantallas.Login.name) {
                PantallaLogin { navController.navigate(it) }
            }
            composable(route = Pantallas.Menu.name) {
                PantallaMenu(
                    onClickCamara = { navController.navigate(Pantallas.Camara.name) },
                    onClickModelo = { navController.navigate(Pantallas.Modelo.name) }
                )
            }
            composable(route = Pantallas.Modelo.name) {
                SpeechToTextScreen(modifier = Modifier)
            }
            composable(route = Pantallas.Camara.name) {
                PantallaMenu(
                    onClickCamara = { navController.navigate(Pantallas.Camara.name) },
                    onClickModelo = { navController.navigate(Pantallas.Modelo.name) }
                )
            }
        }
    }
}