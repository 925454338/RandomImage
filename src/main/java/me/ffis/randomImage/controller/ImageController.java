package me.ffis.randomImage.controller;

import lombok.extern.slf4j.Slf4j;
import me.ffis.randomImage.config.ReadListConfig;
import me.ffis.randomImage.pojo.reponse.ReponseCode;
import me.ffis.randomImage.pojo.reponse.Result;
import me.ffis.randomImage.pojo.reponse.ResultResponse;
import me.ffis.randomImage.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by fanfan on 2020/01/05.
 */

@Slf4j
@Controller
//开启跨域
@CrossOrigin(allowCredentials = "true", maxAge = 3600)
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * 随机获取指定图片列表文件中的图片
     *
     * @param response response对象
     * @return 随机获取到的图片
     */
    @ResponseBody
    @GetMapping(value = {"/", "/{imageFile}"})
    public ResponseEntity<byte[]> getRandomImages(@PathVariable(value = "imageFile", required = false) String imageFile,
                                                  HttpServletRequest request) {
        //调用imageService获取随机图片地址
            if (StringUtils.isEmpty(imageFile)) {
                imageFile = "images";
            }
            String imgUrl = imageService.getRandomImages(imageFile);
            if ("404".equals(imgUrl)) {
                return new ResponseEntity<byte[]>(HttpStatus.NOT_FOUND);
            }
            return imageService.getRandomImagesEntity(imgUrl,request);
    }


    /**
     * 随机获取指定图片列表文件中的图片
     * @param response  response对象
     * @return 随机获取到的图片
     */
    @ResponseBody
    @GetMapping("random")
    public Result getOssRandomImages(HttpServletResponse response) {
        //调用imageService获取随机图片地址
        String imgUrl = imageService.getRandomImages("images");
        try {
            if ("404".equals(imgUrl)) {
                response.sendError(404);
                return new ResultResponse(ReponseCode.FAIL);
            }
            if (imgUrl != null) {
                // 重定向时使用的也是oss的流量，相当于使用了两次，还是算了，没必要不保存了
//                String fileName = getFileName(imgUrl);
//                downloadFile(imgUrl, fileName);
                // 设置响应头为image类型
                response.setContentType("image/jpeg"); // 这里假设图片是JPEG格式，根据实际情况调整类型
                // 设置Content-Disposition响应头可以让浏览器预览而不是下载
                response.setHeader("Content-Disposition", "inline");
                //重定向到图片地址
                response.sendRedirect(imgUrl);
                return new ResultResponse(ReponseCode.SUCCESS);
            }
        } catch (IOException e) {
            log.error("重定向到随机图片地址失败！", e);
        }
        return new ResultResponse(ReponseCode.FAIL);
    }


    /**
     * 获取每日图片，即一天换一张
     *
     * @param imageFile 图片列表文件名
     * @param response  response对象
     * @return 今日图片地址
     */
    @ResponseBody
    @GetMapping("today/{imageFile}")
    public Result getImageByDay(@PathVariable String imageFile, HttpServletResponse response) {
        //调用imageService获取随机图片地址
        String imgUrl = imageService.getImageByDate(imageFile);
        try {
            if ("404".equals(imgUrl)) {
                response.sendError(404);
                return new ResultResponse(ReponseCode.FAIL);
            }
            if (imgUrl != null) {
                // 设置响应头为image类型
                response.setContentType("image/jpeg"); // 这里假设图片是JPEG格式，根据实际情况调整类型
                // 设置Content-Disposition响应头可以让浏览器预览而不是下载
                response.setHeader("Content-Disposition", "inline");
                //重定向到图片地址
                response.sendRedirect(imgUrl);
                return new ResultResponse(ReponseCode.SUCCESS);
            }
        } catch (IOException e) {
            log.error("重定向到随机图片地址失败！", e);
        }
        return new ResultResponse(ReponseCode.FAIL);
    }

    /**
     * 刷新图片缓存
     *
     * @return 刷新结果
     */
    @ResponseBody
    @GetMapping("flush")
    public Result flushCache() {
        log.info("刷新文件列表的缓存");
        Boolean flag = ReadListConfig.loadList();
        if (!flag) {
            return new ResultResponse(ReponseCode.FLUSH_FAIL);
        }
        return new ResultResponse(ReponseCode.FLUSH_SUCCESS);
    }
}
