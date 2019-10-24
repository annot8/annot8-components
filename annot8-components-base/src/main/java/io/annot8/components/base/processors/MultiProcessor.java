/*
 * Crown Copyright (C) 2019 Dstl
 *
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
 */
package io.annot8.components.base.processors;

import io.annot8.api.components.Processor;
import io.annot8.api.components.responses.ProcessorResponse;
import io.annot8.api.data.Item;
import io.annot8.common.components.AbstractProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class MultiProcessor extends AbstractProcessor {

  private Collection<Processor> processors;

  public MultiProcessor(){
    this.processors = new ArrayList<>();
  }

  public MultiProcessor(Processor... processors){
    this.processors = Arrays.asList(processors);
  }

  public MultiProcessor(Collection<Processor> processors){
    this.processors = processors;
  }

  protected void addProcessor(Processor processor){
    this.processors.add(processor);
  }

  @Override
  public ProcessorResponse process(Item item) {
    boolean error = false;
    Collection<Exception> exceptions = new ArrayList<>();

    for(Processor p : processors){
      ProcessorResponse response = p.process(item);
      if(response.getStatus() != ProcessorResponse.Status.OK){
        error = true;
        exceptions.addAll(response.getExceptions());
      }
    }

    if(error){
      return ProcessorResponse.processingError(exceptions);
    }

    return ProcessorResponse.ok();
  }

  @Override
  public void close() {
    for(Processor p : processors){
      p.close();
    }
  }
}
