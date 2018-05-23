//
// Created by raver119 on 06.11.2017.
//

#include <op_boilerplate.h>
#if NOT_EXCLUDED(OP_scatter_list)

#include <ops/declarable/CustomOperations.h>
#include <helpers/ShapeUtils.h>

namespace nd4j {
    namespace ops {
        LIST_OP_IMPL(scatter_list, 1, 1, 0, -2) {
            NDArrayList<T> *list = nullptr;
            NDArray<T>* array = nullptr;
            NDArray<T>* indices = nullptr;

            bool hasList = false;

            if (block.width() == 3){
                list = INPUT_LIST(0);
                array = INPUT_VARIABLE(1);
                indices = INPUT_VARIABLE(2);
                hasList = true;
            } else {
                array = INPUT_VARIABLE(0);
                indices = INPUT_VARIABLE(1);
                list = new NDArrayList<T>(indices->lengthOf(), false);
                block.trackList(list);
            }

            REQUIRE_TRUE(indices->isVector(), 0, "ScatterList: Indices for Scatter should be a vector")
            REQUIRE_TRUE(indices->lengthOf() == array->sizeAt(0), 0, "ScatterList: Indices length should be equal number of TADs along dim0, but got %i instead", indices->lengthOf());

            std::vector<int> axis = ShapeUtils<T>::convertAxisToTadTarget(array->rankOf(), {0});
            auto tads = NDArrayFactory<T>::allTensorsAlongDimension(array, axis);
            for (int e = 0; e < tads->size(); e++) {
                auto idx = (int) indices->getIndexedScalar(e);
                if (idx >= tads->size())
                    return ND4J_STATUS_BAD_ARGUMENTS;

                auto arr = tads->at(e)->dup(array->ordering());
                auto res = list->write(idx, arr);
                if (res != ND4J_STATUS_OK)
                    return res;
            }

            if (list == nullptr) {
                OVERWRITE_RESULT(list);
            }

            delete tads;

            return ND4J_STATUS_OK;
        }
        DECLARE_SYN(TensorArrayScatterV3, scatter_list);
        DECLARE_SYN(tensorarrayscatterv3, scatter_list);
    }
}

#endif