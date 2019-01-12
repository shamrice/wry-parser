package io.github.shamrice.wry.wryparser.sourceparser.linker;

import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;

import java.util.List;

public interface StoryLinker {

    void linkDestinationPageIdsToChoices(List<StoryPage> storyPages);
    void link(Story story, List<StoryPage> storyPages);
}
