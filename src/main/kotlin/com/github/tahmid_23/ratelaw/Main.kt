package com.github.tahmid_23.ratelaw

import jetbrains.datalore.base.math.ipow
import jetbrains.datalore.plot.MonolithicCommon
import jetbrains.datalore.vis.swing.jfx.DefaultPlotPanelJfx
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.letsPlot
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.random.Random

const val EPOCHS = 1000
const val MAX_X = 100
const val MAX_Y = 100
const val MAX_Z = 100
const val MAX_VELOCITY = 10
const val MOLECULES = 50_000
const val REQUIRED_COLLISIONS = 3

data class Vec3(val x: Int, val y: Int, val z: Int)

data class Molecule(val position: Vec3, val velocity: Vec3)

fun main() {
    val random = Random.Default
    val exponent = REQUIRED_COLLISIONS - 1

    val concentrations = mutableListOf<Double>()
    var molecules = buildList {
        repeat(MOLECULES) {
            val position = Vec3(random.nextInt(MAX_X), random.nextInt(MAX_Y), random.nextInt(MAX_Z))
            val velocity = Vec3(
                random.nextInt(-MAX_VELOCITY, MAX_VELOCITY + 1),
                random.nextInt(-MAX_VELOCITY, MAX_VELOCITY + 1),
                random.nextInt(-MAX_VELOCITY, MAX_VELOCITY + 1),
            )
            add(Molecule(position, velocity))
        }
    }
    for (epoch in 0 until EPOCHS) {
        val spaceMap = mutableMapOf<Vec3, MutableSet<Molecule>>()
        val removals = buildList {
            for (molecule in molecules) {
                val otherMolecules = spaceMap.getOrPut(molecule.position, ::mutableSetOf)
                otherMolecules.add(molecule)
                if (otherMolecules.size == REQUIRED_COLLISIONS) {
                    for (otherMolecule in otherMolecules) {
                        add(otherMolecule)
                    }
                    otherMolecules.clear()
                }
            }
        }

        val newMolecules = molecules.toMutableList()
        newMolecules.removeAll(removals)
        molecules = newMolecules

        newMolecules.replaceAll(::moveMolecule)

        concentrations.add(molecules.size.ipow(-exponent))
        println("Epoch $epoch")
    }

    val window = JFrame("Rate Law Graph")
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

    val concentrationLabel = "Concentration (1/M^$exponent)"
    val plot = letsPlot(mapOf(
        "Epoch" to List(EPOCHS) { it },
        concentrationLabel to concentrations
    )) { x = "Epoch"; y = concentrationLabel; } + geomPoint(shape = 1)
    val plotPanel = DefaultPlotPanelJfx(
        MonolithicCommon.processRawSpecs(plot.toSpec(),false),
        preserveAspectRatio = true,
        preferredSizeFromPlot = false,
        repaintDelay = 10
    ) {
        println("[Plot] $it")
    }

    window.contentPane.add(plotPanel)

    SwingUtilities.invokeLater {
        window.pack()
        window.size = Dimension(640, 480)
        window.setLocationRelativeTo(null)
        window.isVisible = true
    }
}

private fun moveMolecule(molecule: Molecule): Molecule {
    var newPositionX = molecule.position.x + molecule.velocity.x
    var newPositionY = molecule.position.y + molecule.velocity.y
    var newPositionZ = molecule.position.z + molecule.velocity.z
    var newVelocityX = molecule.velocity.x
    var newVelocityY = molecule.velocity.y
    var newVelocityZ = molecule.velocity.z

    if (newPositionX < 0 || MAX_X <= newPositionX) {
        newPositionX = molecule.position.x
        newVelocityX *= -1
    }
    if (newPositionY < 0 || MAX_Y <= newPositionY) {
        newPositionY = molecule.position.y
        newVelocityY *= -1
    }
    if (newPositionZ < 0 || MAX_Z <= newPositionZ) {
        newPositionZ = molecule.position.z
        newVelocityZ *= -1
    }

    return Molecule(Vec3(newPositionX, newPositionY, newPositionZ), Vec3(newVelocityX, newVelocityY, newVelocityZ))
}