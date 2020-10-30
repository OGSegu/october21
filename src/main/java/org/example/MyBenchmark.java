/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.example;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MyBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param({"0", "100000", "200000", "300000", "400000", "500000",
                "600000", "700000", "800000"})
        public int listSize;

        public int[] testList;

        @Setup(Level.Trial)
        public void setUp() {
            testList = new Random()
                    .ints()
                    .limit(listSize)
                    .toArray();
        }
    }

    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1, time = 500, timeUnit = MILLISECONDS)
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(value = MICROSECONDS)
    @Measurement(iterations = 5, time = 200, timeUnit = MILLISECONDS)
    public void atomicLong(Blackhole blackhole, BenchmarkState state) {
        int[] array = state.testList.clone();
        heapSort(array);
        blackhole.consume(array);
    }

    private static final Random random = new Random(Calendar.getInstance().getTimeInMillis());

    private static int partition(int[] elements, int min, int max) {
        int x = elements[min + random.nextInt(max - min + 1)];
        int left = min, right = max;
        while (left <= right) {
            while (elements[left] < x) {
                left++;
            }
            while (elements[right] > x) {
                right--;
            }
            if (left <= right) {
                int temp = elements[left];
                elements[left] = elements[right];
                elements[right] = temp;
                left++;
                right--;
            }
        }
        return right;
    }

    private static void quickSort(int[] elements, int min, int max) {
        if (min < max) {
            int border = partition(elements, min, max);
            quickSort(elements, min, border);
            quickSort(elements, border + 1, max);
        }
    }

    public static void quickSort(int[] elements) {
        quickSort(elements, 0, elements.length - 1);
    }

    private static void heapify(int[] elements, int start, int length) {
        int left = 2 * start + 1;
        int right = left + 1;
        int max = start;
        if (left < length && elements[left] > elements[max]) {
            max = left;
        }
        if (right < length && elements[right] > elements[max]) {
            max = right;
        }
        if (max != start) {
            int temp = elements[max];
            elements[max] = elements[start];
            elements[start] = temp;
            heapify(elements, max, length);
        }
    }

    private static void buildHeap(int[] elements) {
        for (int start = elements.length / 2 - 1; start >= 0; start--) {
            heapify(elements, start, elements.length);
        }
    }

    public static void heapSort(int[] elements) {
        buildHeap(elements);
        for (int j = elements.length - 1; j >= 1; j--) {
            int temp = elements[0];
            elements[0] = elements[j];
            elements[j] = temp;
            heapify(elements, 0, j);
        }
    }

}
