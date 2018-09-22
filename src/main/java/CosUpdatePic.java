import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import com.sun.tools.internal.ws.wsdl.document.Output;


import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 上传图片到QQ cloud
 * bucket suishi-1256985330
 * region ap-guangzhou
 * appid 1256985330
 */



public class CosUpdatePic {

    public static List<String> ReadFile(final String path) throws IOException {
        List<String> list = new ArrayList<String>();
        FileInputStream fileInputStream = new FileInputStream(path);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line = "";
        while((line = bufferedReader.readLine()) != null) {
            list.add(line);
        }
        bufferedReader.close();
        inputStreamReader.close();
        fileInputStream.close();
        return list;
    }

    public static void WriteFile(final String path, final String content) {
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true)));
            out.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean DownloadUrlPicture(final String path) {

        InputStream inputStream = null;
        OutputStream outputStream = null;

        try {
            URL url = new URL(path);
            URLConnection conn = url.openConnection();
            inputStream = conn.getInputStream();
            outputStream = new FileOutputStream(new File("/Users/qinguo/Desktop/tmp.jpg"));
            int n = -1;
            byte b [] = new byte[4099];
            while ((n = inputStream.read(b)) != -1) {
                outputStream.write(b, 0, n);
            }
            outputStream.flush();

            inputStream.close();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {

        }
        return true;
    }


    public static void main(String[] argv) {

        // 1 初始化用户身份信息(secretId, secretKey)
        COSCredentials cred = new BasicCOSCredentials("AKIDe9UceY7yVosgEXIXAQQBhy0qQkpHAZN1", "Zv44qOhmcEbklaAXeV6RWLzOaWEgyQfe");
        // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
        ClientConfig clientConfig = new ClientConfig(new Region("ap-guangzhou"));
        // 3 生成cos客户端
        COSClient cosclient = new COSClient(cred, clientConfig);
        // bucket的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
        String bucketName = "suishi-1256985330";

        try {
            List<String> contents = ReadFile("/Users/qinguo/Desktop/project/java/upload_category/src/main/resources/category.txt");
            List<String> results = new ArrayList<String>();
            //解析,处理
            int count = 0;
            for (String content : contents) {
                ++count;
                String text = "正在处理第" + count + "条数据；总共需要：" + contents.size() + "条";
                System.out.println(text);
                String[] splitsTexts = content.split("=");
                if (splitsTexts.length > 0) {
                    if (splitsTexts.length == 2) {
                        String tag = splitsTexts[0];
                        String url = splitsTexts[1];

                        if (!tag.equals("img_url")) {
                            //results.add(content);
                            //写文件
                            WriteFile("./new_category.txt", content);
                            WriteFile("./new_category.txt", "\r\n");
                            continue;
                        }

                        //删除/Users/qinguo/Desktop/tmp.jpg文件
                        File file = new File("/Users/qinguo/Desktop/tmp.jpg");
                        if (file.exists()) {
                            file.delete();
                        }

                        //下载
                       boolean res = DownloadUrlPicture(url);
                       if (!res) {
                           System.out.println("图片下载不下来： "+content);
                           continue;
                       }
                       //上传图片到CO，生成新的url
                        File localFile = new File("/Users/qinguo/Desktop/tmp.jpg");
                        String key = "new_category2/"+new Date().getTime() + ".jpg";
                        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, localFile);
                        PutObjectResult putObjectResult = cosclient.putObject(putObjectRequest);
                        Date expiration = new Date(new Date().getTime() + 3600L * 1000 * 24 * 365 * 10);
                        URL change_url = cosclient.generatePresignedUrl(bucketName, key, expiration);

                        //组装成新的字符串
                        String newContent = tag + "=" + change_url;
                       // results.add(newContent);
                        //写文件
                        WriteFile("./new_category.txt", newContent);
                        WriteFile("./new_category.txt", "\r\n");

                    } else {
                        System.out.println("异常文本： "+content);
                        //results.add(content);
                        //写文件
                        WriteFile("./new_category.txt", content);
                        WriteFile("./new_category.txt", "\r\n");
                    }
                } else {
                    //results.add(content);
                    //写文件
                    WriteFile("./new_category.txt", content);
                    WriteFile("./new_category.txt", "\r\n");
                }

            }

            //存入新的文件
//            System.out.println("开始保存文件");
//            for (String newContent : results) {
////                //写文件
////                WriteFile("./new_category.txt", newContent);
////                WriteFile("./new_category.txt", "\r\n");
//            }
//            System.out.println("文件保存结束");

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("程序执行结束");
        cosclient.shutdown();
    }
}
