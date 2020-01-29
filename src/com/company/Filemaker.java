package com.company;

import java.util.Calendar;
import java.util.UUID;

public class Filemaker {
    public int type;
    public String title;
    private final String titleread;
    private final String author;
    private final String authorread;
    private final String publisher;
    private final int[] widths;
    private final int[] heights;
    private String uuid;
    private final boolean checked;

    public Filemaker(String title, String titleread, String author, String authorread, String publisher, int[] width, int[] height, int type, boolean checked){

        this.title = title;
        this.titleread = titleread;
        this.author = author;
        this.authorread = authorread;
        this.publisher = publisher;
        this.widths = width;
        this.heights = height;
        this.type = type;
        this.checked = checked;
    }

    public String getFixed() {
        return "@charset \"utf-8\";\n" +
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
        uuid=UUID.randomUUID().toString();
        return "<?xml version='1.0' encoding='utf-8'?>\n" +
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

    public String getxhtmls(int page,String extension){
        String name=getName(page);
        String offset=getOffset(page,type,checked)+"page";
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!DOCTYPE html>\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xml:lang=\"ja\">\n" +
                "<head>\n" +
                "<meta charset=\"UTF-8\"/>\n" +
                "<title>"+title+"</title>\n" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"../styles/fixed.css\"/>\n" +
                "<meta name=\"viewport\" content=\"width="+widths[page]+", height="+heights[page]+"\"/>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div id=\"top\">\n" +
                "<svg class=\""+offset+"\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"\n" +
                " xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                " width=\"100%\" height=\"100%\" viewBox=\"0 0 "+widths[page]+" "+heights[page]+"\">\n" +
                "<image width=\""+widths[page]+"\" height=\""+heights[page]+"\" xlink:href=\"../images/image-"+name+extension+"\"/>\n" +
                "</svg>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n";
    }

    public String getName(int page){
        return String.format("%04d",page);
    }

    public String getOffset(int page,int type,boolean checked){
        String offset="center";
        if(type<=1) {
            if(checked){
            if (page > 0) {
                switch (type) {
                    case 0:
                        offset = page % 2 == 0 ? "left" : "right";
                        break;
                    case 1:
                        offset = page % 2 == 1 ? "left" : "right";
                        break;
                }
            }
            }else{
                switch (type) {
                    case 1:
                        offset = page % 2 == 0 ? "left" : "right";
                        break;
                    case 0:
                        offset = page % 2 == 1 ? "left" : "right";
                        break;
                }
            }
        }
        return offset;
    }

    public String getStandard(int pages,String extension){
        Calendar calendar= Calendar.getInstance();
        String date=""+calendar.get(Calendar.YEAR)+"-"+String.format("%02d",calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)
                +"T"+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)+"Z";
        String layout="";
        String direction="";
        switch(type){
            case 0:
            case 2:
                layout="rl";
                direction="rtl";
                break;
            case 1:
            case 3:
                layout="lr";
                direction="ltr";
                break;
        }
        StringBuffer str=new StringBuffer
                ("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<package xmlns=\"http://www.idpf.org/2007/opf\" version=\"3.0\" xml:lang=\"ja\" unique-identifier=\"pub-id\" prefix=\"rendition: http://www.idpf.org/vocab/rendition/#\">\n" +
                "<metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" +
                "<!-- TITLE -->\n" +
                "<dc:title id=\"title\">"+title+"</dc:title>\n" +
                "<meta refines=\"#title\" property=\"file-as\">"+titleread+"</meta>\n" +
                "<!-- AUTHOR --><dc:creator id=\"creator1\">"+author+"</dc:creator>\n" +
                "<meta refines=\"#creator1\" property=\"role\" scheme=\"marc:relators\">aut</meta>\n" +
                "<meta refines=\"#creator1\" property=\"file-as\">"+authorread+"</meta>\n" +
                "<meta refines=\"#creator1\" property=\"alternate-script\" xml:lang=\"ja-kana-jp\">"+authorread+"</meta>\n" +
                "<meta refines=\"#creator1\" property=\"display-seq\">1</meta>\n" +
                "\n" +
                "<!-- PUBLISHER -->\n" +
                "<dc:publisher>"+publisher+"</dc:publisher>\n" +
                "\n" +
                "<!-- LANGUAGE -->\n" +
                "<dc:language>ja</dc:language>\n" +
                "\n" +
                "<!-- FILE ID -->\n" +
                "<dc:identifier id=\"pub-id\">urn:uuid:"+uuid+"</dc:identifier>\n" +
                "\n" +
                "<!-- MODIFIED DATE -->\n" +
                "<meta property=\"dcterms:modified\">"+date+"</meta>\n" +
                "<dc:date>"+date+"</dc:date>\n" +
                "<!-- Fixed-Layout Documents -->\n" +
                "<meta property=\"rendition:layout\">pre-paginated</meta>\n" +
                "<meta property=\"rendition:spread\">landscape</meta>\n" +
                "<meta name=\"fixed-layout\" content=\"true\" />\n" +
                "<meta name=\"orientation-lock\" content=\"none\" />\n" +
                "<meta name=\"original-resolution\" content=\"1045x1500\" />\n" +
                "<meta name=\"book-type\" content=\"comic\" />\n" +
                "<meta name=\"primary-writing-mode\" content=\"horizontal-"+layout+"\" />\n" +
                "<meta name=\"SpineColor\" content=\"#ffffff\" />\n" +
                "\n" +
                "<!-- etc. -->\n" +
                "<meta name=\"cover\" content=\"cover\" />\n" +
                "<dc:type>comic</dc:type>\n" +
                "</metadata>\n" +
                "\n" +
                "<manifest>\n" +
                "<!-- navigation -->\n" +
                "<item media-type=\"application/xhtml+xml\" id=\"toc\" href=\"toc.xhtml\" properties=\"nav\" />\n" +
                "<item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\" />\n" +
                "<!-- style -->\n" +
                "<item media-type=\"text/css\" id=\"style-fixed-layout-jp\" href=\"styles/fixed.css\"/>\n");

        str.append("\n" +
                "<!-- image -->\n" +
                "<item id=\"cover\" href=\"images/image-0000.jpg\" properties=\"cover-image\" media-type=\"image/jpeg\" />\n");
        for(int i=1;i<pages;i++){
            str.append("<item id=\"image-"+getName(i)+"\" href=\"images/image-"+getName(i)+extension+"\" media-type=\"image/jpeg\" />\n");
        }
        str.append("\n" +
                "<!-- xhtml -->\n");
        for(int i=0;i<pages;i++) {
            str.append("<item id=\"b_"+getName(i)+"\" href=\"text/b_"+getName(i)+".xhtml\" properties=\"svg\" media-type=\"application/xhtml+xml\" />\n");
        }
        str.append("\n" +
                "</manifest>\n" +
                "\n" +
                "<spine page-progression-direction=\""+direction+"\" toc=\"ncx\">\n");
        for(int i=0;i<pages;i++){
            String offset="rendition:page-spread-center";
            switch(getOffset(i,type,checked)){
                case "right":
                    offset="page-spread-right";
                    break;
                case "left":
                    offset="page-spread-left";
                    break;
                case "center":
                    offset="rendition:page-spread-center";
            }
            str.append("<itemref idref=\"b_"+getName(i)+"\" properties=\""+offset+"\"/>\n");
        }
        str.append("</spine>\n" +
                "    <guide>\n" +
                "        <reference type=\"cover\" title=\"Cover\" href=\"text/b_0000.xhtml\" />\n" +
                "    </guide>\n" +
                "</package>");
        return str.toString();
    }

    public String getContainer(){
        return "<?xml version=\"1.0\"?>\n" +
                "<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n" +
                "    <rootfiles>\n" +
                "        <rootfile full-path=\"OEBPS/standard.opf\" media-type=\"application/oebps-package+xml\" />\n" +
                "   </rootfiles>\n" +
                "</container>\n";
    }


}
