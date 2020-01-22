package com.company;

import java.util.UUID;

public class Filemaker {
    public int type;
    public String title;
    private final String titleread;
    private final String author;
    private final String authorread;
    private final String publisher;
    private final int width;
    private final int height;

    public Filemaker(String title,String titleread,String author,String authorread,String publisher,int width,int height,int type){

        this.title = title;
        this.titleread = titleread;
        this.author = author;
        this.authorread = authorread;
        this.publisher = publisher;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public String getFixed() {
        return "@charset \"UTF-8\";\n" +
                "\n" +
                "html,\n" +
                "body {\n" +
                "  margin: 0;\n" +
                "  padding: 0;\n" +
                "  font-size: 0;\n" +
                "}\n" +
                "svg {\n" +
                "  margin: 0;\n" +
                "  padding: 0;\n" +
                "}\n" +
                "svg.rightpage{\n" +
                "  padding-left:0px;\n" +
                "}\n" +
                "svg.leftpage{\n" +
                "  padding-right:0px;\n" +
                "}\n" +
                "svg.centerpage{\n" +
                "  padding-left:0px;\n" +
                "  padding-right:0px;\n" +
                "}\n";
    }

    public String getTocxhtml() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!DOCTYPE html>\n" +
                "<html xmlns = \"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xml:lang=\"ja\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\" />\n" +
                "<title>Navigation</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<nav epub:type=\"toc\" id=\"toc\">\n" +
                "<h1>"+title+"</h1>\n" +
                "<ol>\n" +
                "<li><a href=\"text/b_0000.xhtml\">"+title+"</a></li>\n" +
                "\n" +
                "</ol>\n" +
                "</nav>\n" +
                "</body>\n" +
                "</html>\n";

    }

    public String getTocncx(){
        String uuid=UUID.randomUUID().toString();
        return "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\">\n" +
                "\t<head>\n" +
                "\t\t<meta name=\"dtb:uid\" content=\"urn:uuid:"+uuid+"\" />\n" +
                "\t\t<meta name=\"dtb:depth\" content=\"1\" />\n" +
                "\t\t<meta name=\"dtb:totalPageCount\" content=\"0\" />\n" +
                "\t\t<meta name=\"dtb:maxPageNumber\" content=\"0\" />\n" +
                "\t</head>\n" +
                "\t<docTitle>\n" +
                "\t\t<text>"+title+"</text>\n" +
                "\t</docTitle>\n" +
                "\t<docAuthor>\n" +
                "\t\t<text></text>\n" +
                "\t</docAuthor>\n" +
                "\t<navMap>\n" +
                "\t\t<navPoint id=\"navPoint-1\" playOrder=\"1\">\n" +
                "\t\t\t<navLabel>\n" +
                "\t\t\t\t<text>"+title+"</text>\n" +
                "\t\t\t</navLabel>\n" +
                "\t\t\t<content src=\"text/b_0000.xhtml#top\" />\n" +
                "\t\t</navPoint>\n" +
                "\t</navMap>\n" +
                "</ncx>\n";
    }

    public String getxhtmls(String title,String name,int width,int height,int page){
        String offset="centerpage";
        if(page>0) {
            switch (type) {
                case 0:
                    offset = page%2==0 ? "leftpage":"rightpage";
                    break;
                case 1:
                    offset = page%2==1 ? "leftpage":"rightpage";
                    break;
            }
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xml:lang=\"ja\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\"/>\n" +
                "<title>"+title+"</title>\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"../styles/fixed.css\"/>\n" +
                "<meta name=\"viewport\" content=\"width="+width+", height="+height+"\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"top\">\n" +
                "<svg class=\""+offset+"\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n" +
                " xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                " width=\"100%\" height=\"100%\" viewBox=\"0 0 "+width+" "+height+"\">\n" +
                "<image width=\""+width+"\" height=\""+height+"\" xlink:href=\"../images/"+name+"\"/>\n" +
                "</svg>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n";
    }

    public String getName(int page){
        return String.format("%04d",page);
    }

}
