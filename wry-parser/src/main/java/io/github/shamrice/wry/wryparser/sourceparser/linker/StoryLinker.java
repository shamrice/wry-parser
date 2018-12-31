package io.github.shamrice.wry.wryparser.sourceparser.linker;

import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;

import java.util.List;

public class StoryLinker {

    private final static Logger logger = Logger.getLogger(StoryLinker.class);

    public Story link(Story story, List<StoryPage> storyPages) {
        logger.info("Linking pages to story : " + story.getStoryName());



        return story;
    }

}
