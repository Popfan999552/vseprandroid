package com.example.vsepr
import org.junit.Assert.*
import org.junit.Test
class ChemistryTest {
 @Test fun allModelsConserveElectronsAndCharge() { Chemistry.molecules.forEach { assertTrue(it.formula,it.validate()) } }
 @Test fun requestedGeometries() { assertEquals("T-shaped",Chemistry.lookup("ClF3")!!.geometry); assertEquals("Square pyramidal",Chemistry.lookup("IF5")!!.geometry); assertEquals(18,Chemistry.lookup("NO₂⁻")!!.electrons) }
 @Test fun unsupportedAndInvalidAreRejected() { assertNull(Chemistry.lookup("NO2")); assertNull(Chemistry.lookup("C6H6")); assertFalse(Chemistry.lookup("ClF3")!!.copy(electrons=26).validate()) }
 @Test fun geometryAndIndividualChargesCannotDrift() { val m=Chemistry.lookup("ClF3")!!; assertFalse(m.copy(geometry="Linear").validate()); assertFalse(m.copy(ligands=listOf(Ligand("F",charge=1),Ligand("F",charge=-1),Ligand("F"))).validate()) }
}
