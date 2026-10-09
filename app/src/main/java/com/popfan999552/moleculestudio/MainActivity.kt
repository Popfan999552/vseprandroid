package com.popfan999552.moleculestudio

import android.os.Bundle
import android.graphics.Paint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlin.math.*
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent {
  MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF006B61),secondary=Color(0xFF51645F),background=Color(0xFFF5F8F6))) { Surface(Modifier.fillMaxSize()) { App() } }
 } }
}
@Composable fun App() {
 var formula by remember { mutableStateOf("ClF3") }; var selected by remember { mutableStateOf(Chemistry.lookup(formula)) }; var attempted by remember { mutableStateOf(false) }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
  Text("MOLECULE STUDIO",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
  Text("See chemistry take shape.",style=MaterialTheme.typography.headlineLarge)
  Text("Explore validated Lewis structures and spatial geometry.",style=MaterialTheme.typography.bodyLarge)
  OutlinedTextField(formula,{formula=it},label={Text("Chemical formula")},supportingText={Text("Case sensitive · charge as NO2- or NO₂⁻")},singleLine=true,modifier=Modifier.fillMaxWidth())
  Button(onClick={selected=Chemistry.lookup(formula);attempted=true},modifier=Modifier.fillMaxWidth()) { Text("Explore molecule") }
  Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { listOf("ClF3","IF5","NO2-").forEach { f -> AssistChip(onClick={formula=f;selected=Chemistry.lookup(f);attempted=true},label={Text(f)}) } }
  val m=selected
  if(m==null && attempted) { Card { Column(Modifier.padding(20.dp)) { Text("Unsupported molecule",style=MaterialTheme.typography.titleLarge);Text("No validated model for this formula. We will not invent a structure. Supported: " + Chemistry.molecules.joinToString { it.formula }) } } }
  if(m!=null) {
   Card { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
    Text(m.formula,style=MaterialTheme.typography.headlineMedium);Text("Validated · ${m.electrons} valence electrons · ${m.ax}",color=MaterialTheme.colorScheme.primary)
    Text("Molecular geometry: ${m.geometry}");Text("Electron geometry: ${m.electronGeometry}");Text("Bond angles: ${m.angles}")
   } }
   Text("Lewis structure",style=MaterialTheme.typography.titleLarge)
   Diagram(m,false)
   if(m.formula=="NO2-") { Text("↔ Equivalent resonance contributor"); Diagram(m.copy(ligands=m.ligands.reversed()),false) }
   Text("VSEPR projection",style=MaterialTheme.typography.titleLarge);Diagram(m,true)
   Text("Solid triangle: toward you · dashed bond: away. Dots are lone pairs. Projections are schematic; use the listed angles.",style=MaterialTheme.typography.bodySmall)
   Card { Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) { Text("Why this shape?",style=MaterialTheme.typography.titleLarge);Text(m.note.ifEmpty { "${m.ligands.size} bonding domains and ${m.pairs} lone-pair domains arrange in ${m.electronGeometry.lowercase()} electron geometry. Multiple bonds count as one domain. Lone pairs compress adjacent bond angles." });Text("Explanation uses the validated chemistry model. AI never supplies diagram coordinates or bond assignments.",style=MaterialTheme.typography.bodySmall) } }
   AiExplanation(m)
  }
  Text("Educational models · isolated molecules · approximate angles",style=MaterialTheme.typography.labelSmall)
 }
}
@Composable fun Diagram(m: Molecule, spatial: Boolean) {
 Canvas(Modifier.fillMaxWidth().height(310.dp).semantics { contentDescription = "${if (spatial) "VSEPR" else "Lewis"} diagram of ${m.formula}: central ${m.center}, ${m.pairs} lone pairs; " + m.ligands.joinToString { "${it.symbol}, bond order ${it.order}, ${it.pairs} lone pairs, formal charge ${it.charge}" } }) {
  val u=density;val c=Offset(size.width/2,size.height/2);val r=min(size.width,size.height)*0.32f
  val angles: List<Double> = when(m.geometry) {
   "T-shaped" -> listOf(-90.0,0.0,90.0)
   "Square pyramidal" -> listOf(-90.0,-25.0,35.0,145.0,205.0)
   "Octahedral" -> listOf(-90.0,90.0,0.0,180.0,40.0,220.0)
   "Linear" -> listOf(0.0,180.0)
   "Bent" -> listOf(35.0,145.0)
   "Trigonal pyramidal" -> listOf(-90.0,30.0,150.0)
   else -> listOf(-120.0,-30.0,45.0,140.0)
  }
  val ink=Color(0xFF153E39)
  fun label(s:String,p:Offset) { drawContext.canvas.nativeCanvas.drawText(s,p.x,p.y+6*u,Paint(Paint.ANTI_ALIAS_FLAG).apply { color=android.graphics.Color.rgb(21,62,57);textSize=18*u;textAlign=Paint.Align.CENTER;isFakeBoldText=true }) }
  fun pair(p:Offset,tangent:Offset) { drawCircle(ink,2*u,p+tangent*(3*u));drawCircle(ink,2*u,p-tangent*(3*u)) }
  m.ligands.forEachIndexed { i,l ->
   val a=Math.toRadians(angles[i]);val d=Offset(cos(a).toFloat(),sin(a).toFloat());val t=Offset(-d.y,d.x);val end=c+d*r;val start=c+d*(20*u);val stop=end-d*(20*u)
   val depth=if(spatial && m.geometry in listOf("Tetrahedral","Trigonal pyramidal","Square pyramidal","Octahedral")) { if(i==m.ligands.lastIndex) -1 else if(i==m.ligands.lastIndex-1) 1 else 0 } else 0
   if(depth==1) drawPath(Path().apply { moveTo(start.x,start.y);lineTo(stop.x+t.x*8*u,stop.y+t.y*8*u);lineTo(stop.x-t.x*8*u,stop.y-t.y*8*u);close() },ink)
   else if(depth == -1) { for(k in 1..7) { val v=k/8f;val p=start+(stop-start)*v;drawLine(ink,p-t*(8*u*v),p+t*(8*u*v),1.5f*u) } }
   else { for(b in 0 until l.order) { val shift=t*((b-(l.order-1)/2f)*6*u);drawLine(ink,start+shift,stop+shift,2*u) } }
   label(l.symbol+if(l.charge<0) "⁻" else "",end)
   for(j in 0 until l.pairs) { val pa=a+Math.toRadians(listOf(0.0,90.0,-90.0)[j]);val pd=Offset(cos(pa).toFloat(),sin(pa).toFloat());pair(end+pd*(19*u),Offset(-pd.y,pd.x)) }
  }
  label(m.center,c)
  for(j in 0 until m.pairs) { val a=when(m.geometry) { "T-shaped" -> if(j==0) 150.0 else 210.0; "Square pyramidal" -> 90.0; "Trigonal pyramidal" -> -30.0; else -> -70.0-j*65.0 };val rad=Math.toRadians(a);val d=Offset(cos(rad).toFloat(),sin(rad).toFloat());pair(c+d*(25*u),Offset(-d.y,d.x)) }
 }
}
