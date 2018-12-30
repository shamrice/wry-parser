package io.github.shamrice.wry.wryparser.filter.exclude;

import java.util.ArrayList;
import java.util.List;

public class ExcludedSubFilter {

    private List<String> excludedSubNames = new ArrayList<>();

    //TODO : build this like the other exclude filter.
    public ExcludedSubFilter() {
        excludedSubNames.add("pregame");
        excludedSubNames.add("pregame2");
        excludedSubNames.add("pregame3");
        excludedSubNames.add("demome");
        excludedSubNames.add("gameselect2");
        excludedSubNames.add("ldemo");
        excludedSubNames.add("ldemo2");
        excludedSubNames.add("sp2tm");
        excludedSubNames.add("sp3tm");
        excludedSubNames.add("estory");
        excludedSubNames.add("theme");
        excludedSubNames.add("gameovermu");
        excludedSubNames.add("credmus");
        excludedSubNames.add("abouterik");
        excludedSubNames.add("abouteug");
        excludedSubNames.add("faq");
        excludedSubNames.add("quit");
        excludedSubNames.add("whatmyname");
        excludedSubNames.add("crediz");
        excludedSubNames.add("aboutselect");
        excludedSubNames.add("eugabout");
        excludedSubNames.add("storyselect");
        excludedSubNames.add("eugstory3");
        excludedSubNames.add("gameselect");
        excludedSubNames.add("majorsecret");
        excludedSubNames.add("credits");
        excludedSubNames.add("stupidpoll");
        excludedSubNames.add("stupidbabble");
        excludedSubNames.add("deadman"); //maybe? invalid choice brings back to menu.
        excludedSubNames.add("gameover"); //maybe? game over final screen w/ music
        excludedSubNames.add("pretitle");
        excludedSubNames.add("menu02");
        excludedSubNames.add("gamesecrets5"); //maybe? valid choice but shows cheats that will not work
        excludedSubNames.add("menu01");
        excludedSubNames.add("aboutus3");
        excludedSubNames.add("readthis4");
        excludedSubNames.add("story2");
        excludedSubNames.add("title");
        excludedSubNames.add("secretStory");
        excludedSubNames.add("haha1");
        excludedSubNames.add("singopra"); //maybe? transition screen to singopra2
    }

    public boolean isSubExcludedFromPages(String subName) {
        return excludedSubNames.contains(subName);
    }
}
