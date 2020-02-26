package com.xuecheng.fdfs_client;

import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author growdane@gmail.com
 * @date 2020-02-14 12:45
 */

@SpringBootTest
@RunWith(SpringRunner.class)
public class FdfsFileTest {
    //上传文件
    @Test
    public void testUpload() {
        try {
            ClientGlobal.initByProperties("conf/fastdfs-client.properties");
            System.out.println("network_timeout=" + ClientGlobal.g_network_timeout + "ms");
            System.out.println("charset=" + ClientGlobal.g_charset);
            //创建客户端
            TrackerClient tc = new TrackerClient();
            //连接tracker Server
            TrackerServer ts = tc.getConnection();
            if (ts == null) {
                System.out.println("getConnection return null");
                return;
            }
            //获取一个storage server
            StorageServer ss = tc.getStoreStorage(ts);
            if (ss == null) {
                System.out.println("getStoreStorage return null");
            }
            //创建一个storage存储客户端
            StorageClient1 sc1 = new StorageClient1(ts, ss);
            NameValuePair[] meta_list = null; //new NameValuePair[0];
            String item = "/Users/gao/Downloads/timg.jpeg";
            String fileid;
            fileid = sc1.upload_file1(item, "png", meta_list);
            System.out.println("Upload local file " + item + " ok, fileid=" + fileid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //下载文件
    @Test
    public void testDownloadFile() throws IOException, MyException {
        ClientGlobal.initByProperties("conf/fastdfs-client.properties");
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();
        StorageServer storageServer = null;
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
        byte[] result = storageClient1.download_file1("group1/M00/00/00/wKgAaV5GLGCAEYQfAACgvSm0G2w664.png");
        File file = new File("/Users/gao/my_code_trunk/xcEduService01/xc-framework-example/fdfs_client_example/src/main/conf/1.png");
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(result);
        fileOutputStream.close();
    }

    //查询文件
    @Test
    public void testQueryFile() throws IOException, MyException {
        ClientGlobal.initByProperties("conf/fastdfs-client.properties");
        TrackerClient tracker = new TrackerClient();
        TrackerServer trackerServer = tracker.getConnection();
        StorageServer storageServer = null;
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        FileInfo fileInfo = storageClient.query_file_info("group1", "M00/00/00/wKgAaV5GLGCAEYQfAACgvSm0G2w664.png");
        System.out.println(fileInfo);
    }


}
