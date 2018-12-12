package lamada;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Allen on 2018/3/27.
 */
public class Stream {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>();
        names.add("Jack");
        names.add("Allen");
        /**
         * lamada 第一种写法
         */
        Collections.sort(names, (o1, o2) -> o1.compareTo(o2));

        /**
         * lamada 第二种写法
         */
        Collections.sort(names, String::compareTo);
        /**
         * lamada stream写法
         */
        names.stream().map(s -> s.toLowerCase()).collect(Collectors.toList());
        /**
         * lamada 写法
         */


    }
}
