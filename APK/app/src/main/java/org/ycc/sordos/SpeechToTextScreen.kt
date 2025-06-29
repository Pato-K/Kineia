package org.ycc.sordos

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ycc.sordos.ui.theme.BabyLilac

@Composable
fun SpeechToTextScreen(modifier: Modifier) {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasAudioPermission = granted }
    )


    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val model = modelLoader.createModel("models/Esqueleto.glb")
    val modelNode = ModelNode(
        modelInstance = model.instance,
        scaleToUnits = 1.5f,
        autoAnimate = false
    )
    // Check and request permission on first composition
    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    // SpeechRecognizer Manager
    val recognizerManager = remember {
        SpeechRecognizerManager(
            context = context,
            onResult = {
                spokenText = it
                isListening = false
                coroutineScope.launch {
                    val arr = spokenText.split(' ')
                    arr.forEach {
                        val normalized = it.trim().lowercase().replace("[^a-záéíóúüñ]".toRegex(), "")

                        when(normalized) {
                            "hola" -> {
                                saludo(modelNode)
                                delay(1000L)
                            }
                            "hi" -> {
                                saludo(modelNode)
                                delay(1000L)
                            }
                            "hello" -> {
                                saludo(modelNode)
                                delay(1000L)
                            }
                            "how" -> {
                                how(modelNode)
                                delay(1000L)
                            }
                            "you" -> {
                                you(modelNode)
                                delay(1000L)
                            }
                            "feel" -> {
                                feel(modelNode)
                                delay(1000L)
                            }
                            "b" -> b(modelNode)
                            "e" -> e(modelNode)
                        }
                    }

                }
            },
            onError = {
                spokenText = "Error: $it"
                isListening = false
            }
        )
    }

    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color.White),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Mensaje interpretado:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(8.dp))
        Text(
            spokenText,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.size(width = 100.dp, height = 100.dp),
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (!hasAudioPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    return@Button
                }

                if (!isListening) {
                    recognizerManager.startListening()
                    isListening = true
                } else {
                    recognizerManager.stopListening()
                    isListening = false
                }
            } ,
            modifier = Modifier.size(width = 200.dp, height = 100.dp),
            colors = ButtonColors(
            containerColor = BabyLilac,
            contentColor = Color.White,
            disabledContainerColor = Color.Unspecified,
            disabledContentColor = Color.Unspecified
        ),
        border = BorderStroke(2.dp, color = Color.White),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
        ) {
            Text(
                if (isListening) "Parar de escuchar" else "Empezar a escuchar",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }


        val centerNode = rememberNode(engine)

        val cameraNode = rememberCameraNode(engine) {
            position = Position(y = -0.5f, z = 2.0f)
            lookAt(centerNode)
            centerNode.addChildNode(this)
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Scene(
                modifier = Modifier.height(400.dp),
                engine = engine,
                modelLoader = modelLoader,
                cameraNode = cameraNode,
                childNodes = listOf(
                    centerNode,
                    rememberNode {
                        modelNode
                    }
                ),
                onFrame = {
                    cameraNode.lookAt(centerNode)
                },
                isOpaque = false
            )
            modelNode.transform(position = modelNode.position - Float3(0f,1f,0f))
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recognizerManager.destroy()
        }
    }
}

suspend fun saludo(modelNode: ModelNode) {
    val movedNodes = mutableListOf<Node>()
    movedNodes.add(modelNode.nodes.find { it.name == "upper_arm.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "forearm.R" }!!)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(64f,0f,0f), smooth = true)
    delay(500)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(-64f,0f,0f), smooth = true)
    delay(500)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(-90f,0f,0f), smooth = true)
}

suspend fun b(modelNode: ModelNode) {
    val movedNodes = mutableListOf<Node>()
    movedNodes.add(modelNode.nodes.find { it.name == "upper_arm.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "forearm.R" }!!)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(0f,0f,64f), smooth = true)
    delay(800)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(0f,0f,-64f), smooth = true)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(-90f,0f,0f), smooth = true)
}

suspend fun e(modelNode: ModelNode) {
    val movedNodes = mutableListOf<Node>()
    movedNodes.add(modelNode.nodes.find { it.name == "hand.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "forearm.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_middle.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_ring.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_index.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_pinky.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "thumb.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_middle.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_ring.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_index.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_pinky.02.R" }!!)

    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(125f,0f,0f), smooth = true)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(0f,-100f,0f), smooth = true)
    movedNodes[7].transform(rotation = movedNodes[7].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[8].transform(rotation = movedNodes[8].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[9].transform(rotation = movedNodes[9].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[10].transform(rotation = movedNodes[10].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[2].transform(rotation = movedNodes[2].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[3].transform(rotation = movedNodes[3].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[4].transform(rotation = movedNodes[5].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[5].transform(rotation = movedNodes[5].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[6].transform(rotation = movedNodes[6].rotation + Float3(0f,-90f,0f), smooth = true)

    delay(800)

    movedNodes[6].transform(rotation = movedNodes[6].rotation + Float3(0f,90f,0f), smooth = true)
    movedNodes[5].transform(rotation = movedNodes[5].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[4].transform(rotation = movedNodes[4].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[3].transform(rotation = movedNodes[3].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[2].transform(rotation = movedNodes[2].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[10].transform(rotation = movedNodes[10].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[9].transform(rotation = movedNodes[9].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[8].transform(rotation = movedNodes[8].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[7].transform(rotation = movedNodes[7].rotation + Float3(-90f,0f,0f), smooth = true)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(-125f,0f,0f), smooth = true)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(0f,-100f,0f), smooth = true)
}

suspend fun how(modelNode: ModelNode) {
    val movedNodes = mutableListOf<Node>()
    movedNodes.add(modelNode.nodes.find { it.name == "forearm.L" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "forearm.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "upper_arm.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "upper_arm.L" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "hand.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "hand.L" }!!)

    movedNodes[2].transform(rotation = movedNodes[2].rotation + Float3(0f,-30f,0f), smooth = true)
    movedNodes[3].transform(rotation = movedNodes[3].rotation + Float3(0f,30f,0f), smooth = true)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(0f,0f,-120f), smooth = true)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(0f,0f,120f), smooth = true)
    movedNodes[4].transform(rotation = movedNodes[4].rotation + Float3(0f,-130f,0f), smooth = true)
    movedNodes[5].transform(rotation = movedNodes[5].rotation + Float3(0f,130f,0f), smooth = true)
    delay(800)
    movedNodes[5].transform(rotation = movedNodes[5].rotation + Float3(0f,130f,0f), smooth = true)
    movedNodes[4].transform(rotation = movedNodes[4].rotation + Float3(0f,-130f,0f), smooth = true)
    delay(800)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(0f,0f,-120f), smooth = true)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(0f,0f,120f), smooth = true)
    movedNodes[2].transform(rotation = movedNodes[2].rotation + Float3(0f,30f,0f), smooth = true)
    movedNodes[3].transform(rotation = movedNodes[3].rotation + Float3(0f,-30f,0f), smooth = true)
}

suspend fun you(modelNode: ModelNode) {
    val movedNodes = mutableListOf<Node>()
    movedNodes.add(modelNode.nodes.find { it.name == "forearm.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_middle.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_ring.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_pinky.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "thumb.02.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_index.02.R" }!!)

    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(90f,0f,0f), smooth = true)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(130f,0f,0f), smooth = true)
    movedNodes[2].transform(rotation = movedNodes[2].rotation + Float3(130f,0f,0f), smooth = true)
    movedNodes[3].transform(rotation = movedNodes[3].rotation + Float3(130f,0f,0f), smooth = true)
    delay(800)
    movedNodes[3].transform(rotation = movedNodes[3].rotation + Float3(-130f,0f,0f), smooth = true)
    movedNodes[2].transform(rotation = movedNodes[2].rotation + Float3(-130f,0f,0f), smooth = true)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(-130f,0f,0f), smooth = true)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(-90f,0f,0f), smooth = true)
}

suspend fun feel(modelNode: ModelNode) {
    val movedNodes = mutableListOf<Node>()
    movedNodes.add(modelNode.nodes.find { it.name == "forearm.R" }!!)
    movedNodes.add(modelNode.nodes.find { it.name == "f_middle.02.R" }!!)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(90f,0f,0f), smooth = true)
    delay(500)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(0f,-30f,0f), smooth = true)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(135f,0f,0f), smooth = true)
    delay(200)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(20f,0f,0f), smooth = true)
    delay(200)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(-20f,0f,0f), smooth = true)
    delay(200)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(20f,0f,0f), smooth = true)
    delay(200)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(-20f,0f,0f), smooth = true)
    delay(800)
    movedNodes[1].transform(rotation = movedNodes[1].rotation + Float3(-135f,0f,0f), smooth = true)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(0f,30f,-0f), smooth = true)
    delay(500)
    movedNodes[0].transform(rotation = movedNodes[0].rotation + Float3(-90f,0f,0f), smooth = true)
}
