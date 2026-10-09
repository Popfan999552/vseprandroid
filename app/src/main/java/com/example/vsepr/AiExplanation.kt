package com.example.vsepr
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI

@Composable fun AiExplanation(m: Molecule) {
 var endpoint by remember { mutableStateOf("") };var text by remember(m.formula) { mutableStateOf("") };var busy by remember { mutableStateOf(false) };val scope=rememberCoroutineScope()
 Card { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
  Text("Optional AI explanation",style=MaterialTheme.typography.titleLarge)
  Text("Connect your deployed HTTPS explanation service. No AI provider key is stored in this app.")
  OutlinedTextField(endpoint,{endpoint=it},label={Text("Backend URL, e.g. https://chem.example.com")},modifier=Modifier.fillMaxWidth(),singleLine=true)
  Button(enabled=!busy && endpoint.isNotBlank(),onClick={ busy=true;scope.launch {
   text=withContext(Dispatchers.IO) { runCatching {
    val uri=URI(endpoint.trim());require(uri.scheme=="https" && uri.host!=null && uri.userInfo==null && uri.query==null && uri.fragment==null) { "Use an HTTPS backend URL." }
    val connection=URI(endpoint.trim().trimEnd('/')+"/explain").toURL().openConnection() as HttpURLConnection
    try { connection.requestMethod="POST";connection.connectTimeout=15000;connection.readTimeout=30000;connection.instanceFollowRedirects=false;connection.doOutput=true;connection.setRequestProperty("Content-Type","application/json")
     connection.outputStream.use { it.write(JSONObject().put("formula",m.formula).toString().toByteArray()) }
     require(connection.responseCode==200) { "Backend unavailable (${connection.responseCode})." }
     JSONObject(connection.inputStream.bufferedReader().use{it.readText()}).getString("explanation")
    } finally { connection.disconnect() }
   }.getOrElse { "Could not retrieve explanation: ${it.message}" } };busy=false
  } }) { Text(if(busy) "Requesting…" else "Ask AI") }
  if(text.isNotEmpty()) { Text("AI-generated text · may contain errors; validated structures above remain authoritative.",style=MaterialTheme.typography.labelSmall);Text(text) }
 } }
}
