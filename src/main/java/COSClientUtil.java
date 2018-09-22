import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

/**
 * Created by sungy on 2018/3/15.
 */
public class COSClientUtil {
    public static final Logger logger = LoggerFactory.getLogger(COSClientUtil.class);
   // private static Properties properties = PropertyLoader.getPropertiesFromClasspath("cosservice.properties");
    // region
    private static String region;
    // accessKey
    private static String accessKey;
    //
    private static String secretKey;
    // 存储空间
    private static String bucketName;
    // 文件存储目录
    private static String filedir;

    static {
     /*   region = properties.getProperty("cos.region");
        accessKey = properties.getProperty("cos.accessKey");
        secretKey = properties.getProperty("cos.secretKey");
        bucketName = properties.getProperty("cos.bucketName");
        filedir = properties.getProperty("cos.filedir");*/

        region = "ap-guangzhou";
        accessKey = "AKIDe9UceY7yVosgEXIXAQQBhy0qQkpHAZN1";
        secretKey = "Zv44qOhmcEbklaAXeV6RWLzOaWEgyQfe";
        bucketName = "suishi-1256985330";
        filedir = "/img/";
    }

    private COSClient cosClient;


    public COSClientUtil() {
        COSCredentials cred = new BasicCOSCredentials(accessKey, secretKey);
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        cosClient = new COSClient(cred, clientConfig);
    }


    /**
     * 从COS取得图片，临时缓存在本地
     *
     * @return 返回输入流
     * @param图片的key
     */

    public String getImagesBase64(String key) throws Throwable {
        GetObjectRequest request = new GetObjectRequest(bucketName,
                filedir + key);
        File file = new File(System.getProperty("user.dir") +File.separator+ bucketName + ".jpg");
        cosClient.getObject(request, file);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] data = new byte[fileInputStream.available()];
        fileInputStream.read(data);
        Base64 base64 = new Base64();
        String picture = "data:image/jpg;base64," + base64.encodeToString(data);
        fileInputStream.close();
        return picture;

    }


//    public String uploadImg2Cos(MultipartFile file) throws Throwable {
//        String name = UUID.randomUUID() + "_" + file.getName();
//        Integer filesize = Integer.parseInt(properties.getProperty("images.size")) / 1024;
//        if (file.getSize() > Integer.parseInt(properties.getProperty("images.size")) * 1024) {
//            throw new Exception("上传的图片大小超过限制," + filesize + "M");
//        } else {
//            InputStream inputStream = file.getInputStream();
//            this.uploadFile2COS(inputStream, name);
//        }
//        return name;
//    }

    /**
     * 上传到OSS服务器  如果同名文件会覆盖服务器上的
     *
     * @param instream 文件流
     * @param fileName 文件名称 包括后缀名
     * @return 出错返回"" ,唯一MD5数字签名
     */
    public String uploadFile2COS(InputStream instream, String fileName) throws Throwable {
        String ret = "";
        //创建上传Object的Metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(instream.available());
        objectMetadata.setCacheControl("no-cache");
        objectMetadata.setHeader("Pragma", "no-cache");
        objectMetadata.setContentType(getcontentType(fileName));
        objectMetadata.setContentDisposition("inline;filename=" + fileName);
        //上传文件
        PutObjectResult putResult = cosClient.putObject(bucketName, filedir + fileName, instream, objectMetadata);
        if (instream != null) {
            instream.close();
        }
        return ret;
    }


    /**
     * Description: 判断OSS服务文件上传时文件的contentType
     *
     * @param FilenameExtension 文件后缀
     * @return String
     */
    public static String getcontentType(String FilenameExtension) {
        if (FilenameExtension.equalsIgnoreCase("bmp")) {
            return "image/bmp";
        }
        if (FilenameExtension.equalsIgnoreCase("gif")) {
            return "image/gif";
        }
        if (FilenameExtension.equalsIgnoreCase("jpeg") ||
                FilenameExtension.equalsIgnoreCase("jpg") ||
                FilenameExtension.equalsIgnoreCase("png")) {
            return "image/jpeg";
        }
        if (FilenameExtension.equalsIgnoreCase("html")) {
            return "text/html";
        }
        if (FilenameExtension.equalsIgnoreCase("txt")) {
            return "text/plain";
        }
        if (FilenameExtension.equalsIgnoreCase("vsd")) {
            return "application/vnd.visio";
        }
        if (FilenameExtension.equalsIgnoreCase("pptx") ||
                FilenameExtension.equalsIgnoreCase("ppt")) {
            return "application/vnd.ms-powerpoint";
        }
        if (FilenameExtension.equalsIgnoreCase("docx") ||
                FilenameExtension.equalsIgnoreCase("doc")) {
            return "application/msword";
        }
        if (FilenameExtension.equalsIgnoreCase("xml")) {
            return "text/xml";
        }
        return "image/jpeg";
    }

    public static void main(String[] args) throws Throwable{
        InputStream inputStream =new FileInputStream("/Users/qinguo/Desktop/1.jpg");

        COSClientUtil cosClientUtil =new COSClientUtil();

        cosClientUtil.uploadFile2COS(inputStream,"111");
    }

}
