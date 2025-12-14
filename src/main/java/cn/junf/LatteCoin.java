package cn.junf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("cn.junf.mapper")
public class LatteCoin {

    public static void main(String[] args) {
        SpringApplication.run(LatteCoin.class, args);
    }

}
