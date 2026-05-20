package com.example.exemplosimplesdecompose.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject
import java.text.DateFormat
import java.util.*

object PostoRepository {

    private const val PREFS_NAME = "posto_prefs"
    private const val KEY_COUNT = "posto_count"
    private const val KEY_SWITCH = "switch_state"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // salvar e ler o estado do Switch

    fun saveSwitchState(context: Context, isChecked: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_SWITCH, isChecked).apply()
    }

    fun getSwitchState(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_SWITCH, false)
    }

    // CRUD de postos

    // Converte um posto para JSON
    private fun postoToJson(posto: Posto): String {
        return JSONObject().apply {
            put("id", posto.id)
            put("nome", posto.nome)
            put("precoAlcool", posto.precoAlcool)
            put("precoGasolina", posto.precoGasolina)
            put("dataRegistro", posto.dataRegistro)

            // coordenadas fica como um objeto aninhado
            val coordJson = JSONObject()
            coordJson.put("latitude", posto.coordenadas.latitude)
            coordJson.put("longitude", posto.coordenadas.longitude)
            put("coordenadas", coordJson)
        }.toString()
    }

    // Converte JSON de volta para um posto
    private fun jsonToPosto(json: String): Posto? {
        return try {
            val obj = JSONObject(json)
            val coordObj = obj.getJSONObject("coordenadas")

            Posto(
                id = obj.getString("id"),
                nome = obj.getString("nome"),
                precoAlcool = obj.getDouble("precoAlcool"),
                precoGasolina = obj.getDouble("precoGasolina"),
                dataRegistro = obj.getString("dataRegistro"),
                coordenadas = Coordenadas(
                    latitude = coordObj.getDouble("latitude"),
                    longitude = coordObj.getDouble("longitude")
                )
            )
        } catch (e: Exception) {
            null
        }
    }

    // Salva um posto novo
    fun savePosto(context: Context, posto: Posto) {
        val prefs = getPrefs(context)
        val editor = prefs.edit()
        val count = prefs.getInt(KEY_COUNT, 0)

        editor.putString("posto_${posto.id}", postoToJson(posto))
        editor.putInt(KEY_COUNT, count + 1)
        editor.apply()
    }

    // Busca todos os postos salvos
    fun getAllPostos(context: Context): List<Posto> {
        val prefs = getPrefs(context)
        val all = prefs.all
        val postos = mutableListOf<Posto>()

        for ((key, value) in all) {
            if (key.startsWith("posto_") && value is String) {
                val posto = jsonToPosto(value)
                if (posto != null) postos.add(posto)
            }
        }
        return postos.sortedBy { it.nome }
    }

    // Atualiza um posto existente (mesmo id, dados novos)
    fun updatePosto(context: Context, posto: Posto) {
        getPrefs(context).edit()
            .putString("posto_${posto.id}", postoToJson(posto))
            .apply()
    }

    // Deleta um posto pelo id
    fun deletePosto(context: Context, postoId: String) {
        val prefs = getPrefs(context)
        val count = prefs.getInt(KEY_COUNT, 0)
        prefs.edit()
            .remove("posto_$postoId")
            .putInt(KEY_COUNT, maxOf(0, count - 1))
            .apply()
    }

    // Gera um id único baseado no tempo atual
    fun generateId(): String = UUID.randomUUID().toString()

    // Formata a data atual para salvar junto ao posto
    fun getCurrentDate(): String = DateFormat.getDateTimeInstance().format(Date())
}