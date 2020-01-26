package com.company;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    static byte[] buf = new byte[1024];

    public static void main(String[] args) {
        int type = 0;
        for(String arg:args){
            if(arg.contains("-type=")){
                type=Integer.parseInt(arg.substring(6,arg.length()));
            }
        }

        File current = new File(".");
        File[] targets=current.listFiles();

        for(File target:targets) {
            if(!target.isDirectory())continue;
            String foldername = target.getName();


            String title = "unknown";
            String author = "unknown";
            String publisher = "unknown";
            String id = "unknown";

            Matcher matcher = Pattern.compile("[a-zA-Z0-9\\-]+\\-").matcher(foldername);
            if(!matcher.find())continue;
            id = foldername.substring(0, matcher.end() - 1);
            title = foldername.substring(matcher.end(), foldername.length());
            Matcher matcher2 = Pattern.compile("／").matcher(title);

            if (matcher2.find()) {
                author = title.substring(matcher2.end(), title.length());
                title = title.substring(0, matcher2.end() - 1);
            } else {

            }
            //System.out.println(id + "," + title + "," + author);

            List<File> filelist = new ArrayList<File>();

            File root = new File(current, "_EPUBMAKERCUI_ROOT_");
            if(root.exists()){
                delete(root);
            }
            root.mkdir();
            File metainf = new File(root, "META-INF");
            metainf.mkdir();
            filelist.add(new File(root, "META-INF"));

            File folder = new File(root, "OEBPS");
            folder.mkdir();
            filelist.add(new File(root, "OEBPS"));
            File styles = new File(folder, "styles");
            styles.mkdir();
            File images = new File(folder, "images");
            images.mkdir();
            File text = new File(folder, "text");
            text.mkdir();
            File[] imagefiles = target.listFiles();
            BufferedImage img = null;

            try {
                img = ImageIO.read(imagefiles[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }//
            int width = img.getWidth();
            int height = img.getHeight();
            Filemaker fm = new Filemaker(title, title, author, author, publisher, width, height, type);
            try {
                FileWriter containerfw = new FileWriter(new File(metainf, "container.xml"));
                containerfw.write(fm.getContainer());
                containerfw.close();
                //filelist.add(new File(metainf, "container.xml"));
/*
            FileWriter mimetypefw = new FileWriter(new File(root, "mimetype"));
            mimetypefw.write("application/epub+zip");
            mimetypefw.close();
            filelist.add(new File(root, "mimetype"));
*/
                String extension = "";
                for (int i = 0; i < imagefiles.length; i++) {
                    String name = fm.getName(i);

                    String imagefilename = imagefiles[i].getName();
                    extension = imagefilename.substring(imagefilename.lastIndexOf("."));
                    File newimage = new File(images,
                            "image-" + name + extension);
                    Files.copy(imagefiles[i].toPath(), newimage.toPath());

                    FileWriter xhtmlfw = new FileWriter(new File(text, "b_" + name + ".xhtml"));


                    xhtmlfw.write(fm.getxhtmls(i, extension));
                    xhtmlfw.close();
                    //filelist.add(new File(text, "b_" + name + ".xhtml"));
                }

                FileWriter fixedfw = new FileWriter(new File(styles, "fixed.css"));
                fixedfw.write(fm.getFixed());
                fixedfw.close();
                //filelist.add(new File(styles, "fixed.css"));

                FileWriter tocxhtmlfw = new FileWriter(new File(folder, "toc.xhtml"));
                tocxhtmlfw.write(fm.getTocxhtml());
                tocxhtmlfw.close();
                // filelist.add(new File(folder, "toc.xhtml"));

                FileWriter tocncxfw = new FileWriter(new File(folder, "toc.ncx"));
                tocncxfw.write(fm.getTocncx());
                tocncxfw.close();
                //filelist.add(new File(folder, "toc.ncx"));

                FileWriter standardfw = new FileWriter(new File(folder, "standard.opf"));
                standardfw.write(fm.getStandard(imagefiles.length, extension));
                standardfw.close();
                //filelist.add(new File(folder, "standard.opf"));

                String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
                File file = new File("./" + id + ".epub");    //作成するzipファイルの名前
                if(file.exists()){
                    delete(file);
                }
                File[] files = root.listFiles();    //圧縮対象を相対パスで指定
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file));
                try {
                    encode(zos, files);
                    zos.setMethod(ZipOutputStream.STORED); //デフォルトはDEFLATED

                    //1つめのファイルを格納
                    ZipEntry entry = new ZipEntry("mimetype");    //格納ファイル名
                    byte[] data = "application/epub+zip".getBytes();    //格納データ

                    entry.setSize(data.length);    //データサイズをセット

                    CRC32 crc = new CRC32();
                    crc.update(data);
                    entry.setCrc(crc.getValue());    //CRCをセット

                    zos.putNextEntry(entry);
                    zos.write(data);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    zos.close();
                }

            } catch (IOException e) {
                e.printStackTrace();

            } finally {
                delete(root);
            }
        }
    }

    private static void delete(File f) {
        /*
         * ファイルまたはディレクトリが存在しない場合は何もしない
         */
        if (f.exists() == false) {
            return;
        }

        if (f.isFile()) {
            /*
             * ファイルの場合は削除する
             */
            f.delete();

        } else if (f.isDirectory()) {
            /*
             * ディレクトリの場合は、すべてのファイルを削除する
             */

            /*
             * 対象ディレクトリ内のファイルおよびディレクトリの一覧を取得
             */
            File[] files = f.listFiles();

            /*
             * ファイルおよびディレクトリをすべて削除
             */
            for (int i = 0; i < files.length; i++) {
                /*
                 * 自身をコールし、再帰的に削除する
                 */
                delete(files[i]);
            }
            /*
             * 自ディレクトリを削除する
             */
            f.delete();
        }
    }


    static void encode(ZipOutputStream zos, File[] files) throws Exception {
        for (File f : files) {
            if (f.isDirectory()) {
                encode(zos, f.listFiles());
            } else {
                String path = f.getPath();
                path = path.replace('\\', '/');
                //System.out.println(path);
                path = path.substring(path.indexOf('/', 3) + 1, path.length());
                ZipEntry entry = new ZipEntry(path);
                zos.putNextEntry(entry);
                try (InputStream is = new BufferedInputStream(new FileInputStream(f))) {
                    for (; ; ) {
                        int len = is.read(buf);
                        if (len < 0) break;
                        zos.write(buf, 0, len);
                    }
                }
            }
        }
    }

}
