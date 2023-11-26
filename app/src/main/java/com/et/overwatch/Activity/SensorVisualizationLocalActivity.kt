package com.et.overwatch.Activity

import android.os.Bundle
import android.view.Choreographer
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.nio.ByteBuffer

class SensorVisualizationLocalActivity : ComponentActivity() {


    private lateinit var surfaceView: SurfaceView
    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    companion object {
        init {
            Utils.init()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        surfaceView = SurfaceView(this).apply { setContentView(this) }
        choreographer = Choreographer.getInstance()
        modelViewer = ModelViewer(surfaceView)
        surfaceView.setOnTouchListener(modelViewer)

        loadGlb("DamagedHelmet")
        //loadGltf("BusterDrone")
        loadEnvironment("venetian_crossroads_2k")
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(currentTime: Long) {
            choreographer.postFrameCallback(this)
            modelViewer.render(currentTime)
        }
    }

    override fun onResume() {
        super.onResume()
        choreographer.postFrameCallback(frameCallback)
    }

    override fun onPause() {
        super.onPause()
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        choreographer.removeFrameCallback(frameCallback)
    }

    private fun loadGlb(name: String) {
        val buffer = readAsset("models/${name}.glb")
        modelViewer.loadModelGlb(buffer)
        modelViewer.transformToUnitCube()
    }

    private fun readAsset(assetName: String): ByteBuffer {
        val input = assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private fun loadEnvironment(ibl: String) {
        // Create the indirect light source and add it to the scene.
        var buffer = readAsset("envs/$ibl/${ibl}_ibl.ktx")
        KTX1Loader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 50_000f
            modelViewer.scene.indirectLight = this
        }

        // Create the sky box and add it to the scene.
        buffer = readAsset("envs/$ibl/${ibl}_skybox.ktx")
        KTX1Loader.createSkybox(modelViewer.engine, buffer).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun loadGltf(name: String) {
        val buffer = readAsset("models/${name}.gltf")
        modelViewer.loadModelGltf(buffer) { uri -> readAsset("models/$uri") }
        modelViewer.transformToUnitCube()
    }
}
