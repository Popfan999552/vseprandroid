package com.popfan999552.moleculestudio

data class Ligand(val symbol: String, val order: Int = 1, val pairs: Int = 3, val charge: Int = 0)
data class Molecule(val formula: String, val center: String, val pairs: Int, val ligands: List<Ligand>, val geometry: String, val electronGeometry: String, val angles: String, val electrons: Int, val charge: Int = 0, val note: String = "") {
    val ax: String get() = "AX${ligands.size}E$pairs"
    fun validate(): Boolean {
        val valence = mapOf("H" to 1, "C" to 4, "N" to 5, "O" to 6, "F" to 7, "Cl" to 7, "I" to 7, "S" to 6)
        val counted = 2 * (pairs + ligands.sumOf { it.order + it.pairs })
        val expected = valence.getValue(center) + ligands.sumOf { valence.getValue(it.symbol) } - charge
        val centralCharge = valence.getValue(center) - 2*pairs - ligands.sumOf { it.order }
        val expectedShape = mapOf(
            (3 to 2) to ("T-shaped" to "Trigonal bipyramidal"),
            (5 to 1) to ("Square pyramidal" to "Octahedral"),
            (2 to 1) to ("Bent" to "Trigonal planar"),
            (2 to 2) to ("Bent" to "Tetrahedral"),
            (3 to 1) to ("Trigonal pyramidal" to "Tetrahedral"),
            (4 to 0) to ("Tetrahedral" to "Tetrahedral"),
            (2 to 0) to ("Linear" to "Linear"),
            (6 to 0) to ("Octahedral" to "Octahedral")
        )[ligands.size to pairs]
        return expectedShape == (geometry to electronGeometry) && counted == electrons && expected == electrons && centralCharge + ligands.sumOf { it.charge } == charge &&
            ligands.all { valence.getValue(it.symbol) - 2*it.pairs - it.order == it.charge } &&
            ligands.all { 2*(it.order+it.pairs) == if (it.symbol == "H") 2 else 8 } &&
            (center !in listOf("C", "N", "O", "F") || 2*(pairs + ligands.sumOf { it.order }) == 8)
    }
}
object Chemistry {
    private fun f(n: Int) = List(n) { Ligand("F") }
    val molecules = listOf(
        Molecule("ClF3", "Cl", 2, f(3), "T-shaped", "Trigonal bipyramidal", "≈87.5°, ≈175°", 28, note="Two equatorial lone pairs leave two axial and one equatorial bond. Chlorine uses an expanded valence-shell Lewis model."),
        Molecule("IF5", "I", 1, f(5), "Square pyramidal", "Octahedral", "≈82°, ≈89°, ≈164°", 42, note="One octahedral site holds a lone pair; four fluorines form the base and one the apex."),
        Molecule("NO2-", "N", 1, listOf(Ligand("O",2,2), Ligand("O",1,3,-1)), "Bent", "Trigonal planar", "≈115°", 18,-1,"Two equivalent resonance contributors; real N–O bonds are equivalent with average bond order 1.5. The single-bonded oxygen carries −1 in each contributor."),
        Molecule("H2O", "O", 2, List(2){Ligand("H",1,0)}, "Bent", "Tetrahedral", "≈104.5°",8),
        Molecule("NH3", "N", 1,List(3){Ligand("H",1,0)},"Trigonal pyramidal","Tetrahedral","≈107°",8),
        Molecule("CH4", "C",0,List(4){Ligand("H",1,0)},"Tetrahedral","Tetrahedral","≈109.5°",8),
        Molecule("CO2", "C",0,List(2){Ligand("O",2,2)},"Linear","Linear","180°",16),
        Molecule("SF6", "S",0,f(6),"Octahedral","Octahedral","90°, 180°",48)
    )
    fun lookup(input: String): Molecule? {
        val clean = input.trim().replace(" ", "").replace('₀','0').replace('₁','1').replace('₂','2').replace('₃','3').replace('₄','4').replace('₅','5').replace('₆','6').replace('⁻','-').replace('−','-')
        return molecules.firstOrNull { it.formula == clean }?.takeIf { it.validate() }
    }
}
