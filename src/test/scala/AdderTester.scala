package MIPSChisel

import chisel3.core.Bits
import chisel3.{iotesters}
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}

object AdderTB extends App {
  iotesters.Driver.execute(args, () => new Adder(32)) {
    c => new AdderTester(c)
  }
}

class AdderTester(c: Adder) extends PeekPokeTester(c) {
   printf("\nHello MIPS!\n\n")

   poke(c.io.A, 0x1)
   poke(c.io.B, 0x2)
   poke(c.io.Cin, 0)
   step(1)
   expect(c.io.Sum, 0x3)
   expect(c.io.Cout, 0)

   poke(c.io.A, 0xFFFFFFFFL)
   poke(c.io.B, 0x2)
   poke(c.io.Cin, 0)
   step(1)
   expect(c.io.Sum, 0x1)
   expect(c.io.Cout, 1)

   printf("\nAll tests pass!\n\n")
}

