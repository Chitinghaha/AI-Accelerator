#include "acal_lab/includes/op/simd/MxPl.h"
namespace acal_lab {
namespace simd {

    void sMXPLI8I8_VV(int8_t* output, const int8_t* dataA, const int8_t* dataB) {
        output[0] = (dataA[0] > dataB[0]) ? dataA[0] : dataB[0];  // 比較 dataA[0] 和 dataB[0]
        output[1] = (dataA[1] > dataB[1]) ? dataA[1] : dataB[1];  // 比較 dataA[1] 和 dataB[1]

        output[0]    = (output[0] > output[1]) ? output[0] : output[1];
    }
    

    void MxPl::exec() {
        int8_t dataA[2] = {0}, dataB[2] = {0};
        int8_t outputData[2] = {0};  
    
        for (int channel = 0; channel < output->C; channel++) {
            for (int outHeight = 0; outHeight < output->H; outHeight++) {
                for (int outWidth = 0; outWidth < output->W; outWidth++) {
    
                    int indexA = channel * input->H * input->W + (outHeight * info->stride - info->padding )* input->W + (outWidth * info->stride - info->padding);
                    int indexB = channel * input->H * input->W + ((outHeight * info->stride - info->padding ) + 1) * input->W + (outWidth * info->stride - info->padding);
    
                    // 拆成 int8_t
                    dataA[0] = input->data[indexA];
                    dataA[1] = input->data[indexA + 1];
                    dataB[0] = input->data[indexB];
                    dataB[1] = input->data[indexB + 1];
    
                    sMXPLI8I8_VV((int8_t*)&outputData[0], (int8_t*)&dataA[0], (int8_t*)&dataB[0]);
    
                    output->data[channel * output->H * output->W + outHeight * output->W + outWidth] = outputData[0];
                }
            }
        }
    }

}  // namespace simd
}  // namespace acal_lab
