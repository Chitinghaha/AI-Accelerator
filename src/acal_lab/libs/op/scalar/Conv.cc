#include "acal_lab/includes/op/scalar/Conv.h"
namespace acal_lab {
namespace scalar {

int16_t Conv::Buffer[55000] = {0};  // 初始化 Buffer 陣列
void Conv::execPerLayerNaiveQuant() {}

void Conv::execPerLayerAdvanceQuant() {

    // 所有kernel
    for (int filterIdx = 0; filterIdx < info->kernel.N; filterIdx++) {
        // printf("Conv:%d\n",filterIdx);
        // 輸出矩陣的高度
        for (int outHeight = 0; outHeight < output->H; outHeight++) {
            // 輸出矩陣的寬度
            for (int outWidth = 0; outWidth < output->W; outWidth++) {
                // 初始化暫存Buffer中的值為bias
                Buffer[filterIdx * output->H * output->W + outHeight * output->W + outWidth] = info->bias.data[filterIdx];
            }
        }
    }

    // 所有kernel
    for (int filterIdx = 0; filterIdx < info->kernel.N; filterIdx++) {
        // printf("filterIdx:%d\n",filterIdx);
        // 輸出矩陣的高度
        for (int outHeight = 0; outHeight < output->H; outHeight++) {
            // 輸出矩陣的寬度
            for (int outWidth = 0; outWidth < output->W; outWidth++) {
                // 輸入channel
                for (int channel = 0; channel < info->kernel.C; channel++) {
                    // kernel的高度
                    for (int kernelHeight = 0; kernelHeight < info->kernel.H; kernelHeight++) {
                        // kernel的寬度
                        for (int kernelWidth = 0; kernelWidth < info->kernel.W; kernelWidth++) {
                            // 計算輸入和kernel的index
                            int inputIndex = channel * input->H * input->W + (outHeight + kernelHeight) * input->W + (outWidth + kernelWidth);
                            int kernelIndex = ((filterIdx * info->kernel.C + channel) * info->kernel.H + kernelHeight) * info->kernel.W + kernelWidth;

                            // 進行卷積運算並累加到Buffer
                            int16_t product = (int16_t)input->data[inputIndex] * (int16_t)info->kernel.data[kernelIndex];
                            Buffer[filterIdx * output->H * output->W + outHeight * output->W + outWidth] += product;
                        }
                    }
                }
            }
        }
    }

    // 將 Buffer 中的結果進行量化並存入輸出矩陣
    for (int i = 0; i < output->H * output->W * output->C; i++) {
        output->data[i] = (int8_t)((Buffer[i] >> qInfo->scaling_factor) + qInfo->zero_point);
    }
}


void Conv::execPerOperationNaiveQuant() {
	// Perform convolution with bias
	for (int n = 0; n < info->kernel.N; n++) {
		for (int oh = 0; oh < output->H; oh++) {
			for (int ow = 0; ow < output->W; ow++) {
				output->data[n * output->H * output->W + oh * output->W + ow] = info->bias.data[n];
				for (int c = 0; c < info->kernel.C; c++) {
					for (int kh = 0; kh < info->kernel.H; kh++) {
						for (int kw = 0; kw < info->kernel.W; kw++) {
							int16_t temp =
							    (int16_t)input->data[c * input->H * input->W + (oh + kh) * input->W + (ow + kw)] *
							    (int16_t)info->kernel.data[((n * info->kernel.C + c) * info->kernel.H + kh) * info->kernel.W + kw];
							output->data[n * output->H * output->W + oh * output->W + ow] += (int8_t)(temp >> 8);
						}
					}
				}
			}
		}
	}
}

void Conv::execPerOperationAdvanceQuant() {}

}  // namespace scalar
}  // namespace acal_lab
