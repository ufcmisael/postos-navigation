package com.example.exemplosimplesdecompose.view

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.data.Coordenadas
import com.example.exemplosimplesdecompose.data.Posto
import com.example.exemplosimplesdecompose.data.PostoRepository
import com.google.android.gms.location.LocationServices
import com.example.exemplosimplesdecompose.R

@Composable
fun AlcoolGasolinaPreco(navController: NavHostController, editId: String? = null) {
    var alcool by remember { mutableStateOf("") }
    var gasolina by remember { mutableStateOf("") }
    var nomeDoPosto by remember { mutableStateOf("") }
    var checkedState by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val textoInicial = stringResource(id = R.string.initial_result)
    val msgAlcool = stringResource(id = R.string.best_alcohol)
    val msgGasolina = stringResource(id = R.string.best_gasoline)
    val msgErroPreco = stringResource(id = R.string.error_invalid_prices)
    val msgErroGps = stringResource(id = R.string.error_gps_permission)
    val resNomePadrao = stringResource(id = R.string.default_station_name)
    var textoResultado by remember { mutableStateOf(textoInicial) }

    LaunchedEffect(editId) {
        if (editId != null) {
            // Busca na lista de postos o que tem o ID igual ao editId
            val postoParaEditar = PostoRepository.getAllPostos(context).find { it.id == editId }
            postoParaEditar?.let {
                alcool = it.precoAlcool.toString()
                gasolina = it.precoGasolina.toString()
                nomeDoPosto = it.nome
            }
        }
    }

    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Campo de texto para entrada do preço
            OutlinedTextField(
                value = alcool,
                onValueChange = { alcool = it }, // Atualiza o estado
                label = { Text(stringResource(id = R.string.price_alcohol)) },
                modifier = Modifier.fillMaxWidth(), // Preenche a largura disponível
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number) // Configuração do teclado
            )
            // Campo de texto para preço da Gasolina
            OutlinedTextField(
                value = gasolina,
                onValueChange = { gasolina = it },
                label = { Text(stringResource(id = R.string.price_gasoline)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            // Campo de texto para preço da Gasolina
            OutlinedTextField(
                value = nomeDoPosto,
                onValueChange = { nomeDoPosto = it },
                label = { Text(stringResource(id = R.string.station_name)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalArrangement = Arrangement.Start) {
                Text(
                    text = "75%",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Switch(
                    modifier = Modifier.semantics { contentDescription = "Demo with icon" },
                    checked = checkedState,
                    onCheckedChange = { checkedState = it },
                    thumbContent = {
                        if (checkedState) {
                            // Icon isn't focusable, no need for content description
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )
            }
            // Botão de cálculo
            Button(
                onClick = {

                    val pAlcool = alcool.toDoubleOrNull() ?: 0.0
                    val pGasolina = gasolina.toDoubleOrNull() ?: 0.0

                    if (pAlcool > 0 && pGasolina > 0) {
                        // Verifica permissão de GPS
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                // Coordenada padrão de Fortaleza se o GPS falhar
                                val lat = location?.latitude ?: -3.7319
                                val lon = location?.longitude ?: -38.5267

                                val proporcao = if (checkedState) 0.75 else 0.70


                                textoResultado = if (pAlcool / pGasolina <= proporcao) {
                                    msgAlcool
                                } else {
                                    msgGasolina
                                }

                                // update do posto
                                if (editId != null) {
                                    val postos = PostoRepository.getAllPostos(context)
                                    val original = postos.find { it.id == editId }
                                    original?.let {
                                        it.precoAlcool = pAlcool
                                        it.precoGasolina = pGasolina
                                        it.nome = nomeDoPosto
                                        PostoRepository.updatePosto(context, it)
                                        navController.navigate("lista/${original.nome}")
                                    }
                                } else {
                                    val novoPosto = Posto(
                                        id = PostoRepository.generateId(),
                                        nome = nomeDoPosto.ifBlank { resNomePadrao },
                                        precoAlcool = pAlcool,
                                        precoGasolina = pGasolina,
                                        coordenadas = Coordenadas(lat, lon),
                                        dataRegistro = PostoRepository.getCurrentDate()
                                    )

                                    PostoRepository.savePosto(context, novoPosto)
                                    //navController.navigate("lista/${novoPosto.nome}")
                                }
                            }
                        } else {
                            textoResultado = msgErroGps
                        }
                    } else {
                        textoResultado = msgErroPreco
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.calculate_save))
            }

            // Texto do resultado
            Text(
                text = textoResultado,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
                horizontalArrangement = Arrangement.End) {
                FloatingActionButton(

                    onClick = {
                        val nomeEnviar = nomeDoPosto.ifBlank { resNomePadrao }
                        navController.navigate("lista/$nomeEnviar")
                        //navController.navigate("ListaDePostos/$nomeDoPosto")
                              },

                    ) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(id = R.string.btn_view_list))
                }
            }
        }
    }
}
