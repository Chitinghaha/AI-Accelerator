/*
 * Copyright 2021 The CFU-Playground Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "proj_menu.h"

#include <stdio.h>

#include "cfu.h"
#include "menu.h"
#include "tb/tb.h"

namespace
{

void do_SIMD_TB_INT_OP(void)
{
    printf("%s\n", (sADD_vv_TB() & sADD_vx_TB() & sSUB_vv_TB() & sSUB_vx_TB() & sMUL_vv_TB() & sMUL_vx_TB()) ? "Correct"
                                                                                                             : "Wrong");
}

void do_GEMM_with_SIMD(void)
{
    printf("%s\n", myGemm_Kernel() ? "Correct" : "Wrong");
}

void do_CONV_with_SIMD(void)
{
    printf("%s\n", myConv_Kernel() ? "Correct" : "Wrong");
}

void do_RELU_with_SIMD(void)
{
    printf("%s\n", myReLU_Kernel() ? "Correct" : "Wrong");
}

void do_MXPL_with_SIMD(void)
{
    printf("%s\n", myMxPl_Kernel() ? "Correct" : "Wrong");
}

void do_OP_with_SIMD(void)
{
    printf("%s\n", myGemm_Kernel() & myConv_Kernel() & myReLU_Kernel() & myMxPl_Kernel() ? "Correct" : "Wrong");
}

void do_ALEXNET_INFERENCE(void)
{
    myAlexNetTB();
}

struct Menu MENU = {
    "Project Menu",
    "project",
    {
        MENU_ITEM('i', "test SIMD instruction   - Integer Extension", do_SIMD_TB_INT_OP),
#ifdef PER_LAYER_QUANTIZATION
        MENU_ITEM('g', "test GEMM Operation     - with SIMD Extension (Per Layer Quantization)", do_GEMM_with_SIMD),
        MENU_ITEM('c', "test CONV Operation     - with SIMD Extension (Per Layer Quantization)", do_CONV_with_SIMD),
        MENU_ITEM('m', "test MXPL Operation     - with SIMD Extension (Per Layer Quantization)", do_MXPL_with_SIMD),
        MENU_ITEM('r', "test RELU Operation     - with SIMD Extension (Per Layer Quantization)", do_RELU_with_SIMD),
        MENU_ITEM('o', "test ALL  Operation     - with SIMD Extension (Per Layer Quantization)", do_OP_with_SIMD),
        MENU_ITEM('a', "test AlexNet Inference  - with SIMD Extension (Per Layer Quantization)", do_ALEXNET_INFERENCE),
#else // PER_OPERATION_QUANTIZATION
        MENU_ITEM('g', "test GEMM Operation     - with SIMD Extension (Per Operation Quantization)", do_GEMM_with_SIMD),
        MENU_ITEM('c', "test CONV Operation     - with SIMD Extension (Per Operation Quantization)", do_CONV_with_SIMD),
        MENU_ITEM('m', "test MXPL Operation     - with SIMD Extension (Per Operation Quantization)", do_MXPL_with_SIMD),
        MENU_ITEM('r', "test RELU Operation     - with SIMD Extension (Per Operation Quantization)", do_RELU_with_SIMD),
        MENU_ITEM('o', "test ALL  Operation     - with SIMD Extension (Per Operation Quantization)", do_OP_with_SIMD),
        MENU_ITEM('a', "test AlexNet Inference  - with SIMD Extension (Per Operation Quantization)",
                  do_ALEXNET_INFERENCE),
#endif
        MENU_END,
    },
};

}; // anonymous namespace

extern "C" void do_proj_menu()
{
    menu_run(&MENU);
}
