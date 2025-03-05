package org.example;


import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppTest {
    @Test
    public void test(){
        ByteBuffer buffer=ByteBuffer.allocate(1024);

        String a="test";

        buffer.put(a.getBytes(StandardCharsets.UTF_8));

        System.out.println(buffer.capacity());
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.mark());

        buffer.flip();
        System.out.println("-------------------------");
        System.out.println(buffer.capacity());
        System.out.println(buffer.position());
        System.out.println(buffer.limit());
        System.out.println(buffer.mark());


    }

    @Test
    public void test2() throws IOException {
        Path path = Paths.get("a.txt");
        System.out.println(path.toAbsolutePath());
    }
}
