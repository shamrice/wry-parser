package io.github.shamrice.wry.wryparser.datawriter;

import io.github.shamrice.wry.wryparser.datawriter.datatypes.OutputDataTypes;
import io.github.shamrice.wry.wryparser.datawriter.writers.WryCobolDataWriterImpl;
import org.apache.log4j.Logger;


public class WryDataWriterFactory  {

    private static final Logger logger = Logger.getLogger(WryDataWriterFactory.class);

    private String outputDir;

    public WryDataWriterFactory(String outputDir) {
        this.outputDir = outputDir;
    }

    public WryDataWriter makeDataWriter(OutputDataTypes outputDataType) {

        WryDataWriter wryDataWriter = null;

        switch (outputDataType) {
            case WRY_COBOL:
                logger.info("Creating new Wry COBOL Data Writer.");
                wryDataWriter = new WryCobolDataWriterImpl();
                wryDataWriter.setOutputDataType(outputDataType);
                wryDataWriter.setOutputDir(outputDir);
                break;

            default:
                logger.error("Cannot create data writer of type " + outputDataType.name());
                break;

        }

        return wryDataWriter;
    }

}
