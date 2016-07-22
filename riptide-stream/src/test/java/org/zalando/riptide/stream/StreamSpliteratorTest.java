package org.zalando.riptide.stream;

/*
 * ⁣​
 * Riptide: Stream
 * ⁣⁣
 * Copyright (C) 2015 - 2016 Zalando SE
 * ⁣⁣
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
 * ​⁣
 */

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public final class StreamSpliteratorTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldNotSupportParallelExecution() {
        assertNull( new StreamSpliterator<>(null, null, null).trySplit());
    }

    @Test
    public void shouldNotPredictEstimateSize() {
        assertThat(new StreamSpliterator<>(null, null, null).estimateSize(), is(Long.MAX_VALUE));
    }
}