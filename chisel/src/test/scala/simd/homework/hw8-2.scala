package simd.hw

import util.Random
import chisel3._
import chiseltest._
import org.scalatest.freespec.AnyFreeSpec
import chisel3.experimental.BundleLiterals._
import chiseltest.{WriteVcdAnnotation, VerilatorBackendAnnotation}

class TestHw2Addition extends AnyFreeSpec with ChiselScalatestTester {
  "SIMD Execution Unit should execute sADDI8S.vv instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("0000000", 2)
      val funct3 = Integer.parseInt("000", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (0 to 3).map(i => (rs1_arr(i) + rs2_arr(i)).toByte)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(
          Seq.range(3, -1, -1).map { i => (rd_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sADDI16S.vv instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("0000000", 2)
      val funct3 = Integer.parseInt("001", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rs2_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rd_arr  = (0 to 1).map(i => (rs1_arr(i) + rs2_arr(i)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(Seq.range(1, -1, -1).map { i =>
          (rs1_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.bits.rs2.poke(Seq.range(1, -1, -1).map { i =>
          (rs2_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sADDI8S.vx instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("1000000", 2)
      val funct3 = Integer.parseInt("000", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (0 to 3).map(i => (rs1_arr(i) + rs2_arr(0)).toByte)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(
          Seq.range(3, -1, -1).map { i => (rd_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sADDI16S.vx instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("1000000", 2)
      val funct3 = Integer.parseInt("001", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rs2_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rd_arr  = (0 to 1).map(i => (rs1_arr(i) + rs2_arr(0)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(Seq.range(1, -1, -1).map { i =>
          (rs1_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.bits.rs2.poke(Seq.range(1, -1, -1).map { i =>
          (rs2_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }
}

class TestHw2Substraction extends AnyFreeSpec with ChiselScalatestTester {
  "SIMD Execution Unit should execute sSUBI8S.vv instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("0000001", 2)
      val funct3 = Integer.parseInt("000", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (0 to 3).map(i => (rs1_arr(i) - rs2_arr(i)).toByte)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(
          Seq.range(3, -1, -1).map { i => (rd_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sSUBI16S.vv instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("0000001", 2)
      val funct3 = Integer.parseInt("001", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rs2_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rd_arr  = (0 to 1).map(i => (rs1_arr(i) - rs2_arr(i)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(Seq.range(1, -1, -1).map { i =>
          (rs1_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.bits.rs2.poke(Seq.range(1, -1, -1).map { i =>
          (rs2_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sSUBI8S.vx instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("1000001", 2)
      val funct3 = Integer.parseInt("000", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (0 to 3).map(i => (rs1_arr(i) - rs2_arr(0)).toByte)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(
          Seq.range(3, -1, -1).map { i => (rd_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sSUBI16S.vx instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("1000001", 2)
      val funct3 = Integer.parseInt("001", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rs2_arr = (0 to 1).map(_ => Random.nextInt().toShort)
        val rd_arr  = (0 to 1).map(i => (rs1_arr(i) - rs2_arr(0)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(Seq.range(1, -1, -1).map { i =>
          (rs1_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.bits.rs2.poke(Seq.range(1, -1, -1).map { i =>
          (rs2_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }
}

class TestHw2Multiplication extends AnyFreeSpec with ChiselScalatestTester {
  "SIMD Execution Unit should execute sMULI8I16S.vv.L instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("0000010", 2)
      val funct3 = Integer.parseInt("100", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (0 to 1).map(i => (rs1_arr(i) * rs2_arr(i)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sMULI8I16S.vv.H instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("0000010", 2)
      val funct3 = Integer.parseInt("101", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (2 to 3).map(i => (rs1_arr(i) * rs2_arr(i)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sMULI8I16S.vx.L instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("1000010", 2)
      val funct3 = Integer.parseInt("100", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (0 to 1).map(i => (rs1_arr(i) * rs2_arr(0)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }

  "SIMD Execution Unit should execute sMULI8I16S.vx.H instructions" in {
    test(new simd.SIMDEngine()).withAnnotations(Seq(
      WriteVcdAnnotation,
      VerilatorBackendAnnotation
    )) { dut =>
      val funct7 = Integer.parseInt("1000010", 2)
      val funct3 = Integer.parseInt("101", 2)

      for (i <- 0 until 50) {
        val rs1_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rs2_arr = (0 to 3).map(_ => Random.nextInt().toByte)
        val rd_arr  = (2 to 3).map(i => (rs1_arr(i) * rs2_arr(0)).toShort)

        dut.io.cmd_payload.valid.poke(true.B)
        dut.io.cmd_payload.bits.funct7.poke(funct7)
        dut.io.cmd_payload.bits.funct3.poke(funct3)
        dut.io.cmd_payload.bits.rs1.poke(
          Seq.range(3, -1, -1).map { i => (rs1_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.bits.rs2.poke(
          Seq.range(3, -1, -1).map { i => (rs2_arr(i).toInt & 0xff) << (8 * i) }.reduce(_ | _)
        )
        dut.io.cmd_payload.ready.expect(true.B)
        dut.io.rsp_payload.ready.poke(true.B)
        dut.io.rsp_payload.valid.expect(true.B)
        dut.io.rsp_payload.bits.rd.expect(Seq.range(1, -1, -1).map { i =>
          (rd_arr(i).toInt & 0xffff) << (16 * i)
        }.reduce(_ | _))
        dut.clock.step(1)
      }
    }
  }
}
