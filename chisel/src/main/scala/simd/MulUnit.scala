package simd

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

object MulOp extends ChiselEnum {
  val NONE                                           = Value
  // val AMULI8I8S_VV, PMULI8I16S_VV_L, PMULI8I16S_VV_H = Value
  val AMULI8I8S_VV_NQ, PMULI8I16S_VV_L, PMULI8I16S_VV_H = Value
  val PMULI8I16S_VX_L, PMULI8I16S_VX_H = Value // 新添加的指令
  val AMULI8I8S_VX_NQ, AMULI8I8S_VX_AQ = Value
  val AMULI8I8S_VV_AQ = Value
  val Quantize = Value 
}

class MulUnit extends Module {
  val io = IO(new Bundle {
    val opSel = Input(MulOp())
    val rs1   = Input(UInt(32.W))
    val rs2   = Input(UInt(32.W))
    val rdMsb = Output(UInt(32.W))
    val rd    = Output(UInt(32.W))
  })

  val rs1ByteArray    = Wire(Vec(4, UInt(8.W)))
  val rs2ByteArray    = Wire(Vec(4, UInt(8.W)))
  val rdHalfArray     = Wire(Vec(4, UInt(16.W)))
  val rdAllByteConcat = Wire(UInt(32.W))
  val rdMsbByteConcat = Wire(UInt(32.W))
  val rdLsbHalfConcat = Wire(UInt(32.W))
  val rdMsbHalfConcat = Wire(UInt(32.W))


  // Quantization parameters
  val scalingFactor = RegInit(0.S(5.W))
  val zero = RegInit(0.S(9.W))

  // Store scaling factor and zero when opSel is Quantize
  when(io.opSel.isOneOf(MulOp.Quantize)) {
    scalingFactor := io.rs1(4,0).asSInt
    zero := io.rs2(8,0).asSInt
  }

  // printf("%b\n",scalingFactor)
  def quantize(value: UInt, scaling: SInt, zero: SInt): UInt = {
    val scaledValue = Mux(scaling >= 0.S, value >> scaling.asUInt, value << -scaling.asUInt)
    val quantizedValue = scaledValue.asSInt + zero
    quantizedValue(7, 0) // Ensure the result is within 8 bits
  }



  // 8-bit wire assignment
  for (i <- 0 until 4) {
    rs1ByteArray(i) := io.rs1(8 * i + 7, 8 * i)
    rs2ByteArray(i) := io.rs2(8 * i + 7, 8 * i)

    when(io.opSel.isOneOf(MulOp.AMULI8I8S_VV_NQ, MulOp.PMULI8I16S_VV_L, MulOp.PMULI8I16S_VV_H,
                          MulOp.AMULI8I8S_VX_NQ, MulOp.PMULI8I16S_VX_L, MulOp.PMULI8I16S_VX_H)) {
      // rdHalfArray(i) := (rs1ByteArray(i).asSInt * rs2ByteArray(i).asSInt).asUInt
      val scalar8Bit = io.rs2(7, 0).asSInt
      // Vector or Scalar
      val mulRes = Mux(io.opSel.isOneOf(MulOp.AMULI8I8S_VX_NQ, MulOp.PMULI8I16S_VX_L, MulOp.PMULI8I16S_VX_H),
      rs1ByteArray(i).asSInt * scalar8Bit, rs1ByteArray(i).asSInt * rs2ByteArray(i).asSInt)

      rdHalfArray(i) := mulRes.asUInt

    }.elsewhen(io.opSel.isOneOf(MulOp.AMULI8I8S_VX_AQ,MulOp.AMULI8I8S_VV_AQ)) {
      val scalar8Bit = io.rs2(7, 0).asSInt
      val mulRes = rs1ByteArray(i).asSInt * scalar8Bit
      // printf("scalingFactor = %b\n",scalingFactor)
      // 使用量化公式進行量化
      // Vector or Scalar
      rdHalfArray(i) := Mux(io.opSel.isOneOf(MulOp.AMULI8I8S_VX_AQ),
      quantize(( rs1ByteArray(i).asSInt * io.rs2(7, 0).asSInt ).asUInt ,scalingFactor.asSInt, zero.asSInt).asUInt,
      quantize(( rs1ByteArray(i).asSInt * rs2ByteArray(i).asSInt ).asUInt ,scalingFactor.asSInt, zero.asSInt).asUInt)

    }.otherwise {
      rdHalfArray(i) := DontCare
    }
  }

  rdAllByteConcat := Seq.range(3, -1, -1).map { i => rdHalfArray(i)(7, 0) }.reduce(_ ## _)
  rdMsbByteConcat := Seq.range(3, -1, -1).map { i => rdHalfArray(i)(15, 8) }.reduce(_ ## _)
  rdLsbHalfConcat := Seq.range(1, -1, -1).map { i => rdHalfArray(i) }.reduce(_ ## _)
  rdMsbHalfConcat := Seq.range(3, 1, -1).map { i => rdHalfArray(i) }.reduce(_ ## _)

  // output assignment
  io.rdMsb := rdMsbHalfConcat
  io.rd    := MuxLookup(
    io.opSel.asUInt,
    DontCare,
    Seq(
      MulOp.AMULI8I8S_VV_NQ.asUInt -> rdMsbByteConcat,
      MulOp.PMULI8I16S_VV_L.asUInt -> rdLsbHalfConcat,
      MulOp.PMULI8I16S_VV_H.asUInt -> rdMsbHalfConcat,
      MulOp.AMULI8I8S_VX_NQ.asUInt -> rdMsbByteConcat,
      MulOp.PMULI8I16S_VX_L.asUInt -> rdLsbHalfConcat,
      MulOp.PMULI8I16S_VX_H.asUInt -> rdMsbHalfConcat,
      MulOp.AMULI8I8S_VX_AQ.asUInt -> rdAllByteConcat,
      MulOp.AMULI8I8S_VV_AQ.asUInt -> rdAllByteConcat
    )
  )
}

