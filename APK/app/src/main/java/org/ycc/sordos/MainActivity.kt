package org.ycc.sordos

//import org.ycc.sordos.ui.theme.SignToTextScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.ycc.sordos.ui.theme.SordosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SordosTheme {
                AppKineia()
            }
        }
    }
}
