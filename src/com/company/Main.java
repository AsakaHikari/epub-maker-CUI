package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main {
    static byte[] buf = new byte[1024];
    private JFrame frame;
    private JTextArea textArea;
    private Map<String,String> publisherMap=new HashMap<String,String>();
    private JRadioButton radio1;
    private JRadioButton radio2;
    private JRadioButton radio3;
    private JRadioButton radio4;
    private JCheckBox check1;
    private JLabel label;
    private JPanel labelP;
    private List<String> supportedExtensions=new ArrayList<String>();

    public static void main(String[] args) throws UnsupportedEncodingException {


        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Main window = new Main();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


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

    static void write(File f, String text) {
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(text);
            bw.newLine();

            bw.close();

            f.setLastModified(Calendar.getInstance().getTimeInMillis());
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    /**
     * Create the application.
     */
    public Main() {
        initialize();

    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("epub maker CUI");

        //ラジオボタン追加


        JScrollPane scrollPane = new JScrollPane();
        frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

        textArea = new JTextArea();
        scrollPane.setViewportView(textArea);

        // ドロップ操作を有効にする
        textArea.setTransferHandler(new DropFileHandler());

        JPanel p = new JPanel();
        radio1 = new JRadioButton("右->左見開き");
        radio2 = new JRadioButton("左->右見開き");
        radio3 = new JRadioButton("中央(左めくり)");
        radio4 = new JRadioButton("中央(右めくり)");
        check1=new JCheckBox("表紙を中央にする");
        label =new JLabel("上にファイルを追加");

        ButtonGroup group = new ButtonGroup();
        group.add(radio1);
        group.add(radio2);
        group.add(radio3);
        group.add(radio4);

        p.add(radio1);
        p.add(radio2);
        p.add(radio3);
        p.add(radio4);
        p.add(check1);

        radio1.setSelected(true);
        check1.setSelected(true);

        labelP= new JPanel();
        labelP.add(label);
        Container contentPane = frame.getContentPane();
        contentPane.add(p, BorderLayout.NORTH);
        contentPane.add(labelP, BorderLayout.SOUTH);

        //出版社リスト読み込み
        Map<String,String> localPublisherMap=new HashMap<String,String>();
        //System.out.println(System.getProperty("user.home"));
        Long localTime=0L;
        int localVer=1;
        InputStream islocal;
        try {
             islocal = ClassLoader.getSystemResourceAsStream("publisherlist.txt");
            InputStreamReader isr = null;
            isr = new InputStreamReader(islocal, "UTF-8");

            BufferedReader br = new BufferedReader(isr);
            br.readLine();
            localVer= Integer.parseInt(br.readLine());
            localTime = Long.parseLong(br.readLine());


            while (true) {
                String temp = null;

                temp = br.readLine();
                if (temp == null) {
                    break;
                }
                String[] strs=temp.split(",");
                if(strs.length==2){
                    localPublisherMap.put(strs[0],strs[1]);
                }

            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String,String> dcPublisherMap=new HashMap<String,String>();

        File documentfile=new File(new File(System.getProperty("user.home")),"Documents");
        if(!documentfile.exists())documentfile.mkdir();
        File upperfile=new File(documentfile,"epubmakercui");
        if(!upperfile.exists())upperfile.mkdir();
        File file=new File(upperfile,"publisherlist.txt");
        Long dcTime=0L;
        int dcVer=1;
        if(file.exists()) {
            dcTime=file.lastModified();
            try {
                InputStream is = new FileInputStream(file);
                InputStreamReader isr = null;
                isr = new InputStreamReader(is, "UTF-8");

                BufferedReader br = new BufferedReader(isr);
                br.readLine();
                dcVer= Integer.parseInt(br.readLine());
                while (true) {
                    String temp = null;

                    temp = br.readLine();
                    if (temp == null) {
                        break;
                    }
                    String[] strs = temp.split(",");
                    if (strs.length == 2) {
                        localPublisherMap.put(strs[0], strs[1]);
                    }

                }

                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            for (Map.Entry<String, String> e : dcPublisherMap.entrySet()){
                publisherMap.put(e.getKey(),e.getValue());
            }
            for (Map.Entry<String, String> e : localPublisherMap.entrySet()) {
                String value = publisherMap.get(e.getKey());
                if (value != null) {
                    if (value.equals(e.getValue())) {
                    } else if (localTime > dcTime) {
                        publisherMap.put(e.getKey(), e.getValue());
                    }
                } else {
                    publisherMap.put(e.getKey(), e.getValue());
                }
            }
            StringBuffer strblocal=new StringBuffer("\n1\n"+Calendar.getInstance().getTimeInMillis()+"\n");
            StringBuffer strb = new StringBuffer("\n1\n");
            for (Map.Entry<String, String> e : publisherMap.entrySet()) {
                strblocal.append(e.getKey()+","+e.getValue()+"\n");
                strb.append(e.getKey()+","+e.getValue()+"\n");
            }
            write(file,strb.toString());
        }else {

            StringBuffer strb = new StringBuffer("\n1\n");
            for (Map.Entry<String, String> e : localPublisherMap.entrySet()) {
                strb.append(e.getKey()+","+e.getValue()+"\n");
                publisherMap.put(e.getKey(), e.getValue());
            }
            write(file,strb.toString());
        }


    }

    /**
     * ドロップ操作の処理を行うクラス
     */
    private class DropFileHandler extends TransferHandler {

        /**
         * ドロップされたものを受け取るか判断 (ファイルのときだけ受け取る)
         */
        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDrop()) {
                // ドロップ操作でない場合は受け取らない
                return false;
            }

            if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                // ドロップされたのがファイルでない場合は受け取らない
                return false;
            }

            return true;
        }

        /**
         * ドロップされたファイルを受け取る
         */
        @Override
        public boolean importData(TransferSupport support) {
            // 受け取っていいものか確認する
            if (!canImport(support)) {
                return false;
            }

            // ドロップ処理
            Transferable t = support.getTransferable();
            try {
                // ファイルを受け取る
                List<File> targets = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);


                int type = 0;
                if(radio1.isSelected()){
                    type=0;
                }else if(radio2.isSelected()){
                    type=1;
                }else if(radio3.isSelected()){
                    type=2;
                }else if(radio4.isSelected()){
                    type=3;
                }
                boolean isChecked=check1.isSelected();
                /*
                for(String arg:args){
                    if(arg.contains("-type=")){
                        type=Integer.parseInt(arg.substring(6,arg.length()));
                    }
                }*/


                for (File target : targets) {

                    File current = new File(".");
                    //System.out.println(target);
                    //System.out.println(current);
                    //if(true)continue;
                    if (!target.isDirectory()) continue;
                    String foldername = target.getName();


                    String title = "unknown";
                    String author = "unknown";
                    String publisher = "unknown";
                    String id = "unknown";

                    Matcher matcher = Pattern.compile("[a-zA-Z0-9\\-]+\\-").matcher(foldername);
                    if (!matcher.find()) continue;
                    id = foldername.substring(0, matcher.end() - 1);
                    title = foldername.substring(matcher.end(), foldername.length());
                    Matcher matcher2 = Pattern.compile("／").matcher(title);

                    if (matcher2.find()) {
                        author = title.substring(matcher2.end(), title.length());
                        title = title.substring(0, matcher2.end() - 1);
                    } else {

                    }
                    String pubstr=publisherMap.get(id.substring(0,3));
                    if(pubstr==null){
                        pubstr=publisherMap.get(id.substring(0,2));
                    }
                    if(pubstr!=null){
                        publisher=pubstr;
                    }
                    label.setText(id+".epubを出力中...");

                    labelP.update(labelP.getGraphics());
                    //System.out.println(id + "," + title + "," + author);

                    File root = new File(current, "_EPUBMAKERCUI_ROOT_");
                    if (root.exists()) {
                        delete(root);
                    }
                    root.mkdir();
                    File metainf = new File(root, "META-INF");
                    metainf.mkdir();

                    File folder = new File(root, "OEBPS");
                    folder.mkdir();
                    File styles = new File(folder, "styles");
                    styles.mkdir();
                    File images = new File(folder, "images");
                    images.mkdir();
                    File text = new File(folder, "text");
                    text.mkdir();
                    File[] imagefiles_ = target.listFiles();
                    List<File> imagefiles=new ArrayList<File>();

                    String[] formatNames=ImageIO.getWriterFormatNames();

                    //画像ファイルのみをimagefilesに入れる
                    for(File image:imagefiles_){
                        String imagefilename = image.getName();
                        String extension = imagefilename.substring(imagefilename.lastIndexOf(".")+1);
                        boolean flag=false;
                        System.out.println(extension);
                        for(String format:formatNames){
                            if(format.equals(extension)){
                                flag=true;
                                break;
                            }
                        }
                        if(flag){
                            imagefiles.add(image);
                        }
                    }
                    BufferedImage img = null;


                    int[] widths=new int[imagefiles.size()];
                    int[] heights=new int[imagefiles.size()];
                    String[] extensions=new String[imagefiles.size()];
                    Filemaker fm = new Filemaker(title, title, author, author, publisher, widths, heights, type, extensions, isChecked);
                    try {
                        write(new File(metainf, "container.xml"), fm.getContainer());

                        String extension = "";
                        for (int i = 0; i < imagefiles.size(); i++) {
                            img = ImageIO.read(imagefiles.get(i));
                            widths[i]=img.getWidth();
                            heights[i]=img.getHeight();
                            String name = fm.getName(i);

                            String imagefilename = imagefiles.get(i).getName();
                            extension = imagefilename.substring(imagefilename.lastIndexOf("."));
                            extensions[i]=extension;
                            File newimage = new File(images,
                                    "image-" + name + extension);
                            Files.copy(imagefiles.get(i).toPath(), newimage.toPath());


                            write(new File(text, "b_" + name + ".xhtml"), fm.getxhtmls(i, extension));

                            //filelist.add(new File(text, "b_" + name + ".xhtml"));
                        }

                        write(new File(styles,"fixed.css"),fm.getFixed());

                        write(new File(folder, "toc.xhtml"), fm.getTocxhtml());

                        write(new File(folder, "toc.ncx"), fm.getTocncx());

                        write(new File(folder, "standard.opf"), fm.getStandard(imagefiles.size()));


                        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
                        File file = new File("./" + id + ".epub");    //作成するzipファイルの名前
                        if (file.exists()) {
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
                    label.setText(id+".epubの出力が完了しました");
                }
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
