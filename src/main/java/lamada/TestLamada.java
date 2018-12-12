package lamada;

/**
 * Created by Allen on 2018/3/27.
 */
public class TestLamada {

    public static void main(String[] args) {
        TestLamada tester = new TestLamada();

        /**
         *类型声明
         */
        MathOperation addition = (int a, int b) -> a + b;
        /**
         * 不用类型声明
         */
        MathOperation subtraction = (a, b) -> a + b;

        /**
         * 大括号中的返回语句
         */
        MathOperation multiplication = (a, b) -> {
            return a * b;
        };
        MathOperation division = (int a, int b) -> a / b;

        System.out.println("args = [" + tester.operate(10, 5, addition) + "]");

        System.out.println("args = [" + tester.operate(10, 5, subtraction) + "]");
        System.out.println("args = [" + tester.operate(10, 5, multiplication) + "]");
        System.out.println("args = [" + tester.operate(10, 5, division) + "]");
        GreetingService greetService =message-> System.out.println("message = " + message);
        GreetingService greetingService2 =(message)-> System.out.println("message = " + message);
        System.out.println("greetingService2 = " + greetingService2);
        greetService.sayMessage("Runoob");
        greetingService2.sayMessage("Google");

    }


    interface MathOperation {
        int operation(int a, int b);
    }

    interface GreetingService {
        void sayMessage(String message);
    }

    private int operate(int a, int b, MathOperation mathOperation) {
        return mathOperation.operation(a, b);
    }
}
