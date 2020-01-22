package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Main {

    public static void main(String[] args){

        int type=0;

        String foldername="target";



        String title="taitoru";
        String author="sakusha";
        String publisher="shuppansha";

        File current=new File(".");
        File folder=new File(current,"newfolder");
        folder.mkdir();
        File styles=new File(folder,"styles");
        styles.mkdir();
        File images=new File(folder,"images");
        images.mkdir();
        File text=new File(folder,"text");
        text.mkdir();
        File target=new File(current,foldername);
        File[] imagefiles=target.listFiles();
        BufferedImage img = null;

        try {
            img = ImageIO.read(imagefiles[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }//
        int width=img.getWidth();
        int height=img.getHeight();
        Filemaker fm=new Filemaker(title,title,author,author,publisher,width,height,type);
        try {
            for(int i=0;i<imagefiles.length;i++){
                String name=fm.getName(i);
                String extension = "";
                String imagefilename=imagefiles[i].getName();
                extension=imagefilename.substring(imagefilename.lastIndexOf("."));
                File newimage=new File(images,
                        "image-"+name+extension);
                Files.copy(imagefiles[i].toPath(),newimage.toPath());

                FileWriter xhtmlfw=new FileWriter(new File(text,"b_"+name+".xhtml"));


                xhtmlfw.write(fm.getxhtmls(title,newimage.getName(),width,height,i));
                xhtmlfw.close();

            }

            FileWriter fixedfw=new FileWriter(new File(styles,"fixed.css"));
            fixedfw.write(fm.getFixed());
            fixedfw.close();
            FileWriter tocxhtmlfw=new FileWriter(new File(folder,"toc.xhtml"));
            tocxhtmlfw.write(fm.getTocxhtml());
            tocxhtmlfw.close();
            FileWriter tocncxfw=new FileWriter(new File(folder,"toc.ncx"));
            tocncxfw.write(fm.getTocncx());
            tocncxfw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
