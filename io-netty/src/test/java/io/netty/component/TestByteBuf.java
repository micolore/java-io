package io.netty.component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 */
public class TestByteBuf {


    @Test
    public void jdkIo() throws Exception {
        try (FileInputStream in = new FileInputStream("src-file"); FileOutputStream out = new FileOutputStream("out-file")) {

            byte[] bys = new byte[1024];
            int len = 0;

            while ((len = in.read(bys)) != -1) {
                out.write(bys, 0, len);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void jdkBufferIo() throws Exception {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream("src-file"));
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("out-file"));
        byte[] bys = new byte[1024];
        int len = 0;
        while ((len = in.read(bys)) != -1) {
            out.write(bys, 0, len);
        }
    }

    @Test
    public void testJdkByteBuffer() {
        //1、 不能自动扩容，容易发生越界
        //2、 需要自己维护读与写，flip&rewind
        //3、 高级特性不支持, 需要自己手写
        ByteBuffer buf = ByteBuffer.allocate(88);
        String str = "hello jdk bytebuffer";
        buf.put(str.getBytes());
        buf.flip();

        byte[] bytes = new byte[buf.remaining()];
        buf.get(bytes);
        String decodeValue = new String(bytes);
        System.out.println("decodeValue: " + decodeValue);
    }

    /**
     * netty 的 ByteBuf比 jdk的bytebuffer的优势
     * 1、可以自定义缓冲的类型
     * 2、通过一个内置的复合缓冲类型实现零拷贝
     * 3、扩展性好，比如StringBuilder
     * 4、不需要flip来进行切换读写模式
     * 5、读取和写入索引分开
     * 6、方法链
     * 7、引用计数
     * 8、pooling
     * <p>
     * <p>
     * heapBuffer-堆内存、directBuffer-直接内存
     * <p>
     * PooledByteBuf
     */
    @Test
    public void testNettyByteBuf() {

        ByteBuf buf = Unpooled.buffer(10);
        System.out.println("1-原始的byteBuf为:" + buf.toString());
        System.out.println("1-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

        byte[] bytes = {1, 2, 3, 4, 5, 6};
        buf.writeBytes(bytes);
        System.out.println();
        System.out.println("2-写入一段数据的byteBuf为:" + buf.toString());
        System.out.println("2-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

        byte b1 = buf.readByte();
        byte b2 = buf.readByte();
        System.out.println();
        System.out.println("3-读取的一段数据为:" + Arrays.toString(new byte[]{b1, b2}));
        System.out.println("3-读取一段数据之后的byteBuf为:" + buf.toString());
        System.out.println("3-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

        buf.discardReadBytes();
        System.out.println();
        System.out.println("4-将读取的数据丢弃之后的byteBuf为:" + buf.toString());
        System.out.println("4-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

        buf.clear();
        System.out.println();
        System.out.println("5-清理完读写指针之后的byteBuf为:" + buf.toString());
        System.out.println("5-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

        byte[] newBytes = {1, 2, 3, 4, 5, 6};
        buf.writeBytes(newBytes);
        System.out.println();
        System.out.println("6-写入的数据为:" + Arrays.toString(newBytes));
        System.out.println("6-清理完读写指针之后的byteBuf为:" + buf.toString());
        System.out.println("6-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

        buf.setZero(0, buf.capacity());
        System.out.println();
        System.out.println("7-清零之后的byteBuf为:" + buf.toString());
        System.out.println("7-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

        byte[] thirdBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        buf.writeBytes(thirdBytes);
        System.out.println();
        System.out.println("6-写入的数据为:" + Arrays.toString(thirdBytes));
        System.out.println("6-清理完读写指针之后的byteBuf为:" + buf.toString());
        System.out.println("6-原始的byteBuf的内容为:" + Arrays.toString(buf.array()));

    }


    @Test
    public void byteBufV2() {

        ByteBuf buffer = Unpooled.buffer(10);

        String hello = "hello";
        byte[] bytes = hello.getBytes();

        buffer.writeBytes(bytes);

        byte[] b1 = new byte[3];
        b1[0] = 1;
        b1[1] = 2;
        b1[2] = 3;
        buffer.writeBytes(b1);

        System.out.println("array: " + Arrays.toString(buffer.array()));
        byte[] read = new byte[5];
        for (int x = 0; x < 5; x++) {
            read[x] = buffer.getByte(x);
        }

        String readStr = new String(read);
        System.out.println("get buf str: " + readStr);

    }

    /**
     * ZeroCopy 应用层的实现，与jvm虚拟机、操作系统无关
     * <p>
     * slice go
     */
    @Test
    public void zeroCopy() {

        ByteBuf buf = Unpooled.buffer(10);
        buf.writeByte(10);

        ByteBuf buf2 = Unpooled.buffer(10);
        buf2.writeByte(10);

        CompositeByteBuf byteBuf = Unpooled.compositeBuffer();
        CompositeByteBuf newByteBuf = byteBuf.addComponents(true, buf, buf2);
        System.out.println("newByteBuf:" + newByteBuf);

        byte[] bytes = {1, 2, 3};

        ByteBuf wrappedBuffer = Unpooled.wrappedBuffer(bytes);
        System.out.println("wrappedBuffer start:" + wrappedBuffer.getByte(2));
        bytes[2] = 7;
        System.out.println("wrappedBuffer after:" + wrappedBuffer.getByte(2));

        ByteBuf wrappedBuffer1 = Unpooled.wrappedBuffer("zero copy!".getBytes());
        ByteBuf slice = wrappedBuffer1.slice(1, 2);
        slice.unwrap();
        System.out.println("slice:" + slice);

    }


}
