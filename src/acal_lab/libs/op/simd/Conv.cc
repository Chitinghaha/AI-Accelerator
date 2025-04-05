#include "acal_lab/includes/op/simd/Conv.h"
namespace acal_lab {
namespace simd {

void Conv::execPerLayerNaiveQuant() {
	/********************************************************************
	 * TODO:                                                            *
	 * For Homework 13.4, your task is to implement CONV with per Layer *
	 * `Naive Quantization`. This involves using the instruction        *
	 * `sPMULI8I16S(.vv/.vx)` to generate int16 output. However, the    *
	 * int16 output needs to be converted to int8 and then forwarded    *
	 * to the next Operator. To achieve this, utilize `sQNTI16I8S.vv.NQ`*
	 * for the conversion.                                              *
	 *******************************************************************/
}

void Conv::execPerLayerAdvanceQuant() {
    /***********************************************************
     * TODO:                                                   *
     * For Homework 13.4, implement CONV with per Operation    *
     * `Advance Quantization`. Update instruction in the       *
     * `void acal_lab::Conv::execPerLayerNaiveQuant()`         *
     * function from `sQNTI16I8S.vv.NQ` to `sQNTI16I8S.vv.AQ`. *
     **********************************************************/

    // Constants and buffer initialization
    static int16_t intermediateBuffer[5500] = {0};
    static int16_t zeroInit[2] = {0};
    static int8_t inputData[4] = {0};
    static int16_t tempResults[2] = {0};

    // Initialize quantization parameters
    sQNT_INFO(qInfo->scaling_factor, qInfo->zero_point);

    // Preload biases
    for (int filterIdx = 0; filterIdx < info->kernel.N; ++filterIdx) {
        for (int outRow = 0; outRow < output->H; ++outRow) {
            for (int outCol = 0; outCol < output->W; outCol += 2) {
                int bufferIdx = filterIdx * output->H * output->W + outRow * output->W + outCol;
                sADDI16I16S_vx((int16_t*)(&intermediateBuffer[bufferIdx]), zeroInit, (int16_t)info->bias.data[filterIdx]);
            }
        }
    }

    for (int filterIdx = 0; filterIdx < info->kernel.N; ++filterIdx) {
        for (int outRow = 0; outRow < output->H; ++outRow) {
            for (int channelIdx = 0; channelIdx < info->kernel.C; ++channelIdx) {
                for (int kernelRow = 0; kernelRow < info->kernel.H; ++kernelRow) {
                    for (int kernelCol = 0; kernelCol < info->kernel.W; ++kernelCol) {
                        for (int outCol = 0; outCol < output->W; outCol += 2) {
                            int inputIdx = channelIdx * input->H * input->W + (outRow + kernelRow) * input->W + (outCol + kernelCol);
                            int kernelIdx = ((filterIdx * info->kernel.C + channelIdx) * info->kernel.H + kernelRow) * info->kernel.W + kernelCol;

                            inputData[0] = input->data[inputIdx];
                            inputData[1] = input->data[inputIdx + 1];

                            sPMULI8I16S_vx_L(tempResults, inputData, info->kernel.data[kernelIdx]);

                            int bufferIdx = filterIdx * output->H * output->W + outRow * output->W + outCol;
                            intermediateBuffer[bufferIdx] += tempResults[0];
                            intermediateBuffer[bufferIdx + 1] += tempResults[1];
                        }
                    }
                }
            }
        }
    }

    // Quantize 
    for (int idx = 0; idx < output->H * output->W * output->C; idx += 4) {
        sQNTI16I8S_vv_AQ((int8_t*)(&output->data[idx]), (int16_t*)(&((int32_t&)intermediateBuffer[idx])), (int16_t*)(&((int32_t&)intermediateBuffer[idx + 2])));
    }
}


void Conv::execPerOperationNaiveQuant() {
	/********************************************************
	 * TODO:                                                *
	 * For Homework 13.3, implement CONV with per Operation *
	 * Naive Quantization. Utilize `sAMULI8I8S(.vv/.vx).NQ` *
	 * to generate int8 output.                             *
	 *******************************************************/
}

void Conv::execPerOperationAdvanceQuant() {
	/********************************************************
	 * TODO:                                                *
	 * For Homework 8.4, implement CONV with per Operation  *
	 * Advance Quantization. Update instruction in the      *
	 * `void acal_lab::Conv::execPerOperationNaiveQuant()`  *
	 * function from `sAMULI8I8S(.vv/.vx).NQ` to            *
	 * `sAMULI8I8S(.vv/.vx).AQ`.                            *
	 *******************************************************/
}

}  // namespace simd
}  // namespace acal_lab
