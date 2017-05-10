package com.tutorialspoint.struts2.BreakpointContinue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiedan on 2017/5/5.
 */
public class BreakpointResume {
    /**
     * 断点续传时每段的字节数，建议不要设置得太大
     */
    private static final long FRAGMENT_SIZE = 2048L;
    /**
     * 本地文件存储目录
     */
    private static final String LOCAL_PATH = "E:\\practice\\myfile";

    /**
     * args的第一个参数指示Web资源的URL地址，协议类型限定为HTTP。required<br>
     * 第二个参数指示本地文件的绝对路径 + 文件名。optional
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        String localFile = null;
        String url = "http://img.article.pchome.NET/00/22/70/23/pic_lib/wm/03.jpg";

        // 1. 校验URL和本地文件的格式
        String checkURLMsg = checkURLFormat(url);
        if (checkURLMsg != null) {
            System.err.println(checkURLMsg);
            return;
        }
        String checkLocalFileMsg = checkLocalFileFormat(localFile);
        if (checkLocalFileMsg != null) {
            System.err.println(checkLocalFileMsg);
            return;
        }

        // 2. 计算Range的范围
        long startRange, endRange;
        if (localFile == null) {
            startRange = 0L;
            endRange = FRAGMENT_SIZE;
        } else {
            startRange = new File(localFile).length();
            endRange = startRange + FRAGMENT_SIZE;
        }

        // 3. 访问网络资源然后分段下载
        downloadPartially(url, localFile, startRange, endRange);
    }

    /**
     * 分段下载。<br>
     * <p>假设要下载的资源为http://img.article.pchome.net/00/22/70/23/pic_lib/wm/03.jpg，分段下载的思路如下：</p>
     * <p>1) 设置请求属性Range</p>
     * <p>2) 连接远程Web资源</p>
     * <p>3) 校验响应中的状态行，如果不是200或者206，就停止传输</p>
     * <p>4) 查看.tmp文件(03.jpg.tmp)是否已经存在，如果不存在，就新建该文件</p>
     * <p>5) 将响应内容写入.tmp文件</p>
     * <p>6) 如果Web资源已经全部传输完了，将.tmp文件的后缀去掉，还原为文件本来的后缀和格式，然后结束while循环</p>
     * <p>7) 如果Web资源没有传输完，计算下一次传输的Range的范围</p>
     *
     * @param url
     * @param localFile
     * @param startRange
     * @param endRange
     * @throws MalformedURLException    应该不会抛出该异常，因为已经限定了只能用HTTP协议访问Web资源，并且进行了校验
     */
    private static void downloadPartially(String url, String localFile, long startRange, long endRange) throws MalformedURLException {
        long startTime = System.currentTimeMillis();
        URL resource = new URL(url);

        // 加入num是用来模拟Web资源传输了一部分，然后第二次传输时，从上次结束的部分开始获取
        int num = 0;
        while (true) {
            if (++num == 100) {
              break;
            }
            HttpURLConnection conn = null;
            InputStream in = null;
            RandomAccessFile raf = null;
            try {
                conn = (HttpURLConnection) resource.openConnection();
                // 1) 设置请求属性Range
                conn.setRequestProperty("Range", "bytes=" + startRange + "-" + (endRange == -1L ? "" : endRange));
                // 2) 连接远程Web资源
                conn.connect();
                // 3) 校验状态行，如果不是成功或者部分内容，就停止传输
                String statusLine = conn.getHeaderField(null);// 状态行
                System.out.println("statusLine=" + statusLine);
                if (!statusLine.contains("200") && !statusLine.contains("206")) {
                    throw new Exception("获取Web资源[" + url + "]时，响应状态不是200或者206");
                }

                // 获取资源长度
                String cr = conn.getHeaderField("Content-Range");
                if (cr == null || "".equals(cr.trim())) {
                    throw new Exception("获取Web资源[" + url + "]时，响应信息中Content-Range为null");
                }
                System.out.println("Content-Range=" + cr);
                cr = cr.replace("[", "").replace("]", "").replace("bytes", "").trim();
                // 解析响应消息头中Content-Range字段的值
                long resourceStartPos = Long.parseLong(cr.substring(0, cr.indexOf("-")));
                long resourceEndPos = Long.parseLong(cr.substring(cr.indexOf("-") + 1, cr.indexOf("/")));
                long resourceTotalLength = Long.parseLong(cr.substring(cr.indexOf("/") + 1));
                System.out.println("resourceStartPos=" + resourceStartPos
                        + ", resourceEndPos=" + resourceEndPos
                        + ", resourceTotalLength=" + resourceTotalLength);
                // 将相应内容读取到buf中
                byte[] buf = new byte[(int) (resourceEndPos - resourceStartPos)];
                in = conn.getInputStream();
                in.read(buf);

                // 4) 查看.tmp文件是否已经存在，如果不存在，就新建该文件
                if (localFile == null) {
                    localFile = LOCAL_PATH + File.separator + url.substring(url.lastIndexOf("/") + 1) + ".tmp";
                }
                System.out.println("localFile=" + localFile);
                File f = new File(localFile);
                if (!f.exists()) {// .tmp文件不存在，使用OutputStream手动创建该文件
                    OutputStream os = new FileOutputStream(f);
                    try {os.close();} catch (Exception e) {}
                }
                /*
                 * 如果不添加下面一行代码，多次运行该类的话，下载到本地的文件会出问题。
                 * 加上下面一行代码的话，会极大地减小出问题的概率，但并不能绝对避免出问题。
                 * 个人初步怀疑，是因为RandomAccessFile在创建以及关闭时，都需要调用native
                 * 方法请求分配资源或者释放资源，这种JNI调用实际上效率并不高。在本例中的
                 * while循环中频繁调用RandomAccessFile的创建和关闭方法，可能会在分配
                 * 资源或者释放资源时并不彻底，然后通过RandomAccessFile去写的时候就会
                 * 出问题。加上一个短暂的休眠时间，是为了让JNI有充分的时间能够正确地
                 * 分配资源以及正确地释放资源。10ms对于计算机系统应该算是一个比较长的
                 * 时间间隔了
                 */
                //TimeUnit.MILLISECONDS.sleep(10L);
                raf = new RandomAccessFile(f, "rwd");
                raf.seek(startRange);
                raf.write(buf);
                try {raf.close();} catch (Exception e) {}

                // 6) 如果Web资源已经全部传输完了，将.tmp文件的后缀去掉，还原为文件本来的后缀和格式，然后结束while循环
                // 从Web服务器返回的内容中的字节，是以0为索引开始计数的
                if (resourceEndPos == resourceTotalLength - 1) {
                    f.renameTo(new File(LOCAL_PATH + File.separator + url.substring(url.lastIndexOf("/") + 1)));
                    break;
                }

                // 7) 如果Web资源没有传输完，计算下一次传输的Range的范围
                // 如果剩下的要传输的内容不超过FRAGMENT_SIZE的1.5倍，就一起全部传输过来，减少HttpURLConnection连接带来的资源消耗
                if (resourceTotalLength - resourceEndPos <= FRAGMENT_SIZE * 3 / 2) {
                    startRange = resourceEndPos;
                    endRange = -1L;
                } else {
                    startRange = resourceEndPos;
                    endRange = resourceEndPos + FRAGMENT_SIZE;
                }
                System.out.println("next startRange=" + startRange + ", endRange=" + endRange);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return;
            } finally {
                if (in != null) {
                    try {in.close();} catch (Exception e) {}
                }
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        long time = System.currentTimeMillis() - startTime;
        System.out.println("传输Web资源[" + url + "]共耗时" + time / 1000 + "s" + time % 1000 + "ms");
    }

    /**
     * URL格式限定如下：
     * <p>
     * URL的长度至少是20
     * </p>
     * <p>
     * URL的协议类型必须是HTTP
     * </p>
     * <p>
     * URL中必须包含"."，且"."后面的字符个数不能超过10
     * </p>
     *
     * @param url
     * @return
     */
    private static String checkURLFormat(String url) {
        if (url.length() < 20) {
            return "url的长度至少为20";
        }

        String protocol = url.substring(0, 7);
        if (!protocol.equalsIgnoreCase("http://")) {
            return "url必须以http://开头(不区分大小写)";
        }

        int dotIndex = url.lastIndexOf(".");
        if (dotIndex == -1) {
            return "url中必须有'.'";
        }

        String resourceSuffix = url.substring(dotIndex);
        if (resourceSuffix.length() > 10) {
            return "url格式不正确，资源名称的后缀('.'后面的字符)不能超过10个字符";
        }
        return null;
    }

    /**
     * 本地文件格式限定如下：
     * <p>
     * 如果localFile不为null，那么该文件在本地必须存在并且是文件
     * </p>
     * <p>
     * 如果localFile不为null，那么该文件的后缀必须是.tmp，说明该文件在之前没有传输完，本次继续传输
     * </p>
     *
     * @param localFile
     * @return
     */
    private static String checkLocalFileFormat(String localFile) {
        if (localFile == null) {
            return null;
        }

        String retMsg = null;
        try {
            File f = new File(localFile);
            if (!f.exists()) {
                retMsg = "本地文件[" + localFile + "]不存在";
            } else if (!f.isFile()) {
                retMsg = "本地文件[" + localFile + "]不是一个文件";
            } else if (!localFile.endsWith(".tmp")) {
                retMsg = "本地文件[" + localFile + "]应该以.tmp结尾";
            }
        } catch (Exception e) {
            retMsg = "在读取本地文件[" + localFile + "]时出错，请检查该文件";
        }
        return retMsg;
    }
}
