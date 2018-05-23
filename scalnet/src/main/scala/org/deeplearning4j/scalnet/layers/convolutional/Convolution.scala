/*
 * Copyright 2016 Skymind
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deeplearning4j.scalnet.layers.convolutional

import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException
import org.deeplearning4j.scalnet.layers.core.Node

/**
  * Base class for convolutional layers.
  *
  * @author David Kale
  */
abstract class Convolution(protected val kernelSize: List[Int],
                           protected val stride: List[Int],
                           protected val padding: List[Int],
                           protected val nChannels: Int = 0,
                           protected val nIn: Option[List[Int]] = None,
                           protected val nFilter: Int = 0)
    extends Node {

  override def inputShape: List[Int] = nIn.getOrElse(List(nChannels))

  if (kernelSize.lengthCompare(stride.length) != 0 || kernelSize.lengthCompare(padding.length) != 0) {
    throw new IllegalArgumentException("Kernel, stride, and padding must all have same shape.")
  }

  private def validateShapes(inHeight: Int,
                             inWidth: Int,
                             kernelHeight: Int,
                             kernelWidth: Int,
                             strideHeight: Int,
                             strideWidth: Int,
                             padHeight: Int,
                             padWidth: Int): Unit = {

    // Check filter > size + padding
    if (kernelHeight > (inHeight + 2 * padHeight))
      throw new InvalidInputTypeException(
        s"Invalid input: activations into layer are h=$inHeight but kernel size is $kernelHeight with padding $padHeight"
      )

    if (kernelWidth > (inWidth + 2 * padWidth))
      throw new InvalidInputTypeException(
        s"Invalid input: activations into layer are w=$inWidth but kernel size is $kernelWidth with padding $padWidth"
      )

    // Check stride
    if ((strideHeight <= 0) || (strideWidth <= 0))
      throw new InvalidInputTypeException(
        s"Invalid stride: strideHeight is $strideHeight and strideWidth is $strideWidth and both should be great than 0"
      )
  }

  override def outputShape: List[Int] = {
    val nOutChannels: Int =
      if (nFilter > 0) nFilter
      else if (inputShape.nonEmpty) inputShape.last
      else 0
    if (inputShape.lengthCompare(3) == 0) {
      validateShapes(inputShape.head,
                     inputShape.tail.head,
                     kernelSize.head,
                     kernelSize.tail.head,
                     stride.head,
                     stride.tail.head,
                     padding.head,
                     padding.tail.head)
      List[List[Int]](inputShape.init, kernelSize, padding, stride).transpose
        .map(x => (x.head - x(1) + 2 * x(2)) / x(3) + 1) :+ nOutChannels
    } else if (nOutChannels > 0) List(nOutChannels)
    else List()
  }
}
