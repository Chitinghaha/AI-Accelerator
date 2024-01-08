#ifndef __OP_SIMD_CONV_H__
#define __OP_SIMD_CONV_H__
#include "acal_lab/includes/op/Op.h"

#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "acal_lab/includes/info/op/ConvInfo.h"

namespace acal_lab
{

namespace simd
{

class Conv : public Operator
{
  protected:
    convInfo *info;
    quantiInfo *qInfo;

  public:
    Conv(tensorInfo *opt, tensorInfo *ipt, convInfo *opInfo, quantiInfo *quantInfo, QauntiType qType)
        : Operator(opt, ipt, qType), info(opInfo), qInfo(quantInfo)
    {
    }

    void execPerLayerNaiveQuant() override;
    void execPerLayerAdvanceQuant() override;
    void execPerOperationNaiveQuant() override;
    void execPerOperationAdvanceQuant() override;
};
} // namespace simd
} // namespace acal_lab

#endif // __OP_SIMD_CONV_H__