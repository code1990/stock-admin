package com.aidex.generator.service;

import com.aidex.generator.domain.GeneratedFile;
import com.aidex.generator.domain.GeneratorRequest;
import java.util.List;

public interface GeneratorService
{
    List<GeneratedFile> preview(GeneratorRequest request);

    List<GeneratedFile> generate(GeneratorRequest request);

    byte[] generateZip(GeneratorRequest request);
}
