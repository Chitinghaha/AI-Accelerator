#include "acal_lab/includes/op/simd/ReLU.h"
namespace acal_lab {
namespace simd {

    void sRELU8I8(int8_t* output, const int8_t* input) {
        for (int i = 0; i < 4; ++i) {
            // 如果 input[i] 大於 0，則將其保持不變；否則設為 0
            output[i] = input[i] > 0 ? input[i] : 0;
        }
    }

    void ReLU::exec() {
        int total_elements =input->C * input->H * input->W;
        for(int i = 0 ; i < total_elements ; i += 4){
            sRELU8I8((int8_t*)&(output->data[i]), (int8_t*)&(input->data[i]));
        }
    }



}  // namespace simd
}  // namespace acal_lab