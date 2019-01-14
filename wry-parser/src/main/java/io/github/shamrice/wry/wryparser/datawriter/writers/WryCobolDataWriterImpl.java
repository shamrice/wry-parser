package io.github.shamrice.wry.wryparser.datawriter.writers;

import io.github.shamrice.wry.wryparser.datawriter.WryDataWriter;
import io.github.shamrice.wry.wryparser.story.Story;
import org.apache.log4j.Logger;

import java.util.List;

public class WryCobolDataWriterImpl extends WryDataWriter {

    private static final Logger logger = Logger.getLogger(WryCobolDataWriterImpl.class);

    @Override
    public void writeDataFiles(List<Story> storyList) {
        logger.info("Writing data files for Wry Cobol using output dir: " + outputDir);
    }


}
