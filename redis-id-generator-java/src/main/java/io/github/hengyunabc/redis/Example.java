package io.github.hengyunabc.redis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class Example {

	public static void main(String[] args) {
		String tab = "order";

		IdGenerator idGenerator = IdGenerator.builder()
				.addHost("47.91.248.236", 6380, "be6d4e21e9113bf8af47ce72f3da18e00580d402")
				.addHost("47.91.248.236", 6381, "97f65601d0aaf1a0574da69b1ff3092969c4310e")
				.build();
		int hello = 0;
        while (hello<3){
            long id = idGenerator.next(tab);

            System.out.println("分布式id值:" + id);
            List<Long> result = IdGenerator.parseId(id);

            System.out.println("分布式id生成的时间是:" + new SimpleDateFormat("yyyy-MM-dd").format(new Date(result.get(0))) );
            System.out.println("redis节点:" + result.get(1));
            hello++;
        }

	}
}
