package me.ffis.randomImage.service;

import lombok.extern.slf4j.Slf4j;
import me.ffis.randomImage.config.ReadListConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.List;

/**
 * 随机图片服务类
 * Created by fanfan on 2020/01/06.
 */

@Slf4j
@Service
public class ImageService {


    @Value("${fileBaseUrl}")
    private String fileBaseUrl;

    @Value("${baseUrl}")
    private String baseUrl;

    //随机获取images集合中的图片地址
    public String getRandomImages(String imageFile) {
        //屏蔽domains的请求
        if ("domains".equals(imageFile)) {
            return "404";
        }
        //判断集合是否为空
        if (ReadListConfig.listMap.isEmpty()) {
            log.error("listMap集合为空，请检查列表文件是否存在");
            return null;
        } else {
            //根据传入的参数images获取对应的集合
            List<String> images = ReadListConfig.listMap.get(imageFile + ".txt");
            if (images == null) {
                return "404";
            }
            //根据images集合大小生成随机数
            int index = (int) (Math.random() * images.size());
            String uri = images.get(index);
            uri = creatUrl(uri);

            //获取随机的图片地址
            return baseUrl + uri ;
        }
    }

    private String creatUrl(String uri) {
        StringBuffer newUrl = new StringBuffer();
        if (uri != null){
            String[] split = uri.split("/");
            // 对每一段分别编码
            for (String subUri : split) {
                newUrl.append("/").append(URLEncoder.encode(subUri));
            }
            return newUrl.toString();
        }
        return "";
    }

    //获取今日图片
    public String getImageByDate(String imageFile) {
        //屏蔽domains的请求
        if ("domains".equals(imageFile)) {
            return "404";
        }
        //判断集合是否为空
        if (ReadListConfig.listMap.isEmpty()) {
            log.error("listMap集合为空，请检查列表文件是否存在");
            return null;
        } else {
            //根据传入的参数images获取对应的集合
            List<String> images = ReadListConfig.listMap.get(imageFile + ".txt");
            if (images == null) {
                return "404";
            }
            //获取Calendar对象，并设置为2020年1月1日
            Calendar begin = Calendar.getInstance();
            begin.set(2020, Calendar.JANUARY, 1);
            //获取Calendar对象，计算今天日期到2020年1月1日之间相差多少天
            Calendar today = Calendar.getInstance();
            int day = (int) (today.getTimeInMillis() - begin.getTimeInMillis()) / (24 * 60 * 60 * 1000);
            //根据今天的日期获取今日图片
            int index = (day % images.size());
            //获取今日随机的图片地址
            return images.get(index);
        }
    }

    public ResponseEntity<byte[]> getRandomImagesEntity(String imgUrl, HttpServletRequest request) {

        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader == null){
            System.out.println("下载请求");
        }else {
            System.out.println("浏览请求");
        }
        if (imgUrl != null) {
            try {
                String fileName = getFileName(imgUrl);
                downloadFile(imgUrl, fileName);
                // 返回图片
                Path imagePath = Paths.get(fileBaseUrl + fileName);
                byte[] imageData = Files.readAllBytes(imagePath);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_JPEG); // Change to the appropriate media type for your image
                headers.setCacheControl("no-cache, no-store, must-revalidate");
                headers.setPragma("no-cache");
                headers.setExpires(0);
                ContentDisposition inline = ContentDisposition.builder("inline").build();
                headers.setContentDisposition(inline);
                return new ResponseEntity<byte[]>(imageData, headers, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * 从文件服务器获取文件
     */
    public void downloadFile(String fileUrl, String localFilePath) {
        try {
            URL url = new URL(fileUrl);
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            FileOutputStream fileOutputStream = new FileOutputStream(fileBaseUrl + localFilePath);

            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            in.close();
            fileOutputStream.close();
            System.out.println("文件已保存至 " + localFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取保存文件的名字
     */
    public String getFileName(String imgUrl) throws UnsupportedEncodingException {
        String decode = URLDecoder.decode(imgUrl, "UTF-8");
        String[] split = decode.split("/");
        String fileName = "";
        if (split.length >= 2) {
            //创建list文件夹的文件对象
            File file = new File(fileBaseUrl + split[split.length - 2] );
            if (!file.exists()) {
                // 如果文件夹不存在，就创建  图片保存在文件夹下  否则不指定文件夹，直接命名
                if (file.mkdirs()){
                    fileName = split[split.length - 2] +"/"+ split[split.length - 1];
                }else {
                    fileName = split[split.length - 2] + split[split.length - 1];
                }
            }else {
                fileName = split[split.length - 2] +"/"+ split[split.length - 1];
            }
        } else {
            fileName = split[split.length - 1];
        }
        return fileName;
    }
}
