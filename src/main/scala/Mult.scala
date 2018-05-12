// See LICENSE.txt for license details.
package MIPSChisel

import chisel3._
import chisel3.util._

//A n-bit unsigned multiplier
class Mult(val n:Int, val log2n:Int) extends Module {
  val io = IO(new Bundle {
    val Multiplicand = Input(UInt(n.W))
    val Multiplier   = Input(UInt(n.W))
    val Start = Input(UInt(1.W))
    val Product  = Output(UInt((n*2).W))
    val Complete = Output(UInt(1.W))
    val AdderA  = Output(UInt(n.W))
    val AdderB  = Output(UInt(n.W))
    val AdderSum  = Input(UInt(n.W))
  })

  // Todo: How to calculate log in Chisel?
  val State = RegInit((0.U(log2n.W)))
  val StartOld = RegInit(0.U(1.W))
  val Product = RegInit((0.U((n*2).W)))

  StartOld := io.Start
  io.Product := Product
  io.AdderA := Product(2*n-1, n+1)
  io.AdderB := Mux(Product(0), io.Multiplicand, 0.U)

  // State transfer
  when ((io.Start===1.U)&&(StartOld===0.U)) {
    State := 1.U;
  } .elsewhen ((State < (n+2).U)&&(State > 0.U)) {
    State := State+1.U;
  } .otherwise {
    State := 0.U
  }

  // Complete control
  when (State === (n+2).U) {
    io.Complete := 1.U
  } .otherwise {
    io.Complete := 0.U
  }

  // Product control
  when ((State> 1.U)&&(State < (n+2).U)) {
     Product := Cat (io.AdderSum,  Product(n, 1))
  } .elsewhen ((State===1.U)) {
     Product := Cat (0.U, io.Multiplicand)
  }

  // Debug
  when (State>0.U){
     printf("Cycle=%d, Multiplicand=0x%x, Multiplier=0x%x, Product=0x%x\n",
       State, io.Multiplicand, io.Multiplier, Product)
     printf("AdderA=0x%x, AdderB=0x%x, AdderSum=0x%x, Start=%d\n",
       io.AdderA, io.AdderB, io.AdderSum, io.Start)
  }

}
