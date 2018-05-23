//
//  @author raver119@gmail.com
//

#include <ops/declarable/helpers/dropout.h>
#include <NDArrayFactory.h>
#include <NativeOps.h>
#include <vector>
#include <memory>

namespace nd4j {
namespace ops {
namespace helpers {

    template <typename T>
    int dropOutFunctor(nd4j::random::RandomBuffer* rng, NDArray<T>* input, NDArray<T>* output, NDArray<T>* reduceShape, int seed, T probValue) {
        NativeOps native;

        native.reSeedBuffer(nullptr, (long)seed, rng);
        //if (newRng )
        if (rng == nullptr)
            return ND4J_STATUS_BAD_RNG;

  
        if (reduceShape == nullptr)
            input->template applyRandom<randomOps::DropOutInverted<T>>(rng, nullptr, output, &probValue);
        else {
            REQUIRE_TRUE(reduceShape->lengthOf() <= input->rankOf(), 0, "dropout: Noise shape should be fittable to input");
        
            std::vector<Nd4jLong> dims(reduceShape->lengthOf());
        
            bool fit = true;

#pragma omp parallel
            for( int i = 0; fit && (i < dims.size()); i++ ) {
                dims[i] = (*reduceShape)(i);
                for (int e = 0; fit && (e < input->rankOf()); ++e)
                    if (input->sizeAt(e) % dims[i]) {
                        fit = false;
                    }
            }
        
            // check dims to fit input
            REQUIRE_TRUE(fit, 0, "dropout: Noise shape should fit to input rank.");
            std::unique_ptr<NDArray<T>> chunk(new NDArray<T>('c', dims));
            chunk->assign(T(1.0));
            chunk->template applyRandom<randomOps::DropOutInverted<T>>(rng, nullptr, chunk.get(), &probValue);
        
            // broadcast chunk to full matrix
            std::unique_ptr<NDArray<T>> dropOutMultiplier(new NDArray<T>(*input));
            dropOutMultiplier->assign(T(0.0));
        
            *dropOutMultiplier += *chunk;
        
            input->template applyPairwiseTransform<simdOps::Multiply<T>>(dropOutMultiplier.get(), output, nullptr);
        }

        return ND4J_STATUS_OK;
    }
    template int dropOutFunctor(nd4j::random::RandomBuffer* rng, NDArray<float>* input, NDArray<float>* output, NDArray<float>* reduceShape, int seed, float probValue);
    template int dropOutFunctor(nd4j::random::RandomBuffer* rng, NDArray<float16>* input, NDArray<float16>* output, NDArray<float16>* reduceShape, int seed, float16 probValue);
    template int dropOutFunctor(nd4j::random::RandomBuffer* rng, NDArray<double>* input, NDArray<double>* output, NDArray<double>* reduceShape, int seed, double probValue);

}
}
}