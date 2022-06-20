package com.github.tahmid_23.ratelaw

import jetbrains.datalore.plot.MonolithicCommon
import jetbrains.datalore.vis.swing.jfx.DefaultPlotPanelJfx
import jetbrains.letsPlot.geom.geomPoint
import jetbrains.letsPlot.intern.toSpec
import jetbrains.letsPlot.letsPlot
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.SwingUtilities
import kotlin.math.sqrt
import kotlin.random.Random

const val EPOCHS = 1000
const val MAX_X = 1000
const val MAX_Y = 1000
const val MOLECULES = 100_000

data class Vec2(val x: Int, val y: Int)

data class Molecule(val position: Vec2, val velocity: Vec2)

fun main() {
    val random = Random.Default

    val rateConstants = mutableListOf<Double>()
    var molecules = buildList {
        repeat(MOLECULES) {
            val position = Vec2(random.nextInt(MAX_X), random.nextInt(MAX_Y))
            val velocity = Vec2(random.nextInt(-1, 2), random.nextInt(-1, 2))
            add(Molecule(position, velocity))
        }
    }
    for (epoch in 0 until EPOCHS) {
        val spaceMap = mutableMapOf<Vec2, Molecule>()
        val removals = buildList {
            for (molecule in molecules) {
                val otherMolecule = spaceMap[molecule.position]
                if (otherMolecule != null) {
                    add(molecule)
                    add(otherMolecule)
                }
                else {
                    spaceMap[molecule.position] = molecule
                }
            }
        }

        val newMolecules = molecules.toMutableList()
        newMolecules.removeAll(removals)
        molecules = newMolecules

        newMolecules.replaceAll {
            var newPositionX = it.position.x + it.velocity.x
            var newPositionY = it.position.y + it.velocity.y
            var newVelocityX = it.velocity.x
            var newVelocityY = it.velocity.y

            if (newPositionX < 0 || MAX_X <= newPositionX) {
                newPositionX = it.position.x
                newVelocityX *= -1
            }
            if (newPositionY < 0 || MAX_Y <= newPositionY) {
                newPositionY = it.position.y
                newVelocityY *= -1
            }

            return@replaceAll Molecule(Vec2(newPositionX, newPositionY), Vec2(newVelocityX, newVelocityY))
        }

        val rateConstant = removals.size.toDouble() / (molecules.size.toLong() * molecules.size.toLong())
        rateConstants.add(rateConstant)

        val mean = rateConstants.average()
        val stdev = run {
            var sum = 0.0
            for (previousConstant in rateConstants) {
                val diff = previousConstant - mean
                sum += diff * diff
            }
            return@run sqrt(sum / rateConstants.size)
        }

        println("Epoch $epoch:")
        println("Count: ${molecules.size}")
        println("Rate: ${removals.size}")
        println("Rate Constant: $rateConstant")
        println("Mean: $mean")
        println("Standard Deviation: $stdev")
        println()
    }

    val window = JFrame("Rate Law Graph")
    window.defaultCloseOperation = JFrame.EXIT_ON_CLOSE


    val plot = letsPlot(mapOf(
        "Epoch" to List(EPOCHS) { i -> i },
        "Rate Constant" to rateConstants
    )) { x = "Epoch"; y = "Rate Constant" } + geomPoint(shape=1)
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
        window.size = Dimension(1280, 720)
        window.setLocationRelativeTo(null)
        window.isVisible = true
    }
}