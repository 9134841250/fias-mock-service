package ru.kontur.fias.fiasmockservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {"fias.databases.dir ="})
public class FiasMockServiceApplicationTests {

    @Test
    public void contextLoads() {
    }

}
