package com.example.exemplosimplesdecompose.data

import java.io.Serializable
import java.text.DateFormat
import java.util.Date
import java.util.UUID


data class Posto(
    var nome: String,
    val coordenadas: Coordenadas,
    val id: String,
    var precoAlcool: Double,
    var precoGasolina: Double,
    val dataRegistro: String
) : Serializable {

    constructor(nome: String, coordenadas: Coordenadas) : this(
        nome,
        coordenadas,
        UUID.randomUUID().toString(),
        0.0,
        0.0,
        DateFormat.getDateTimeInstance().format(Date())
    )

    constructor(nome: String) : this(
        nome,
        Coordenadas(41.40338, 2.17403),
        UUID.randomUUID().toString(),
        0.0,
        0.0,
        DateFormat.getDateTimeInstance().format(Date())
    )
}