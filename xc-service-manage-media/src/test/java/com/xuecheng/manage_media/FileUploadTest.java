package com.xuecheng.manage_media;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author gaoxiong@asiainfo.com
 * @date 2020-02-20 14:46
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileUploadTest {
    //测试文件分块方法
    @Test
    public void testChunk() throws IOException {
        File originFile = new File("/Users/gao/my_code_trunk/xcEduService01/file/lucene.mp4");
        String chunkPath = "/Users/gao/my_code_trunk/xcEduService01/file/chunk/";
        File chunkFolder = new File(chunkPath);
        if (!chunkFolder.exists()) {
            chunkFolder.mkdirs();
        }

        long chunkSize = 1024 * 1024;
        long chunkNum = (long)Math.ceil(originFile.length() * 1.0 / chunkSize);
        if (chunkNum <= 0) {
            chunkNum = 1;
        }

        byte[] temp = new byte[1024];
        RandomAccessFile read = new RandomAccessFile(originFile, "r");

        //分块写入
        for (int i=0; i< chunkNum; i++) {
            File file = new File(chunkPath + i);
            boolean newFile = file.createNewFile();
            if (newFile) {
                RandomAccessFile write = new RandomAccessFile(file, "rw");
                //创建文件成功则向文件中写入内容
                int len = -1;
                while ((len = read.read(temp)) != -1) {
                    write.write(temp, 0, len);
                    if (file.length() > chunkSize) {
                        break;
                    }
                }
                write.close();
            }
        }
        read.close();
    }

    //测试文件合并方法
    @Test
    public void testMerge() throws IOException {
        File newFile = new File("/Users/gao/my_code_trunk/xcEduService01/file/lucenenew.mp4");
        File chunkFolder = new File("/Users/gao/my_code_trunk/xcEduService01/file/chunk/");
        if (newFile.exists()) {
            newFile.delete();
        }
        newFile.createNewFile();

        RandomAccessFile write = new RandomAccessFile(newFile, "rw");
        write.seek(0);
        //缓冲区
        byte[] temp = new  byte[1024];
        File[] files = chunkFolder.listFiles();
        List<File> fileList = Arrays.asList(files);
        Collections.sort(fileList, (o1, o2) -> {
            if (Integer.parseInt(o1.getName()) < Integer.parseInt(o2.getName())) {
                return -1;
            }
            return 1;
        });

        for (File chunkFile:fileList) {
            RandomAccessFile read = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while ((len = read.read(temp)) != -1) {
                write.write(temp, 0, len);
            }
            read.close();
        }
    }

}
