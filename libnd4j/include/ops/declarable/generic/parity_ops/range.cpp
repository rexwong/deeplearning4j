//
// @author raver119@gmail.com
//

#include <op_boilerplate.h>
#if NOT_EXCLUDED(OP_range)

#include <NDArray.h>
#include <graph/VariableSpace.h>
#include <ops/declarable/CustomOperations.h>

/**
 * FIXME: current shape_fn design does NOT allow run-time evaluation for this op.
 */
namespace nd4j {
    namespace ops {
        CUSTOM_OP_IMPL(range, -2, 1, false, -2, -2) {
            std::vector<T> data;

            auto output = OUTPUT_VARIABLE(0);

            if (block.getIArguments()->size() > 0) {
                int start = INT_ARG(0);
                int stop = INT_ARG(1);
                int step = INT_ARG(2);

                int cnt = 0;
                T e = start;
                if (start > stop) {
                    while (e > (T) stop) {
                        output->putScalar(cnt++, e);
                        e = (T) step > (T) 0.0 ? e - (T)step : e + (T)step;
                    }
                } else {
                    while (e < (T) stop) {
                        output->putScalar(cnt++, (T) e);
                        e += step;
                    }
                }

                STORE_RESULT(output);
            } else if (block.getTArguments()->size() > 0) {
                T start = T_ARG(0);
                T stop = T_ARG(1);
                T step = T_ARG(2);

                int cnt = 0;
                T e = start;
                if (start > stop) {
                    while (e > stop) {
                        output->putScalar(cnt++, e);
                        e = step > (T) 0.0 ? e - step : e + step;
                    }
                } else {
                    while (e < stop) {
                        output->putScalar(cnt++, e);
                        e += step;
                    }
                }

                STORE_RESULT(output);
            } else if (block.width() > 0) {
                REQUIRE_TRUE(block.width() == 3, 0, "Runtime range should have 3 arrays as input, but got %i instead", block.width());

                auto arr0 = INPUT_VARIABLE(0);
                auto arr1 = INPUT_VARIABLE(1);
                auto arr2 = INPUT_VARIABLE(2);

                T start = arr0->getScalar(0);
                T stop = arr1->getScalar(0);
                T step = arr2->getScalar(0);

                //for (T e = start; e < (T) stop; e += step)
                //    data.emplace_back((T) e);
                T e = (T) start;
                if (start > stop) {
                    while (e > stop) {
                        data.emplace_back(e);
                        e = step > (T) 0.0 ? e - step : e + step;
                    }
                } else {
                    while (e < stop) {
                        data.emplace_back(e);
                        e += step;
                    }
                }

                if (output->lengthOf() == data.size()) {
                    memcpy(output->buffer(), data.data(), data.size() * sizeof(T));
                } else {
                    auto array = new nd4j::NDArray<T>({(Nd4jLong) data.size()}, block.getWorkspace());
                    memcpy(array->buffer(), data.data(), data.size() * sizeof(T));

                    //block.pushNDArrayToVariableSpace(block.nodeId(), 0, array);
                    OVERWRITE_RESULT(array);
                }
            } else {
                REQUIRE_TRUE(false, 0, "Runtime range should have inputs defined in any possible way: T_args, INT_args, or INPUT variables")
            }

            return ND4J_STATUS_OK;
        }
        DECLARE_SHAPE_FN(range) {
            Nd4jLong *newShape;
            std::vector<Nd4jLong> shape;
            if (block.getIArguments()->size() > 0) {
                auto start = INT_ARG(0);
                auto stop = INT_ARG(1);
                auto step = INT_ARG(2);

                Nd4jLong cnt = 0;
                T e = (T) start;
                if (start > stop) {
                    while (e > (T) stop) {
                        cnt++;
                        e = (T) step > (T) 0.0 ? e - (T)step : e + (T)step;
                    }
                } else {
                    while (e < (T) stop) {
                        cnt++;
                        e += step;
                    }
                }
                
                shape.emplace_back(cnt);
            } else if (block.getTArguments()->size() > 0) {
                T start = T_ARG(0);
                T stop = T_ARG(1);
                T step = T_ARG(2);

                Nd4jLong cnt = 0;
                T e = start;
                if (start > stop) {
                    while (e > stop) {
                        cnt++;
                        e = step > (T) 0.0 ? e - step : e + step;
                    }
                } else {
                    while (e < stop) {
                        cnt++;
                        e += step;
                    }
                }

                shape.emplace_back(cnt);
            } else {
                // FIXME:if that's runtime evaluation - we'll just pass some vector. 
                shape.emplace_back(119);
            }
            
            ALLOCATE(newShape, block.getWorkspace(), shape::shapeInfoLength(1), Nd4jLong);
            //shape::shapeBuffer(1, shape.data(), newShape);
            shape::shapeVector(shape[0], newShape);

            return SHAPELIST(newShape);
        }
    }
}

#endif