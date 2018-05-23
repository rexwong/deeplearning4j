//
// @author raver119@gmail.com
//

#ifndef LIBND4J_GRAPHEXECUTIONER_H
#define LIBND4J_GRAPHEXECUTIONER_H

#include <graph/generated/node_generated.h>
#include <graph/generated/graph_generated.h>

#include <graph/Variable.h>
#include <graph/VariableSpace.h>
#include <graph/Node.h>
#include <graph/Graph.h>
#include <sys/stat.h>

#define TF_INPUT "Placeholder"
#define TF_CONST "Const"
#define TF_VAR "VariableV2"

namespace nd4j {
    namespace graph {

    template <typename T>
    class GraphExecutioner {
    protected:


    public:
        //static Nd4jStatus executeFlatNode(nd4j::graph::Graph *graph, nd4j::graph::Node *node, nd4j::graph::VariableSpace<float> *variableSpace);

        static Nd4jStatus executeFlatNode(Graph<T> *graph, Node<T> *node, VariableSpace<T> *variableSpace);

        /**
        * This method executes given Graph
        * @return
        */
        static Nd4jStatus execute(Graph<T> *graph, VariableSpace<T>* variableSpace = nullptr);


        /**
        * This method executes graph stored at given FlatBuffers pointer
        *
        * @param pointer Pointer to FlatBuffer
        * @return pointer to FlatBuffer with result
        */
        static Nd4jPointer executeFlatBuffer(Nd4jPointer pointer);


        static Graph<T> *importFromTensorFlow(const char *fileName);


        static Graph<T> *importFromFlatBuffers(const char *filename);

        static Graph<T> *importFromFlatPointer(Nd4jPointer ptr);
    };

    long getFileSize(const char * filename);
    uint8_t* readFlatBuffers(const char * filename);

    }
}


#endif //LIBND4J_GRAPHEXECUTIONER_H
