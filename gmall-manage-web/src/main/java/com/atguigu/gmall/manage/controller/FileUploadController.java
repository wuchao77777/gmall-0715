package com.atguigu.gmall.manage.controller;


import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@CrossOrigin
@Controller
public class FileUploadController {


    @Value("${fileServer.url}")
    String fileUrl;


  //  http://localhost:8082/fileUpload
    @RequestMapping(value = "fileUpload",method = RequestMethod.POST)
    @ResponseBody
    public String fileUpload(MultipartFile file) throws IOException, MyException {
        String imgUrl = fileUrl;

       if(file !=null){
           String configFile  = this.getClass().getResource("/tracker.conf").getFile();
           ClientGlobal.init(configFile);
           TrackerClient trackerClient=new TrackerClient();
           TrackerServer trackerServer=trackerClient.getConnection();
           StorageClient storageClient=new StorageClient(trackerServer,null);

           String filename = file.getOriginalFilename();//获取了  22.jpg
           String extName  = StringUtils.substringAfterLast(filename, ".");//获取后缀名

//           String orginalFilename="D:\\imge\\22.jpg";
           imgUrl = fileUrl;

           String[] upload_file = storageClient.upload_file(file.getBytes(), extName , null);
           for (int i = 0; i < upload_file.length; i++) {
               String path = upload_file[i];
               System.out.println("s = " +path );
               imgUrl += "/"+path;
           }
       }
       return imgUrl;

    }



}
