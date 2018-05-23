//
// Created by raver119 on 29/10/17.
//

#include <op_boilerplate.h>
#if NOT_EXCLUDED(OP_transpose)

#include <ops/declarable/CustomOperations.h>
#include <helpers/ShapeUtils.h>

namespace nd4j {
namespace ops {

    //////////////////////////////////////////////////////////////////////////
    CUSTOM_OP_IMPL(transpose, 1, 1, true, 0, 0) {
        NDArray<T>* x = INPUT_VARIABLE(0);
        if (block.width() == 1) {
            if (block.isInplace()) {
                x->transposei();
                STORE_RESULT(*x);
            } else {
                NDArray<T> *output = OUTPUT_VARIABLE(0);
                x->transpose(*output);
                STORE_RESULT(*output);
            }
        } else {
            // this is tf-mode transpose, that's nd4j permute
            bool replace = false;
            auto arguments = block.getIArguments();

            int w = block.width();
            int a = arguments->size();

            if (block.width() == 2 && arguments->size() == 0) {
                auto axis = INPUT_VARIABLE(1);
                for (int e = 0; e < axis->lengthOf(); e++) {
                    int ax = (int) axis->getScalar(e);
                    if (ax < 0)
                        ax += x->rankOf();

                    arguments->emplace_back(ax);
                }

                replace = true;
            } else if (arguments->size() == 0) {
                for (int e = x->rankOf() - 1; e >= 0; e--)
                    arguments->emplace_back(e);
            }

            // 0D edge case
            if (x->rankOf() == 0) {
                REQUIRE_TRUE(arguments->size() == 1, 0, "Permute: only one axis is allowed for scalar");
                auto output = OUTPUT_VARIABLE(0);
                if (!block.isInplace())
                    output->assign(x);

                return ND4J_STATUS_OK;
            }

            if(block.isInplace()) {		// in-place
                x->permutei(*arguments);
                STORE_RESULT(x);
            } else {
                if (!replace) {			// not-in-place
                    NDArray<T>* output = OUTPUT_VARIABLE(0);

                    x->permute(*arguments, *output);

                    STORE_RESULT(output);
                } else {
                    auto output = x->dup();
                    output->permutei(*arguments);

                    OVERWRITE_RESULT(output);
                }
            }
        }
        return Status::OK();
    }


    DECLARE_SHAPE_FN(transpose) {
        if (block.width() == 1) {
            auto outputShapeInfo = ShapeUtils<T>::evalTranspShapeInfo(*INPUT_VARIABLE(0), block.workspace());
            return SHAPELIST(outputShapeInfo);
        } else {
            // this is basically permute mode
            auto shapeList = SHAPELIST();
            auto arguments = block.getIArguments();
            if (shape::rank(inputShape->at(0)) == 0) {
                Nd4jLong *newshape;
                ALLOCATE(newshape, block.getWorkspace(), shape::shapeInfoLength(inputShape->at(0)), Nd4jLong);
                newshape[0] = 0;
                newshape[1] = 0;
                newshape[2] = 1;
                newshape[3] = 99;
                shapeList->push_back(newshape);
            } else if (arguments->size() > 0 || inputShape->size() > 1) {
                std::vector<int> axis = arguments->size() > 0 ? *arguments : (INPUT_VARIABLE(1))->template asVectorT<int>();
                auto outputShapeInfo = ShapeUtils<T>::evalPermShapeInfo(axis.data(), axis.size(), *INPUT_VARIABLE(0), block.workspace());
                shapeList->push_back(outputShapeInfo);
            } else if (inputShape->size() == 2) {
                // dead end
                Nd4jLong *newshape;
                COPY_SHAPE(inputShape->at(0), newshape);
                shapeList->push_back(newshape);
            } else {
                int rank = shape::rank(inputShape->at(0));
                for (int e = rank - 1; e >= 0; e--)
                    arguments->emplace_back(e);

                auto outputShapeInfo = ShapeUtils<T>::evalPermShapeInfo(arguments->data(), arguments->size(), *INPUT_VARIABLE(0), block.workspace());
                shapeList->push_back(outputShapeInfo);
            }

            return shapeList;
        }
    }
}
}

#endif