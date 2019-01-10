package io.github.shamrice.wry.wryparser.story.storypage;

public enum PageType {
    PREGAME_PAGE,
    REGULAR_PAGE,
    MULTI_PAGE, //Subs that use ifs and gotos instead of switch statements and subs.
    PASS_THROUGH_PAGE,
    GAMEOVER_PAGE,
    WIN_PAGE,
    ERROR,
    NOT_SET;
}
