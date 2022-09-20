package io.github.shamrice.wry.wryparser.sourceparser.stories;

import io.github.shamrice.wry.wryparser.sourceparser.choices.ChoiceParser;
import io.github.shamrice.wry.wryparser.sourceparser.pages.validate.PageValidator;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.github.shamrice.wry.wryparser.sourceparser.constants.ParseConstants.*;

public class StoryBuilderImpl implements StoryBuilder {

    private static final Logger logger = LogManager.getLogger(StoryBuilderImpl.class);

    private Map<String, List<String>> rawSubData;

    public StoryBuilderImpl(Map<String, List<String>> rawSubData) {
        this.rawSubData = rawSubData;
    }

    @Override
    public List<Story> build() {

        /*
            There are two story choice screens. The unlocked one that has the destination for episode 4
            lacks the pregame screens and the locked one lacks the link to the pregame of episode 4. To get
            around this, we have to comb through both of these subs to find the pregame screen destinations
        */

        List<Story> storyData = new ArrayList<>();

        List<String> rawStorySubDataLocked = rawSubData.get(STORY_SUB_NAME1);
        List<String> rawStorySubDataUnlocked = rawSubData.get(STORY_SUB_NAME2);

        ChoiceParser choiceParser = new ChoiceParser();

        List<PageChoice> storyChoicesLocked = choiceParser.getChoicesForSub(rawStorySubDataLocked);
        List<PageChoice> storyChoicesUnlocked = choiceParser.getChoicesForSub(rawStorySubDataUnlocked);

        storyChoicesLocked.addAll(storyChoicesUnlocked);

        for (PageChoice storyChoice : storyChoicesLocked) {

            if (PageValidator.isPreGameScreen(storyChoice.getDestinationSubName())) {

                String storyChoiceText = storyChoice.getChoiceText();
                storyChoiceText = storyChoiceText.replace(EPISODE_4_UNLOCKED_TEXT, "");

                Story story = new Story(storyChoice.getChoiceId(), storyChoiceText);
                story.setFirstPageSubName(storyChoice.getDestinationSubName());

                //add story to story data to return
                storyData.add(story);

                logger.info("generateStories :: Added story id " + story.getStoryId() + " : "
                        + story.getStoryName() + " to storyData. First sub name= "
                        + story.getFirstPageSubName());
            }
        }

        return storyData;

    }
}
