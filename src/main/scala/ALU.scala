package MIPSChisel

import chisel3._
import chisel3.util._

class ALU(val n:Int) extends Module {
  // I/O define
  val io = IO(new Bundle {
    val a         = Input(UInt(n.W))
    val b         = Input(UInt(n.W))
    val opcode    = Input(UInt(6.W))
    val start     = Input(Bool())
    val c         = Output(UInt(n.W))
    val zero      = Output(Bool())
    val product   = Output(UInt((n*2).W))
    val quotient  = Output(UInt(n.W))
    val remainder = Output(UInt(n.W))
    val complete  = Output(Bool())
  })

  // The unique adder
  val AdderUnique = Module(new Adder(n))
  AdderUnique.io.A := io.a
  AdderUnique.io.B := io.b
  AdderUnique.io.Cin := 0.U

  // The mult
  val Mult = Module(new Mult(32, 6))
  Mult.io.Multiplicand := io.a
  Mult.io.Multiplier := io.b
  Mult.io.Start := io.start
  Mult.io.AdderSum := AdderUnique.io.Sum

  // Output register to avoid combitional loop
  val c   = RegInit(0.U(n.W))
  val zero   = RegInit(0.U(1.W))
  val quotient  = RegInit(0.U(n.W))
  val remainder   = RegInit(0.U(n.W))

  // Result
  val result = Wire(UInt(n.W))
  result := AdderUnique.io.Sum

  // Output connect to coresponding register
  io.c := result
  io.zero := (result === 0.U)
  io.quotient := quotient
  io.remainder := remainder
  io.product := Mult.io.Product
  io.complete:= Mult.io.Complete

  switch(io.opcode) {
    //add, addu, addi, addiu
    is("b100000".U, "b100001".U, "b001000".U, "b001001".U) {
      result := AdderUnique.io.Sum
    }
    //and
    is("b100100".U) {
      result := io.a & io.b
    }
    //andi
    is("b001100".U) {
      result := io.a & io.b
    }
    //div
    is("b011010".U) {
    }
    //divu
    is("b011011".U) {
    }
    //mult
    is("b011000".U) {
      AdderUnique.io.A := Mult.io.AdderA
      AdderUnique.io.B := Mult.io.AdderB
    }
    //multu
    is("b011001".U) {
    }
    //nor
    is("b100111".U) {
      result := ~(io.a | io.b)
    }
    //or
    is("b100101".U) {
      result := io.a | io.b
    }
    //ori
    is("b001101".U) {
      result := io.a | io.b
    }
    //sll
    is("b000000".U) {
      result := io.a << Cat(io.b(31, 5).orR(), io.b(4, 0))
    }
    //sllv
    is("b000100".U) {
      result := io.a << Cat(io.b(31, 5).orR(), io.b(4, 0))
    }
    //sra
    is("b000011".U) {
      result := (io.a.asSInt() >> Cat(io.b(31, 5).orR(), io.b(4, 0))).asUInt()
    }
    //srav
    is("b000111".U) {
      result := (io.a.asSInt() >> Cat(io.b(31, 5).orR(), io.b(4, 0))).asUInt()
    }
    //srl
    is("b000010".U) {
      result := io.a >> Cat(io.b(31, 5).orR(), io.b(4, 0))
    }
    //srlv
    is("b000110".U) {
      result := io.a >> Cat(io.b(31, 5).orR(), io.b(4, 0))
    }
    //sub, subu
    is("b100010".U, "b100011".U) {
      AdderUnique.io.A := io.a
      AdderUnique.io.B := ~io.b
      AdderUnique.io.Cin := 1.U
      result := AdderUnique.io.Sum
    }
    //xor, xori
    is("b100110".U, "b001110".U) {
      result := io.a ^ io.b
    }
  }
}

