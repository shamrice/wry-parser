package io.github.shamrice.wry.wryparser.datawriter;

import io.github.shamrice.wry.wryparser.datawriter.datatypes.OutputDataTypes;
import io.github.shamrice.wry.wryparser.story.Story;
import org.apache.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public abstract class WryDataWriter {

    private static final Logger logger = Logger.getLogger(WryDataWriter.class);

    protected String outputDir;
    OutputDataTypes outputDataType = OutputDataTypes.NOT_SET;

    void setOutputDir(String outputDir) {

        if (outputDir == null || outputDir.isEmpty()) {
            Path currentRelativePath = Paths.get("");
            outputDir = currentRelativePath.toAbsolutePath().toString();
        }

        this.outputDir = outputDir;
        logger.info("Setting output directory to: " + outputDir);
    }

    void setOutputDataType(OutputDataTypes outputDataType) {
        this.outputDataType = outputDataType;
        logger.info("Setting output data type to: " + outputDataType.name());
    }


    public abstract void writeDataFiles(List<Story> storyList);
}
