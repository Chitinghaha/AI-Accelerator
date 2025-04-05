#include "acal_lab/includes/op/simd/Gemm.h"
namespace acal_lab {
namespace simd {

void Gemm::execPerLayerNaiveQuant() {}

void Gemm::execPerOperationNaiveQuant() {
	int    index_A, index_B, index_C;
	int8_t temp_A[4] = {0}, temp_B[4] = {0}, temp_C[4] = {0};

	for (int m = 0; m < input->H; m++) {
		index_A = m * input->W;   // M * K
		index_C = m * output->W;  // M * N
		for (int k = 0; k < input->W; k++) {
			index_B = k * info->weight.W;  // K * N
			for (int n = 0; n < info->weight.W; n += 4) {
				for (int i = 0; i < 4; ++i) temp_A[i] = input->data[index_A + k];
				*(int32_t*)temp_B = *(int32_t*)&(info->weight.data)[index_B + n];
				int output_index  = index_C + n;
				sAMULI8I8S_vv_NQ(temp_C, temp_A, temp_B);
				sADDI8I8S_vv((int8_t*)&(output->data[output_index]), (int8_t*)&(output->data[output_index]),
				             (int8_t*)(temp_C));
			}
		}
		for (int n = 0; n < info->weight.W;) {
			sADDI8I8S_vv((int8_t*)&(output->data[index_C + n]), (int8_t*)&(output->data[index_C + n]),
			             (int8_t*)&(info->bias.data[index_C + n]));
			n += 4;
		}
	}
}

void Gemm::execPerLayerAdvanceQuant() {
	/********************************************************************
	 * TODO:                                                            *
	 * For Homework 13.3, your task is to implement GEMM with per Layer *
	 * `Advance Quantization`. This involves using the instruction      *
	 * `sPMULI8I16S(.vv/.vx)` to generate int16 output. However, the    *
	 * int16 output needs to be converted to int8 and then forwarded    *
	 * to the next Operator. To achieve this, utilize `sQNTI16I8S.vv.AQ`*
	 * for the conversion.                                              *
	 *******************************************************************/
	// preOp();

	sQNT_INFO(qInfo->scaling_factor, qInfo->zero_point);
	int16_t tempINT16_Buffer[10000] = {0};

	int index_A, index_B, index_C;
	int8_t  temp_A[4]={0}, temp_B[4]={0};
	int16_t temp_C[4] = {0, 0, 0, 0};
	for (int m = 0; m < input->H; m++) {
		// printf("Gemm:%d\n",m);
		index_A = m * input->W;   // M * K
		index_C = m * output->W;  // M * N
		for (int k = 0; k < input->W; k++) {
			index_B = k * info->weight.W;  // K * N
			for (int n = 0; n < info->weight.W; n += 4) {
				// printf("n = %d\n",n);

				// tempINT16_Buffer[index_C + n] +=
				//     (int16_t)input->data[index_A + k] * (int16_t)info->weight.data[index_B + n];
				int output_index  = index_C + n;
				for (int i = 0; i < 4; ++i) temp_A[i] = input->data[index_A + k];

				*(int32_t *)temp_B = *(int32_t *)&(info->weight.data)[index_B + n];
				sPMULI8I16S_vx_L((int16_t*)&temp_C, temp_B, temp_A[0]);  
				sPMULI8I16S_vx_H((int16_t*)&temp_C[2], temp_B, temp_A[0]); 

				// C[m][n]   += temp_0;
				// C[m][n+1] += temp_1;
				// C[m][n+2] += temp_2;
				// C[m][n+3] += temp_3;
				sADDI16I16S_vv((int16_t *)&tempINT16_Buffer[output_index], (int16_t *)&tempINT16_Buffer[output_index], (int16_t *)&temp_C[0]);
                sADDI16I16S_vv((int16_t *)&tempINT16_Buffer[output_index + 2], (int16_t *)&tempINT16_Buffer[output_index + 2], (int16_t *)&temp_C[2]);

			}
		}
		for (int n = 0; n < info->weight.W; n++) {
			tempINT16_Buffer[index_C + n]+=(int16_t)info->bias.data[index_C + n];
		}
	}

	// PER LAYER QUANTIZATION
	// int tempH = 0, tempW = 0;
	// for (int h = 0; h < output->H; h++) {
	// 	tempH = h * output->W;  // M * N
	// 	for (int w = 0; w < output->W; w++) {
	// 		tempW = tempH + w;
	// 		output->data[tempW] = (int8_t)((tempINT16_Buffer[tempW] >> qInfo->scaling_factor) + qInfo->zero_point);
	// 	}
	// }
	
	int total_elements = output->H * output->W;  // �p���X�ƾڤ����`�����ƶq
    int remainder = total_elements % 4;  // �p�⤣��Q4�㰣���Ѿl�����ƶq


	if(remainder == 0){
		// �D�`�������A�C���B�z�|�Ӥ���
		for (int i = 0; i < total_elements - remainder; i += 4) {
			// printf("total_elements:%d\n",i);
			// �N�q�ƫ᪺���G�s�^��X�ƾڤ�
			// output->data[current_index] = (int8_t)((tempINT16_Buffer[current_index] >> qInfo->scaling_factor) + qInfo->zero_point);
			sQNTI16I8S_vv_AQ((int8_t*)&output->data[i], (int16_t*)&(*(int32_t*)&tempINT16_Buffer[i]), (int16_t*)&(*(int32_t*)&tempINT16_Buffer[i+2]));
		}
	}else{
		// �D�`�������A�C���B�z�|�Ӥ���
		for (int i = 0; i < total_elements - remainder; i += 4) {
			// printf("total_elements:%d\n",i);
			// �N�q�ƫ᪺���G�s�^��X�ƾڤ�
			// output->data[current_index] = (int8_t)((tempINT16_Buffer[current_index] >> qInfo->scaling_factor) + qInfo->zero_point);
			sQNTI16I8S_vv_AQ((int8_t*)&output->data[i], (int16_t*)&(*(int32_t*)&tempINT16_Buffer[i]), (int16_t*)&(*(int32_t*)&tempINT16_Buffer[i+2]));
		}

		

		// �B�z�Ѿl�����|�Ӥ���������(���i�ण�O2�����ơA�G���ק�)
		
		for (int i = total_elements - remainder; i < total_elements; i++) {
			// printf("total_elements - remainder:%d\n",i);
			// �N�q�ƫ᪺���G�s�^��X�ƾڤ�
			output->data[i] = (int8_t)((tempINT16_Buffer[i] >> qInfo->scaling_factor) + qInfo->zero_point);
		}

	}
	// postOp();
	// // ���L�ާ@�p�ƾ���
	// printf("Number of operations (Naive Quant): %d\n", num_operation);
}

void Gemm::execPerOperationAdvanceQuant() {
	/********************************************************************
	 * TODO:                                                            *
	 * For Homework 13.3, your task is to implement GEMM with per Layer *
	 * `Advance Quantization`. This involves using the instruction      *
	 * `sPMULI8I16S(.vv/.vx)` to generate int16 output. However, the    *
	 * int16 output needs to be converted to int8 and then forwarded    *
	 * to the next Operator. To achieve this, utilize `sQNTI16I8S.vv.AQ`*
	 * for the conversion.                                              *
	 *******************************************************************/
}

}  // namespace simd
}  // namespace acal_lab
