package com.example.exemplosimplesdecompose.view

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.exemplosimplesdecompose.R
import com.example.exemplosimplesdecompose.data.Posto
import com.example.exemplosimplesdecompose.data.PostoRepository
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaDePostos(navController: NavHostController, nomeDoPosto: String) {
    val context= LocalContext.current

    // 1. Pegue a lista real de postos salvos no celular
    val postos = remember {
        mutableStateListOf<Posto>().apply {
            addAll(PostoRepository.getAllPostos(context))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.list_title)) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(postos) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Título e Data
                        Text(text = item.nome, style = MaterialTheme.typography.titleLarge)
                        Text(text = item.dataRegistro, style = MaterialTheme.typography.bodySmall)

                        Spacer(Modifier.height(8.dp))

                        // EXIBIÇÃO DOS PREÇOS (Recuperados do JSON)
                        //Text(text = "Alc: R$ ${item.precoAlcool} | Gas: R$ ${item.precoGasolina}")
                        Text(text = stringResource(id = R.string.price_label, item.precoAlcool, item.precoGasolina))

                        // LÓGICA DA RECOMENDAÇÃO (Qual é vantajoso?)
                        val proporcao = item.precoAlcool / item.precoGasolina
                        val recomendacao = if (proporcao <= 0.75)
                            stringResource(id = R.string.best_alcohol)
                        else
                            stringResource(id = R.string.best_gasoline)

                        Text(
                            text = stringResource(id = R.string.res_label, recomendacao),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        // BOTÕES DE AÇÃO (Mapa, Editar, Deletar)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            // Abrir Mapa
                            IconButton(onClick = {
                                val uri = Uri.parse("geo:${item.coordenadas.latitude},${item.coordenadas.longitude}?q=${item.coordenadas.latitude},${item.coordenadas.longitude}(${item.nome})")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps"))
                            }) {
                                Icon(Icons.Default.Place, contentDescription = "Mapa")
                            }

                            // EDITAR (Update) - Volta para a tela de cálculo com os dados
                            IconButton(onClick = {
                                // Aqui você navegaria de volta passando o ID ou os dados para preencher o formulário
                                navController.navigate("mainalcgas?editId=${item.id}")
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            // DELETAR (Delete)
                            IconButton(onClick = {
                                PostoRepository.deletePosto(context, item.id)
                                postos.remove(item)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Deletar", tint = Color.Red)
                            }
                        }
                    }
                }
            }
/*            items(postos) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.nome, style = MaterialTheme.typography.titleLarge)

                            // Exibe os preços salvos no JSON
                            Text(
                                text = stringResource(
                                    id = R.string.price_label,
                                    item.precoAlcool,
                                    item.precoGasolina
                                )
                            )

                            // Lógica da recomendação baseada no que foi salvo
                            val proporcao = item.precoAlcool / item.precoGasolina
                            val recomendacao = if (proporcao <= 0.75)
                                stringResource(id = R.string.best_alcohol)
                            else
                                stringResource(id = R.string.best_gasoline)

                            Text(
                                text = stringResource(
                                    id = R.string.recommendation_label,
                                    recomendacao
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        // Botão de Abrir Mapa
                        IconButton(onClick = {
                            val uri =
                                Uri.parse("geo:${item.coordenadas.latitude},${item.coordenadas.longitude}?q=${item.coordenadas.latitude},${item.coordenadas.longitude}(${item.nome})")
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    uri
                                ).setPackage("com.google.android.apps.maps")
                            )
                        }) {
                            Icon(Icons.Default.Place, contentDescription = "Mapa")
                        }

                        // Botão de Deletar (CRUD - Delete)
                        IconButton(onClick = {
                            PostoRepository.deletePosto(context, item.id)
                            postos.remove(item) // Remove da tela na hora
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Deletar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }*/
/*                Card(
                    onClick = {

                        //Abrir Mapa

                        val lat = item.coordenadas.latitude
                        val lon = item.coordenadas.longitude
                        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${item.nome})")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)

                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Box(Modifier.fillMaxSize()) {
                        Text(
                            text = item.nome,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }*/
        }
    }
}