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
const val MAX_X = 1000
const val MAX_Y = 1000
const val MAX_VELOCITY = 3
const val MOLECULES = 50_000
const val REQUIRED_COLLISIONS = 4

data class Vec2(val x: Int, val y: Int)

data class Molecule(val position: Vec2, val velocity: Vec2)

fun main() {
    val random = Random.Default

    val rates = mutableListOf<Double>()
    var molecules = buildList {
        repeat(MOLECULES) {
            val position = Vec2(random.nextInt(MAX_X), random.nextInt(MAX_Y))
            val velocity = Vec2(random.nextInt(-MAX_VELOCITY, MAX_VELOCITY + 1), random.nextInt(-MAX_VELOCITY, MAX_VELOCITY + 1))
            add(Molecule(position, velocity))
        }
    }
    for (epoch in 0 until EPOCHS) {
        val spaceMap = mutableMapOf<Vec2, MutableSet<Molecule>>()
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

        rates.add(molecules.size.ipow(-REQUIRED_COLLISIONS + 1))
        println("Epoch $epoch")
    }

    val window = JFrame("Rate Law Graph")
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE


    val plot = letsPlot(mapOf(
        "Epoch" to List(EPOCHS) { it },
        "Rate" to rates
    )) { x = "Epoch"; y = "Rate"; } + geomPoint(shape = 1)
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
    var newVelocityX = molecule.velocity.x
    var newVelocityY = molecule.velocity.y

    if (newPositionX < 0 || MAX_X <= newPositionX) {
        newPositionX = molecule.position.x
        newVelocityX *= -1
    }
    if (newPositionY < 0 || MAX_Y <= newPositionY) {
        newPositionY = molecule.position.y
        newVelocityY *= -1
    }

    return Molecule(Vec2(newPositionX, newPositionY), Vec2(newVelocityX, newVelocityY))
}