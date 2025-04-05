#include "acal_lab/includes/op/scalar/Conv.h"
namespace acal_lab {
namespace scalar {

int16_t Conv::Buffer[55000] = {0};  // ��l�� Buffer �}�C
void Conv::execPerLayerNaiveQuant() {}

void Conv::execPerLayerAdvanceQuant() {

    // �Ҧ�kernel
    for (int filterIdx = 0; filterIdx < info->kernel.N; filterIdx++) {
        // printf("Conv:%d\n",filterIdx);
        // ��X�x�}������
        for (int outHeight = 0; outHeight < output->H; outHeight++) {
            // ��X�x�}���e��
            for (int outWidth = 0; outWidth < output->W; outWidth++) {
                // ��l�ƼȦsBuffer�����Ȭ�bias
                Buffer[filterIdx * output->H * output->W + outHeight * output->W + outWidth] = info->bias.data[filterIdx];
            }
        }
    }

    // �Ҧ�kernel
    for (int filterIdx = 0; filterIdx < info->kernel.N; filterIdx++) {
        // printf("filterIdx:%d\n",filterIdx);
        // ��X�x�}������
        for (int outHeight = 0; outHeight < output->H; outHeight++) {
            // ��X�x�}���e��
            for (int outWidth = 0; outWidth < output->W; outWidth++) {
                // ��Jchannel
                for (int channel = 0; channel < info->kernel.C; channel++) {
                    // kernel������
                    for (int kernelHeight = 0; kernelHeight < info->kernel.H; kernelHeight++) {
                        // kernel���e��
                        for (int kernelWidth = 0; kernelWidth < info->kernel.W; kernelWidth++) {
                            // �p���J�Mkernel��index
                            int inputIndex = channel * input->H * input->W + (outHeight + kernelHeight) * input->W + (outWidth + kernelWidth);
                            int kernelIndex = ((filterIdx * info->kernel.C + channel) * info->kernel.H + kernelHeight) * info->kernel.W + kernelWidth;

                            // �i����n�B��ò֥[��Buffer
                            int16_t product = (int16_t)input->data[inputIndex] * (int16_t)info->kernel.data[kernelIndex];
                            Buffer[filterIdx * output->H * output->W + outHeight * output->W + outWidth] += product;
                        }
                    }
                }
            }
        }
    }

    // �N Buffer �������G�i��q�ƨæs�J��X�x�}
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
