import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 
 * @author Moses
 * @date 2017-10-25 10:51
 * 
 */
public class Test {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();

        Future<String> future = executor.submit(() -> { //Lambda 是一个 callable， 提交后便立即执行，这里返回的是 FutureTask 实例
            System.out.println("running task");
            Thread.sleep(5000);
            System.out.println("完成任务啦！");
            return "return task";
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        System.out.println("主线程");  //前面的的 Callable 在其他线程中运行着，可以做一些其他的事情
    }

}
