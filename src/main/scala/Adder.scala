// See LICENSE.txt for license details.
package MIPSChisel

import chisel3._
import chisel3.util._

//A n-bit adder with carry in and carry out
class Adder(val n:Int) extends Module {
  val io = IO(new Bundle {
    val A    = Input(UInt(n.W))
    val B    = Input(UInt(n.W))
    val Cin  = Input(UInt(1.W))
    val Sum  = Output(UInt(n.W))
    val Cout = Output(UInt(1.W))
  })

  val SumAll = Cat(0.U(1.W), io.A) + Cat(0.U(1.W), io.B)+ io.Cin;

  io.Sum  := SumAll(n-1, 0)
  io.Cout := SumAll(n)
}
