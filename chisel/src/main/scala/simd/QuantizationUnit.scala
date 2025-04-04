package simd

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum

// add for QNT op 

object QntOp extends ChiselEnum {
    val NONE            = Value
    val QNTI16I8S_VV_NQ , QNTI16I8S_VV_AQ= Value
    val Quantize = Value 
    }

    class QntUnit extends Module { 
    val io = IO(new Bundle {
        val opSel = Input(QntOp())
        val rs1   = Input(UInt(32.W))
        val rs2   = Input(UInt(32.W))
        val rd    = Output(UInt(32.W))
    })

    val rdHalfArray     = Wire(Vec(4, UInt(16.W)))
    val rdAllByteConcat = Wire(UInt(32.W))


    // Quantization parameters
    val scalingFactor = RegInit(0.S(5.W))
    val zero = RegInit(0.S(9.W))

    // Store scaling factor and zero when opSel is Quantize
    when(io.opSel.isOneOf(QntOp.Quantize)) {
        scalingFactor := io.rs1(4,0).asSInt
        zero := io.rs2(8,0).asSInt
    }

    // printf("%b\n",scalingFactor)
    def quantize(value: UInt, scaling: SInt, zero: SInt): UInt = {
        val scaledValue = Mux(scaling >= 0.S, value >> scaling.asUInt, value << -scaling.asUInt)
        val quantizedValue = scaledValue.asSInt + zero
        quantizedValue(7, 0) // Ensure the result is within 8 bits
    }

    when(io.opSel.isOneOf(QntOp.QNTI16I8S_VV_NQ)) {
        rdHalfArray(0) := (io.rs1(15, 0) >> 8).asUInt
        rdHalfArray(1) := (io.rs1(31,16) >> 8).asUInt
        rdHalfArray(2) := (io.rs2(15, 0) >> 8).asUInt
        rdHalfArray(3) := (io.rs2(31,16) >> 8).asUInt
    }.otherwise {
        rdHalfArray(0) := quantize( (io.rs1(15, 0)).asUInt ,scalingFactor.asSInt, zero.asSInt).asUInt
        rdHalfArray(1) := quantize( (io.rs1(31,16)).asUInt ,scalingFactor.asSInt, zero.asSInt).asUInt
        rdHalfArray(2) := quantize( (io.rs2(15, 0)).asUInt ,scalingFactor.asSInt, zero.asSInt).asUInt
        rdHalfArray(3) := quantize( (io.rs2(31,16)).asUInt ,scalingFactor.asSInt, zero.asSInt).asUInt
    }

    rdAllByteConcat := Seq.range(3, -1, -1).map { i => rdHalfArray(i)(7, 0) }.reduce(_ ## _)

    io.rd := MuxLookup(
        io.opSel.asUInt,
        DontCare,
        Seq(
        QntOp.QNTI16I8S_VV_NQ.asUInt -> rdAllByteConcat,
        QntOp.QNTI16I8S_VV_AQ.asUInt -> rdAllByteConcat,
        )
    )
}