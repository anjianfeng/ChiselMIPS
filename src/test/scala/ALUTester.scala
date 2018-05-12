package MIPSChisel

import chisel3.core.Bits
import chisel3.{iotesters}
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

object ALUTB extends App {
  iotesters.Driver.execute(args, () => new ALU(32)) {
    c => new ALUTester(c)
  }
}

class ALUTester(c: ALU) extends PeekPokeTester(c) {
  def repeatWhileFalse(a: Bits, m: (Bits, Bits, Bits), b: (Long, Long) => Long, p: (Long, Long)): Unit = {
    poke(m._1, p._1)
    poke(m._2, p._2)
    while (peek(a).equals(0)) {
      step(1)
    }
    expect(m._3, b.tupled(p))
    step(1)
  }

  def repeatWhileFalseWithDualOutput(a: Bits, m: (Bits, Bits, Bits, Bits), b: (Long, Long) => (Long, Long), p: (Long, Long)): Unit = {
    poke(m._1, p._1)
    poke(m._2, p._2)
    while (peek(a).equals(0)) {
      step(1)
    }
    expect(m._3, b.tupled(p)._1)
    expect(m._4, b.tupled(p)._2)
    step(1)
  }

  def monocycleCheck(m: (Bits, Bits, Bits), b: (Long, Long) => Long, p: (Long, Long)): Unit = {
    poke(m._1, p._1)
    poke(m._2, p._2)
    step(1)
    expect(m._3, b.tupled(p))
  }

  private val alu = c

  poke(c.io.start, 0)

  poke(c.io.opcode, 32) //add
  printf("Add test\n")
  for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => ((a + b) & 0x00000000FFFFFFFFL), (i, j))
  }
  poke(c.io.opcode, 34) //sub
  printf("Sub test\n")
  for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => ((a - b) & 0x00000000FFFFFFFFL), (i, j))
  }
  poke(c.io.opcode, 36) //and
  printf("And test\n")
  for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => (a & b), (i, j))
  }
  poke(c.io.opcode, 37) //or
  for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => (a | b), (i, j))
  }
  poke(c.io.opcode, 39) //nor
  for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => ((~(a | b)) & 0x00000000FFFFFFFFL), (i, j))
  }
  poke(c.io.opcode, 38) //xor
  for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => ((a ^ b) & 0x00000000FFFFFFFFL), (i, j))
  }
  poke(c.io.opcode, 0) //sll
  for (i <- 0L to 50000L by 10000L; j <- 0L to 63) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => ((a << b) & 0x00000000FFFFFFFFL), (i, j))
  }
  poke(c.io.opcode, 3) //sra
  for (i <- 0L to 50000L by 10000L; j <- 0L to 63) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => ((a.toInt.toLong >> b) & 0x00000000FFFFFFFFL), (i, j))
  }
  poke(c.io.opcode, 2) //srl
  for (i <- 0L to 50000L by 10000L; j <- 0L to 63) {
    monocycleCheck((c.io.a, c.io.b, c.io.c), (a: Long, b: Long) => ((a >>> b) & 0x00000000FFFFFFFFL), (i, j))
  }
  // poke(c.io.opcode, 24) //mult
  // for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
  //   poke(c.io.start, 1)
  //   repeatWhileFalse(c.io.complete, (c.io.a, c.io.b, c.io.product), (a: Long, b: Long) => (a * b), (i, j))
  //   poke(c.io.start, 0)
  //   step(1)
  // }
  poke(c.io.opcode, 24) //mult
  poke(c.io.start, 1)
  repeatWhileFalse(c.io.complete, (c.io.a, c.io.b, c.io.product),
    (a: Long, b: Long) => (a * b), (3, 3))
  poke(c.io.start, 0)
  step(1)
  // poke(c.io.opcode, 25) //multu
  // for (i <- 0L to 50000L by 10000L; j <- 0L to 50000L by 10000L) {
  //   poke(c.io.start, 1)
  //   repeatWhileFalse(c.io.complete, (c.io.a, c.io.b, c.io.product), (a: Long, b: Long) => (a * b), (i, j))
  //   poke(c.io.start, 0)
  //   step(1)
  // }
  // poke(c.io.opcode, 26) //div
  // for (i <- 0L to 50000L by 10000L; j <- 1L to 50000L by 10000L) {
  //   poke(c.io.start, 1)
  //   repeatWhileFalseWithDualOutput(c.io.complete, (c.io.a, c.io.b, c.io.quotient, c.io.remainder), (a: Long, b: Long) => (a / b, a % b), (i, j))
  //   poke(c.io.start, 0)
  //   step(1)
  // }
  // poke(c.io.opcode, 27) //divu
  // for (i <- 0L to 50000L by 10000L; j <- 1L to 50000L by 10000L) {
  //   poke(c.io.start, 1)
  //   repeatWhileFalseWithDualOutput(c.io.complete, (c.io.a, c.io.b, c.io.quotient, c.io.remainder), (a: Long, b: Long) => (a / b, a % b), (i, j))
  //   poke(c.io.start, 0)
  //   step(1)
  // }
}

