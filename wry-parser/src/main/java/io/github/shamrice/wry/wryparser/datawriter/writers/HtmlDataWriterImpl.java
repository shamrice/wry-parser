package io.github.shamrice.wry.wryparser.datawriter.writers;

import io.github.shamrice.wry.wryparser.datawriter.WryDataWriter;
import io.github.shamrice.wry.wryparser.story.Story;
import io.github.shamrice.wry.wryparser.story.storypage.PageChoice.PageChoice;
import io.github.shamrice.wry.wryparser.story.storypage.PageType;
import io.github.shamrice.wry.wryparser.story.storypage.StoryPage;
import org.apache.log4j.Logger;
import org.omg.CosNaming._NamingContextExtStub;
import sun.jvm.hotspot.debugger.linux.LinuxDebugger;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class HtmlDataWriterImpl extends WryDataWriter {

    private static final Logger logger = Logger.getLogger(HtmlDataWriterImpl.class);

    private static final String STORY_PATTERN = "%d";
    private static final String PAGE_PATTERN = "%03d";
    private static final String CHOICE_PATTERN = "%d";

    private static final String START_FILE_NAME = "index.html";
    private static final String EPISODE_START_FILE_NAME = "episode.html";
    private static final String FILE_NAME_EXTENSION = ".html";
    private static final String STORY_START_FILE_NAME_PATTERN = STORY_PATTERN + "_start" + FILE_NAME_EXTENSION;
    private static final String FILE_NAME_PATTERN = STORY_PATTERN + PAGE_PATTERN + FILE_NAME_EXTENSION;

    private static final String FILE_HEADER_START = "<HTML><HEAD><TITLE>Wry On the Web</TITLE>";
    private static final String PAGE_CSS = "" +
            "\t<style type=\"text/css\">\n" +
            "\t\tbody {\n" +
            "\t\t\tfont-family: \"Courier New\", Courier, monospace;\n" +
            "\t\t\tbackground-color: black;\n" +
            "\t\t\tcolor: white;\t\t\n" +
            "\t\t\tfont-weight: bold;\t\n" +
            "\t\t}\n" +
            "\n" +
            "\t\ta {\n" +
            "\t\t\tcolor: white;\n" +
            "\t\t\ttext-decoration: none;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\ta.visited {\n" +
            "\t\t\tcolor: white;\n" +
            "\t\t\ttext-decoration: none;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\ta:hover {\n" +
            "\t\t\tbackground-color: white;\n" +
            "\t\t\tcolor: black;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tp {\n" +
            "\t\t\tmargin: 20px;\n" +
            "\t\t\tmargin-bottom: 0px;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\tol {\n" +
            "\t\t\tmargin-left: 5%;\n" +
            "\t\t}\n" +
            "\t\t.start-link {\n" +
            "\t\t\tmargin-left: 5%;\n" +
            "\t\t}" +
            "\t</style>";

    private static final String TITLE_PAGE_CSS = "" +
            "\t<style type=\"text/css\">\n" +
            "\t\tbody {\n" +
            "\t\t\tfont-family: \"Courier New\", Courier, monospace;\n" +
            "\t\t\tbackground-color: black;\n" +
            "\t\t\tcolor: white;\t\t\n" +
            "\t\t\tfont-weight: bold;\t\n" +
            "\t\t}\n" +
            "\n" +
            "\t\ta {\n" +
            "\t\t\tcolor: #ffff55;\n" +
            "\t\t\ttext-decoration: none;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\ta.visited {\n" +
            "\t\t\tcolor: #ffff55;\n" +
            "\t\t\ttext-decoration: none;\n" +
            "\t\t}\n" +
            "\n" +
            "\t\ta:hover {\n" +
            "\t\t\tbackground-color: #ffff55;\n" +
            "\t\t\tcolor: black;\n" +
            "\t\t}\n" +
            "\t\t\t#container {\n" +
            "\t\t\t\twidth: 100%;\n" +
            "\t\t\t\theight: 100%;\n" +
            "\t\t\t\tposition: absolute;\n" +
            "\t\t\t\tleft: 0px;\n" +
            "\t\t\t\ttop: 0px;\n" +
            "\t\t\t\tz-index: -1;\n" +
            "\t\t\t}" +
            "\t</style>";

    private static final String FILE_HEADER_END = "</HEAD><BODY>";

    private static final String FILE_HEADER = FILE_HEADER_START + PAGE_CSS + FILE_HEADER_END;
    private static final String TITLE_HEADER = FILE_HEADER_START + TITLE_PAGE_CSS + getStarFieldJs() + FILE_HEADER_END;

    private static final String FILE_FOOTER = "</BODY></HTML>";

    @Override
    public void writeDataFiles(List<Story> storyList) {
        logger.info("Writing data files for HTML using output dir: " + outputDir);

        //create title screen
        String titleScreenFileContents = TITLE_HEADER + getTitleScreenHtml() + FILE_FOOTER;
        writeFile(this.outputDir + "/" + START_FILE_NAME, titleScreenFileContents);

        //create episode select page
        String episodeSelectFileContents = FILE_HEADER + "<P>Please select an episode:</p><ol>";
        for (Story story : storyList) {
            episodeSelectFileContents += "<li><a href=\"" + String.format(STORY_START_FILE_NAME_PATTERN, story.getStoryId())
                    + "\">" + story.getStoryName() + "</a></li>";
        }
        episodeSelectFileContents += "</ol><br/>" + FILE_FOOTER;
        writeFile(this.outputDir + "/" + EPISODE_START_FILE_NAME, episodeSelectFileContents);

        //create story pages.
        for (Story story : storyList) {
            for (StoryPage storyPage : story.getPages()) {
                if (storyPage.getPageType().equals(PageType.PREGAME_PAGE)) {
                    String storyStartFile = this.outputDir + "/" + STORY_START_FILE_NAME_PATTERN;
                    storyStartFile = String.format(storyStartFile, storyPage.getSourceStoryId());

                    int destinationPage = storyPage.getPageChoices().get(0).getDestinationPageId();
                    String destinationUrl = String.format(FILE_NAME_PATTERN, storyPage.getSourceStoryId(), destinationPage);

                    String fileContents = FILE_HEADER + "<P>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
                            + storyPage.getPageText().replace("\n", "<br />")
                            + "</P><BR /><div class=\"start-link\"><a href=\"" + destinationUrl + "\">Click here to begin.</a></div><br />"
                            + FILE_FOOTER;
                    writeFile(storyStartFile, fileContents);
                } else {

                    String currentPageFile = String.format(this.outputDir + "/" + FILE_NAME_PATTERN, storyPage.getSourceStoryId(), storyPage.getStoryPageId());
                    String fileContents = FILE_HEADER + "<P>" + storyPage.getPageText().replace("\n", "<br />")
                            + "</P><BR /><OL>";

                    for (PageChoice choice : storyPage.getPageChoices()) {
                        String destinationUrl;
                        String choiceText = choice.getChoiceText();
                        if (choice.getDestinationSubName().equalsIgnoreCase("gameover")) {
                            destinationUrl = START_FILE_NAME;
                            choiceText = "Game over. Click here to continue.";
                        } else {
                            destinationUrl = String.format(FILE_NAME_PATTERN, storyPage.getSourceStoryId(), choice.getDestinationPageId());
                        }
                        fileContents += "<li><a href=\"" + destinationUrl + "\">" + choiceText + "</a></li>";
                    }

                    fileContents += "</OL><br />" + FILE_FOOTER;
                    writeFile(currentPageFile, fileContents);
                }
            }
        }
    }

    private void writeFile(String filename, String fileContents) {
        logger.info("Writing output file: " + filename + " :: contents: " + fileContents);
        try {
            FileWriter fileWriter = new FileWriter(filename);
            fileWriter.write(fileContents);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            logger.error("Error creating output file: " + filename + " :: " + ex.getMessage(), ex);
        }
    }

    private String getTitleScreenHtml() {

        String companyText = " " +
                "                 (c) Nukem Enterprises and Sim Creations Inc\n" +
                "\n";
        String titleText = "" +
                "           ÛÛÛÛ              ±ÛÛÛÛ   ÛÛÛÛÛÛÛÛÛ      ÛÛÛ      ÛÛÛ\n" +
                "           ±ÛÛÛÛ            ±ÛÛÛÛ   ±ÛÛÛ±±±±±ÛÛÛ    ±ÛÛÛ   ±ÛÛÛ\n" +
                "            ±ÛÛÛÛ          ±ÛÛÛÛ    ±ÛÛÛ    ±ÛÛÛ     ±ÛÛÛ ±ÛÛÛ\n" +
                "             ±ÛÛÛÛ        ±ÛÛÛÛ     ±ÛÛÛÛÛÛÛÛÛ        ±ÛÛÛÛÛ\n" +
                "              ±ÛÛÛÛ  ±Û  ±ÛÛÛÛ      ±ÛÛÛ    ±ÛÛÛ       ±ÛÛÛ\n" +
                "               ±ÛÛÛÛ±ÛÛÛ±ÛÛÛÛ       ±ÛÛÛ     ±ÛÛÛ     ±ÛÛÛ\n" +
                "                ±ÛÛÛÛÛ±ÛÛÛÛÛ        ±ÛÛÛ      ±ÛÛÛ   ±ÛÛÛ\n" +
                "                 ±ÛÛÛ  ±ÛÛÛ         ±±±       ±±±    ±±±\n" +
                "\n ";
        String subTitleText = "" +
                "                            A TEXTED BASED COMEDY\n" +
                "                                  Ver. 1.412\n" +
                "\n " +
                "\n";

        String gotoMenuText = "" +
                "                        MENU_URL\n" +
                "\n " +
                "\n" +
                "\n ";

        String copyWriteText = "" +
                "       WRY (R) is copyright of Nukem Enterprises and Sim Creations Inc.\n" +
                "                                   (c) 2000\n" +
                "\n";

        companyText = companyText.replaceAll("\\n", "<br>").replaceAll("\\s", "&nbsp;");
        titleText = titleText.replaceAll("\\n", "<br>").replaceAll("\\s", "&nbsp;").replaceAll("Û", "&block;").replaceAll("±", "&blk34;");
        subTitleText = subTitleText.replaceAll("\\n", "<br>").replaceAll("\\s", "&nbsp;");
        gotoMenuText = gotoMenuText.replaceAll("\\n", "<br>").replaceAll("\\s", "&nbsp;");
        copyWriteText = copyWriteText.replaceAll("\\n", "<br>").replaceAll("\\s", "&nbsp;");

        gotoMenuText = gotoMenuText.replace("MENU_URL", "<a href=\"" + EPISODE_START_FILE_NAME + "\">CLICK HERE TO GO TO THE MENU</a>");

        companyText = "<div style=\"color:#00aaaa;\">" + companyText + "</div>";
        titleText = "<div style=\"color:#aa5500;\">" + titleText + "</div>";
        subTitleText = "<div style=\"color:#ffffff;\">" + subTitleText + "</div>";
        gotoMenuText = "<div style=\"color:#ffff55;\">" + gotoMenuText + "</div>";
        copyWriteText = "<div style=\"color:#555555;\">" + copyWriteText + "</div>";

        String starFieldStart = "\t<div id=\"container\"></div>\n" +
                "\t<script src=\"starfield.js\"></script>\n" +
                "\t<script>\n" +
                "\t  //  Get the container and turn it into a starfield.\n" +
                "\t\tvar container = document.getElementById('container');\n" +
                "\t\tvar starfield = new Starfield();\n" +
                "\t\tstarfield.initialise(container);\n" +
                "\t\tstarfield.start();\n" +
                "\t</script>";

        return starFieldStart + companyText + titleText + subTitleText + gotoMenuText + copyWriteText;

    }

    public static String getStarFieldJs() {
        return "" +
                "<script type=\"text/javascript\">" +
                "/*\n" +
                "\tStarfield lets you take a div and turn it into a starfield.\n" +
                "*/\n" +
                "\n" +
                "//\tDefine the starfield class.\n" +
                "function Starfield() {\n" +
                "\tthis.fps = 30;\n" +
                "\tthis.canvas = null;\n" +
                "\tthis.width = 0;\n" +
                "\tthis.height = 0;\n" +
                "\tthis.minVelocity = 15;\n" +
                "\tthis.maxVelocity = 30;\n" +
                "\tthis.stars = 100;\n" +
                "\tthis.intervalId = 0;\n" +
                "}\n" +
                "\n" +
                "//\tThe main function - initialises the starfield.\n" +
                "Starfield.prototype.initialise = function(div) {\n" +
                "\tvar self = this;\n" +
                "\n" +
                "\t//\tStore the div.\n" +
                "\tthis.containerDiv = div;\n" +
                "\tself.width = window.innerWidth;\n" +
                "\tself.height = window.innerHeight;\n" +
                "\n" +
                "\twindow.addEventListener('resize', function resize(event) {\n" +
                "\t\tself.width = window.innerWidth;\n" +
                "\t\tself.height = window.innerHeight;\n" +
                "\t\tself.canvas.width = self.width;\n" +
                "\t\tself.canvas.height = self.height;\n" +
                "\t\tself.draw();\n" +
                "\t});\n" +
                "\n" +
                "\t//\tCreate the canvas.\n" +
                "\tvar canvas = document.createElement('canvas');\n" +
                "\tdiv.appendChild(canvas);\n" +
                "\tthis.canvas = canvas;\n" +
                "\tthis.canvas.width = this.width;\n" +
                "\tthis.canvas.height = this.height;\n" +
                "};\n" +
                "\n" +
                "Starfield.prototype.start = function() {\n" +
                "\n" +
                "\t//\tCreate the stars.\n" +
                "\tvar stars = [];\n" +
                "\tfor(var i=0; i<this.stars; i++) {\n" +
                "\t\tstars[i] = new Star(Math.random()*this.width, Math.random()*this.height, Math.random()*3+1,\n" +
                "\t\t (Math.random()*(this.maxVelocity - this.minVelocity))+this.minVelocity);\n" +
                "\t}\n" +
                "\tthis.stars = stars;\n" +
                "\n" +
                "\tvar self = this;\n" +
                "\t//\tStart the timer.\n" +
                "\tthis.intervalId = setInterval(function() {\n" +
                "\t\tself.update();\n" +
                "\t\tself.draw();\t\n" +
                "\t}, 1000 / this.fps);\n" +
                "};\n" +
                "\n" +
                "Starfield.prototype.stop = function() {\n" +
                "\tclearInterval(this.intervalId);\n" +
                "};\n" +
                "\n" +
                "Starfield.prototype.update = function() {\n" +
                "\tvar dt = 1 / this.fps;\n" +
                "\n" +
                "\tfor(var i=0; i<this.stars.length; i++) {\n" +
                "\t\tvar star = this.stars[i];\n" +
                "\t\tstar.y += dt * star.velocity;\n" +
                "\t\t//\tIf the star has moved from the bottom of the screen, spawn it at the top.\n" +
                "\t\tif(star.y > this.height) {\n" +
                "\t\t\tthis.stars[i] = new Star(Math.random()*this.width, 0, Math.random()*3+1, \n" +
                "\t\t \t(Math.random()*(this.maxVelocity - this.minVelocity))+this.minVelocity);\n" +
                "\t\t}\n" +
                "\t}\n" +
                "};\n" +
                "\n" +
                "Starfield.prototype.draw = function() {\n" +
                "\n" +
                "\t//\tGet the drawing context.\n" +
                "\tvar ctx = this.canvas.getContext(\"2d\");\n" +
                "\n" +
                "\t//\tDraw the background.\n" +
                " \tctx.fillStyle = '#000000';\n" +
                "\tctx.fillRect(0, 0, this.width, this.height);\n" +
                "\n" +
                "\t//\tDraw stars.\n" +
                "\tctx.fillStyle = '#ffffff';\n" +
                "\tfor(var i=0; i<this.stars.length;i++) {\n" +
                "\t\tvar star = this.stars[i];\n" +
                "\t\tctx.fillRect(star.x, star.y, star.size, star.size);\n" +
                "\t}\n" +
                "};\n" +
                "\n" +
                "function Star(x, y, size, velocity) {\n" +
                "\tthis.x = x;\n" +
                "\tthis.y = y; \n" +
                "\tthis.size = size;\n" +
                "\tthis.velocity = velocity;\n" +
                "}</script>";
    }
}
