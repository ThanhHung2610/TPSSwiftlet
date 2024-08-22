package com.example.tpsembedding.data

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.tpsembedding.R
import com.github.mikephil.charting.data.BarData
import kotlinx.serialization.Serializable

@Serializable//(with=GeneralSerializer::class)
data class General(
    val labels: List<String>,
    val data: List<Int>,
    val data1: List<Int>,
    val data2: List<Int>)
/*
object GeneralSerializer : KSerializer<General> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("General") {
        element<List<String>>("labels")
        element<List<Int>>("data")
        element<List<Int>>("data1")
        element<List<Int>>("data2")
    }
    override fun serialize(encoder: Encoder, value: General) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, ListSerializer(String.serializer()), value.labels)
            encodeSerializableElement(descriptor, 1, ListSerializer(Int.serializer()), value.data)
            encodeSerializableElement(descriptor, 2, ListSerializer(Int.serializer()), value.data1)
            encodeSerializableElement(descriptor, 3, ListSerializer(Int.serializer()), value.data2)
        }
    }
    override fun deserialize(decoder: Decoder): General {
        return decoder.decodeStructure(descriptor) {
            var labels: List<String> = emptyList()
            var data: List<Int> = emptyList()
            var data1: List<Int> = emptyList()
            var data2: List<Int> = emptyList()

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> labels = decodeSerializableElement(descriptor, index, ListSerializer(String.serializer()))
                    1 -> data = decodeSerializableElement(descriptor, index, ListSerializer(Int.serializer()))
                    2 -> data1 = decodeSerializableElement(descriptor, index, ListSerializer(Int.serializer()))
                    3 -> data2 = decodeSerializableElement(descriptor, index, ListSerializer(Int.serializer()))
                    else -> decodeElementIndex(descriptor) // Skip unknown elements
                }
            }
            General(labels, data, data1, data2)
        }
    }
}
*/

@Serializable
data class Flyin(
    val labels: ArrayList<String>,
    val flyIn: ArrayList<Int>,
    val flyIn2: ArrayList<Int>,
    val flyIn3: ArrayList<Int>)
@Serializable
data class Flyout(
    val labels: ArrayList<String>,
    val flyOut: ArrayList<Int>,
    val flyOut2: ArrayList<Int>,
    val flyOut3: ArrayList<Int>)
@Serializable
data class Birdprediction(
    val labelPrediction:ArrayList<String>,
    val prediction:ArrayList<Int>,
    val prediction2: ArrayList<Int>)
@Serializable
data class WeatherLabel(
    val weather: ArrayList<String> = ArrayList(),
    val weather2: ArrayList<String> = ArrayList(),
    val weather3: ArrayList<String> = ArrayList()
)
data class BirdPredictionDataChart(
    val labels: ArrayList<String>,
    val data: BarData
)

const val sizeOfWeatherIcon = 40f