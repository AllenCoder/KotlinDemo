package emptytest;

/**
 * Created by Allen on 2018/6/11.
 */
public class JavaTest {
    public static void main(String[] args) {
        double sum=0.0;
        long t = (System.currentTimeMillis());
        for (double i = 0; i <20000; i+=0.1) {
            for (double n = 0; n < 20000;n++) {
                sum=(sum+i*n);
            }
        }

        System.out.println(sum);
        System.out.println((float) (System.currentTimeMillis() - t) );
        System.out.println("Java");
    }
}
